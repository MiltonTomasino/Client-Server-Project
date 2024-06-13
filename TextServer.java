import java.io.*; 
import java.net.*;
import java.util.*;

class TextServer {
   public static void main(String argv[]) throws Exception 
   {
      User users = new User();

      users.addUser("Alice", "1234");
      users.addUser("Bob", "5678");
      users.sendMessage("Alice", "Hello Bob", "Bob");

      String clientUsername = null;
      String clientPassword;
      Boolean isLoggedIn = false;
      ServerSocket welcomeSocket = new ServerSocket(8000); 
      System.out.println("SERVER is running ... ");

      while(true) {
         Socket connectionSocket = welcomeSocket.accept();
         BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
         DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
         ObjectOutputStream objectToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
         String option;
         
         while (true) {
            option = inFromClient.readLine();
//            System.out.println("Client option: " + option);
            switch (option) {
               case "0":
                  while (!isLoggedIn) {
                     clientUsername = inFromClient.readLine();
                     clientPassword = inFromClient.readLine();
                     System.out.println("FROM CLIENT: " + clientUsername + " " + clientPassword);

                     if (users.isUserInList(clientUsername)) { // check if user exists
                        if (users.getPassword(clientUsername).equals(clientPassword)) { // check if user info is correct
                           System.out.println("\n" + clientUsername + " is logged in.");
                           outToClient.writeBytes("Access Granted\n");
                           isLoggedIn = true; // user is logged in
                        } else {
                           outToClient.writeBytes("Access Denied - Username/Password Incorrect\n");
                        }
                     } else {
                        outToClient.writeBytes("User does not exist\n");
                     }
                  }
                  break;
               case "1":
                  System.out.println("User requested list of users");
                  System.out.println(users.getUserList());
                  objectToClient.writeObject(users.getUserList()); // send userList to user
                  break;
               case "2":
                  System.out.println("User requested message sent");
                  String clientDestination = inFromClient.readLine();
                  String clientMessage = inFromClient.readLine();

                  // check if user exists in memory
                  // send user message if destination does not exist
                  while (!users.isUserInList(clientDestination)) {
                     outToClient.writeBytes("User does not exist\n");
                     clientDestination = inFromClient.readLine();
                  }

                  // add message to messages associated with destination
                  outToClient.writeBytes("success");
                  users.sendMessage(clientUsername, clientMessage, clientDestination);
                  outToClient.writeBytes("Message sent successfully\n");
                  break;
               case "3":
                  // Get messages associated with client
                  System.out.println("\n==============================\nSending client their messages...\n==============================");
                  objectToClient.writeObject(users.getMessages(clientUsername));
                  System.out.println("\n===============\nMessages sent\n===============");
                  break;
               case "4":
                  String newUsername = inFromClient.readLine();
                  String newPassword = inFromClient.readLine();

                  // Check if user already exists when trying to add user
                  if (users.isUserInList(newUsername)) {
                     outToClient.writeBytes("User already exists");
                  } else {
                     users.addUser(newUsername, newPassword);
                     outToClient.writeBytes(newUsername + " has been added\n");
                  }
                  break;
               case "5":
                  // break connection to client when they log off
                  System.out.println("\n" + clientUsername + " has logged off");
                  if (connectionSocket != null) {connectionSocket.close();}
                  isLoggedIn = false;
                  break;
               default:
                  break;
            }
            if (option.equals("5")) {
               break;
            }
         }
      }
   }
}

class User {
   private Map<String, List<String>> messages = new HashMap<>();
   private  Map<String, String> userList = new HashMap<>();

   public void addUser(String username, String password) {
      userList.put(username, password);
   }

   public boolean isUserInList(String user) {

      return userList.containsKey(user);
   }

   public Map<String, String> getUserList() {
      return userList;
   }

   public String getPassword(String user) {
      return userList.get(user);
   }

   public List<String> getMessages(String user) {
      return messages.getOrDefault(user, new ArrayList<>());
   }

   public void sendMessage(String user, String message, String destination) {
      messages.putIfAbsent(destination, new ArrayList<>());
      messages.get(destination).add(user + ": " + message);
   }

}
