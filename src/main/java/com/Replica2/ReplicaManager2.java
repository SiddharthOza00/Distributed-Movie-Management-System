package com.Replica2;

import com.Replica2.Interfaces.WebInterface;
import com.Replica2.Service.Server;
import com.Request.RequestData;
import com.Request.ResponseData;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicaManager2 {

    private static ArrayList<String> allOrderedRequests;
    private static ConcurrentHashMap<Integer, String> allRequests;
    private static int lastExecutedSeqNum;

    public static void main(String[] args) throws Exception {
        allRequests = new ConcurrentHashMap<>();
        allOrderedRequests = new ArrayList<>();
        lastExecutedSeqNum = 0;
        new Thread(() -> {
            try {
                receiveMulticast();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void restartServer() {
        System.out.println("Before");
        String[] arg = new String[]{"Start"};
        Server.main(arg);
        System.out.println("Here");
    }

    private static void receiveMulticast() throws UnknownHostException {

        InetAddress group = InetAddress.getByName("228.5.6.7");

        byte[] buf = new byte[1000];

        try(MulticastSocket socket = new MulticastSocket(5555)) {

            socket.joinGroup(group);

            restartServer();
            while (true) {
                System.out.println("Test");
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                socket.receive(recv);
                String dataReceived = new String(recv.getData(), 0, recv.getLength());

                System.out.println("Received : " + dataReceived);

                String[] requestparams = dataReceived.split(",");
                int sequenceID = Integer.parseInt(requestparams[7]);

                if((sequenceID-lastExecutedSeqNum) == 1) {
                    allRequests.put(sequenceID, dataReceived);
                    allOrderedRequests.add(dataReceived);

                    String serverReply = requestToReplica(dataReceived);
                    lastExecutedSeqNum++;
                    InetAddress aHost = InetAddress.getLocalHost();
                    String reply = makeResponseData(serverReply, String.valueOf(aHost.getHostAddress()), sequenceID);
                    System.out.println(reply);
                    sendUnicast(reply, "192.168.247.36");
                } else {
                    // ask from other RM
                }

                String serverResponse = requestToReplica(dataReceived);
                System.out.println(serverResponse);

                sendUnicast(serverResponse, "192.168.247.35");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void allRequestsTillNow() throws MalformedURLException {
        System.out.println("Executing all requests again");

        for(int i = 0; i< allOrderedRequests.size(); i++) {
            requestToReplica(allOrderedRequests.get(i));
            String[] allParams = allOrderedRequests.get(i).split(",");
            lastExecutedSeqNum = Integer.parseInt(allParams[7]);
        }
        System.out.println("allRequestsTillNow() - done : lastExecutedSeqNum = " + lastExecutedSeqNum);
    }

    private static void sendUnicast(String reply, String ipAddress) {
        System.out.println("Trying Unicast - " + reply);
        int FEport = 44553;
        int RMport = 9957;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(RMport);
            byte[] arr = reply.getBytes();
            InetAddress address = InetAddress.getByName(ipAddress);

            DatagramPacket dp = new DatagramPacket(arr, arr.length, address, FEport);
            ds.send(dp);
            System.out.println("Unicast sent to FE (IP:" + ipAddress + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ds != null) {
            ds.close();
        }
    }

    private static void contactRM(RequestData request) {
        int port = 0000;
        String ipAddress = " ";
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

    private static String makeResponseData(String result, String senderReplica, Integer sequenceID) {
        ResponseData data = new ResponseData(result, senderReplica, sequenceID);
        return data.toString();
    }

    private static String requestToReplica(String dataReceived) throws MalformedURLException {

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


        if (customerID.startsWith("ATW")) {
            url = new URL("http://localhost:8080/ServerAtwater/?wsdl");
            qName = new QName("http://Interfaces.Replica2.com/", "ImplementationService");
        } else if (customerID.startsWith("VER")) {
            url = new URL("http://localhost:8080/ServerVerdun/?wsdl");
            qName = new QName("http://Interfaces.Replica2.com/", "ImplementationService");
        } else if (customerID.startsWith("OUT")) {
            url = new URL("http://localhost:8080/ServerOutremont/?wsdl");
            qName = new QName("http://Interfaces.Replica2.com/", "ImplementationService");
        } else {
            return "No response";
        }

        Service service = Service.create(url, qName);

        WebInterface impl = service.getPort(WebInterface.class);

        String typeOfUser =customerID.substring(3, 4);

        switch (typeOfUser) {
            case "A":
                if(methodName.equalsIgnoreCase("addMovieSlots")) {
                    return impl.addMovieSlots(movieID, movieName, numberOfTickets);
                }
                if(methodName.equalsIgnoreCase("removeMovieSlots")) {
                    return impl.removeMovieSlots(movieID, movieName);
                }
                if(methodName.equalsIgnoreCase("listMovieShowsAvailability")) {
                    return impl.listMovieShowsAvailability(movieName);
                }
                if(methodName.equalsIgnoreCase("bookMovieTickets")) {
                    return impl.bookMovieTickets(customerID,movieID, movieName, numberOfTickets);
                }
                if(methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    return impl.cancelMovieTickets(customerID,movieID, movieName, numberOfTickets);
                }
                if(methodName.equalsIgnoreCase("getBookingSchedule")) {
                    return impl.getBookingSchedule(customerID);
                }
                if(methodName.equalsIgnoreCase("exchangeTickets")) {
                    return impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName, numberOfTickets);
                }
                break;
            case "C":
                if(methodName.equalsIgnoreCase("bookMovieTickets")) {
                    return impl.bookMovieTickets(customerID,movieID, movieName, numberOfTickets);
                }
                if(methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    return impl.cancelMovieTickets(customerID,movieID, movieName, numberOfTickets);
                }
                if(methodName.equalsIgnoreCase("getBookingSchedule")) {
                    return impl.getBookingSchedule(customerID);
                }
                if(methodName.equalsIgnoreCase("exchangeTickets")) {
                    return impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName, numberOfTickets);
                }
                break;
        }
        return "No response";
    }
}