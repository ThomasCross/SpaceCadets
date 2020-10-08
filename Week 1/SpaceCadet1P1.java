import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class SpaceCadet1P1 {
  /**
   * Finds the name of someone from their email address using their email id.
   */
  public static void main (String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String email = "", id = "";

    //Get email from user and validate
    do {
      if (!email.equals("")) {
        System.out.println("Invalid Soton Email address");
      }
      System.out.print("Enter Soton Email Address > ");
      email = reader.readLine();
    } while (!email.matches("^([a-zA-Z0-9]+)(@soton.ac.uk|@ecs.soton.ac.uk)$"));
    reader.close();

    //Split id from domain
    id = email.replaceAll("^([a-zA-Z0-9]+)(@soton.ac.uk|@ecs.soton.ac.uk)$", "$1");


    //Access page and get name
    String web = "https://www.ecs.soton.ac.uk/people/" + id;
    URL page = new URL(web);

    reader = new BufferedReader(new InputStreamReader(page.openStream()));
    String inputLine, name = "";
    while ((inputLine = reader.readLine()) != null) {
      if (inputLine.matches("(.*)property=\"name\">([A-Za-z\\s]+)<(.*)")) {
        name = inputLine.replaceAll("(.*)property=\"name\">([A-Za-z\\s]+)<(.*)", "$2");
        break;
      }
    }
    reader.close();

    //Output name
    if (name.equals("")) name = "Unable to find this user.";
    System.out.println(name);
  }
}
