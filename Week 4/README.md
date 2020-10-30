# IRC Server and Client
This contains an IRC server and client, written for the University of Southampton's Space Cadets coding challanges.

## Notes
- This uses symmetric encryption, with both the server and clients require the same certificate file.
- This uses ANSI colours, so issues may appear in Windows CMD with "Esc[32m" appearing before lines.

## Creating the Certificate
To create the x509 certificate you can use the Java keytool utility.

    keytool -genkey -keyalg RSA -alias selfsigned -keystore filename.jks -storepass password -validity 360 -keysize 2048

For Windows (Note, may vary depending on location of install and version of java)

    C:\Program Files\Java\jdk-15\bin\keytool.exe

## Client
### Arguments
    java Client <host> <port> <jks file> <jks password>
    
    <host> This is a domain name of the IRC server.
    <port> This is the port for the IRC server. This needs to be between or equal to 1024 and 49151.
    <jks file> This is the encryption key file.
    <jks password> This is the password for the file.

![Example of Client Arguments](https://tcnetwork.co.uk/resources/images/Client.PNG)

All arguments are required.

### Commands
    /logout /l (To logout)
    /nick /n <name> (To change name)
    /msg /m <name> <message> (To send a private message)
    /users /u (Get a list of active users)

## Server
### Arguments
    java Server <Argument>
    
    -b --banner <file> (Add to display a custom welcome banner)
    -P --port <port> (Specify port, defaults to 2704)
    -e --encrypt <jks file> <password> (Adds encryption, requires password)
    -l --logging <file> (Outputs the console into a file)
    -f --filter <file> (Adds a filter on words said by users)
    
![Example of Client Arguments](https://tcnetwork.co.uk/resources/images/Server.PNG)

Port and Encrypt arguments required.

### Commands
    /shutdown /s (To shutdown the server)
    /msg /m <name> <message> (To send a private message)
    /users /u (Get a list of active users)
    /kick /k <name> <message> (To kick a user)
    
### Files
(Note, "//" can be used as a comment in any file and won't be displayed)

**Banner** - Just enter test into the file, and it will be printed out. View example files to see an example banner.

**Logging** - This fill will be automatically created or appended to.

**Filter** - Enter one filtered word per line, words including spaces are invalid.
