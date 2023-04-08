package com.Replica4;

import com.Replica4.Interface.Interface;
import com.Replica4.Server.BookingImplementation;
import com.Replica4.Server.WebInterface;

import java.net.*;

public class RManager4 {




    public static void main(String[] args) throws Exception {
        new Thread( () -> {
            try {
                receiveMulticast();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void receiveMulticast() throws UnknownHostException {
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
                int numberOfTickets = Integer.parseInt(requestParams[6]);
                String sequenceID = requestParams[7];

                String serverName = null;
                if(customerID.substring(0,3).equalsIgnoreCase("ATW")){
                    serverName = "Atwater";
                } else if (customerID.substring(0,3).equalsIgnoreCase("OUT")) {
                    serverName = "Outremont";
                } else if (customerID.substring(0,3).equalsIgnoreCase("VER")) {
                    serverName = "Verdun";
                }
//
//                WebInterface webInterface = new BookingImplementation(customerID.substring(0,2), serverName);
//
//                switch (methodName) {
//                    case "addMovieSlots":
//                        webInterface.addMovieSlots(movieID, movieName, numberOfTickets);
//                        break;
//
//                    case "removeMovieSlots":
//                        webInterface.removeMovieSlots(movieID, movieName);
//                        break;
//
//                    case "listMovieShowsAvailability":
//                        webInterface.listMovieShowsAvailability(movieName);
//                        break;
//
//                    case "getBookingSchedule":
//                        webInterface.getBookingSchedule(customerID);
//                        break;
//
//                    case "bookMovieTickets":
//                        webInterface.bookMoviesTickets(customerID, movieID, movieName, numberOfTickets);
//                        break;
//
//                    case "cancelMovieTickets":
//                        webInterface.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
//                        break;
//
//                    case "exchangeTickets":
//                        webInterface.exchangeTickets(customerID, newMovieID, newMovieName, movieID, movieName, numberOfTickets);
//                        break;
//                }

                sendUnicast(dataReceived, "192.168.247.35");

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUnicast(String reply, String ipAddress) {
        System.out.println("Trying Unicast - " + reply);
        int FEport = 44553;
        int RMport = 9955;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(RMport);
            byte[] arr = reply.getBytes();
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
