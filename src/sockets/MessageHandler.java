package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static sockets.Sockets.writers;
import static sockets.Sockets.sockets;

public class MessageHandler extends Thread{
	
	BufferedReader br;
	PrintWriter pw;


	public MessageHandler(BufferedReader br, PrintWriter pr){
		super();
		start();
		this.br = br;
		this.pw = pr;
	}
	class ServerList {
		List<Integer> servers;
		ServerList(){
			servers = new ArrayList<>();
		}
		public void addServer(int server) {servers.add(server);}
	}
	public void run(){
		String message = null;
		Map<String, ArrayList<String>> successfulCreations = new HashMap<>();
		Map<String, String> chunkLocator = new HashMap<>(); // filename, latest_chunk
		Map<String, ArrayList<String>> serverLocator = new HashMap<>(); // chunkname , list of servers

		// chunk, <filename, list<servers>, version,  >
		try {
			while((message = br.readLine()) != null){//message = "<create> <message> <filename>"
				if(message.split(" ")[0].equals("exit"))
					break;
				else if(message.split(" ")[0].equals("create")) { //intended for meta server
					// code for duplicate filenames
					ArrayList<Integer> serversList = generate3Random();
					System.out.println(serversList);
					for(int i = 0; i < 3 ; i++) {
						PrintWriter pw = writers.get(sockets.get("S"+serversList.get(i)));
						pw.println(message+"_1"); // first chunk = create <message> <filename_1>
						pw.flush();
					}
				}
				else if (message.split(" ")[0].equals("success")) { // intended for meta server from fileserver
					// : success " + message.split(" ")[1] + " " + ID +"" + data.length)
					if (successfulCreations.containsKey(message.split(" ")[1])) {
						ArrayList<String> tempServers = successfulCreations.get(message.split(" ")[1]);
						tempServers.add(message.split(" ")[2]);
						successfulCreations.put(
								message.split(" ")[1], tempServers);
						if(successfulCreations.get(message.split(" ")[1]).size()==3) { // all 3 servers requests were received
							chunkLocator.put(message.split(" ")[1].split("_")[0], message.split(" ")[1]);
							serverLocator.put(message.split(" ")[2], successfulCreations.get(message.split(" ")[1]));
							System.out.println("3 ack received for chunk : " + message.split(" ")[1]);
							// TODO: send success to client
						}
					}
					else {
						ArrayList<String> tempServers = new ArrayList<>();
						tempServers.add(message.split(" ")[2]);
						successfulCreations.put(message.split(" ")[1], tempServers);
					}

				}
			}
			// 1. create new file request (this is the request received by client to metadata server)
			// 2. create new chunk request (this is the request received from metadataserver to fileserver)
			// 3. read a chunk (request from client to fileserver)
			// 4. locate the chunk (function in metadata server)
			// 5. send heartbeat (function in file server )
			// 6. receive heartbeat (from fileserver to meta server)
			// 7. append Data to a file (request from client to meta)
			// 8. append data to chunk (request from client to fileserver)
			System.out.println(message);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static ArrayList<Integer> generate3Random() {
		Set<Integer> unique = new HashSet<>();
		ArrayList<Integer> list = new ArrayList<Integer>(3);
		while(unique.size() < 3) {
			unique.add((int)(5.0 * Math.random())+1);
		}
		for(Integer i: unique) {
			list.add(i);
		}
		return list;
	}
}
