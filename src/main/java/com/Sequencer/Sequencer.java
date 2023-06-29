package com.Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sequencer {
    private static int sequencerId = 0;
    private static final String sequencerIP = "192.168.48.53";

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

        String finalMessage = message + "," + sequencerId1;
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
}