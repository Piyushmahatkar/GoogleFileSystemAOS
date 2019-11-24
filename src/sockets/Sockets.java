package sockets;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Sockets {
	static Map<String, String> resolver = new HashMap<>();
	static Map<String, Socket> sockets = new HashMap<>();
	static Map<Socket, BufferedReader> readers = new HashMap<Socket, BufferedReader>();
	static Map<Socket, PrintWriter> writers = new HashMap<Socket, PrintWriter>();
	
	static String[] messageType = {"request", "reply", "read", "write", "enquiry"};
	static Map<String, Integer> SID = new HashMap<>();
	static Map<String, Integer> CID = new HashMap<>();
	private static Map<String, String> IDToIPResolver = new HashMap<>();
	static int ID;
	static String path;
	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		
		resolver.put("10.176.69.32", "S1");
		resolver.put("10.176.69.33", "S2");
		resolver.put("10.176.69.34", "S3");
		resolver.put("10.176.69.35", "S4");
		resolver.put("10.176.69.36", "S5");

		resolver.put("10.176.69.37", "C1");
		resolver.put("10.176.69.38", "C2");
		resolver.put("10.176.69.39", "M");

		IDToIPResolver.put("S1","10.176.69.32"); // server 1
		IDToIPResolver.put("S2","10.176.69.33"); // server 2
		IDToIPResolver.put("S3","10.176.69.34"); // server 3
		IDToIPResolver.put("S4","10.176.69.35"); // server 4
		IDToIPResolver.put("S5","10.176.69.36"); // server 5

		IDToIPResolver.put("C1","10.176.69.37"); // client 1
		IDToIPResolver.put("C2","10.176.69.38"); // client 2

		IDToIPResolver.put("M","10.176.69.39"); // metadata server M0


		SID.put("10.176.69.32", 1);
		SID.put("10.176.69.33", 2);
		SID.put("10.176.69.34", 3);
		SID.put("10.176.69.35", 4);
		SID.put("10.176.69.36", 5);

		CID.put("10.176.69.37", 1);
		CID.put("10.176.69.38", 2);
		
		if(args[0].equals("client")){
			
			ID = CID.get(InetAddress.getLocalHost().getHostAddress());
			ServerSocket SS = new ServerSocket(Integer.parseInt(args[1]));
			sockets.Listener listen = new sockets.Listener(SS);
			
			Scanner input = new Scanner(System.in);
			String[] string = input.nextLine().split(" ");
			while(!string[0].equals("exit")){
				
				if(string[0].equals("connect")){
					String serverAddress = string[1];
					int serverPort = Integer.parseInt(string[2]);
					Socket s = new Socket(serverAddress, serverPort);
					PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
					BufferedReader bf = new BufferedReader( new InputStreamReader(s.getInputStream()));
					sockets.put(resolver.get(s.getInetAddress().getHostAddress()),s);
					readers.put(s, bf);
					writers.put(s, pw);
					MessageHandler MH = new MessageHandler(bf);
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
					
		} else {
			ID = SID.get(InetAddress.getLocalHost().getHostAddress());
			
			path = "./"+ID;

			ServerSocket SS = new ServerSocket(Integer.parseInt(args[1]));
			sockets.Listener listen = new sockets.Listener(SS);
			
			Scanner input = new Scanner(System.in);
			String[] string = input.nextLine().split(" ");
			while(!string[0].equals("exit")){
				String out = string[0];
				PrintWriter pw = writers.get(sockets.get(out));
				pw.println(string[1]);
				pw.flush();
						
				string = input.nextLine().split(" ");

			}
			System.exit(0);
		}
	}

}
