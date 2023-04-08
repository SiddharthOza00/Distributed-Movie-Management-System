package com.Replica1;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.Request.ResponseData;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReplicaManager {

    ArrayList<Object> requestQueue = new ArrayList<>();
    ConcurrentHashMap<String, Object> allMessages = new ConcurrentHashMap<>();
    
    public static void main(String[] args) throws Exception {
        new Thread( () -> {
            try {
                receive();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        //Start Atwater Server
        // AtwaterServer.main(args);
        // VerdunServer.main(args);
        // OutremontServer.main(args);
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

                String[] requestParams = dataReceived.split(",");
                String methodName = requestParams[0];
                String customerID = requestParams[1];
                String movieID = requestParams[2];
                String movieName = requestParams[3];
                String newMovieID = requestParams[4];
                String newMovieName = requestParams[5];
                String numberOfTickets = requestParams[6];
                String sequenceID = requestParams[7];

                
                ResponseData response = new ResponseData();

                // switch(methodName){
                //     case "addMovieSlots":
                        
                //     break;
                //     case "removeMovieSlots":
                //     break;
                //     case "listMovieShowsAvailability":
                //     break;
                //     case "bookMovieTickets":
                //     break;
                //     case "getBookingSchedule":
                //     break;
                //     case "cancelMovieTickets":
                //     break;
                //     case "exchangeTickets":
                //     break;
                // }
                
                //dataREcieved mani - func, params
                //call replica-> impl
                //result

                sendToFrontend(dataReceived, "192.168.247.35");

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendToFrontend(String dataReceived, String ipAddress) {
        System.out.println("Trying Unicast - " + dataReceived);
        int FEport = 44553;
        int RMport = 9955;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(RMport);
            byte[] arr = dataReceived.getBytes();
            InetAddress address = InetAddress.getByName(ipAddress);

            DatagramPacket dp = new DatagramPacket(arr, arr.length, address, FEport);
            ds.send(dp);
            System.out.println("Unicast sent to FE (IP:" + ipAddress + ")");
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(ds!=null) {
            ds.close();
        }
    }

    
}