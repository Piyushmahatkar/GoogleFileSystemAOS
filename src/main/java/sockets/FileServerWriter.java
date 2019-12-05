package sockets;
import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import static sockets.Sockets.*;

class FileServerWriter extends Thread{

    BufferedReader br;
    PrintWriter pw;

    public FileServerWriter(BufferedReader br, PrintWriter pw){
        super();
        start();
        this.br = br;
        this.pw = pw;
    }

    public void run(){
        String message = null;
        try {
            while((message = br.readLine()) != null){
                ID = resolver.get(InetAddress.getLocalHost().getHostName());
                if(message.split(" ")[0].equals("exit"))
                    break;
                else if(message.split(" ")[0].equals("create") ) { // first chunk creation
                    System.out.println("File Creation Started");
                    String path = "./"
                            + "aos/project3"
                            + File.separator
                            + ID
                            + File.separator
                            + message.split(" ")[2];
                    File f = new File(path);
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    String data = manageSize(message.split(" ")[1]);
                    writeUsingFiles(data, path);
//                    Thread.sleep(1000);
                    System.out.println("File Creation Successful");
                    pw = writers.get(sockets.get("M"));
                    pw.println("success " + message.split(" ")[2] + " " + ID + " " + data.length()); // first chunk
                    pw.flush();
                }
                // write to file.txt on server
                else if(message.split(" ")[0].equals("read") ) { // first chunk creation
                    System.out.println("File reading Started");
                    String requestingClient =  message.split(" ")[3];
                    String path = "./"
                            + "aos/project3"
                            + File.separator
                            + ID
                            + File.separator
                            + message.split(" ")[1];
                    File file = new File(path);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    br.skip(Integer.parseInt(message.split(" ")[2])-1); // chars to skip
                    String data = br.readLine();
                    System.out.println("Read Data: " + data);
                    pw = writers.get(sockets.get(requestingClient));
                    pw.println("ReadData " + data);
                    pw.flush();
                }

            }

            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void writeUsingFiles(String data, String path) throws IOException {
        Path file = Paths.get(path);
        Files.write(file, Collections.singleton(data), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private static String manageSize(String data){
        StringBuilder stringBuilder = new StringBuilder(data);
        if(data.length()>1024)
            return data.substring(0, 1023);
        //        while(stringBuilder.length()<=1024) {
        //            stringBuilder.append(" ");
        //        }
        return stringBuilder.toString();
    }
}
