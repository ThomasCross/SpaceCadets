# Interpreter.java
This program is an extended interpreter for the bare bones language, written for the University of Southampton's Space Cadets coding challanges.

## Notes
- This intpreter ignores indentation, so it can be used for ease of readability.
- All variables are initalised with a value of 0 and can be intalised with any command.
- Variables can be any length but must only contain letters. (ie, X, Y, Z, Abc, abC)

## Comments
    // This is a comment
A comment is created by starting with // followed by text. 

Comments can only contain letters, numbers and a fullstop. Shown by this regex `^//([A-z0-9.]| )*$`

## Maths 
    //X is used as a variable, 1 is where a variable or integer can be used.
    clear X; //Used to set variable to 0
    incr X 1; //Add
    decr X 1; //Subtract
    mult X 1; //Multiply
    div X 1; //Quotient
    mod X 1; //Modulo

`clear X;` only accepts 1 variable and commonly used to initalise variables.

`incr X 1;` 1 variable, 1 integer

`incr X Y;` 2 variable

## While Loop
    //Y is used as a variable, X can be a variable or integer.
    while Y not X do;
      ...
    end;

This can be used to loop thougth code.

## If Statements
    //Both T and 10 can be used as a variable or integer.
    if T != 10;
    ...
    else if T == 10;
    ...
    else;
    ...
    endif;

Note, indentation does not affect running, make sure each if statement starts with an **if** and ends with a **endif**. 
