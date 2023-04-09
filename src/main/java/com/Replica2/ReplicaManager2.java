package com.Replica2;

import com.Replica2.Interfaces.WebInterface;
import com.Request.RequestData;

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

        InetAddress group = InetAddress.getByName("228.5.6.7");

        byte[] buf = new byte[1000];

        try(MulticastSocket socket = new MulticastSocket(5555)) {

            socket.joinGroup(group);

            while (true) {
                System.out.println("Test");
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                socket.receive(recv);

                String dataReceived = new String(recv.getData(), 0, recv.getLength());

                System.out.println("Received : " + dataReceived);

                String serverResponse = requestToReplica(dataReceived);
                System.out.println(serverResponse);

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
            qName = new QName("http://Impl/", "BookingImplService");
        } else if (customerID.startsWith("VER")) {
            url = new URL("http://localhost:8080/ServerVerdun/?wsdl");
            qName = new QName("http://Impl/", "BookingImplService");
        } else if (customerID.startsWith("OUT")) {
            url = new URL("http://localhost:8080/ServerOutremont/?wsdl");
            qName = new QName("http://Impl/", "BookingImplService");
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
