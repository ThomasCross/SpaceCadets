import javax.net.ssl.SSLSocket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * IRC Client Writer
 *
 * <p>Reads user console and sends data to server.
 *
 * @author Thomas Cross
 * @author thomas@thomascross.net
 * @version 2.0
 */
public class ClientWriter implements Runnable {
  private SSLSocket socket; // Stores the user's socket connection
  private String name; // Stores the user's name
  private Boolean running; // Stores the state of the stream
  private Scanner userIn; // Data from user console
  private DataOutputStream serverOut; // Data to server
  private ConsoleOut consoleOut; // Object used to output text to user

  /**
   * This initialises the writer stream.
   *
   * @param socket User's socket connection
   * @param name User's name
   * @param running Used to store the state of the streams
   * @param consoleOut Object used to output text to user
   */
  public ClientWriter(SSLSocket socket, String name, Boolean running, ConsoleOut consoleOut) {
    this.socket = socket;
    this.name = name;
    this.running = running;
    this.consoleOut = consoleOut;

    // Initialises streams
    try {
      userIn = new Scanner(System.in);
      serverOut = new DataOutputStream(this.socket.getOutputStream());

      // Sends name to server
      serverOut.writeUTF("##" + this.name);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Used to read user console inputs and send data to the server. */
  public void run() {
    while (running) {
      try {
        if (userIn.hasNextLine()) { // Checks if the user has inputted a line
          String line = userIn.nextLine(); // Reads line

          if (line.length() == 0) {
            continue;
          }

          if (line.charAt(0) == '/') { // Checks if it is a command or message
            if (line.matches("^/logout|/l$")) { // Logs the user out
              serverOut.writeUTF("/logout");
              running = false; // Stops running

            } else if (line.matches("^/help|/h$")) { // Displays list of commands
              serverOut.writeUTF("/help");

            } else if (line.matches("^(/msg|/m) [A-z0-9]+ .+$")) { // Used to PM another user
              serverOut.writeUTF(
                  line.replaceAll("^((?:/msg|/m) [A-z0-9]+ ).+$", "$1")
                      + consoleOut.getTimestamp()
                      + "Private message from "
                      + name
                      + ": "
                      + line.replaceAll(
                          "^(?:/msg|/m) [A-z0-9]+ (.+)$",
                          "$1")); // Formats data and sends to server
            } else if (line.matches("^(/msg|/m) [A-z0-9]+ ")) { // Detects PM's without a message
              consoleOut.out("- msg is missing it's message.", Colours.Error, true);

            } else if (line.matches("^/users|/u$")) { // Gets a list of users
              serverOut.writeUTF("/users");

            } else if (line.matches("^(/nick|/n) [A-z0-9]+$")) { // Changes the user's name
              name = line.replaceAll("^(/nick|/n) ([A-z0-9]+)$", "$2"); // Sets name
              serverOut.writeUTF("##" + name); // Sends name change to server
            } else if (line.contains("/nick")
                || line.contains("/n")) { // Detects a name change with no name
              consoleOut.out(
                  "- Unable to understand name, refer to naming guide.", Colours.Error, true);
            } else { // Other command statements
              consoleOut.out(
                  "- Unable to understand this command, refer /help /h.", Colours.Error, true);
            }
          } else { // Standard message
            serverOut.writeUTF(consoleOut.getTimestamp() + name + ": " + line);
          }
        }
      } catch (IOException e) {
        running = false;
      }
    }
  }

  /** Stops running and closes the stream. */
  public void shutdown() throws IOException {
    running = false;
    userIn.close();
    serverOut.close();
  }
}
