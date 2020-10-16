import java.io.*;
import java.util.*;

public class Interpreter {
  public static void main(String[] args) {
    Interpreter myInterpreter = new Interpreter();
  }

  /**
   * This is used to run the code and call the methods required
   */
  public Interpreter() {
    BufferedReader reader = new BufferedReader(new InputStreamReader((System.in)));

    try {
      //This is used get and store the program code
      List<String> program = getFile(reader);
      //This is used to get and store the users settings
      int[] settings = getSettings(reader);

      //This stores the current instruction address
      int programCounter = 0;
      //This stores variables, <identifier, value>
      Map<String, Integer> variables = new HashMap<>();
      //This is used to store the instruction address for a loop to return to
      Stack<Integer> LoopStack = new Stack<>();
      //This is used to store if the current if condition has been true
      Stack<Boolean> IfStack = new Stack<>();

      //Instruction processing loop: processes until programCounter has reached the ned of program
      do {
        //Gets current line and removes whitespace
        String line = program.get(programCounter).trim();
        String identifier, modifier, operation;

        if (line.matches("^clear ([A-Za-z]+);$")) {
          identifier = line.replaceAll("^clear ([A-Za-z]+);$", "$1");
          basic(identifier, "0", 0, variables);

        } else if (line.matches("^incr ([A-Za-z]+) ([A-Za-z0-9]+);$")) {
          identifier = line.replaceAll("^incr ([A-Za-z]+) ([A-Za-z0-9]+);$", "$1");
          modifier = line.replaceAll("^incr ([A-Za-z]+) ([A-Za-z0-9]+);$", "$2");
          basic(identifier, modifier, 1, variables);

        } else if (line.matches("^decr ([A-Za-z]+) ([A-Za-z0-9]+);$")) {
          identifier = line.replaceAll("^decr ([A-Za-z]+) ([A-Za-z0-9]+);$", "$1");
          modifier = line.replaceAll("^decr ([A-Za-z]+) ([A-Za-z0-9]+);$", "$2");
          basic(identifier, modifier, 2, variables);

        } else if (line.matches("^mult ([A-Za-z]+) ([A-Za-z0-9]+);$")) {
          identifier = line.replaceAll("^mult ([A-Za-z]+) ([A-Za-z0-9]+);$", "$1");
          modifier = line.replaceAll("^mult ([A-Za-z]+) ([A-Za-z0-9]+);$", "$2");
          basic(identifier, modifier, 3, variables);

        } else if (line.matches("^div ([A-Za-z]+) ([A-Za-z0-9]+);$")) {
          identifier = line.replaceAll("^div ([A-Za-z]+) ([A-Za-z0-9]+);$", "$1");
          modifier = line.replaceAll("^div ([A-Za-z]+) ([A-Za-z0-9]+);$", "$2");
          basic(identifier, modifier, 4, variables);

        } else if (line.matches("^mod ([A-Za-z]+) ([A-Za-z0-9]+);$")) {
          identifier = line.replaceAll("^mod ([A-Za-z]+) ([A-Za-z0-9]+);$", "$1");
          modifier = line.replaceAll("^mod ([A-Za-z]+) ([A-Za-z0-9]+);$", "$2");
          basic(identifier, modifier, 5, variables);

        } else if (line.matches("^while ([A-Za-z]+) not ([A-Za-z0-9]+) do;$")) {
          if (checkWhile(line, variables)) { //Checks if variable meets condition
            //Meets condition: Iterated through lines until it find the next end instruction
            do {
              programCounter++;
              line = program.get(programCounter);
            } while (!line.matches("^end;$"));
          } else {
            //Fails condition: adds instruction address to stack and continues
            LoopStack.push(programCounter);
          }
        } else if (line.matches("^end;$")) {
          if (checkWhile(program.get(LoopStack.peek()).trim(), variables)) { //Checks if variable meets condition
            //Meets condition: Pops instruction address and continues
            LoopStack.pop();
          } else {
            //Fails condition: sets programCounter to address of start of loop
            programCounter = LoopStack.peek();
          }
        } else if (line.matches("^if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$")) {
          identifier = line.replaceAll("^if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$", "$1");
          operation = line.replaceAll("^if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$", "$2");
          modifier = line.replaceAll("^if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$", "$3");

          if (ifstate(identifier, operation, modifier, variables)) {
            //Condition true: sets skip state and continues through program
            IfStack.push(true);
          } else {
            //Condition false: skips to next part of statement
            IfStack.push(false);
            programCounter = ifskip(programCounter, line, program, false);
          }
        } else if (line.matches("^else if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$")) {
          //Checks if a previous section of the if statement was run
          if (IfStack.peek()) {
            //Skips to endif
            programCounter = ifskip(programCounter, line, program, true);
          } else {
            identifier = line.replaceAll("^else if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$", "$1");
            operation = line.replaceAll("^else if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$", "$2");
            modifier = line.replaceAll("^else if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$", "$3");

            if (ifstate(identifier, operation, modifier, variables)) {
              //Condition true: sets skip state and continues through program
              IfStack.pop();
              IfStack.push(true);
            } else {
              //Condition false: skips to next part of statement
              programCounter = ifskip(programCounter, line, program, false);
            }
          }
        } else if (line.matches("^else;$")) {
          //Checks if a previous section of the if statement was run
          if (IfStack.peek()) {
            //skips to endif
            programCounter = ifskip(programCounter, line, program, true);
          }

        } else if (line.matches("^endif;$")) {
          //removes if from stack since it has finished
          IfStack.pop();
        } else {
          //Displays error for unknown code, except comments
          if (!line.matches("^//([A-z0-9.]| )*$")) {
            System.out.println("Invalid Line: " + line);
            System.exit(1);
          }
        }

        //Increments programCounter
        programCounter++;

        //If setting is enabled it will output data
        if (settings[0] == 1) {
          output(variables);
          System.out.println("PC: " + programCounter);
          System.out.println("Line: " + line);
          System.out.println("LoopStack: " + LoopStack);
        }

        //If setting is enabled it will allow step
        if (settings[1] == 1) {
          System.out.println("PRESS ENTER TO CONTINUE");
          reader.readLine();
        }

      } while (programCounter < program.size());

      reader.close();
      output(variables); //Outputs final variable values

    } catch (Exception e) {
      System.out.println("Error occurred");
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * This method will prompt the user for a file name.
   * It will then check and validate the file is valid.
   *
   * @param reader This is used to read user inputs
   * @return This will return the program as a List<String>
   * @throws IOException File read or user input gone wrong
   */
  private List<String> getFile(BufferedReader reader) throws IOException {
    String choice;
    List<String> code = new ArrayList<>();

    System.out.println("" +
        "- - - - - - - - - - - - - -\n   Crossy's Interpreter\n- - - - - - - - - - - - - -");
    do {
      //This section will get the name of the file to be run.
      System.out.print("Use default file 'code.txt' (y or file name)\n> ");
      choice = reader.readLine();

      //Check if choice is y and fill default value, and check it is not empty
      if (choice.toLowerCase().equals("y") || choice.toLowerCase().equals("yes")) {
        choice = "code.txt";
      } else if (choice.equals("")) {
        continue;
      }

      if (choice.matches(".+\\.txt$")) { //Verify it is a .txt file
        if (new File(choice).isFile()) { //Checks if file exists
          //This will read the file and put it into program
          BufferedReader file = new BufferedReader(new FileReader(choice));
          String line;
          while ((line = file.readLine()) != null) {
            code.add(line);
          }
          file.close();
        } else {
          System.out.println(choice + " is an invalid file.");
        }
      } else {
        System.out.println(choice + " is an invalid filetype.");
      }
    } while (code.isEmpty());

    return code;
  }

  /**
   * This method will get values for the settings inside the program.
   * (ie, step through program and output variables).
   *
   * @param reader This is used to read user inputs
   * @return This will return an array of flags, to denote on or off
   * @throws IOException User input gone wrong
   */
  private int[] getSettings(BufferedReader reader) throws IOException {
    int[] settings = {-1, -1}; //{Output, Step}
    String temp;

    System.out.println(" - - - - Variables - - - - ");
    do {
      //Gets user input
      System.out.print("Output program info (y/n)\n> ");
      temp = reader.readLine();

      //Checks user input is valid
      if (temp.toLowerCase().equals("y") || temp.toLowerCase().equals("yes")) {
        settings[0] = 1;

        do {
          //Gets user input
          System.out.print("Step Program (y/n)\n> ");
          temp = reader.readLine();

          //Checks user input is valid
          if (temp.toLowerCase().equals("y") || temp.toLowerCase().equals("yes")) {
            settings[1] = 1;
          } else if (temp.toLowerCase().equals("n") || temp.toLowerCase().equals("no")) {
            settings[1] = 0;
          }
        } while (settings[1] == -1);

      } else if (temp.toLowerCase().equals("n") || temp.toLowerCase().equals("no")) {
        settings[0] = 0;
        settings[1] = 0;
      } else {
        System.out.println("Err:" + temp);
      }
    } while (settings[0] == -1);

    return settings;
  }

  /**
   * This method will iterate though the dictionary of variables and output name and value.
   *
   * @param variables This stores the variables which will be displayed
   */
  private void output(Map<String, Integer> variables) {
    System.out.println(" - - - - Variables - - - - ");
    for (String key : variables.keySet()) {
      System.out.println(key + ": " + variables.get(key));
    }
  }

  /**
   * This method is used to process the basic instructions (Clear, Incr, Decr).
   *
   * @param identifier What the instruction is performed on
   * @param value      Type of instruction
   * @param variables  Dictionary of variables
   */
  private void basic(String identifier, String value, int type, Map<String, Integer> variables) {
    int modifier;

    checkVariable(identifier, variables);
    if (value.matches("[A-Za-z]+")) {
      checkVariable(value, variables);
      modifier = variables.get(value);
    } else {
      modifier = Integer.parseInt(value);
    }

    switch (type) {
      case 0 -> variables.put(identifier, 0); //Sets value to 0 - Clear
      case 1 -> variables.put(identifier, variables.get(identifier) + modifier); //Increments value - Incr
      case 2 -> variables.put(identifier, variables.get(identifier) - modifier); //Decrements value - Decr
      case 3 -> variables.put(identifier, variables.get(identifier) * modifier); //Multiply value - Mult
      case 4 -> variables.put(identifier, variables.get(identifier) / modifier); //Divide value - Div
      case 5 -> variables.put(identifier, variables.get(identifier) % modifier); //Modulo value - Mod
    }
  }

  /**
   * This method will take the if statement's values and operator and determine if it is true or false.
   *
   * @param identifier first value in if statement
   * @param operator   operator in if statement
   * @param modifier   second value in if statement
   * @param variables  Dictionary of variables
   * @return True or false, depending if the condition is true or false
   */
  private Boolean ifstate(String identifier, String operator, String modifier, Map<String, Integer> variables) {
    int a, b;
    boolean condition;

    //Gets the value for the first value
    if (identifier.matches("[A-Za-z]+")) {
      checkVariable(identifier, variables);
      a = variables.get(identifier);
    } else {
      a = Integer.parseInt(identifier);
    }

    //Gets the value for the second value
    if (modifier.matches("[A-Za-z]+")) {
      checkVariable(modifier, variables);
      b = variables.get(modifier);
    } else {
      b = Integer.parseInt(modifier);
    }

    //Makes comparison
    switch (operator) {
      case "==" -> condition = a == b;
      case "!=" -> condition = a != b;
      case ">" -> condition = a > b;
      case "<" -> condition = a < b;
      case ">=" -> condition = a >= b;
      default -> condition = a <= b;
    }

    return condition;
  }

  /**
   * This program is used to skip througth the code to the next section of the if or endif.
   *
   * @param programCounter This stores the current instruction address
   * @param line           This stored the line from the program currently being operated on
   * @param program        This stores the program
   * @param position       This determines how far it skips. true to endif and false to next section
   * @return This will return the new programCounter position
   */
  private int ifskip(int programCounter, String line, List<String> program, boolean position) {
    //Used to determine how far into nested ifs the program is and so it exits on the correct section.
    int ifdepth = 0;
    boolean flag = true;

    do {
      //Gets next line
      programCounter++;
      line = program.get(programCounter);

      //Checks if ifdepth needs to increase
      if (line.matches("^if ([A-Za-z0-9]+) (==|!=|>|<|>=|<=) ([A-Za-z0-9]+);$")) {
        ifdepth += 1;
      }

      //Checks if ifdepth needs to decrease
      if (line.matches("^endif;$") && ifdepth > 0) {
        ifdepth -= 1;
      }

      //Determines which check is run depending on the skip setting
      if (position) {
        //Go to endif
        if (line.matches("^endif;$") && ifdepth == 0) {
          flag = false;
        }
      } else {
        //Go to next section
        if (line.matches("(^else if ([A-Za-z0-9]+) (==|!=) ([A-Za-z0-9]+);$)|(^else;$)|(^endif;$)") && ifdepth == 0) {
          flag = false;
        }
      }
    } while (flag);
    return programCounter - 1;
  }

  /**
   * This will check if a while loop's variable meets its condition.
   *
   * @param line      While instruction
   * @param variables Dictionary of variables
   * @return Returns true of false if the condition meets or fails
   */
  private boolean checkWhile(String line, Map<String, Integer> variables) {
    //Gets the identifier of the variable
    String ident = line.replaceAll("^while ([A-Za-z]+) not ([0-9]+) do;$", "$1");
    int identInt;
    checkVariable(ident, variables);
    identInt = variables.get(ident);

    //Gets the value the variable needs to meet
    String value = line.replaceAll("^while ([A-Za-z]+) not ([0-9]+) do;$", "$2");
    int valueInt;

    //Gets the value for the condition
    if (value.matches("[A-Za-z]+")) {
      checkVariable(value, variables);
      valueInt = variables.get(value);
    } else {
      valueInt = Integer.parseInt(value);
    }

    return identInt == valueInt;
  }

  /**
   * This is used to check if a variable already exists and if not create the variable in the dictionary.
   *
   * @param identifier Identifier of the variable
   * @param variables  Dictionary of variables to compare against and add to.
   */
  private void checkVariable(String identifier, Map<String, Integer> variables) {
    //Checks if the variable's identifier exists
    boolean flag = variables.containsKey(identifier);
    if (!flag) {
      //Adds to dictionary
      variables.put(identifier, 0);
    }
  }
}
