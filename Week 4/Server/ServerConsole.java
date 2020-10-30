import java.util.ArrayList;
import java.util.Scanner;

/**
 * IRC Server Console
 *
 * <p>Gets inputs from the server user, to perform commands.
 *
 * @author Thomas Cross
 * @author thomas@thomascross.net
 * @version 2.0
 */
public class ServerConsole implements Runnable {
  private ArrayList<ServerClient> clients;
  private Boolean running;
  private Scanner consoleIn;
  private ConsoleOut consoleOut;

  /**
   * This is used to initialize the class variables and scanner stream.
   *
   * @param clients Arraylist of connected clients
   * @param consoleOut Object used to send text to console
   */
  public ServerConsole(ArrayList<ServerClient> clients, ConsoleOut consoleOut) {
    this.clients = clients;
    this.running = true;
    consoleIn = new Scanner(System.in);
    this.consoleOut = consoleOut;
  }

  /** This is processes the commands from the scanner and performs an action. */
  public void run() {
    while (running) {
      if (consoleIn.hasNextLine()) {
        // Gets line from server user
        String line = consoleIn.nextLine();

        if (line.length() == 0) {
          continue;
        }

        if (line.charAt(0) == '/') {
          if (line.matches("^/shutdown|/s$")) { // Shutdown server
            running = false;
            consoleOut.out("- Shutting down IRC Server", Colours.Green, true);
          } else if (line.matches("^/help|/h$")) { // Displays list of commands
            consoleOut.out(
                " - - - Help - - - \n"
                    + "/shutdown /s (To shutdown the server)\n"
                    + "/msg /m <name> <message> (To send a private message)\n"
                    + "/users /u (Get a list of active users)\n"
                    + "/kick /k <name> <message> (To kick a user)\n"
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
                    Colours.Magenta
                        + "Private message from SERVER: "
                        + line.replaceAll("^(?:/msg|/m) [A-z0-9]+ (.+)$", "$1")
                        + Colours.Reset,
                    true); // Sends message
                validFlag = false;
              }
            }
            if (validFlag) { // Returns error is no message was sent
              consoleOut.out(
                  "- " + recipient + ", does not exist check /users.", Colours.Error, true);
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
            consoleOut.out(clientNames, Colours.Cyan, true); // Sends message to requester
          } else if (line.matches("^(/kick|/k) [A-z0-9]+ ?.*$")) { // Used to kick a user
            String recipient =
                line.replaceAll("^(?:/kick|/k) ([A-z0-9]+) ?.*$", "$1"); // Gets recipient of kick
            boolean validFlag = true; // Used to check a user has been kicked

            for (ServerClient client : clients) {
              if (client.getName().equals(recipient)) {
                client.kick(line.replaceAll("^(?:/kick|/k) [A-z0-9]+ ?(.*)$", "$1"));
                validFlag = false;
              }
            }
            if (validFlag) { // Returns error is no message was sent
              consoleOut.out(
                  "- " + recipient + ", does not exist check /users.", Colours.Error, true);
            }
          } else {
            consoleOut.out(
                "- Unable to understand this command, refer /help /h.", Colours.Error, true);
          }

        } else {
          for (ServerClient client : clients) {
            if (client.getActive()) {
              client.sendMsg("SERVER: " + line, true);
            }
          }
        }
      }
    }
  }

  public Boolean getRunning() {
    return running;
  }
}
