package com.Sequencer;

import com.Replica2.ReplicaManager2;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Sequencer {
    private static int sequencerId = 0;
    private static final String sequencerIP = "192.168.48.53";

//    private static ArrayList<String> allOrderedRequests;

    public static void main(String[] args) throws IOException {
        try (DatagramSocket aSocket = new DatagramSocket(2233, InetAddress.getByName(sequencerIP))) {
            byte[] buffer = new byte[1000];
            System.out.println("Sequencer UDP Server Started");
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer,
                        buffer.length);

                aSocket.receive(request);
                String sentence = new String(request.getData(), 0, request.getLength());
                sendMessage(sentence, 0);
            }
        }
    }

    public static void sendMessage(String message, int sequencerId1) {
        int port = 5555;

        if (sequencerId1 == 0) {
            sequencerId1 = ++sequencerId;
        }
//        String finalMessage = "this is a test";
        String finalMessage = message + "," + sequencerId1;
//        allOrderedRequests.add(finalMessage);
//          String finalMessage = message;
        System.out.println("Message: " + finalMessage);
        try (DatagramSocket aSocket = new DatagramSocket()) {

            byte[] messages = finalMessage.getBytes();
            InetAddress aHost = InetAddress.getByName("228.5.6.7");

            DatagramPacket request = new DatagramPacket(messages,
                    messages.length, aHost, port);
            aSocket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private static void allRequestsTillNow(int lastExecutedSeqNum) throws MalformedURLException {
//        System.out.println("Executing all requests again");
//
//        for (String allOrderedRequest : allOrderedRequests) {
//            String temp = ReplicaManager2.requestToReplica(allOrderedRequest);
//            System.out.println(temp);
//            String[] allParams = allOrderedRequest.split(",");
//            lastExecutedSeqNum = Integer.parseInt(allParams[7]);
//        }
//        System.out.println("allRequestsTillNow() - done : lastExecutedSeqNum = " + lastExecutedSeqNum);
//    }
}