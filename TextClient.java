import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TextClient {
    public static void main(String argv[]) throws Exception {
        String sentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = null;
        DataOutput outToServer = null;
        BufferedReader inFromServer = null;
        ObjectInputStream objectFromServer = null;
        Boolean isLoggedIn = false;
        String username;

        while (true) {
            System.out.println("\n0. Connect to the server");
            System.out.println("1. Get the user list");
            System.out.println("2. Send a message");
            System.out.println("3. Get my messages");
            System.out.println("4. Add new user");
            System.out.println("5. Exit");
            System.out.print("Enter a number: ");

            String option = inFromUser.readLine();
            switch (option) {

                case "0":
                    // connect to server and initialize data for communication
                    System.out.println("\nConnecting to server...");
                    clientSocket = new Socket("127.0.0.1", 8000);
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    objectFromServer = new ObjectInputStream(clientSocket.getInputStream());
                    outToServer.writeBytes(option + "\n");

                    // While user is not logged in, ask for info to log in
                    while (!isLoggedIn) {
                        System.out.print("Enter username: ");
                        username = inFromUser.readLine();
                        outToServer.writeBytes(username + "\n");
                        System.out.print("Enter password: ");
                        sentence = inFromUser.readLine();
                        outToServer.writeBytes(sentence + "\n");

                        String response = inFromServer.readLine();
                        System.out.println("\n" + response);

                        if (response.equals("User does not exist")) {
                            continue;
                        }

                        if (!(response.contains("Denied") || response.contains("exist"))) {
                            isLoggedIn = true;
                        }

                    }
                    break;

                case "1":
                    if (isLoggedIn) {
                        outToServer.writeBytes(option + "\n");

                        // retrieve user list from server
                        Map<?, ?> list = (Map<?, ?>) objectFromServer.readObject();

                        for (Map.Entry<?, ?> entry : list.entrySet()) {
                            System.out.println("\nUser: " + entry.getKey() + " = " + entry.getValue());
                        }
                    } else {
                        System.out.println("\nNot Logged in");
                    }
                    break;

                case "2":
                    if (isLoggedIn) {
                        // collect message info
                        outToServer.writeBytes(option + "\n");
                        System.out.print("Who will you send a message to? ");
                        String destination = inFromUser.readLine();
                        System.out.print("Write your message: ");
                        String message = inFromUser.readLine();

                        outToServer.writeBytes(destination + "\n");
                        outToServer.writeBytes(message + "\n");

                        String check = inFromServer.readLine();

                        // determine whether desired user exists in server
                        // keep looping until desired user is found
                        while (check.contains("does not exist")) {
                            System.out.println("User does not exist, try again");
                            System.out.print("Who will you send a message to? ");
                            destination = inFromUser.readLine();
                            System.out.print("Write your message: ");
                            message = inFromUser.readLine();

                            outToServer.writeBytes(destination + "\n");
                            outToServer.writeBytes(message + "\n");

                            check = inFromServer.readLine();
                        }

                        System.out.println("\n===============================================");
                        System.out.println("FROM SERVER: " + check);
                        System.out.println("===============================================");
                    } else {
                        System.out.println("\nNot Logged in");
                    }
                    break;

                case "3":
                    if (isLoggedIn) {
                        outToServer.writeBytes(option + "\n");
                        List<?> messagesList = (List<?>) objectFromServer.readObject();

                        // return list from server whether empty or populated
                        if (!(messagesList.isEmpty())) {
                            System.out.println("\n==========Your Messages==========");
                            for (Object entry: messagesList) {
                                System.out.println(entry);
                            }
                            System.out.println("=================================");
                        } else {
                            System.out.println("\n=====No messages=====");
                        }
                    } else {
                        System.out.println("\nNot logged in");
                    }
                    break;
                case "4":
                    if (isLoggedIn) {
                        // add user
                        outToServer.writeBytes(option + "\n");
                        System.out.println("\nAdding new user...");
                        System.out.print("Enter username: ");
                        username = inFromUser.readLine();
                        outToServer.writeBytes(username + "\n");
                        System.out.print("Enter password: ");
                        sentence = inFromUser.readLine();
                        outToServer.writeBytes(sentence + "\n");

                        String response = inFromServer.readLine();
                        System.out.println("\n" + response);
                    } else {
                        System.out.println("\nNot logged in");
                    }
                    break;
                case "5":
                    // cut connection to server
                    System.out.println("Closing connection...");
                    if (clientSocket != null) {
                        outToServer.writeBytes(option + "\n");
                        clientSocket.close();
                    }
                    break;
                default:
                    System.out.println("\nInvalid input");
                    break;
            }
            // exit program
            if (option.equals("5")) {
                break;
            }
        }
    }
}

