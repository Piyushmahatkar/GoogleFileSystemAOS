package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MessageHandler extends Thread{
	
	BufferedReader br;
	PrintWriter pw;

	public MessageHandler(BufferedReader br, PrintWriter pr){
		super();
		start();
		this.br = br;
		this.pw = pr;
	}
	
	public void run(){
		String message = null;
		try {
			while((message = br.readLine()) != null){
				if(message.split(" ")[0].equals("exit"))
					break;
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
