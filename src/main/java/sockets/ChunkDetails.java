package sockets;

import java.io.File;
import java.sql.Timestamp;

public class ChunkDetails {
    File filename;
    Long createTimestamp;
    ChunkDetails(File file, Long ts){
        this.filename = file;
        this.createTimestamp = ts;
    }
}
