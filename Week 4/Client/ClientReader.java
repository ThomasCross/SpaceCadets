import javax.net.ssl.SSLSocket;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
/**
 * IRC Client Reader
 *
 * <p>Receives data from server and prints in console.
 *
 * @author Thomas Cross
 * @author thomas@thomascross.net
 * @version 2.0
 */
public class ClientReader implements Runnable {
  private SSLSocket socket; // Used to store the socket
  private Boolean running; // Used to store the running state
  private DataInputStream serverIn; // Data from server

  /**
   * Used to initialise the inputs
   *
   * @param socket User's socket connection
   */
  public ClientReader(SSLSocket socket, Boolean running) {
    this.socket = socket;
    this.running = running;

    // Starts stream from server
    try {
      serverIn = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Prints any data from the server */
  public void run() {
    while (running) {
      try {
        System.out.println(serverIn.readUTF());
      } catch (IOException e) {
        running = false;
      }
    }
  }

  /** Stops running and closes the stream. */
  public void shutdown() throws IOException {
    running = false;
    serverIn.close();
  }
}
