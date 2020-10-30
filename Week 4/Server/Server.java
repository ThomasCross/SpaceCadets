import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * IRC Server
 *
 * <p>Creates a encrypted server socket which accepts connections. Creates clients and enables chat
 * between them.
 *
 * @author Thomas Cross
 * @author thomas@thomascross.net
 * @version 2.0
 */
public class Server {
  /**
   * This is used to get commandline inputs and start the server
   *
   * @param args Used to input commandline arguments
   */
  public static void main(String[] args) {
    int port = 2704;

    // Stores argument settings
    StringBuilder banner = null;
    String[] encrypt = null;
    String logging = null;
    ArrayList<String> filter = null;

    try {
      for (int i = 0; i < args.length; i++) {
        switch (args[i]) {
          case "-h":
          case "--help":
            // Outputs help menu
            System.out.println(
                ""
                    + " - - - Help Menu - - - \n"
                    + "    -b --banner <file> (Add to display a custom welcome banner)\n"
                    + "    -P --port <port> (Specify port, defaults to 2704)\n"
                    + "    -e --encrypt <jks file> <password> (Adds encryption, requires password)\n"
                    + "    -l --logging <file> (Outputs the console into a file)\n"
                    + "    -f --filter <file> (Adds a filter on words said by users)\n"
                    + " - - - - - - - - - - - ");
            System.exit(1);
            break;

          case "-b":
          case "--banner":
            // Gets banner from banner file
            banner = new StringBuilder();
            try {
              Scanner reader = new Scanner(new File(args[i + 1]));
              String line;

              while (reader.hasNextLine()) {
                line = reader.nextLine();
                // Removes comments in file
                if (!line.substring(0, 2).matches("//")) {
                  banner.append(line).append("\n");
                }
              }

            } catch (FileNotFoundException e) {
              argumentError("Banner file in argument could not be found.");
            }

            System.out.println(Colours.Green + "Banner file loaded." + Colours.Reset);
            i++;
            break;

          case "-P":
          case "--port":
            // Gets port number
            try {
              // Converts String to Integer
              port = Integer.parseInt(args[i + 1]);
              i++;
              System.out.println(Colours.Green + "Port changed to " + port + Colours.Reset);
            } catch (NumberFormatException e) {
              argumentError("Inputted port number is invalid.");
            }
            break;

          case "-e":
          case "--encrypt":
            encrypt = new String[2];

            // Validates entries look valid
            if (!args[i + 1].matches("^([a-zA-Z0-9\\s_.\\-()])+(.jks)$")) { // JKS file
              argumentError("Invalid JKS file, see --help.");
            }
            if (!args[i + 2].matches("^(.*)$")) { // JKS PW
              argumentError("Invalid password, see --help.");
            }

            encrypt[0] = args[i + 1];
            encrypt[1] = args[i + 2];

            i += 2;
            break;

          case "-l":
          case "--logging":
            // Moves file location
            logging = args[i + 1];
            System.out.println(Colours.Green + "Logging file added" + Colours.Reset);
            i++;
            break;

          case "-f":
          case "--filter":
            // gets filtered words from filter file
            filter = new ArrayList<>();
            try {
              Scanner reader = new Scanner(new File(args[i + 1]));
              String line;

              while (reader.hasNextLine()) {
                line = reader.nextLine();
                // Removes comments
                if (!line.substring(0, 2).matches("//")) {
                  filter.add(line);
                }
              }

            } catch (FileNotFoundException e) {
              argumentError("Filter file in argument could not be found.");
            }

            System.out.println(Colours.Green + "Filter file added" + Colours.Reset);
            i++;
            break;

          default:
            argumentError(args[i] + " is not a valid entry, please view -h --help for help.");
        }
      }
    } catch (IndexOutOfBoundsException e) {
      argumentError("Argument Error, see --help and check you put a valid number of arguments in.");
    }

    if (encrypt == null) {
      argumentError("Argument Error, missing encryption entry see --help.");
    }
    new Server(port, banner.toString(), encrypt, logging, filter); // Starts server
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
   * This is used to run the server. First it will create the ServerSocket and initialize the *
   * clients arraylist. Loop accepting sockets and creating a client for each. This is then added to
   * the clients arraylist. It will also check for inactive clients and remove them.
   *
   * @param port Port used to connect to the server.
   * @param banner Contains connection banner
   * @param encrypt Contains encryption file location and password
   * @param logging Contains file location for logging file
   * @param filter Contains filtered words
   */
  private Server(
      int port, String banner, String[] encrypt, String logging, ArrayList<String> filter) {
    // Initialise logging and console output
    ConsoleOut consoleOut = new ConsoleOut(logging);

    try {
      consoleOut.out("Starting Tom's IRC Server | Base Version", Colours.Green, false);

      // Create store the certificate from the jks file
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(new FileInputStream(encrypt[0]), encrypt[1].toCharArray());

      // Creates KeyManagers which are responsible for managing key material used for
      // authentication.
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
      keyManagerFactory.init(keyStore, encrypt[1].toCharArray());

      // Decides if credentials from a peer should be accepted
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
      trustManagerFactory.init(keyStore);

      // Creates SSLSocketFactories with a secure socket protocol
      SSLContext sslContext = SSLContext.getInstance("TLS");
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

      // Creates the secure server socket factory used to accept connecting users
      SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
      SSLServerSocket server = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

      // Arraylist of all the client connections.
      ArrayList<ServerClient> clients = new ArrayList<>();

      // Initialise console
      ServerConsole consoleIn = new ServerConsole(clients, consoleOut); // Create console
      Thread consoleThread = new Thread(consoleIn); // Create thread for console
      consoleThread.start(); // Start thread

      consoleOut.out("- Console Started.", Colours.Green, true);

      // Initialise running, accepts clients and starts their streams
      Runnable main = () -> {
            SSLSocket socket;
            while (consoleIn.getRunning()) {
              try {
                socket = (SSLSocket) server.accept(); // Accept new socket
                consoleOut.out("- Client accepted: " + socket, Colours.Green, true);

                // Create client for socket
                ServerClient client = new ServerClient(socket, clients, banner, filter, consoleOut);

                Thread thread = new Thread(client); // Create Thread for socket
                thread.start(); // Start thread
                clients.add(client); // Add client to clients arraylist

                // Checks for inactive clients to remove
                clients.removeIf(clientThread -> !clientThread.getActive());
              } catch (IOException e) {
                if (consoleIn.getRunning()) {
                  consoleOut.out(
                      "ERROR - BaseServer Main | Socket and new Thread creation",
                      Colours.Error,
                      true);
                }
              }
            }
          };
      Thread mainThread = new Thread(main);
      mainThread.start();

      consoleOut.out("- Server Started, awaiting clients.", Colours.Green, true);

      // Running Server
      synchronized (consoleThread) {
        try {
          consoleThread.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        // Shutdown
        //Closes all client connections
        for (ServerClient client : clients) {
          if (client.getActive()) {
            client.sendMsg(
                Colours.Green
                    + "- Server Shutdown, you have been disconnected. Please logout with /logout"
                    + Colours.Reset,
                true);
            client.close();
          }
        }

        consoleOut.out("- All clients disconnected", Colours.Green, true);

        server.close(); //Closes serverSocket
        mainThread.join(); //Closes main running thread
        consoleThread.join(); //Closes logging and console output

        consoleOut.out("- IRC Server shutdown", Colours.Green, true);
      }
    } catch (IOException | InterruptedException | KeyStoreException e) {
      consoleOut.out("ERROR - BaseServer | Server and new Thread creation", Colours.Error, true);
      e.printStackTrace();
    } catch (CertificateException
        | NoSuchAlgorithmException
        | UnrecoverableKeyException
        | KeyManagementException e) {
      consoleOut.out("ERROR - BaseServer | Encryption", Colours.Error, true);
      e.printStackTrace();
    }

    consoleOut.shutdown();
  }
}
