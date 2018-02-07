/*--------------------------------------------------------

1. Christopher Shikami 2/03/2018

2. java version "9"
Java(TM) SE Runtime Environment (build 9+181)
Java HotSpot(TM) 64-Bit Server VM (build 9+181, mixed mode)

3. Precise command-line compilation examples / instructions:

> javac MyWebServer.java


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java MyWebServer

Go to http://localhost:2540 in a FireFox browser, you will see a directory with hyperlinks
you may click on.
Alternatively, go to the individual files by http://localhost:2540/cat.html or http://localhost:2540/dog.txt

5. List of files needed for running the program.

MyWebServer.java

5. Notes:

e.g.:

Have not implemented the addnumbers functionality yet.

----------------------------------------------------------*/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class Worker extends Thread { //class worker creates a thread
    Socket sock;              //Class member, socket, local to Worker.

    Worker(Socket s)         //constructor
    {
        sock = s;           // assign argument s to socket
    }

    public void run() {
        //Get I/O streams in/out from the socket
        PrintStream out = null;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream())); //get data from browser
            out = new PrintStream(sock.getOutputStream()); //send printstream to browser

            String name;
            String fileName = null;
            String requestType;
            String requestVersion;
            String fileType;

            name = in.readLine();
            System.out.println(name);
            String[] result = name.split(" "); //split first line back from browser by blank space " " delimiter

            while (in.ready()) {
                name = in.readLine(); //read in lines from browser in background..
                System.out.println(name); //print those lines out to console
            }

            requestType = result[0]; //assign first substring of result to requestType

            if (requestType.equals("GET")) { //If first substring of result is a GET request..
                fileName = result[1]; //assing second substring of result to fileName (the file (or directory))
                System.out.println(fileName); //print to console for debugging

                if (fileName.startsWith("/")) { //if file starts with "/" ..
                    fileName = fileName.substring(1); //fileName is set to the fileName from / onwards
                }
                System.out.println(fileName); //print to console fileName for debugging

                if (!fileName.contains(".")) {
                    showDirectory(out, fileName); //if fileName doesn't have a . (such as dog.txt), call showDirectory method, which creates a directory
                    //and sends it/shows it on browser
                }

                InputStream inputStream = new FileInputStream(fileName); //put file of fileName into a FileInputStream
                File file = new File(fileName); //create file object (in order to get fileName file length)
                //print out MIME header for file with file content length and file type (that uses returnFileType method)
                out.print("HTTP/1.1 200 OK\r\n" + "Content-Length: " + file.length() + "\r\n" + "Content-Type: " + returnFileType(fileName) + "\r\n\r\n");
                System.out.print("HTTP/1.1 200 OK\r\n" + "Content-Length: " + file.length() + "\r\n" + "Content-Type: " + returnFileType(fileName) + "\r\n\r\n");

                sendFile(inputStream, out); //method to send file contents to browser
            } else {
                throw new RuntimeException();
            }

            sock.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String returnFileType(String fileName) { //return file type based on end of fileName
        if (fileName.endsWith(".txt")) { //if file ends in .txt..
            return "text/plain"; //return "text/plain"
        } else if (fileName.endsWith(".html")) { //if file ends in .html..
            return "text/html"; //return "text/html"
        } else
            return "text/plain"; //else, return "text/plain"
    }

    private static void sendFile(InputStream file, OutputStream out) { //send file contents to browser
        try {
            byte[] buffer = new byte[1000];
            while (file.available() > 0) {
                out.write(buffer, 0, file.read(buffer)); //read in file contents to buffer and write to browser
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //method create and show directory in browser
    private static void showDirectory(PrintStream out, String fileName) {
        StringBuilder directoryHtml = new StringBuilder(); //html for directory in string form
        StringBuilder header = new StringBuilder(); //MIME header in string form

        File f1 = new File("."); //create a file for the current directory
        String parent = null;

        //if fileName is empty, show root directory
        if (fileName.equals("")) {
            f1 = new File(".");
            parent = "/";
        }

        else //otherwise show sub directory
        {
            f1 = new File("./" + fileName); //create file for fileName directory
            fileName = "/" + fileName; //add "/" in front of filename
            parent = fileName.substring(0, fileName.lastIndexOf("/")); //get parent directory of fileName (before "/")

            //if parent is empty, then parent is itself
            if (parent.equals(""))
                parent = "/";
        }

        File[] stringFileDirectory = f1.listFiles(); //get all the files in the directory
        String path = null;

        //create html for directory
        directoryHtml.append("<h1> Index of " + fileName + "</h1>");
        directoryHtml.append("<pre>");
        directoryHtml.append("<a href='" + parent + "'>Parent Directory</a><br><br>");

        //for all the files in the directory
        for (int i = 0; i < stringFileDirectory.length; i++) {
            //if it is a directory
            if (stringFileDirectory[i].isDirectory()) {
                //append directory and path to html
                directoryHtml.append("<a href=" + stringFileDirectory[i].toString().substring(1) + ">" + stringFileDirectory[i].toString().substring(2) + "</a>\n");
                System.out.println("directory: " + stringFileDirectory[i]);
            }
            //if it is a file
            else if (stringFileDirectory[i].isFile()) {
                //append directory and path to html
                directoryHtml.append("<a href=" + stringFileDirectory[i].toString().substring(1) + ">" + stringFileDirectory[i].toString().substring(2) + "</a>\n");
                System.out.println("file: " + stringFileDirectory[i]);
            }
        }

        directoryHtml.append("</pre>");

        String directoryString = directoryHtml.toString();
        byte[] fileSize = directoryHtml.toString().getBytes(); //amount of bytes in files

        //create MIME header
        header.append("HTTP/1.1 200 OK\r\n" + "Content-Length: " + fileSize.length + "\r\n" + "Content-Type: text/html \r\n" + "\r\n\r\n");

        System.out.println(header);
        System.out.println(directoryString);

        out.println(header.toString()); //send MIME header
        out.println(directoryString); //send html to browser
    }
}

public class MyWebServer {

    public static void main(String[] args) throws IOException {
        int q_len = 6; /* Not interesting. Number of requests for OpSys to queue */
        int port = 2540; //port number

        Socket sock;

        ServerSocket servsock = new ServerSocket(port, q_len); //new ServerSocket object with port and q_len arguments

        System.out.println("Chris Shikami's Port listener, running at port " + port + "\n"); //print out this message in server window

        while (true) {
            sock = servsock.accept(); //wait for the next client connection
            new Worker(sock).start(); //start the worker thread
        }
    }
}