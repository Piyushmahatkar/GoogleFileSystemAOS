package sockets;

import java.io.File;
import java.sql.Timestamp;

public class ChunkDetails {
    File filename;
    Long createTimestamp;
    Long fileSize;
    ChunkDetails(File file, Long ts, Long filesize){
        this.filename = file;
        this.createTimestamp = ts;
        this.fileSize = filesize;
    }
}
