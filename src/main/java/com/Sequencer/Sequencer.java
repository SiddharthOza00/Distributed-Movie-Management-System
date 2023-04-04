package com.Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sequencer {
    private static int sequencerId = 0;
    private static final String sequencerIP = "192.168.238.53";

    public static void main(String[] args) {
        try (DatagramSocket aSocket = new DatagramSocket(2233, InetAddress.getByName(sequencerIP))) {
            byte[] buffer = new byte[1000];
            System.out.println("Sequencer UDP Server Started");
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer,
                        buffer.length);

//                aSocket.receive(request);

                String sentence = new String(request.getData(), 0,
                        request.getLength());

                //new comment
//                String[] parts = sentence.split(";");
//                int sequencerId1 = Integer.parseInt(parts[0]);
//                String ip = request.getAddress().getHostAddress();
//
//                String sentence1 = ip + ";" +
//                        parts[2] + ";" +
//                        parts[3] + ";" +
//                        parts[4] + ";" +
//                        parts[5] + ";" +
//                        parts[6] + ";" +
//                        parts[7] + ";" +
//                        parts[8] + ";" +
//                        parts[9] + ";";

                System.out.println(sentence);
                sendMessage(sentence, 0, true);

//                byte[] SeqId = (Integer.toString(sequencerId)).getBytes();
//                InetAddress aHost1 = request.getAddress();
//                int port1 = request.getPort();
//
//                System.out.println(aHost1 + ":" + port1);
//                DatagramPacket request1 = new DatagramPacket(SeqId,
//                        SeqId.length, aHost1, port1);
//                aSocket.send(request1);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    public static void sendMessage(String message, int sequencerId1, boolean isRequest) {
        int port = 5555;

        if (sequencerId1 == 0 && isRequest) {
            sequencerId1 = ++sequencerId;
        }
        String finalMessage = "this is a test";
//        String finalMessage = sequencerId1 + ";" + message;
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
}
