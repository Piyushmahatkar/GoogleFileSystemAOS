package sockets;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageHandler extends Thread{
	
	BufferedReader br;

	public MessageHandler(BufferedReader br){
		super();
		start();
		this.br = br;
	}
	
	public void run(){
		String message = null;
		try {
			while((message = br.readLine()) != null){
				if(message.split(" ")[0].equals("exit"))
					break;
				System.out.println(message);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
