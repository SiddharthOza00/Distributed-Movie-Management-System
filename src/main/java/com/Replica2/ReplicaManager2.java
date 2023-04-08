package com.Replica2;

import com.Replica2.Interfaces.Implementation;
import com.Replica2.Interfaces.WebInterface;
import com.Request.RequestData;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicaManager2 {

    ArrayList<Object> requestQueue = new ArrayList<>();
    ConcurrentHashMap<String, Object> allMessages = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            try {
                receiveMulticast();
            } catch (Exception e) {
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

            while (true) {
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
                if (customerID.substring(0,3).equalsIgnoreCase("ATW")) {
                    serverName = "Atwater";
                }
                else if (customerID.substring(0,3).equalsIgnoreCase("OUT")) {
                    serverName = "Outremont";
                }
                else if (customerID.substring(0,3).equalsIgnoreCase("VER")) {
                    serverName = "Verdun";
                }

                WebInterface webInterface = new Implementation(customerID.substring(0,2), serverName);

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
//                        webInterface.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private static String requestToReplica(RequestData myObject) throws MalformedURLException {

        URL url;
        QName qName;

        if (myObject.getCustomerID().substring(0, 3).equals("ATW")) {
            url = new URL("http://localhost:8080/ServerAtwater/?wsdl");
            qName = new QName("http://Impl/", "BookingImplService");
        } else if (myObject.getCustomerID().substring(0, 3).equals("VER")) {
            url = new URL("http://localhost:8080/ServerVerdun/?wsdl");
            qName = new QName("http://Impl/", "BookingImplService");
        } else if (myObject.getCustomerID().substring(0, 3).equals("OUT")) {
            url = new URL("http://localhost:8080/ServerOutremont/?wsdl");
            qName = new QName("http://Impl/", "BookingImplService");
        } else {
            return "No response";
        }

        Service service = Service.create(url, qName);

        WebInterface impl = service.getPort(WebInterface.class);

        String customerID = myObject.getCustomerID();
        String serverID = customerID.substring(0, 3);
        String serverName;

        String typeOfUser = myObject.getCustomerID().substring(3, 4);

        switch (typeOfUser) {
            case "A":
                if (myObject.getMethod().equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.addMovieSlots(myObject.getMovieID(), myObject.getMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                if (myObject.getMethod().equalsIgnoreCase("removeMovieSlots")) {
                    String serverReply = impl.removeMovieSlots(myObject.getMovieID(), myObject.getMovieName());
                    return serverReply;
                }
                if (myObject.getMethod().equalsIgnoreCase("listMovieShowsAvailability")) {
                    String serverReply = impl.listMovieShowsAvailability(myObject.getMovieName());
                    return serverReply;
                }
                break;
            case "C":
                if (myObject.getMethod().equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMovieTickets(myObject.getCustomerID(), myObject.getMovieID(), myObject.getMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                if (myObject.getMethod().equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(myObject.getCustomerID(), myObject.getMovieID(), myObject.getMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                if (myObject.getMethod().equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(myObject.getCustomerID());
                    return serverReply;
                }
                if (myObject.getMethod().equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(myObject.getCustomerID(), myObject.getMovieName(), myObject.getMovieID(), myObject.getnewMovieID(), myObject.getnewMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                break;
        }
        return "No response";
    }
}
