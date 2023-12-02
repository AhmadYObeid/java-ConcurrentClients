package ca.concordia.client;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;

public class SimpleWebClient implements Runnable {

    private static List<String> ClientArysss = new ArrayList<>();

    // Method to read account IDs from the file
    private static void readAccountIds(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                ClientArysss.add(data[0]); // Assuming account ID is the first column
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Random toselect = new Random();
        Socket socket = null;
        PrintWriter writer = null;
        BufferedReader bread = null;

        try {
            socket = new Socket("localhost", 5000);
            OutputStream out = socket.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(out));

            // Randomly pick account IDs for the transaction
            String fromAccount = ClientArysss.get(1);
            String toAccount = ClientArysss.get(toselect.nextInt(ClientArysss.size()));
            while (toAccount.equals(fromAccount)) {
                toAccount = ClientArysss.get(toselect.nextInt(ClientArysss.size())); // Ensure different accounts
            }
            String value = String.valueOf(toselect.nextInt(100) + 1); // Random value between 1 and 100

            // Prepare and send the POST request
            String postData = "account=" + fromAccount + "&value=" + value + "&toAccount=" + toAccount;
            writer.println("POST /submit HTTP/1.1");
            writer.println("Host: localhost:5000");
            writer.println("Content-Type: application/x-www-form-urlencoded");
            writer.println("Content-Length: " + postData.length());
            writer.println();
            writer.println(postData);
            writer.flush();

            // Read the response
            InputStream in = socket.getInputStream();
            bread = new BufferedReader(new InputStreamReader(in));
            String theeline;
            while ((theeline = bread.readLine()) != null) {
                System.out.println(theeline);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Read account IDs from the file
        readAccountIds("C:\\Users\\Ahmad\\Desktop\\ForVScode\\webserver\\src\\main\\java\\ca\\concordia\\server\\accounts.txt");

        // Launch client threads
        for (int i = 0; i < 2; i++) { // Only 4 threads for demonstration
            Thread thread = new Thread(new SimpleWebClient());
            thread.start();
        }
    }
}