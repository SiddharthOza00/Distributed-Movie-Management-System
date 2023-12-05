package com.Replica3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.Request.Config;
import com.Request.RequestData;
import com.Replica3.Impl.IBooking;
import com.Request.ResponseData;

public class RManager {

    private static Map<Integer, String> allRequests;
    private static ArrayList<String> allOrderedRequests;
    private static int lastExecutedSeqNum;
    // ConcurrentHashMap<String, Object> allMessages = new ConcurrentHashMap<>();
    
    public static void main(String[] args) throws Exception {
        allRequests = new ConcurrentHashMap<>();
        allOrderedRequests = new ArrayList<>();
        lastExecutedSeqNum = 0;
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
//        executor.execute(new Thread( () -> {
//            try {
//                receiveMulticast();
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
//        }));
//        executor.schedule(new Thread( () -> {
//            try {
//                receiveFromFE();
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
//        }), 500, TimeUnit.MILLISECONDS);

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

        InetAddress group = InetAddress.getByName("228.5.6.7");

        byte[] buf = new byte[1000];

        try (MulticastSocket socket = new MulticastSocket(5555)){

            socket.joinGroup(group);

            while(true) {
                System.out.println("receiveMulticast()");
                                
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                socket.receive(recv);

                String dataReceived = new String(recv.getData(), 0, recv.getLength());

                System.out.println("Received : " + dataReceived);


                String[] requestParams = dataReceived.split(",");
                int sequenceID = Integer.parseInt(requestParams[7]);

                if((sequenceID-lastExecutedSeqNum) == 1) {
                    allRequests.put(sequenceID, dataReceived);
                    allOrderedRequests.add(dataReceived);

                    try {
                        String serverReply = requestToReplica(dataReceived);
                        InetAddress aHost = InetAddress.getLocalHost();
                        // String reply = makeResponseData(serverReply, String.valueOf(aHost.getHostAddress()), sequenceID);
                        String reply = makeResponseData(serverReply, "RM3", sequenceID);
                        System.out.println(reply);
                        sendUnicast(reply, Config.FRONTEND_IP);
                    } catch (Exception e) {
                        System.out.println("Exception occurred (Possibly servers are down)");
                        e.printStackTrace();
                    }
                }
                else {
                    // contactRM(dataReceived);
                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUnicast(String reply, String ipAddress) {
        // System.out.println("Trying Unicast - " + reply);
        int FEport = Config.FRONTEND_PORT;
        int RMport = Config.RM3_PORT_SQ;
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
        int RMport = Config.RM3_PORT_FE;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(RMport);
            byte[] arr = new byte[1000];
            while(true) {
                System.out.println("receiveFromFE()");
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);
                String dataReceived = new String(dp.getData(), 0, dp.getLength());

                if(dataReceived.equalsIgnoreCase("Crash Failure")) {
                    //start all servers here
                    System.out.println("Crash Failure received");
                    String[] args = new String[]{};
                    LaunchServer.main(args);

                    Thread.sleep(5000);

                    allRequestsTillNow();
                }
                else if(dataReceived.equalsIgnoreCase("Software Failure")) {
                    replaceServer();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

//        if(ds!=null) {
//            ds.close();
//        }
    }

    private static void contactRM(String request) {
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
    private static void replaceServer() throws MalformedURLException {
       System.out.println("Trying to replace replica!\n");
       String[] args = new String[]{"Start"};
       boolean serverReplaced = true;
       System.out.println("Replica Replaced!\n");
       allRequestsTillNow();
    }

    private static String makeResponseData(String result, String senderReplica, Integer sequenceID ) {
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
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("removeMovieSlots")) {
                    String serverReply = impl.removeMovieSlots(movieID, movieName);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("listMovieShowsAvailability")) {
                    String serverReply = impl.listMovieShowsAvailability(movieName);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMovieTickets(customerID,movieID, movieName, numberOfTickets);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(customerID,movieID, movieName, numberOfTickets);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(customerID);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName, numberOfTickets);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                break;
            case "C":
                if(methodName.equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMovieTickets(customerID,movieID, movieName, numberOfTickets);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(customerID,movieID, movieName, numberOfTickets);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(customerID);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                if(methodName.equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName, numberOfTickets);
                    lastExecutedSeqNum++;
                    return serverReply;
                }
                break;
            }


        return "No response";
    }
    
}
