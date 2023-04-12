package com.Replica1;

import com.Client.Client;
import com.FrontEnd.FrontEnd;

public class StartAll {
    public static void main(String[] args) throws Exception {
        AtwaterServer.main(args);
        VerdunServer.main(args);
        OutremontServer.main(args);
        // FrontEnd.main(args);
        // ReplicaManager.main(args);
        // Client.main(args);
    }
}
