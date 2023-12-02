package ca.concordia.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.io.FileReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


public class WebServer {
    private static final Map<Integer, Account> accountsMap = new HashMap<>();
    private ExecutorService executorService;

    public WebServer() throws IOException {
        this.executorService = Executors.newFixedThreadPool(4);
        initializeAccounts("C:\\Users\\Ahmad\\Desktop\\ForVScode\\webserver\\src\\main\\java\\ca\\concordia\\server\\accounts.txt");
    }

    private void initializeAccounts(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Ahmad\\Desktop\\ForVScode\\webserver\\src\\main\\java\\ca\\concordia\\server\\accounts.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                int balance = Integer.parseInt(parts[1]);
                accountsMap.put(id, new Account(balance, id));
            }
        }
    }


    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);

        while (true) {
            System.out.println("Waiting for a client to connect...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client...");

            // Submit a new task to handle the client connection
            executorService.submit(() -> {
                try {
                    handleClientConnection(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void handleClientConnection(Socket clientSocket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String request = in.readLine();
            if (request != null) {
                if (request.startsWith("GET")) {
                    handleGetRequest(out);
                } else if (request.startsWith("POST")) {
                    handlePostRequest(in, out);
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    public static Account getAccountById(int id) {
        return accountsMap.get(id);
    }

    public static void calcus(int value, int cliento1, int cliento2) {
        Account fromAccount = getAccountById(cliento1);
        Account toAccount = getAccountById(cliento2);

        if (fromAccount != null && toAccount != null) {
            // Lock both accounts to ensure exclusive access
            fromAccount.lock();
            try {
                toAccount.lock();
                try {
                    // Perform the transaction
                    fromAccount.withdraw(value);
                    toAccount.deposit(value);
                } finally {
                    toAccount.unlock(); // Unlock the second account in the inner finally block
                }
            } finally {
                fromAccount.unlock(); // Unlock the first account in the outer finally block
            }
        }
    }


    private static void handleGetRequest(OutputStream out) throws IOException {
        // Respond with a basic HTML page
        System.out.println("Handling GET request");
        String response = "HTTP/1.1 200 OK\r\n\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title>Concordia Transfers</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<h1>Welcome to Concordia Transfers</h1>\n" +
                "<p>Select the account and amount to transfer</p>\n" +
                "\n" +
                "<form action=\"/submit\" method=\"post\">\n" +
                "        <label for=\"account\">Account:</label>\n" +
                "        <input type=\"text\" id=\"account\" name=\"account\"><br><br>\n" +
                "\n" +
                "        <label for=\"value\">Value:</label>\n" +
                "        <input type=\"text\" id=\"value\" name=\"value\"><br><br>\n" +
                "\n" +
                "        <label for=\"toAccount\">To Account:</label>\n" +
                "        <input type=\"text\" id=\"toAccount\" name=\"toAccount\"><br><br>\n" +
                "\n" +
                "        <input type=\"submit\" value=\"Submit\">\n" +
                "    </form>\n" +
                "</body>\n" +
                "</html>\n";
        out.write(response.getBytes());
        out.flush();
    }

    private static void handlePostRequest(BufferedReader in, OutputStream out) throws IOException {
        System.out.println("Handling post request");
        StringBuilder requestBody = new StringBuilder();
        int contentLength = 0;
        String line;

        // Read headers to get content length
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.substring(line.indexOf(' ') + 1));
            }
        }

        // Read the request body based on content length
        for (int i = 0; i < contentLength; i++) {
            requestBody.append((char) in.read());
        }

        System.out.println(requestBody.toString());
        // Parse the request body as URL-encoded parameters

        String[] params = requestBody.toString().split("&");
        String account = null, toAccount = null, value = null;
        int ThisAc1 =0, ThisAc2=0;

        String[] data = ReadCol(0,"C:\\Users\\Ahmad\\Desktop\\ForVScode\\webserver\\src\\main\\java\\ca\\concordia\\server\\accounts.txt",",");
        String[] data2 = ReadCol(1,"C:\\Users\\Ahmad\\Desktop\\ForVScode\\webserver\\src\\main\\java\\ca\\concordia\\server\\accounts.txt",",");

        Account clint1 = new Account(0, 0);
        Account clint2 = new Account(0, 0);
        Account clint3 = new Account(0, 0);
        Account clint4 = new Account(0, 0);

        Account[] ClientArray = {clint1, clint2, clint3, clint4};


        for (int i = 0; i < data.length; i++) {
            int IDs = Integer.valueOf(data[i]);
            ClientArray[i].setID(IDs);
        }

        for (int i = 0; i < data2.length; i++) {
            int AMouNts = Integer.valueOf(data2[i]);
            ClientArray[i].setBalance(AMouNts);
        }

        int accountID = 0, toAccountID = 0, value3 = 0;

        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], "UTF-8");
                String val = URLDecoder.decode(parts[1], "UTF-8");

                switch (key) {
                    case "account":
                        accountID = Integer.parseInt(val);
                        break;
                    case "toAccount":
                        toAccountID = Integer.parseInt(val);
                        break;
                    case "value":
                        value3 = Integer.parseInt(val);
                        break;
                }
            }
        }

        calcus(value3, accountID, toAccountID);

// Construct the response with updated balances
        Account fromAccount = getAccountById(accountID);
        Account toAccountP = getAccountById(toAccountID);

        String responseContent = "<html><body><h1>Thank you for using Concordia Transfers</h1>" +
                "<h2>Received Form Inputs:</h2>" +
                "<p>Account: " + accountID + "</p>" +
                "<p>Value: " + value3 + "</p>" +
                "<p>Current Balance for " + accountID + ": " + (fromAccount != null ? fromAccount.getBalance() : "N/A") + "</p>" +
                "<p>To Account: " + toAccountID + "</p>" +
                "<p>Current Balance for " + toAccountID + ": " + (toAccountP != null ? toAccountP.getBalance() : "N/A") + "</p>" +
                "</body></html>";

        // Respond with the received form inputs
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseContent.length() + "\r\n" +
                "Content-Type: text/html\r\n\r\n" +
                responseContent;

        out.write(response.getBytes());
        out.flush();
    }

    public static void main(String[] args) throws IOException {
        //Start the server, if an exception occurs, print the stack trace
        WebServer server = new WebServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] ReadCol(int col, String filepath, String delimiter){
        String data[];
        String currentline;
        ArrayList<String> colData = new ArrayList<String>();

        try{
            FileReader fr = new FileReader(filepath);
            BufferedReader br = new BufferedReader(fr);

            while((currentline = br.readLine()) != null ){
                data = currentline.split(delimiter);
                colData.add(data[col]);
            }

        } catch(IOException ie) {
            System.out.println(ie);
            return null;
        }
        return colData.toArray(new String[0]);
    }

}