package com.Replica1;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class ReplicaManager {
    
    public static void main(String[] args) throws Exception {
        new Thread( () -> {
            try {
                receive();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        //Start Atwater Server
        AtwaterServer.main(args);
        VerdunServer.main(args);
        OutremontServer.main(args);
        // System.out.println("Atawater Server started!!");
    }

    private static void receive() throws UnknownHostException {
        MulticastSocket socket = null;

        InetAddress group = InetAddress.getByName("228.5.6.7");

        byte[] buf = new byte[1000];

        try {

            socket = new MulticastSocket(5555);

            socket.joinGroup(group);

            while(true) {
                System.out.println("Test");
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                socket.receive(recv);

                String dataReceived = new String(recv.getData(), 0, recv.getLength());

                System.out.println("Received : " + dataReceived);

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
}