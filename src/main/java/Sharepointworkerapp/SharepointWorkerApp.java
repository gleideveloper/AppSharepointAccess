package Sharepointworkerapp;

import java.util.InputMismatchException;
import java.util.Scanner;

public class SharepointWorkerApp {
    public static void main(String[] args) throws Exception {
        System.out.println("Java App-Only GraphService Tutorial");
        System.out.println();

        initializeGraph();
        Scanner input = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("Please choose one of the following options:");
            System.out.println("0. Exit");
            System.out.println("1. Display access token");
            System.out.println("2. List sites graph");

            try {
                choice = input.nextInt();
            } catch (InputMismatchException ex) {
                // Skip over non-integer input
            }

            input.nextLine();
            // Process user choice
            switch (choice) {
                case 0:
                    // Exit the program
                    System.out.println("Goodbye...");
                case 1:
                    // Display access token
                    displayAccessToken();
                    break;
                case 2:
                    // Display Site List
                    getSiteList();
            }
        }

        input.close();
    }

    private static void initializeGraph() {
        try {
            GraphService.initializeGraphForAppOnlyAuth();
        } catch (Exception e) {
            System.out.println("Error initializing GraphService for user auth");
            System.out.println(e.getMessage());
        }
    }

    private static void displayAccessToken() {
        try {
            System.out.println("Access token:: "+ GraphService.getToken());
            System.out.println("acquireToken:: "+ GraphService.acquireToken());
        } catch (Exception e) {
            System.out.println("Error getting access token");
            System.out.println(e.getMessage());
        }
    }

    private static void getSiteList() {
        try {
            //GraphService.printResponseJson();
            System.out.println("\nSite ID:: " + GraphService.getSiteId("https://graph.microsoft.com/v1.0/sites/78zd7n.sharepoint.com:/sites/fibrasil"));
        } catch (Exception e) {
            System.out.println("Error getting Sites");
            System.out.println(e.getMessage());
        }
    }

}
