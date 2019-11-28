package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Sockets {
    static volatile Map<String, String> resolver = new HashMap<>(); // ip, id
    static volatile Map<String, Socket> sockets = new HashMap<>(); // nodeId, socket
    static volatile Map<Socket, BufferedReader> readers = new HashMap<Socket, BufferedReader>();
    static volatile Map<Socket, PrintWriter> writers = new HashMap<Socket, PrintWriter>();
    static volatile Map<String, String> IDToIPResolver = new HashMap<>(); //id,ip
    static int ID;
    static String path;

    Sockets() {
        resolver.put("10.176.69.32", "S1");
        resolver.put("10.176.69.33", "S2");
        resolver.put("10.176.69.34", "S3");
        resolver.put("10.176.69.35", "S4");
        resolver.put("10.176.69.36", "S5");

        resolver.put("10.176.69.37", "C1");
        resolver.put("10.176.69.38", "C2");

        resolver.put("10.176.69.40", "M");

        IDToIPResolver.put("S1", "10.176.69.32"); // server 1: DC31
        IDToIPResolver.put("S2", "10.176.69.33"); // server 2: DC32
        IDToIPResolver.put("S3", "10.176.69.34"); // server 3: DC33
        IDToIPResolver.put("S4", "10.176.69.35"); // server 4: DC34
        IDToIPResolver.put("S5", "10.176.69.36"); // server 5: DC35

        IDToIPResolver.put("C1", "10.176.69.37"); // client 1: DC36
        IDToIPResolver.put("C2", "10.176.69.38"); // client 2: DC37

        IDToIPResolver.put("M", "10.176.69.40"); // metadata server M: DC38
    }

    public static void main(String[] args) throws Throwable {
        // TODO Auto-generated method stub

        if (args[0].equals("client")) {

            ServerSocket SS = new ServerSocket(5000);
            Listener listen = new Listener(SS);

            Scanner input = new Scanner(System.in);
            String[] string = input.nextLine().split(" ");
            while (!string[0].equals("exit")) {
                if (string[0].equals("connect")) {
                    List<String> nodeList = new ArrayList<>();
                    nodeList.add("S1");
                    nodeList.add("S2");
                    nodeList.add("S3");
                    nodeList.add("S4");
                    nodeList.add("S5");
                    nodeList.add("M");
                    for (String node : nodeList) {
                        connectNode(IDToIPResolver.get(node));
                    }
                }

                // todo: client operations to define here on, below is old code
                else {
                    String out = string[0];
                    PrintWriter pw = writers.get(sockets.get(out));
                    pw.println(string[1]);
                    pw.flush();
                }
                string = input.nextLine().split(" ");
            }
            System.exit(0);
        } else if (args[0].equals("server")) {

            // TODO: find current server ID
            String ID = resolver.get(InetAddress.getLocalHost().getHostAddress());
            path = "./" + ID;
            ServerSocket SS = new ServerSocket(5000);
            FileServerListener fileServerListener = new FileServerListener(SS);

            Scanner input = new Scanner(System.in);
            String[] string = input.nextLine().split(" ");
            while (!string[0].equals("exit")) {
                if(string[0].equals("connect")) {
                    List<String> nodeList = new ArrayList<>();
                    nodeList.add("S1");
                    nodeList.add("S2");
                    nodeList.add("S3");
                    nodeList.add("S4");
                    nodeList.add("S5");
                    nodeList.add("M");
                    for (String node : nodeList) {
                        if (!node.equals(ID))
                            connectNode(IDToIPResolver.get(node));
                    }
                }
                // string format : <command> <node id> <data>
                else if (string[0].equals("send")) {
                    // TODO: modify server code here
                    sendToFileServer(string[1], string[2]);
                }
                string = input.nextLine().split(" ");
            }

            System.exit(0);
        } else {
            ServerSocket SS = new ServerSocket(5000);
            Listener listener = new Listener(SS);
            Scanner input = new Scanner(System.in);
            //TODO: code for meta server goes here
            // this code might not be necessary
            // string format : <command> <node id> <data>
            String[] string = input.nextLine().split(" ");
            while (!string[0].equals("exit")) {
                if(string[0].equals("connect")) {
                    List<String> nodeList = new ArrayList<>();
                    nodeList.add("S1");
                    nodeList.add("S2");
                    nodeList.add("S3");
                    nodeList.add("S4");
                    nodeList.add("S5");
                    for (String node : nodeList) {
                        if (!node.equals(ID))
                            connectNode(IDToIPResolver.get(node));
                    }
                }
                if (string[0].equals("send")) {
                    // TODO: modify metadata server code here
                }
                string = input.nextLine().split(" ");
            }
            System.exit(0);
        }
    }

    public static void connectNode(String serverAddress) throws IOException {
        int serverPort = 5000;
        if (!sockets.containsKey(resolver.get(serverAddress))) {
            Socket s = new Socket(serverAddress, serverPort);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
            sockets.put(resolver.get(s.getInetAddress().getHostAddress()), s);
            readers.put(s, bf);
            writers.put(s, pw);
        }
        MessageHandler MH = new MessageHandler(
                readers.get(sockets.get(resolver.get(serverAddress))),
                writers.get(sockets.get(resolver.get(serverAddress)))
        );
    }

    /**
     * @param server : receivers ID
     * @param out    : sending data
     */
    public static void sendToFileServer(String server, String out) {
        PrintWriter pw = writers.get(sockets.get(server));
        pw.println(out);
        pw.flush();
    }

    public static void sendToMetaServer(String out) {
        PrintWriter pw = writers.get(sockets.get("S0"));
        pw.println(out);
        pw.flush();
    }
}
