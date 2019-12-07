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
                    String requestingClient = message.split(" ")[1];
                    String path = "./"
                            + "aos/project3"
                            + File.separator
                            + ID
                            + File.separator
                            + message.split(" ")[2];
                    File f = new File(path);
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    createUpdateVersionFile(path, false);
                    System.out.println("File Creation Successful");
                    pw = writers.get(sockets.get("M"));
                    pw.println("success " + message.split(" ")[2] + " " + ID + " " + requestingClient); // first chunk
                    pw.flush();
                }
                else if(message.split(" ")[0].equals("read") ) { // reading of chunk
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
                    pw.println("ReadSuccess " + data);
                    pw.flush();

                } else if(message.split(" ")[0].equals("SendRecoveryDataToServer")){
                    // SendRecoveryDataToServer "+chunkName+" "+ recoveringServer
                    // read current file from this server and send the content to other fileServer
                    String path = "./"
                            + "aos/project3"
                            + File.separator
                            + resolver.get(InetAddress.getLocalHost().getHostName()) //ID
                            + File.separator
                            + message.split(" ")[1];
                    File file = new File(path);
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String data = bufferedReader.readLine();
                    PrintWriter pr = writers.get(sockets.get(message.split(" ")[2]));
                    pr.println("recoveringFile "+message.split(" ")[1]+" "+data);
                    pr.flush();

                } else if(message.split(" ")[0].equals("recoveringFile")) {
                    String path = "./"
                            + "aos/project3"
                            + File.separator
                            + resolver.get(InetAddress.getLocalHost().getHostName())//ID
                            + File.separator
                            + message.split(" ")[1];
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        file.delete();
                    }
                    file.createNewFile();
                    writeUsingFiles(message.split(" ")[2], path);
                }
                //todo: Implement 2PHASE COMMIT PROTOCOL
                else if(message.split(" ")[0].equals("append.") ) { // appending of chunk
                    System.out.println("Append Request Recieved");
                    int dataSize = Integer.parseInt(message.split(" ")[2]);
                    String requestingClient =  message.split(" ")[3];
                    String path = "./"
                            + "aos/project3"
                            + File.separator
                            + ID
                            + File.separator
                            + message.split(" ")[1];
                    FileServerBuffer tempBuffer = new FileServerBuffer();
                    tempBuffer.chunkPath = path;
                    tempBuffer.dataSize = dataSize;
                    tempBuffer.requestingClient = requestingClient;
                    tempBuffer.serverId = ID;
                    appendBuffer.add(tempBuffer);
                    pw = writers.get(sockets.get(requestingClient));
                    pw.println("AppendAck");
                    pw.flush();
                }
                else if (message.split(" ")[0].equals("append")){
                    System.out.println("Append Request Recieved");
                    int dataSize = Integer.parseInt(message.split(" ")[2]);
                    String requestingClient =  message.split(" ")[3];
                    String path = "./"
                            + "aos/project3"
                            + File.separator
                            + ID
                            + File.separator
                            + message.split(" ")[1];
                    System.out.println("Commit Request Recieved");
                    System.out.println("File Appending Started");
//                    int index = Integer.parseInt(message.split(" ")[1]);
//                    int dataSize = appendBuffer.get(index).dataSize;
//                    String requestingClient =  appendBuffer.get(index).requestingClient;
//                    String path = appendBuffer.get(index).chunkPath;
                    File file = new File(path);
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
                    writer.write(generateDataBySize(dataSize));
                    writer.close();
                    createUpdateVersionFile(path, true);
                    System.out.println("Append Successful");
                    pw = writers.get(sockets.get(requestingClient));
                    pw.println("AppendSuccess : " + ID);
                    pw.flush();
                }
                else if (message.split(" ")[0].equals("ReadSuccess")) {
                    System.out.println(message);
                }
                else if (message.split(" ")[0].equals("CreateSuccess")) {
                    System.out.println(message);
                }
                else if (message.split(" ")[0].equals("AppendAck")) {
                    System.out.println(message);
                    if(appendBuffer.size()==3){//will get the available server for the chunk
                        for(int i = 0; i < 3 ; i++) {
                            pw = writers.get(sockets.get(appendBuffer.get(i).serverId));
                            pw.println("commit" + i); //
                            pw.flush();
                            System.out.println("sending commit request to "+appendBuffer.get(i).serverId);
                        }
                    }
                }
                else if (message.split(" ")[0].equals("AppendSuccess")) {
                    System.out.println(message);
                }
                else {
                    System.out.println(message);
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

    public static String generateDataBySize (Integer dataSize){
        String SampleString = "abcdefghijklmnopqrstuvwxyz";
        int length = dataSize;
        String generatedString = "";
        while (generatedString.length()<length) {
            generatedString += SampleString;
            if(generatedString.length()>length){
                generatedString = generatedString.substring(0, length);
            }
        }
        return generatedString;
    }

    public static void createUpdateVersionFile (String filePath, Boolean isAppend) throws IOException {
        String versionFilePath = filePath+"_v";
        File file = new File(versionFilePath);
        int version = 0;
        if(isAppend) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st = br.readLine();
            br.close();
            int currentVersion = st != null? Integer.parseInt(st.split(":")[1]): 0;
//            int currentVersion =  Integer.parseInt(st.split(":")[1]);
            version = currentVersion + 1;
            System.out.println("version was : " + currentVersion);
        }
        System.out.println("version updated to :" + version);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.createNewFile();
        writeUsingFiles("Version:"+version, versionFilePath);
    }
}
