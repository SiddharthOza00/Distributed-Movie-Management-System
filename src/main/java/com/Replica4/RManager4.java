package com.Replica4;

import com.Replica3.Impl.IBooking;
import com.Replica4.Interface.Interface;
import com.Replica4.Server.BookingImplementation;
import com.Replica4.Server.WebInterface;
import com.Request.RequestData;


import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RManager4 {

    ArrayList<Object> requestQueue = new ArrayList<>();
    ConcurrentHashMap<String, Object> allMessages = new ConcurrentHashMap<>();

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

                String serverReply = requestToReplica(dataReceived);
                System.out.println(serverReply);


//                String[] requestParams = dataReceived.split(",");
//                String methodName = requestParams[0];
//                String customerID = requestParams[1];
//                String movieID = requestParams[2];
//                String movieName = requestParams[3];
//                String newMovieID = requestParams[4];
//                String newMovieName = requestParams[5];
//                int numberOfTickets = Integer.parseInt(requestParams[6]);
//                String sequenceID = requestParams[7];

//                String serverName = null;
//                if(customerID.substring(0,3).equalsIgnoreCase("ATW")){
//                    serverName = "Atwater";
//                } else if (customerID.substring(0,3).equalsIgnoreCase("OUT")) {
//                    serverName = "Outremont";
//                } else if (customerID.substring(0,3).equalsIgnoreCase("VER")) {
//                    serverName = "Verdun";
//                }
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

                sendUnicast(serverReply, "192.168.247.36");

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUnicast(String reply, String ipAddress) {
        System.out.println("Trying Unicast - " + reply);
        int FEport = 44553;
        int RMport = 9958;
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

    private static void contactRM(RequestData request){
        int port = 0000; //change this
        String ipAddress = " "; //change this
        try {
            DatagramSocket ds = new DatagramSocket();
            byte[] arr = request.toString().getBytes();
            InetAddress addr = InetAddress.getByName(ipAddress);
            DatagramPacket dp = new DatagramPacket(arr, arr.length, addr, port);
            ds.send(dp);
            System.out.println("Sent to other RM - " + request.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String requestToReplica(String dataReceived) throws MalformedURLException{

        URL url;
        QName qName;

        String[] requestParams = dataReceived.split(",");
        String methodName = requestParams[0];
        String customerID = requestParams[1];
        String movieID = requestParams[2];
        String movieName = requestParams[3];
        String newMovieID = requestParams[4];
        String newMovieName = requestParams[5];
        int numberOfTickets = Integer.parseInt(requestParams[6]);
        String sequenceID = requestParams[7];


        if( customerID.substring(0,3).equals("ATW")) {
            url = new URL("http://localhost:7300/ATW?wsdl");
            qName = new QName("http://Server.Replica4.com/","BookingImplementationService");
        }

        else if(customerID.substring(0,3).equals("VER")) {
            url = new URL("http://localhost:7400/VER?wsdl");
            qName = new QName("http://Server.Replica4.com/","BookingImplementationService");
        }

        else if(customerID.substring(0,3).equals("OUT")) {
            url = new URL("http://localhost:7500/OUT?wsdl");
            qName = new QName("http://Server.Replica4.com/","BookingImplementationService");
        }
        else {
            return "No response";
        }

        Service service = Service.create(url, qName);

        WebInterface impl = service.getPort(WebInterface.class);

//        String customerID = myObj.getCustomerID();
//        String serverID = customerID.substring(0,3);
//        String serverName;

        String typeOfUser = customerID.substring(3,4);

        switch(typeOfUser) {
            case "A":
                if(methodName.equalsIgnoreCase("addMovieSlots")) {
                    String serverReply = impl.addMovieSlots(movieID, movieName, numberOfTickets);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("removeMovieSlots")) {
                    String serverReply = impl.removeMovieSlots(movieID, movieName);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("listMovieShowsAvailability")) {
                    String serverReply = impl.listMovieShowsAvailability(movieName);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMoviesTickets(customerID,movieID, movieName, numberOfTickets);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(customerID,movieID, movieName, numberOfTickets);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(customerID);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName, numberOfTickets);
                    return serverReply;
                }
                break;
            case "C":
                if(methodName.equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMoviesTickets(customerID,movieID, movieName, numberOfTickets);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(customerID,movieID, movieName, numberOfTickets);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(customerID);
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName, numberOfTickets);
                    return serverReply;
                }
                break;
        }


        return "No response";

    }

}