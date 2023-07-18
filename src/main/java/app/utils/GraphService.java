// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

// <ImportSnippet>
package app.utils;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import okhttp3.Request;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class GraphService {

    private final static List<String> graphScopes = List.of("https://graph.microsoft.com/.default");
    private static ClientSecretCredential clientSecretCredential;

    public static void initializeGraphForAppOnlyAuth() throws Exception {
        Properties oAuthProperties = OAuthPropertiesLoader.loadProperties();
        Logger logger = LoggerFactory.getLogger(GraphService.class);
        if (clientSecretCredential == null) {
            clientSecretCredential = new ClientSecretCredentialBuilder()
                    .clientId(oAuthProperties.getProperty("clientId"))
                    .tenantId(oAuthProperties.getProperty("tenantId"))
                    .clientSecret(oAuthProperties.getProperty("clientSecret"))
                    .build();
        }

        // Configurar o provedor de autenticação usando o token de acesso
        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(graphScopes, clientSecretCredential);

        // Criar cliente GraphServiceClient usando o provedor de autenticação
        GraphServiceClient<Request> appClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

    }

    private static IAuthenticationResult authenticateWithClientCredentials() {
        try {
            Properties oAuthProperties = OAuthPropertiesLoader.loadProperties();
            ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                            oAuthProperties.getProperty("clientId"),
                            ClientCredentialFactory.createFromSecret(oAuthProperties.getProperty("clientSecret")))
                    .authority("https://login.microsoftonline.com/" + oAuthProperties.getProperty("tenantId"))
                    .build();

            ClientCredentialParameters parameters = ClientCredentialParameters.builder(
                            Collections.singleton("https://graph.microsoft.com/.default"))
                    .build();

            CompletableFuture<IAuthenticationResult> future = app.acquireToken(parameters);
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String acquireToken() throws Exception {
        IAuthenticationResult result = authenticateWithClientCredentials();
        assert result != null;
        return result.accessToken();
    }

    public static String getToken() throws Exception {
        // Ensure credential isn't null
        if (clientSecretCredential == null) {
            throw new Exception("GraphService has not been initialized for app-only auth");
        }

        // Request the .default scope as required by app-only auth
        final TokenRequestContext context = new TokenRequestContext();
        context.addScopes(graphScopes.toArray(new String[0]));

        final AccessToken token = clientSecretCredential.getToken(context).block();
        assert token != null;
        return token.getToken();
    }

    private static String getIdFromUrl(String url) throws Exception {
        String response = getResponseFromUrl(url);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        return jsonObject.get("id").getAsString();
    }

    private static String getResponseFromUrl(String url) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "Bearer " + acquireToken());
        HttpResponse response = (HttpResponse) client.execute(request);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    public static JsonNode getResponse(String endPoint) throws Exception {
        URL url = new URL(endPoint);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        request.setRequestMethod("GET");
        request.setRequestProperty("Authorization", "Bearer " + getToken());
        request.setRequestProperty("Accept", "application/json");

        int httpResponseCode = request.getResponseCode();
        if (httpResponseCode == HTTPResponse.SC_OK) {

            StringBuilder response;
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(request.getInputStream()))) {

                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            // Parse o JSON de resposta
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readTree(response.toString());
        } else {
            throw new Exception(String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, request.getResponseMessage()));
        }
    }

    public static JsonNode getSiteList() throws Exception {
        JsonNode responseJson = getResponse("https://graph.microsoft.com/v1.0/sites/78zd7n.sharepoint.com:/sites/fibrasil");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return responseJson;
    }

    public static String getSiteId(String siteUrl) throws Exception {
        return getIdFromUrl(siteUrl);
    }

    public static String getSiteId() throws Exception {
        JsonNode responseJson = getSiteList();
        // Obter o siteId
        return responseJson.get("id").asText();
    }

    public static JsonNode getDriveList() throws Exception {
        JsonNode responseJson = getResponse("https://graph.microsoft.com/v1.0/sites/78zd7n.sharepoint.com,a2955302-a1e4-4f50-a0dd-ddde85823fd3,2f7fffd6-4001-40bb-9179-e5ff73e51a7e/drives");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return responseJson;
    }

    public static String getDriveIdFromJson(JsonNode jsonNode) throws Exception {
        JsonNode valueNode = jsonNode.get("value");
        if (valueNode != null && valueNode.isArray()) {
            JsonNode driveNode = valueNode.get(0);
            if (driveNode != null) {
                JsonNode idNode = driveNode.get("id");
                if (idNode != null) {
                    return idNode.asText();
                }
            }
        }

        throw new Exception("Failed to retrieve drive ID from JSON.");
    }

    public static void printResponseJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        String sites = objectMapper.writeValueAsString(getSiteList());
        System.out.println("\nLista de Sites usando objectMapper:: " + sites);

        JsonNode drivelist = getDriveList();
        String drives = objectMapper.writeValueAsString(getDriveList());
        System.out.println("\nLista de Drives usando objectMapper:: " + drives);
        System.out.println("\nID do drive:: " + getDriveIdFromJson(drivelist));
    }


}
