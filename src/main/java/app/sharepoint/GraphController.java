package app.sharepoint;


import com.google.gson.JsonArray;
import com.nimbusds.jose.shaded.json.JSONArray;

public class GraphController {
    private final GraphServiceApi api;
    private final String SITE_ID;
    private final String DRIVE_ID;
    private final String ITEM_ID;
    private final JsonArray rangeValues;

    public GraphController() throws Exception {
        this.api = new GraphServiceApi();
        SITE_ID = api.getSiteId("https://graph.microsoft.com/v1.0/sites/78zd7n.sharepoint.com:/sites/fibrasil");
        DRIVE_ID = api.getDriveId("https://graph.microsoft.com/v1.0/sites/" + SITE_ID + "/drives");
        ITEM_ID = api.getItemId("https://graph.microsoft.com/v1.0/sites/" + SITE_ID + "/drives/" + DRIVE_ID + "/root:/BookFibrasil.xlsx");
        rangeValues = api.getRangeValues("https://graph.microsoft.com/v1.0/sites/" + SITE_ID + "/drives/" + DRIVE_ID + "/items/" + ITEM_ID + "/workbook/worksheets/SheetFibrasil/usedRange");
    }

    public void displayRangeValues() {
        System.out.println("\nRange Values:: " + rangeValues.toString());

    }

    public void displayItemId() {
        //GraphService.printResponseJson();
        System.out.println("\nSite ID:: " + ITEM_ID);

    }

    public void displayDriveId() {
        //GraphService.printResponseJson();
        System.out.println("\nDrive ID:: " + DRIVE_ID);
    }

    public void displaySiteId() {
        //GraphService.printResponseJson();
        System.out.println("\nSite ID:: " + SITE_ID);

    }

    public void displayAccessToken() {
        System.out.println("acquireToken:: " + api.getAccessToken());
    }
}

