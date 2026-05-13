package io.github.neczpal.petstore.client;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class PetstoreClientApp {

    static void main() {
        String serverUrl = "http://localhost:8080";
        try (InputStream input = PetstoreClientApp.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                serverUrl = prop.getProperty("petstore.server.url", serverUrl);
            }
        } catch (Exception ex) {
            System.out.println("Warning: Unable to read application.properties, defaulting to " + serverUrl);
        }

        System.out.println("Connecting to Petstore Server at: " + serverUrl);

        PetClient petClient = new PetClient(serverUrl);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Petstore Client ---");
            System.out.println("1. Add a new Pet");
            System.out.println("2. Get Pet by ID");
            System.out.println("3. Find Pets by Status");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            if ("4".equals(input)) {
                System.out.println("Exiting...");
                break;
            }

            try {
                switch (input) {
                    case "1":
                        System.out.print("Enter Pet Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Status (available/pending/sold): ");
                        String status = scanner.nextLine();
                        
                        Pet newPet = new Pet(null, name, new Category(1, "Dogs"), new ArrayList<>(), new ArrayList<>(), status);
                        Pet created = petClient.addPet(newPet);
                        System.out.println("Successfully added pet! ID: " + created.id());
                        break;
                    case "2":
                        System.out.print("Enter Pet ID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        Pet found = petClient.getPetById(id);
                        System.out.println("Found Pet: " + found);
                        break;
                    case "3":
                        System.out.print("Enter Status: ");
                        String stat = scanner.nextLine();
                        List<Pet> pets = petClient.findPetsByStatus(stat);
                        System.out.println("Found " + pets.size() + " pets: ");
                        pets.forEach(System.out::println);
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error performing request: " + e.getMessage());
            }
        }
    }
}
