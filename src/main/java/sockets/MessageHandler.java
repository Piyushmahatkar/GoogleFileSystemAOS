package sockets;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.*;

import static sockets.Sockets.*;

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

		// chunk, <filename, list<servers>, version,  >
		try {
			while((message = br.readLine()) != null){//message = "<create> <message> <filename>"
				if (message.split(" ")[0].equals("exit"))
					break;
				else if(message.split(" ")[0].equals("create")) { //intended for meta server
					// code for duplicate filenames
					System.out.println(message);
					ArrayList<Integer> serversList = generate3Random();
					System.out.println(serversList);
					for(int i = 0; i < 3 ; i++) {
						pw = writers.get(sockets.get("S"+serversList.get(i)));
						System.out.println(sockets.get("S"+serversList.get(i)));
						pw.println(message+"_1"); // first chunk = create <message> <filename_1>
						pw.flush();
						System.out.println("sending create request to S"+serversList.get(i));
					}
				}
				else if (message.split(" ")[0].equals("Heartbeat")) {
                    System.out.println("Recieved Heartbeat Message :" + message);
                    Gson gson = new Gson();
					MetaDataHeartBeat metaDataHeartBeat = gson.fromJson( message.split(" ")[1], MetaDataHeartBeat.class);
					System.out.println("metadataobject is : " + metaDataHeartBeat);
					lastServerBeat.set(Character.getNumericValue(metaDataHeartBeat.serverName.charAt(1)-1), metaDataHeartBeat.timestamp);
					updateServerStatuses();
                }
				else if (message.split(" ")[0].equals("success")) { // intended for meta server from fileserver
					// : success " + message.split(" ")[1] + " " + ID +"" + data.length)
					System.out.println("Recieved Success Message :" + message);
					if (successfulCreations.containsKey(message.split(" ")[1])) {
						ArrayList<String> tempServers = successfulCreations.get(message.split(" ")[1]);
						tempServers.add(message.split(" ")[2]);
                        successfulCreations.put(message.split(" ")[1], tempServers);

						if (successfulCreations.get(message.split(" ")[1]).size() == 3) { // all 3 servers requests were received
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
				else if (message.split(" ")[0].equals("read")) {
					System.out.println(message);
					String fileName = message.split(" ")[1];
					String offset = message.split(" ")[2];// this has the offset
					String requestingClient = message.split(" ")[3];
					int chunkId = Integer.parseInt(offset)/4096 + 1;
					int chunkOffset = Integer.parseInt(offset)%4096;
					String chunkName = fileName+ "_" + chunkId;
					System.out.println("ChunkName "+ chunkName +" Server Selected: " + successfulCreations.get(chunkName));
					if(successfulCreations.containsKey(chunkName)){
						ArrayList<String> chunkServers = successfulCreations.get(chunkName);
						//todo :validate server available or not
						int index = new Random().nextInt(chunkServers.size());
						String selectedServer = chunkServers.get(index);
						pw = writers.get(sockets.get(requestingClient));
						pw.println("canRead " + chunkName+ " " + chunkOffset + " " + selectedServer);
						pw.flush();
						System.out.println("canRead " + chunkName+ " " + chunkOffset + " " + selectedServer);
					}
				}
				else if (message.split(" ")[0].equals("append")) {
					System.out.println(message);
					String fileName = message.split(" ")[1];
					String dataSize = message.split(" ")[2];// this has the offset
					String requestingClient = message.split(" ")[3];
					//todo: verify if last chunk can accomodate the data or not
					//if yes
					String latestChunkName = chunkLocator.get(fileName);//latest chunk
					if(successfulCreations.containsKey(latestChunkName)){
						ArrayList<String> chunkServers = successfulCreations.get(latestChunkName);
						//todo :validate server available or not
						pw = writers.get(sockets.get(requestingClient));
						pw.println("canAppend "
								+ latestChunkName + " " +
								dataSize + " "
								+ chunkServers.get(0) + " "
								+ chunkServers.get(1) + " "
								+ chunkServers.get(2));
						pw.flush();
						System.out.println("canAppend " + latestChunkName+ " " + dataSize + " " + chunkServers);
					// else
					//append null to the old chunk
					// create new chunk name and select new server list
					}
				}

				// client incoming request
				else if (message.split(" ")[0].equals("canRead")) {
					System.out.println("Recieved server details for read");
					String chunkName = message.split(" ")[1];
					String offset = message.split(" ")[2];
					String selectedServer = message.split(" ")[3];
					String currentHostId = resolver.get(InetAddress.getLocalHost().getHostName());
					pw = writers.get(sockets.get(selectedServer));
					pw.println("read "+ chunkName + " " + offset + " " + currentHostId);
					pw.flush();
					System.out.println("sending read request to "+selectedServer);
				}
				else if (message.split(" ")[0].equals("canAppend")) {
					System.out.println("Recieved server list for append");
					String latestChunkName = message.split(" ")[1];
					String dataSize = message.split(" ")[2];
					ArrayList<String> serversList = new ArrayList<String>();
					serversList.add(message.split(" ")[3]);
					serversList.add(message.split(" ")[4]);
					serversList.add(message.split(" ")[5]);
					// todo: implement 2 commit protocol
					//send commit request
					for(int i = 0; i < 3 ; i++) {
						System.out.println("sending read request to "+serversList.get(i));
						pw = writers.get(sockets.get(serversList.get(i)));
						System.out.println(sockets.get(serversList.get(i)));
						pw.println("commit"); //
						pw.flush();
						System.out.println("sending create request to "+serversList.get(i));
					}
					for(int i = 0; i < 3 ; i++) {
						System.out.println("sending read request to "+serversList.get(i));
						pw = writers.get(sockets.get(serversList.get(i)));
						System.out.println(sockets.get(serversList.get(i)));
						pw.println("commit"); //
						pw.flush();
						System.out.println("sending create request to "+serversList.get(i));
					}
//					String currentHostId = resolver.get(InetAddress.getLocalHost().getHostName());
//					pw = writers.get(sockets.get(selectedServer));
//					pw.println("read "+ chunkName + " " + offset + " " + currentHostId);
//					pw.flush();
//					System.out.println("sending read request to "+selectedServer);
				}
				else if (message.split(" ")[0].equals("ReadSuccess")) {
					System.out.println(message);
				}
				else if (message.split(" ")[0].equals("CreateSuccess")) {
					System.out.println(message);
				}
				System.out.println(message);
			}
			// 1. create new file request (this is the request received by client to metadata server)
			// 2. create new chunk request (this is the request received from metadataserver to fileserver)
			// 3. read a chunk (request from client to fileserver)
			// 4. locate the chunk (function in metadata server)
			// 5. send heartbeat (function in file server )
			// 6. receive heartbeat (from fileserver to meta server)
			// 7. append Data to a file (request from client to meta)
			// 8. append data to chunk (request from client to fileserver)
//			System.out.println(message);
		}
		catch (IOException  ex) {
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
	public static void updateServerStatuses(){
		for (int i=0;i<lastServerBeat.size();i++) {
			if(System.currentTimeMillis() - lastServerBeat.get(i) > 15000 && !downServers.contains(i)) {
				downServers.add(i);
			}
			else if(System.currentTimeMillis() - lastServerBeat.get(i) < 15000 && downServers.contains(i)) {
				downServers.remove(new Integer(i));
			}
		}
		System.out.println("DownServers: " + downServers);
	}
}
