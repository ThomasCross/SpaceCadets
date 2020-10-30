import javax.net.ssl.SSLSocket;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * IRC Server Client
 *
 * <p>Performs I/O operations on data to and from the user.
 *
 * @author Thomas Cross
 * @author thomas@thomascross.net
 * @version 2.0
 */
public class ServerClient implements Runnable {
  private SSLSocket socket; // Stores the socket
  private ArrayList<ServerClient> clients; // Stores the reference to the clients arraylist
  private DataInputStream in; // Data from user
  private DataOutputStream out; // Data to user
  private boolean running; // Stores if the client is currently running or closed
  private boolean nameFlag; // Stores if a connection message has been displayed
  private String name; // Stores the user's name
  String banner; // Stores welcome banner
  ArrayList<String> filter; // Stores filtered words
  ConsoleOut consoleOut; // Object used to output to user

  /**
   * This is used to initialize the class variables and data streams. It will also send the welcome
   * banner.
   *
   * @param socket Inputs the client's socket
   * @param clients Inputs the clients arraylist's reference
   */
  public ServerClient(
      SSLSocket socket,
      ArrayList<ServerClient> clients,
      String banner,
      ArrayList<String> filter,
      ConsoleOut consoleOut) {
    this.socket = socket;
    this.clients = clients;
    running = true;
    nameFlag = true;
    name = "";
    this.banner = banner;
    this.filter = filter;
    this.consoleOut = consoleOut;

    // Initialize Data streams
    try {
      in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
      out = new DataOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      consoleOut.out("ERROR - BaseServerClient | User I/O Creation " + socket, Colours.Error, true);
      e.printStackTrace();
    }

    if (this.banner != null) {
      sendMsg(Colours.Green + (String) this.banner + Colours.Reset, false);
    } else {
      sendMsg(
          Colours.Green + "IRC Initialised | use /logout to exit or /help." + Colours.Reset, false);
    }
  }

  /** This is process the data going in and out of the IRC server to and from users. */
  public void run() {
    String line; // Line from user
    try {
      while (!socket.isClosed() && running) {
        line = filter(in.readUTF()); // Line input is read
        consoleOut.out(name + " >> " + line, true); // Output to server console

        if (line.matches("^/logout$")) { // Logs out / disconnects the user.
          logout();

        } else if (line.matches("^/help$")) { // Displays a list of commands
          sendMsg(
              " - - - Help - - - \n"
                  + "/logout /l (To logout)\n"
                  + "/nick /n <name> (To change name)\n"
                  + "/msg /m <name> <message> (To send a private message)\n"
                  + "/users /u (Get a list of active users)\n"
                  + " - - - - - - - - - ",
              false);

        } else if (line.matches(
            "^(/msg|/m) [A-z0-9]+ .+$")) { // Used to send a private message to another user
          String recipient =
              line.replaceAll(
                  "^(?:/msg|/m) ([A-z0-9]+) .+$", "$1"); // Gets recipient of the message
          boolean validFlag = true; // Used to check a message has been sent

          for (ServerClient client : clients) {
            if (client.getName().equals(recipient)) {
              client.sendMsg(
                  Colours.Green
                      + line.replaceAll("^(?:/msg|/m) [A-z0-9]+ (.+)$", "$1")
                      + Colours.Reset,
                  false); // Sends message
              validFlag = false;
            }
          }
          if (validFlag) { // Returns error is no message was sent
            sendMsg(
                Colours.Error + "- " + recipient + ", does not exist check /users." + Colours.Reset,
                true);
          }

        } else if (line.matches(
            "^/users$")) { // Used to display a list of currently connected users
          String clientNames = "Clients: ";
          for (ServerClient client : clients) { // Iterates through clients and gets all names.
            if (client.getActive()) { // Checks the clients are still active/connected
              clientNames = clientNames + client.getName() + ", ";
            }
          }
          clientNames =
              clientNames.substring(0, clientNames.length() - 2)
                  + "."; // Replaces last comma with a full stop
          sendMsg(Colours.Cyan + clientNames + Colours.Reset, true); // Sends message to requester

        } else if (line.matches("^##[A-z0-9]+$")) { // Used to change a users name
          String oldName = name;

          name = line.replaceAll("^##([A-z0-9]+)$", "$1"); // Changes name
          consoleOut.out(
              "- Client named: " + socket + " | " + name,
              Colours.Cyan,
              true); // Displays broadcast message

          if (nameFlag) { // Checks if it is a connection or name change
            massMsg(Colours.Green + "- " + name + " has connected." + Colours.Reset, true);
            nameFlag = false;
          } else {
            sendMsg(Colours.Cyan + "- Name updated to " + name + Colours.Reset, true);
            massMsg(
                Colours.Cyan
                    + "- "
                    + oldName
                    + " has changed name to "
                    + name
                    + "."
                    + Colours.Reset,
                true);
          }
        } else { // Standard message
          massMsg(line, false);
        }
      }
    } catch (Exception e) {
      if (running) {
        consoleOut.out(
            "ERROR - BaseServerClient | User Run " + socket + " | " + name, Colours.Error, true);
        logout();
      }
    }
  }

  /**
   * This is used to send a message to this client.
   *
   * @param message Message to be sent
   */
  public void sendMsg(String message, boolean timestamp) {
    if (running) {
      String time = timestamp ? consoleOut.getTimestamp() : " ";
      try {
        out.writeUTF(time + message);
      } catch (IOException e) {
        consoleOut.out(
            "ERROR - BaseServerClient | User sendMsg " + socket + " | " + name,
            Colours.Error,
            true);
      }
    }
  }

  /**
   * This is used to send a message to all other clients.
   *
   * @param message Message to be sent
   */
  private void massMsg(String message, boolean timestamp) {
    for (ServerClient client : clients) { // Iterates through clients and calls sendMsg
      if (client != this) {
        client.sendMsg(message, timestamp);
      }
    }
  }

  /**
   * This is used to filter incoming text from users, before it is sent to other users.
   *
   * @param line Line to be filtered
   * @return Returns filtered lines
   */
  private String filter(String line) {
    // Checks if filter is active
    if (filter != null) {
      // Splits line into words
      String[] words = line.split("\\s+");
      StringBuilder filtered = new StringBuilder();
      for (String word : words) {
        // Checks word against filter
        if ((filter).stream().anyMatch(word::matches)) {
          // Replaces word with ****
          word = "*".repeat(word.length());
        }
        filtered.append(word).append(" ");
      }
      return filtered.toString();
    } else {
      return line;
    }
  }

  public String getName() {
    return name;
  }

  public boolean getActive() {
    return running;
  }

  /** Used to disconnect and ensure all of it properly closes. */
  private void logout() {
    // Displays messages to clients
    sendMsg(Colours.Green + "Server: you have been logged out." + Colours.Reset, true);
    massMsg(Colours.Green + "- " + name + " has disconnected." + Colours.Reset, true);
    consoleOut.out("- Client Disconnect: " + socket + " | " + name, Colours.Error, true);

    close();
  }

  /**
   * Used to kick a client from the server
   *
   * @param message reason for being kicked message.
   */
  public void kick(String message) {
    // Displays messages to clients
    sendMsg(
        Colours.Green
            + "Kicked by SERVER reason: "
            + message
            + " | please logout with /logout"
            + Colours.Reset,
        true);
    massMsg(Colours.Green + "- " + name + " has been kicked." + Colours.Reset, true);
    consoleOut.out("- Client Kicked: " + socket + " | " + name, Colours.Error, true);

    close();
  }

  /** Contains the disconnect code to close all streams and connections. */
  public void close() {
    try {
      // Stops running
      running = false;

      // Closes streams
      in.close();
      out.close();

      // socket.close(); Done on user end.
    } catch (IOException e) {
      consoleOut.out(
          "ERROR - BaseServerClient | User logout " + socket + " | " + name, Colours.Error, true);
    }
  }
}
