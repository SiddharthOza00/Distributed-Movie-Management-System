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

import com.Request.RequestData;
import org.omg.CORBA.Request;

import com.Replica3.Impl.BookingImpl;
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

    // private static void allRequestsTillNow() {
    //     System.out.println("Executing all requests again");
    //     while(true) {
    //         //queue iterator
    //         while() {

    //         }
    //     }
    // }

    private static void contactRM(Request request) {
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

    private static String requestToReplica(RequestData myObject) throws MalformedURLException {

        URL url;
        QName qName;
        

        if(myObject.getCustomerID().substring(0,3).equals("ATW")) {
           url = new URL("http://localhost:8080/ServerAtwater/?wsdl");
           qName = new QName("http://Impl/","BookingImplService");
        }

        else if(myObject.getCustomerID().substring(0,3).equals("VER")) {
            url = new URL("http://localhost:8080/ServerVerdun/?wsdl");
            qName = new QName("http://Impl/","BookingImplService");
        }

        else if(myObject.getCustomerID().substring(0,3).equals("OUT")) {
            url = new URL("http://localhost:8080/ServerOutremont/?wsdl");
            qName = new QName("http://Impl/","BookingImplService");
        }
        else {
            return "No response";
        }

        Service service = Service.create(url, qName);

        IBooking impl = service.getPort(IBooking.class);

        String customerID = myObject.getCustomerID();
        String serverID = customerID.substring(0,3);
        String serverName;

        String typeOfUser = myObject.getCustomerID().substring(3,4);
        
        switch(typeOfUser) {
            case "A":
                if(myObject.getMethod().equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.addMovieSlots(myObject.getMovieID(), myObject.getMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                if(myObject.getMethod().equalsIgnoreCase("removeMovieSlots")) {
                    String serverReply = impl.removeMovieSlots(myObject.getMovieID(), myObject.getMovieName());
                    return serverReply;
                }
                if(myObject.getMethod().equalsIgnoreCase("listMovieShowsAvailability")) {
                    String serverReply = impl.listMovieShowsAvailability(myObject.getMovieName());
                    return serverReply;
                }
                break;
            case "C":
                if(myObject.getMethod().equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMovieTickets(myObject.getCustomerID(),myObject.getMovieID(), myObject.getMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                if(myObject.getMethod().equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(myObject.getCustomerID(),myObject.getMovieID(), myObject.getMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                if(myObject.getMethod().equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(myObject.getCustomerID());
                    return serverReply;
                }
                if(myObject.getMethod().equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(myObject.getCustomerID(), myObject.getMovieName(), myObject.getMovieID(), myObject.getnewMovieID(), myObject.getnewMovieName(), myObject.getnumberOfTickets());
                    return serverReply;
                }
                break;
            }


        return "No response";
    }
    
}
