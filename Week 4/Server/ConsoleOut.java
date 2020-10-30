import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * IRC Server - ConsoleOut
 *
 * <p>Used to output text to a user, with the options for colour.
 *
 * @author Thomas Cross
 * @author thomas@thomascross.net
 * @version 2.0
 */
public class ConsoleOut {
  private PrintStream fileOut = null; // Logging file
  private SimpleDateFormat ukFormat; // Datetime format

  /**
   * Used to initialise a the object. If logging is enabled it will create the print stream. Sets
   * datetime format.
   *
   * @param path Contains the file path or null, depending if logging is active.
   */
  public ConsoleOut(Object path) {
    if (path != null) {
      try {
        fileOut = new PrintStream(new FileOutputStream((String) path, true));
      } catch (FileNotFoundException e) {
        System.out.println(Colours.Error + "Error locating/creating logging file." + Colours.Reset);
      }
    }
    ukFormat = new SimpleDateFormat("HH:mm");
    ukFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * Sends a non coloured message to console and log.
   *
   * @param message message to console and log
   * @param timestamp whether a time stamp is needed
   */
  public void out(String message, boolean timestamp) {
    String time = timestamp ? getTimestamp() : " ";
    if (fileOut != null) {
      fileOut.println(time + message);
    }
    System.out.println(time + message);
  }

  /**
   * Sends a coloured message to console and log.
   *
   * @param message message to console and log
   * @param colour colour of message
   * @param timestamp whether a time stamp is needed
   */
  public void out(String message, String colour, boolean timestamp) {
    String time = timestamp ? getTimestamp() : " ";
    if (fileOut != null) {
      fileOut.println(time + message);
    }
    System.out.println(colour + time + message + Colours.Reset);
  }

  /**
   * Returns the timestamp, for the UK/GMT timezone.
   *
   * @return returns formatted timestamp
   */
  public String getTimestamp() {
    Date date = new Date();
    return "[" + ukFormat.format(date.getTime()) + "] ";
  }

  /** Used to add spacer and close log file. */
  public void shutdown() {
    if (fileOut != null) {
      fileOut.println("\n \n \n \n"); // Spacer between appended logs
      fileOut.close();
    }
  }
}
