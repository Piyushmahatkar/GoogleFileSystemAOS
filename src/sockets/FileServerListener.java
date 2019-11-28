package sockets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServerListener extends Thread{

    ServerSocket SS;

    public FileServerListener(ServerSocket SS){
        super();
        start();
        this.SS = SS;
    }

    public void run(){
        while(true){
            try {
                Socket s = SS.accept();
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                BufferedReader bf = new BufferedReader( new InputStreamReader(s.getInputStream()));
                Sockets.sockets.put(Sockets.resolver.get(s.getInetAddress().getHostAddress()), s);
                Sockets.readers.put(s, bf);
                Sockets.writers.put(s, pw);
                System.out.println("someone connected to this FileServer!");
                FileServerWriter fileServerWriter = new FileServerWriter(bf);
            } catch (IOException e) {
                // 	TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
