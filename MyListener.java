import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

class Worker extends Thread { //Class definition, class Worker creates a thread
    Socket sock;              //Class member, socket, local to Worker.

    Worker(Socket s)         //Constructor, assign arg s to local sock
    {
        sock = s;
    }

    public void run() {
        //Get I/O streams in/out from the socket
        PrintStream out = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());
            //note that this branch might not output when expected:
            try {
                String name;

                while((name = in.readLine()) != null) { //read line of data from client, which gets assigned to name
                    printRemoteAddress(name, out); //call printRemoteAddress method with name and out arguments
                    System.out.println(name);//print out name to server window
                }

            } catch (IOException x) {
                System.out.println("Server read error"); //if for some reason there is an IOException, print out Server read error
                x.printStackTrace(); //print a stack trace of the error
            }
            sock.close(); //close this connection, but not the server

        } catch (IOException ioe) {
            System.out.println(ioe); //print the exception if there is an IOException
        }
    }


    static void printRemoteAddress(String name, PrintStream out) {
        //try { //these are all printed in the client window
            //out.println("" + name); //print Looking up and the name supplied from the run() method
            out.println("Got your request."); //print out "Got your request." to client window
            //InetAddress machine = InetAddress.getByName(name);
            //out.println("Host name: " + machine.getHostName()); //print out in client window the host name
            //out.println("Host IP: " + toText(machine.getAddress())); //print out in client window the ip address formatted by the toText method
//        } catch (UnknownHostException ex) {
//            out.println("Failed in attempt to lookup " + name); //if host is not know/does not exist, print out Unknown host exception and the name attempted
//        }
        //}
    }

    //Not interesting to us:
    static String toText(byte[] ip) {
        StringBuffer result = new StringBuffer(); //new StringBuffer object named result
        //format the ip address:
        for (int i = 0; i < ip.length; ++i) {
            if (i > 0) result.append(".");
            result.append(0xff & ip[i]);
        }
        return result.toString(); //return StringBuffer object as string
    }
}


public class MyListener {

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
