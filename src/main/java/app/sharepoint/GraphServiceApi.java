package app.sharepoint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.graph.content.BatchResponseContent;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GraphServiceApi {

//    private void updateCellsInBatches(String accessToken, List<Integer> rowsToProcess, int statusColumnIndex, String siteId, String driveId, String itemId) throws Exception {
//        String sheetName = "SheetFibrasil";
//        int batchSize = 20;
//        CloseableHttpClient client = HttpClients.createDefault();
//
//        for (List<Integer> batchRows : Iterable.partition(rowsToProcess, batchSize)) {
//            String boundary = "batch_" + UUID.randomUUID().toString();
//            MultipartEntityBuilder batchBuilder = MultipartEntityBuilder.create().setBoundary(boundary);
//
//            for (int row : batchRows) {
//                String columnLetter = columnLetter(statusColumnIndex);
//                String cellAddress = columnLetter + (row + 1);
//                String updateUrl = String.format("https://graph.microsoft.com/v1.0/sites/%s/drives/%s/items/%s/workbook/worksheets/%s/range(address='%s')", siteId, driveId, itemId, sheetName, cellAddress);
//
//                JsonObject updateValues = new JsonObject();
//                updateValues.add("values", new Gson().toJsonTree(new String[][] { { "EM PROCESSAMENTO" } }));
//
//                HttpPatch updateRequest = new HttpPatch(updateUrl);
//                updateRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
//                updateRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
//                updateRequest.setEntity(new StringBody(updateValues.toString(), ContentType.APPLICATION_JSON));
//
//                batchBuilder.addPart("request", new StringBody(updateRequest.toString(), ContentType.APPLICATION_JSON));
//            }
//
//            HttpPost batchRequest = new HttpPost("https://graph.microsoft.com/v1.0/$batch");
//            batchRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
//            batchRequest.setEntity(batchBuilder.build());
//
//            HttpResponse batchResponse = client.execute(batchRequest);
//            String batchResponseContent = EntityUtils.toString(batchResponse.getEntity(), "UTF-8");
//            BatchResponseContent batchResult = new Gson().fromJson(batchResponseContent, BatchResponseContent.class);
//
//            // Verifique os resultados da solicitação em lote para garantir o sucesso
//
//            Thread.sleep(60000); // Atraso de 1 minuto
//        }
//
//        client.close();
//    }

    public String columnLetter(int columnIndex) {
        int dividend = columnIndex + 1;
        StringBuilder columnName = new StringBuilder();

        while (dividend > 0) {
            int modulo = (dividend - 1) % 26;
            columnName.insert(0, Character.toString((char) (65 + modulo)));
            dividend = (dividend - modulo) / 26;
        }

        return columnName.toString();
    }

    public List<Integer> getRowsToProcess(JsonArray rangeValues, int statusColumnIndex) {
        List<Integer> rowsToProcess = new ArrayList<>();
        for (int i = 1; i < rangeValues.size(); i++) {
            if (rangeValues.get(i).getAsJsonArray().get(statusColumnIndex).getAsString().toUpperCase().equals("A PROCESSAR")) {
                rowsToProcess.add(i);
            }
        }
        return rowsToProcess;
    }

    public int getStatusColumnIndex(JsonArray rangeValues) {
        for (int i = 0; i < rangeValues.get(0).getAsJsonArray().size(); i++) {
            if (rangeValues.get(0).getAsJsonArray().get(i).getAsString().toLowerCase().equals("status")) {
                return i;
            }
        }
        return -1;
    }

    public JsonArray getRangeValues(String rangeUrl) throws Exception {
        String response = getResponseFromUrl(rangeUrl);
        JsonObject range = JsonParser.parseString(response).getAsJsonObject();
        return range.getAsJsonArray("text");
    }

    public String getItemId(String itemUrl) throws Exception {
        return getIdFromUrl(itemUrl);
    }

    public String getDriveId(String driveUrl) throws Exception {
        String response = getResponseFromUrl(driveUrl);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        return jsonObject.getAsJsonArray("value").get(0).getAsJsonObject().get("id").getAsString();
    }

    public String getSiteId(String siteUrl) throws Exception {
        return getIdFromUrl(siteUrl);
    }

    public String getAccessToken() {
        return GraphServiceAuth.getInstance().getAccessToken();
    }

    private String getIdFromUrl(String url) throws Exception {
        String response = getResponseFromUrl(url);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        return jsonObject.get("id").getAsString();
    }

    private String getResponseFromUrl(String url) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "Bearer " + GraphServiceAuth.getInstance().getAccessToken());
        HttpResponse response = (HttpResponse) client.execute(request);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }
}
