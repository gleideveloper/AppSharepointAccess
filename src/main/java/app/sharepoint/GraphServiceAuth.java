package app.sharepoint;

import app.utils.OAuthPropertiesLoader;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class GraphServiceAuth {
    private static GraphServiceAuth instance;
    private String accessToken = null;

    private GraphServiceAuth() {
        // Construtor privado para impedir a instanciação direta
    }

    public static synchronized GraphServiceAuth getInstance() {
        if (instance == null) {
            instance = new GraphServiceAuth();
        }
        return instance;
    }

    public String getAccessToken() {
        if (accessToken == null) {
            authenticateWithClientCredentials();
        }
        return accessToken;
    }

    private void authenticateWithClientCredentials() {
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
            IAuthenticationResult result = future.get();
            accessToken = result.accessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
