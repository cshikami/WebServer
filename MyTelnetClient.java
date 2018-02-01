import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MyTelnetClient {

    public static void main(String[] args) {
        String serverName;
        if (args.length < 1) { //if there are no arguments added when running the class, then serverName is localhost
            serverName = "localhost";
        } else {
            serverName = args[0]; //otherwise, serverName is assigned the first argument provided
        }

        System.out.println("Chris Shikami's MyTelnet Client, 1.8\n"); //print out in client window
        System.out.println("Using server: " + serverName + ", Port: 80"); //print out in client window, serverName being either localhost or the string provided as argument by user
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); //create object to read system input into BufferedReader
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String name;
            do {
                System.out.print("Enter text to send to the server, type <quit> and then press enter key twice to end: "); //print out to client window
                System.out.flush(); //have to flush the output because we don't want a newline after previous printed print statement
                name = in.readLine();
                stringBuilder.append(name + "\r\n");
                name = in.readLine();
                stringBuilder.append(name + "\r\n");
                stringBuilder.append("\r\n\r\n");

                if (stringBuilder.indexOf("quit") < 0) {  //if the user does not write quit in system input..
                    getRemoteAddress(stringBuilder.toString(), serverName); //call getRemoteAddress method with name and serverName arguments
                    stringBuilder = new StringBuilder();
                }
            } while (stringBuilder.indexOf("quit") < 0);  //if user input in client window is quit...
            System.out.println("Cancelled by user request."); //print out Cancelled by user request
        }
        catch (IOException x) { //if there is an exception
            x.printStackTrace(); //print stacktrace
        }
    }

    static String toText (byte ip[]) { /* Make portable for 128 bit format */
        StringBuffer result = new StringBuffer();//new StringBuffer object named result
        //format the ip address:
        for (int i = 0; i < ip.length; ++ i) {
            if (i > 0) {
                result.append(".");
            }
            result.append(0xff & ip[i]);
        }
        return result.toString(); //return StringBuffer object as string
    }

    static void getRemoteAddress (String name, String serverName) {
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try {
            //new Socket object with serverName and port number arguments
            sock = new Socket(serverName, 80);

            //Create filter I/O streams for the socket:
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //get input stream from server
            toServer = new PrintStream(sock.getOutputStream()); //send output stream to server

            //Send machine name or IP address to server:

            //toServer.println(name + "\r\nHost:" + serverName +"\r\n");
            toServer.println(name);
            toServer.flush();

            //Read two or three lines of response from the server,
            //and block while synchronously waiting:
            for  (int i = 1; i <= 20; i++) {
                textFromServer = fromServer.readLine();
                if (textFromServer != null) { //if textFromServer is not null
                    System.out.println(textFromServer); //print text response from server
                }
            }
            sock.close(); //close socket connection
        }
        catch (IOException x) { //print exception stack trace if there is an IOException
            System.out.println("Socket error.");  //print Socket error. above stack trace
            x.printStackTrace(); //print exception stack trace
        }

    }
}
