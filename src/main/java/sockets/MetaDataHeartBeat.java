package sockets;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;

public class MetaDataHeartBeat {
    String serverName;
    LinkedList<ChunkDetails> listOfChunks;
    Long timestamp;
    MetaDataHeartBeat(String server, LinkedList<ChunkDetails> list, Long ts) {
        this.timestamp = ts;
        this.serverName = server;
        this.listOfChunks = list;
    }
}
