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
    static volatile Map<String, ArrayList<String>> successfulCreations = new HashMap<>();  // chunk , servers_list_hosting_it
    static volatile Map<String, String> chunkLocator = new HashMap<>(); // filename, latest_chunk
    static volatile Map<String, ArrayList<String>> serverLocator = new HashMap<>(); // filename , list of servers
    static volatile ArrayList<Long> lastServerBeat = new ArrayList<>(5);
    static volatile ArrayList<Integer> downServers = new ArrayList<>(5);

    static String ID;
    static String path;
    static int port;

    Sockets() { }
    public static void main(String[] args) throws Throwable {
        // TODO Auto-generated method stub
        initializeSockets();
        if (args[0].equals("client")) {

            ServerSocket SS = new ServerSocket(port);
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
                else if(string[0].equals("create")) {
                    String data = string[1];
                    String fileName = string[2];// this has the filename
                    PrintWriter pw = writers.get(sockets.get("M"));
                    pw.println(string[0] +" "+ fileName + " " + data);
                    pw.flush();
                }
                else if (string[0].equals("read")) {
                    String fileName = string[1];
                    String offset = string[2];// this has the filename
                    String currentHostId = resolver.get(InetAddress.getLocalHost().getHostName());
                    PrintWriter pw = writers.get(sockets.get("M"));
                    pw.println(string[0] +" "+ fileName + " " + offset + " " + currentHostId);
                    pw.flush();
                }else if (string[0].equals("append")) {
                    String fileName = string[1];
                    String dataSize = string[2];// this has the filename
                    String currentHostId = resolver.get(InetAddress.getLocalHost().getHostName());
                    PrintWriter pw = writers.get(sockets.get("M"));
                    pw.println(string[0] +" "+ fileName + " " + dataSize + " " + currentHostId);
                    pw.flush();
                }
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
            String ID = resolver.get(InetAddress.getLocalHost().getHostName());
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            System.out.println(InetAddress.getLocalHost().getHostName());
            System.out.println(ID);
            path = "./" + ID;
            ServerSocket SS = new ServerSocket(port);
            FileServerListener fileServerListener = new FileServerListener(SS);

            Scanner input = new Scanner(System.in);
            String[] string = input.nextLine().split(" ");
            while (!string[0].equals("exit")) {
                if(string[0].equals("connect")){
                    int serverIndex = Integer.parseInt(ID.split("S")[1]);
                    System.out.println(serverIndex);
                    List<String> nodeList = new ArrayList<>();
                    for (int i = serverIndex; i < 6; i++){
                        nodeList.add("S"+i);
                    }
                    nodeList.add("M");
//                    nodeList.add("C1");
//                    nodeList.add("C2");
                    for (String node : nodeList) {
                            System.out.println(node);
                            connectNode(IDToIPResolver.get(node));
                    }
                    HeartbeatSender heartbeat = new HeartbeatSender();
                }
                // string format : <command> <node id> <data>
                else {
                    // file server code
                    String out = string[0];
                    PrintWriter pw = writers.get(sockets.get(out));
                    pw.println(string[1]);
                    pw.flush();
                }
                string = input.nextLine().split(" ");
            }

            System.exit(0);
        } else {
            ServerSocket SS = new ServerSocket(port);
            Listener listener = new Listener(SS);
            for(int i=0;i<5;i++) {
                lastServerBeat.add(System.currentTimeMillis());
            }
            Scanner input = new Scanner(System.in);
            //TODO: code for meta server goes here
            // this code might not be necessary
            // string format : <command> <node id> <data>
            String[] string = input.nextLine().split(" ");
            while (!string[0].equals("exit")) {
                if(string[0].equals("connect")) {
                    List<String> nodeList = new ArrayList<>();
                    nodeList.add("C1");
                    nodeList.add("C2");
                    for (String node : nodeList) {
                        if (!node.equals(ID))
                            connectNode(IDToIPResolver.get(node));
                    }
                }
                else {
                    // meta server code
                    String out = string[0];
                    PrintWriter pw = writers.get(sockets.get(out));
                    pw.println(string[1]);
                    pw.flush();
                }
                string = input.nextLine().split(" ");
            }
            System.exit(0);
        }
    }

    public static void connectNode(String serverAddress) throws IOException {
        int serverPort = port;
        if (!sockets.containsKey(resolver.get(serverAddress))) {
            Socket s = new Socket(serverAddress, serverPort);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
            sockets.put(resolver.get(s.getInetAddress().getHostName()), s);
            readers.put(s, bf);
            writers.put(s, pw);
            if(resolver.get(serverAddress).equals('M') || resolver.get(serverAddress).equals("C1") || resolver.get(serverAddress).equals("C2")){
                MessageHandler MH = new MessageHandler(
                        readers.get(sockets.get(resolver.get(serverAddress))),
                        writers.get(sockets.get(resolver.get(serverAddress)))
                );
            }
//
//            else if (resolver.get(serverAddress).equals("C1") || resolver.get(serverAddress).equals("C2")) {
//                ClientRequestHandler clientRequestHandler = new ClientRequestHandler(
//                        readers.get(sockets.get(resolver.get(serverAddress))),
//                        writers.get(sockets.get(resolver.get(serverAddress)))
//                );
//
//            }
            else {
                FileServerWriter fileServerWriter = new FileServerWriter(
                        readers.get(sockets.get(resolver.get(serverAddress))),
                        writers.get(sockets.get(resolver.get(serverAddress)))
                );

            }
        }
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

    public static void initializeSockets() {
        port = 9021;
        resolver.put("dc01.utdallas.edu", "S1");
        resolver.put("dc02.utdallas.edu", "S2");
        resolver.put("dc03.utdallas.edu", "S3");
        resolver.put("dc09.utdallas.edu", "S4");
        resolver.put("dc05.utdallas.edu", "S5");

        resolver.put("dc06.utdallas.edu", "C1");
        resolver.put("dc07.utdallas.edu", "C2");

        resolver.put("dc08.utdallas.edu", "M");

        IDToIPResolver.put("S1", "dc01.utdallas.edu"); // server 1: DC01
        IDToIPResolver.put("S2", "dc02.utdallas.edu"); // server 2: DC02
        IDToIPResolver.put("S3", "dc03.utdallas.edu"); // server 3: DC03
        IDToIPResolver.put("S4", "dc09.utdallas.edu"); // server 4: DC09
        IDToIPResolver.put("S5", "dc05.utdallas.edu"); // server 5: DC05

        IDToIPResolver.put("C1", "dc06.utdallas.edu"); // client 1: DC06
        IDToIPResolver.put("C2", "dc07.utdallas.edu"); // client 2: DC07

        IDToIPResolver.put("M", "dc08.utdallas.edu"); // metadata server M: DC08
    }
}
