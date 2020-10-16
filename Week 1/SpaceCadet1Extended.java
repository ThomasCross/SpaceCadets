import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceCadet1Extended {
  /**
   * Finds the name of someone from their email address using their email id.
   * On the secure.ecs part of the intranet.
   */
  public static void main (String[] args) throws IOException, InterruptedException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String email = "", id;

    //Get email from user and validate
    do {
      if (!email.equals("")) {
        System.out.println("Invalid Soton Email address");
      }
      System.out.print("Enter Soton Email Address > ");
      email = reader.readLine();
    } while (!email.matches("^([a-zA-Z0-9]+)(@soton.ac.uk|@ecs.soton.ac.uk)$"));

    //Split id from domain
    id = email.replaceAll("^([a-zA-Z0-9]+)(@soton.ac.uk|@ecs.soton.ac.uk)$", "$1");

    // Get login data
    Map<Object, Object> loginParam = new HashMap<>();
    System.out.print("Login > ");
    loginParam.put("ecslogin_username", reader.readLine());

    System.out.print("Password > ");
    loginParam.put("ecslogin_password", System.console().readPassword());
    reader.close();

    //Build login data
    var loginBuilder = new StringBuilder();
    for (Map.Entry<Object, Object> entry : loginParam.entrySet()) {
      if(loginBuilder.length() > 0) loginBuilder.append("&");

      loginBuilder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
      loginBuilder.append("=");
      loginBuilder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
    }

    //Create Client
    HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    //Create and Send POST Request
    HttpRequest login = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(loginBuilder.toString()))
        .uri(URI.create("https://secure.ecs.soton.ac.uk/login/now/index.php"))
        .setHeader("User-Agent", "Java HttpClient Bot")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build();

    HttpResponse<String> response = client.send(login, HttpResponse.BodyHandlers.ofString());
    List<String> cookies = response.headers().allValues("set-cookie");
    String sentCookie = "";

    for(String cookie : cookies){
      if (cookie.contains("ecs_intra_session=")){
        sentCookie = cookie;
        break;
      }
    }
    if (sentCookie.equals("")){
      System.out.println("Invalid login.");
      System.exit(0);
    }

    //Create and Get Data
    HttpRequest data = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create("https://secure.ecs.soton.ac.uk/people/"+id))
        .setHeader("User-Agent", "Java HttpClient Bot")
        .setHeader("Cookie", sentCookie)
        .build();

    String[] responseData = client.send(data, HttpResponse.BodyHandlers.ofString()).body().split("\\n");
    String lineData = "";

    //Searches for line containing data inside the HTML
    for (String line: responseData){
      if (line.contains("<h1 class=\"withIntro\"><span id=\"name\" class=\"editable_text\"><span itemprop='name'>")){
        lineData = line;
        break;
      }
      if (line.contains("<p>This page either does not exist or you are not able to view it.</p>")){
        System.out.println("This user does not exist, or you are unable to access it with your permissions.");
        System.exit(0);
      }
    }

    //Output Data
    System.out.println("ID: "+ id);
    System.out.println("Email: "+ email);
    System.out.println("Name: " + lineData.replaceAll("^.*<span itemprop='name'>([A-z\\s]+)</span.*$", "$1"));
    System.out.println("Position: "+ lineData.replaceAll("^.*<span class='role'>([A-z0-9\\s]+)</span>([A-z0-9\\s]+)<br/><strong>.*$", "$1$2"));
  }
}
