package sockets;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import static sockets.Sockets.*;

public class HeartbeatSender extends Thread {

    PrintWriter pw;
    Socket s;

    public HeartbeatSender(){
        super();
        start();
    }

    public void run(){

        while(true) {
            try {
                Thread.sleep(5 * 1000); // 5 seconds
                Date date= new Date();
                long time = date.getTime();
                Timestamp ts = new Timestamp(time);
                String path = "./"
                        + "aos/project3"
                        + File.separator
                        + resolver.get(InetAddress.getLocalHost().getHostName());
                File file = new File(path);

                pw = writers.get(sockets.get("M"));
                File[] arr = file.listFiles();

//                if(arr[index].isFile())
//                    System.out.println(arr[index].getName());
                LinkedList<ChunkDetails> listOfFiles =  new LinkedList<>();
                if(arr != null) {
                    for (File filepath : arr) {
                        Path p = Paths.get(filepath.getAbsolutePath());
                        BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
                        FileTime fileTime = view.creationTime();
                        Long fileSize = view.size();
                        listOfFiles.add(new ChunkDetails(filepath, fileTime.toMillis(), fileSize));
                    }
                }
                MetaDataHeartBeat metaDataHeartBeat = new MetaDataHeartBeat(
                        resolver.get(InetAddress.getLocalHost().getHostName()),
                        listOfFiles,
                        System.currentTimeMillis()
                );
                // send this object to Metadata
                Gson gson = new Gson();
                String jsonInString = gson.toJson(metaDataHeartBeat);
                pw.println("Heartbeat " + jsonInString);
                pw.flush();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
