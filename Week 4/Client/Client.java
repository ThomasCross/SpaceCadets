import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;

/**
 * IRC Client
 *
 * <p>Creates encrypted socket and streams to communicate with IRC server.
 *
 * @author Thomas Cross
 * @author thomas@thomascross.net
 * @version 2.0
 */
public class Client {
  private SSLSocket sslSocket;

  /**
   * This is used to get commandline inputs and start the client.
   *
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    if (args.length == 1 && (args[0].matches("^-h|--help$"))) { // Help
      System.out.println(
          "- - - Help - - -\n"
              + "    java BaseClient <host> <port> <jks file> <jks password>\n"
              + "    <host> This is a domain name of the IRC server.\n"
              + "    <port> This is the port fo the IRC server.\n"
              + "           This needs to be between or equal to 1024 and 49151.\n"
              + "    <jks file> This is the encryption key file.\n"
              + "    <jks password> This is the password for the file.");

    } else if (args.length == 4) { // Default
      String host;
      String port;
      String jks;
      String jksPW;
      int portInt = 0;

      // Checks they are valid
      if (!args[0].matches( // Host
          "^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$")) {
        argumentError("Invalid host, see --help.");
      }
      if (!args[1].matches("^([0-9]+)$")) { // Port
        argumentError("Invalid port, see --help.");
      }
      if (!args[2].matches("^([a-zA-Z0-9\\s_.\\-()])+(.jks)$")) { // JKS file
        argumentError("Invalid JKS file, see --help.");
      }
      if (!args[3].matches("^(.*)$")) { // JKS PW
        argumentError("Invalid password, see --help.");
      }

      // Separates host and port
      host = args[0];
      port = args[1];
      jks = args[2];
      jksPW = args[3];

      // Converts port to a number
      try {
        portInt = Integer.parseInt(port);
      } catch (NumberFormatException e) {
        argumentError("Invalid port number entry, see --help");
      }

      // Checks it is in a valid port range
      if (!(portInt >= 1024 && portInt <= 49151)) {
        argumentError("Invalid port it is outside the allowed range, see --help.");
      }

      new Client(host, portInt, jks, jksPW); // Starts Client

    } else { // Error
      argumentError("Invalid arguments, see --help.");
    }
  }

  /**
   * This is used to exit the program due to an error in the user inputted arguments.
   *
   * @param message Error message to be outputted, to the user.
   */
  private static void argumentError(String message) {
    System.out.println(Colours.Error + message + Colours.Reset);
    System.exit(1);
  }

  /**
   * This is used to create a encrypted socket connection and start the I/O streams.
   *
   * @param host host's domain/ip
   * @param port port number
   * @param jks certificate file location
   * @param jksPW password for certificate file
   */
  private Client(String host, int port, String jks, String jksPW) {
    // Initialising ConsoleClient
    ConsoleOut consoleOut = new ConsoleOut(null);

    consoleOut.out("Starting Tom's IRC Client | Base Version", false);
    String name = getUsername(); // Gets user's username
    Boolean running = true;

    // Attempts to create encrypted connection to server
    try {
      // Create store the certificate from the jks file
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(new FileInputStream(jks), jksPW.toCharArray());

      // Creates KeyManagers which are responsible for managing key material used for
      // authentication.
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
      keyManagerFactory.init(keyStore, jksPW.toCharArray());

      // Decides if credentials from a peer should be accepted
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
      trustManagerFactory.init(keyStore);

      // Creates SSLSocketFactories with a secure socket protocol
      SSLContext sslContext = SSLContext.getInstance("TLS");
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

      // Creates the secure socket used to communicate with the server.
      SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);
      consoleOut.out("Connected to " + host + ":" + port, Colours.Green, true);

    } catch (IOException | KeyStoreException e) { // On fail it will display message and exit
      consoleOut.out("Failed to connect to " + host + ":" + port, Colours.Error, true);
      e.printStackTrace();
      System.exit(1);
    } catch (CertificateException
        | UnrecoverableKeyException
        | NoSuchAlgorithmException
        | KeyManagementException e) {
      consoleOut.out("Failed to encryption to " + host + ":" + port, Colours.Error, true);
      e.printStackTrace();
      System.exit(1);
    }

    // Initialises I/O streams
    ClientReader reader = new ClientReader(sslSocket, running);
    ClientWriter writer = new ClientWriter(sslSocket, name, running, consoleOut);

    // Creates I/O threads
    Thread readerThread = new Thread(reader);
    Thread writerThread = new Thread(writer);

    // Starts streams
    readerThread.start();
    writerThread.start();

    synchronized (writerThread) {
      try {
        writerThread.wait(); // Waits till writer logs out or until the writer detects the socket has closed.
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      try {
        consoleOut.out("- Logging out of IRC server.", Colours.Green, true);

        // Stops streams
        reader.shutdown();
        writer.shutdown();

        // Closes socket connection
        sslSocket.close();

        // Kills threads
        readerThread.join();
        writerThread.join();
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * This is used to get a user's username.
   *
   * @return Returns a valid username
   */
  private String getUsername() {
    Scanner reader = new Scanner(System.in);
    String name = "";

    System.out.println("Enter username, only letters and numbers (no spaces).");

    while (!name.matches("^[A-z0-9]+$")) {
      System.out.print("> ");
      name = reader.nextLine().trim();
    }

    return name;
  }
}
