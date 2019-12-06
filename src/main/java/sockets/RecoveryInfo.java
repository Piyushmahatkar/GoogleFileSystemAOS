package sockets;

import java.util.LinkedList;

public class RecoveryInfo {
    String chunkName;
    String recoveringServer;
    LinkedList <String> recoveringSources;
    RecoveryInfo(String chunkName,String recoveringServer, LinkedList <String> recoveringSources){
        this.chunkName = chunkName;
        this.recoveringServer = recoveringServer;
        this.recoveringSources = recoveringSources;
    }
    RecoveryInfo(){
        recoveringSources = new LinkedList<>();
    }
}
