package com.Replica3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.omg.CORBA.Request;

import com.Replica3.Impl.BookingImpl;
import com.Replica3.Impl.IBooking;

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

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUnicast(String reply, String ipAddress) {
        System.out.println("Unicast - " + reply);
        int port = 0000;
        int port2 = 0000;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(port);
            byte[] arr = reply.getBytes();
            InetAddress address = InetAddress.getByName(ipAddress);

            DatagramPacket dp = new DatagramPacket(arr, arr.length, address, port2);
            ds.send(dp);
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

    private static void contactRM(Request request) {
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

    // private static String requestToReplica(Request myObject) throws MalformedURLException {

    //     URL url;
    //     QName qName;
        

    //     if(myObject.userID.substring(0,3).equals("ATW")) {
    //        url = new URL("http://localhost:8080/ServerAtwater/?wsdl");
    //        qName = new QName("http://Impl/","BookingImplService");
    //     }

    //     else if(myObject.userID.substring(0,3).equals("VER")) {
    //         url = new URL("http://localhost:8080/ServerVerdun/?wsdl");
    //         qName = new QName("http://Impl/","BookingImplService");
    //     }

    //     else {
    //         //OUTREMONT
    //         url = new URL("http://localhost:8080/ServerOutremont/?wsdl");
    //         qName = new QName("http://Impl/","BookingImplService");
    //     }

    //     Service service = Service.create(url, qName);

    //     IBooking impl = service.getPort(IBooking.class);

    //     String customerID = myObject.userID;
    //     String serverID = customerID.substring(0,3);
    //     String serverName;

    //     // BookingImpl impl = new BookingImpl(serverID, serverName);

    //     String typeOfUser = myObject.userID.substring(3,4);
        
    //     switch(typeOfUser) {
    //         case "A":
    //             if(myObject.actionToPerform.equalsIgnoreCase("bookMovieTickets")) {
    //                 String serverReply = impl.addMovieSlots(myObject.movieID, myObject.movieName, myObject.bookingCapacity);
    //                 return serverReply;
    //             }
    //             if(myObject.actionToPerform.equalsIgnoreCase("removeMovieSlots")) {
    //                 String serverReply = impl.removeMovieSlots(myObject.movieID, myObject.movieName);
    //                 return serverReply;
    //             }
    //             if(myObject.actionToPerform.equalsIgnoreCase("listMovieShowsAvailability")) {
    //                 String serverReply = impl.listMovieShowsAvailability(myObject.movieName);
    //                 return serverReply;
    //             }
    //             break;
    //         case "C":
    //             if(myObject.actionToPerform.equalsIgnoreCase("bookMovieTickets")) {
    //                 String serverReply = impl.bookMovieTickets(myObject.customerID,myObject.movieID, myObject.movieName, myObject.numberOfTickets);
    //                 return serverReply;
    //             }
    //             if(myObject.actionToPerform.equalsIgnoreCase("cancelMovieTickets")) {
    //                 String serverReply = impl.cancelMovieTickets(myObject.customerID,myObject.movieID, myObject.movieName, myObject.numberOfTickets);
    //                 return serverReply;
    //             }
    //             if(myObject.actionToPerform.equalsIgnoreCase("getBookingSchedule")) {
    //                 String serverReply = impl.getBookingSchedule(myObject.customerID);
    //                 return serverReply;
    //             }
    //             if(myObject.actionToPerform.equalsIgnoreCase("exchangeTickets")) {
    //                 String serverReply = impl.exchangeTickets(myObject.customerID, myObject.movieName, myObject.movieID, myObject.new_movieID, myObject.new_movieName, myObject.numberOfTickets);
    //                 return serverReply;
    //             }
    //             break;
            // }


        // return "";
    // }
    
}
