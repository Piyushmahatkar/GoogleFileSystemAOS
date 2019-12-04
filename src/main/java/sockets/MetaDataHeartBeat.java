package sockets;

import java.sql.Timestamp;
import java.util.ArrayList;

public class MetaDataHeartBeat {
    String serverName;
    ArrayList<ChunkDetails> listOfChunks;
    Long timestamp;
    MetaDataHeartBeat(String server, ArrayList<ChunkDetails> list, Long ts) {
        this.timestamp = ts;
        this.serverName = server;
        this.listOfChunks = list;
    }
}
