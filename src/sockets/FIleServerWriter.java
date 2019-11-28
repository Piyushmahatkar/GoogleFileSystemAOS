package sockets;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

class FileServerWriter extends Thread{

    BufferedReader br;

    public FileServerWriter(BufferedReader br){
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
                // write to file.txt on server
                System.out.println("Writing into file.txt : "+message);
                writeUsingFiles(message);
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void writeUsingFiles(String data) throws IOException {
        Path file = Paths.get("file.txt");
        Files.write(file, Collections.singleton(data), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }
}
