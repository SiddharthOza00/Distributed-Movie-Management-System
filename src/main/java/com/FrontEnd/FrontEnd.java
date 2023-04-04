package com.FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.Request.RequestData;

public class FrontEnd {
    public static final String sequencerIP = "192.168.137.89";
    public static final int sequencerPort = 2233;

    public static void main(String[] args) {

    }

    public void sendRequestToSequencer(RequestData requestData) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(2234);
            // String dataFromClient = "Hello from FE";
            String dataFromClient = requestData.toString();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName(sequencerIP);
            DatagramPacket requestToSequencer = new DatagramPacket(message, dataFromClient.length(), aHost,
                    sequencerPort);

            aSocket.send(requestToSequencer);

            // aSocket.setSoTimeout(1000);
            // // Set up an UPD packet for recieving
            // byte[] buffer = new byte[1000];
            // DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            // // Try to receive the response from the ping
            // aSocket.receive(response);
            // String sentence = new String(response.getData(), 0,
            //         response.getLength());
            // System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>" + sentence);
            // int sequenceID = Integer.parseInt(sentence.trim());
            // System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>SequenceID:"
            // + sequenceID);
        } catch (SocketException e) {
            // System.out.println("Failed: " + requestFromClient.noRequestSendError());
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            // System.out.println("Failed: " + requestFromClient.noRequestSendError());
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}