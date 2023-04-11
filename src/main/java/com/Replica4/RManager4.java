package com.Replica4;

import com.Replica3.Impl.IBooking;
import com.Replica3.LaunchServer;
import com.Replica4.Interface.Interface;
import com.Replica4.Server.BookingImplementation;
import com.Replica4.Server.WebInterface;
import com.Request.RequestData;
import com.Request.ResponseData;
import com.Request.Config;


import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RManager4 {

    ArrayList<Object> requestQueue = new ArrayList<>();
    ConcurrentHashMap<String, Object> allMessages = new ConcurrentHashMap<>();

    private static Map<Integer, String> allRequests;
    private static ArrayList<String> allOrderedRequests;
    private static int lastExecutedSeqNum;

    public static void main(String[] args) throws Exception {
        allRequests = new ConcurrentHashMap<>();
        allOrderedRequests = new ArrayList<>();
        lastExecutedSeqNum = 0;
        new Thread( () -> {
            try {
                receiveMulticast();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread( () -> {
            try {
                receiveFromFE();
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

                int sequenceID = Integer.parseInt(requestParams[7]);

                if((sequenceID-lastExecutedSeqNum) == 1) {
                    allRequests.put(sequenceID, dataReceived);
                    allOrderedRequests.add(dataReceived);

                    String serverReply = requestToReplica(dataReceived);
                    lastExecutedSeqNum++;
                    InetAddress aHost = InetAddress.getLocalHost();
                    String reply = makeResponseData(serverReply, "RM4", sequenceID);
                    System.out.println(reply);
                    sendUnicast(reply, Config.FRONTEND_IP);
                }
                else {
                    //ask from other RM
                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUnicast(String reply, String ipAddress) {
//        System.out.println("Trying Unicast - " + reply);
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


    private static void allRequestsTillNow() throws MalformedURLException {
        System.out.println("Executing all requests again");

        for(int i = 0; i< allOrderedRequests.size(); i++) {
            String temp = requestToReplica(allOrderedRequests.get(i));
            System.out.println(temp);
            String[] allParams = allOrderedRequests.get(i).split(",");
            lastExecutedSeqNum = Integer.parseInt(allParams[7]);
        }
        System.out.println("allRequestsTillNow() - done : lastExecutedSeqNum = " + lastExecutedSeqNum);
    }

    private static void receiveFromFE() {
        int RMport = 11111;
        try {
            DatagramSocket ds = new DatagramSocket(RMport);
            byte[] arr = new byte[1000];
            while(true) {
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);
                String dataReceived = new String(dp.getData(), 0, dp.getLength());

                if(dataReceived.equalsIgnoreCase("Crash Failure")) {
                    //start all servers here
                    String[] args = new String[]{};
                    LaunchServer.main(args);

                    Thread.sleep(5000);

                    allRequestsTillNow();
                }
                else if(dataReceived.equalsIgnoreCase("Software Failure")) {
                    //TODO: handle software failure
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
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

    private static String makeResponseData(String result, String senderReplica, Integer sequenceID ) {
        ResponseData data = new ResponseData(result, senderReplica, sequenceID);
        return data.toString();
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
