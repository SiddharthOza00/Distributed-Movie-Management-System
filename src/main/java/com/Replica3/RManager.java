package com.Replica3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.Replica3.Impl.IBooking;
import com.Request.RequestData;

public class RManager {

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

                sendUnicast(serverReply, "192.168.247.35");

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUnicast(String reply, String ipAddress) {
        // System.out.println("Trying Unicast - " + reply);
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

    // private static void allRequestsTillNow() {
    //     System.out.println("Executing all requests again");
    //     while(true) {
    //         //queue iterator
    //         while() {

    //         }
    //     }
    // }

    private static void contactRM(RequestData request) {
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
        

        if(customerID.substring(0,3).equals("ATW")) {
           url = new URL("http://localhost:8080/ServerAtwater/?wsdl");
           qName = new QName("http://Impl.Replica3.com/","BookingImplService");
        }

        else if(customerID.substring(0,3).equals("VER")) {
            url = new URL("http://localhost:8080/ServerVerdun/?wsdl");
            qName = new QName("http://Impl.Replica3.com/","BookingImplService");
        }

        else if(customerID.substring(0,3).equals("OUT")) {
            url = new URL("http://localhost:8080/ServerOutremont/?wsdl");
            qName = new QName("http://Impl.Replica3.com/","BookingImplService");
        }
        else {
            return "No response";
        }

        Service service = Service.create(url, qName);

        IBooking impl = service.getPort(IBooking.class);

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
                    String serverReply = impl.bookMovieTickets(customerID,movieID, movieName, numberOfTickets);
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
                    String serverReply = impl.bookMovieTickets(customerID,movieID, movieName, numberOfTickets);
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
