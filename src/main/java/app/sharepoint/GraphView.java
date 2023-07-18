package app.sharepoint;

import java.util.InputMismatchException;
import java.util.Scanner;

public class GraphView {
    private final GraphController graph;
    public GraphView() throws Exception {
        this.graph = new GraphController();
        initializeMenu();
    }

    private void initializeMenu() {
        System.out.println("Java App-Only GraphService Tutorial");
        Scanner input = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("Please choose one of the following options:");
            System.out.println("0. Exit");
            System.out.println("1. Display access token");
            System.out.println("2. Site ID::");
            System.out.println("3. Drive ID::");
            System.out.println("4. Item ID::");
            System.out.println("5. Range Values::");

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
                    break;
                case 1:
                    // Display access token
                    graph.displayAccessToken();
                    break;
                case 2:
                    // Display Site List
                    graph.displaySiteId();
                case 3:
                    // Display access token
                    graph.displayDriveId();
                    break;
                case 4:
                    // Display Site List
                    graph.displayItemId();
                case 5:
                    // Display Site List
                    graph.displayRangeValues();
            }
        }

        input.close();
    }
}
