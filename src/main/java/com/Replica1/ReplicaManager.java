package com.Replica1;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Map;

import com.Request.Config;
import com.Request.ResponseData;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReplicaManager {

    private static Map<Integer, String> allRequests;
    private static ArrayList<String> allOrderedRequests;
    private static int lastExecutedSeqNum;

    ArrayList<Object> requestQueue = new ArrayList<>();
    ConcurrentHashMap<String, Object> allMessages = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        allRequests = new ConcurrentHashMap<>();
        allOrderedRequests = new ArrayList<>();
        lastExecutedSeqNum = 0;
        new Thread(() -> {
            try {
                receive();
            } catch (Exception e) {
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

    private static void receive() throws UnknownHostException {

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

                String[] requestParams = dataReceived.split(",");
                int sequenceID = Integer.parseInt(requestParams[7]);

                if((sequenceID-lastExecutedSeqNum) == 1) {
                    allRequests.put(sequenceID, dataReceived);
                    allOrderedRequests.add(dataReceived);

                    try{
                        String serverReply = requestToReplica(dataReceived);
                    System.out.println(serverReply);
                    lastExecutedSeqNum++;
                    String reply = makeResponseData(serverReply, "RM1", sequenceID);
                    System.out.println(reply);
                    sendToFrontend(reply, Config.FRONTEND_IP);
                    }catch(Exception e){
                        System.out.println("Exception occurred (Possibly servers are down)");
                        e.printStackTrace();
                    }
                }
                else {
                    //ask from other RM
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveFromFE() {
        int RMport = Config.RM1_PORT_FE;
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
                    AtwaterServer.main(args);
                    VerdunServer.main(args);
                    OutremontServer.main(args);

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

    private static String makeResponseData(String result, String senderReplica, Integer sequenceID ) {
        ResponseData data = new ResponseData(result, senderReplica, sequenceID);
        return data.toString();
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

        if (customerID.substring(0, 3).equals("ATW")) {
            url = new URL("http://localhost:8081/atwater?wsdl");
            qName = new QName("http://Replica1.com/", "WebserviceImplService");
        }

        else if (customerID.substring(0, 3).equals("VER")) {
            url = new URL("http://localhost:8082/verdun?wsdl");
            qName = new QName("http://Replica1.com/", "WebserviceImplService");
        }

        else if (customerID.substring(0, 3).equals("OUT")) {
            url = new URL("http://localhost:8083/outremont?wsdl");
            qName = new QName("http://Replica1.com/", "WebserviceImplService");
        } else {
            return "No response";
        }

        Service service = Service.create(url, qName);

        Webservice impl = service.getPort(Webservice.class);

        String typeOfUser = customerID.substring(3, 4);

        switch (typeOfUser) {
            case "A":
                if (methodName.equalsIgnoreCase("addMovieSlots")) {
                    String serverReply = "";
                    boolean serverReplyb = impl.addMovieSlots(movieID, movieName, numberOfTickets);
                    if(serverReplyb){
                        serverReply = "Success";
                    }else{
                        serverReply = "Failure";
                    }
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("removeMovieSlots")) {
                    String serverReply = "";
                    boolean replyb = impl.removeMovieSlots(movieID, movieName);
                    if(replyb){
                        serverReply = "Success";
                    }else{
                        serverReply = "Failure";
                    }
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("listMovieShowsAvailability")) {
                    String serverReply = impl.listMovieShowsAvailability(movieName,true);
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMovieTickets(customerID, movieID, movieName, numberOfTickets, true);
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(customerID, true);
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName,
                            numberOfTickets);
                    return serverReply;
                }
                break;
            case "C":
                if (methodName.equalsIgnoreCase("bookMovieTickets")) {
                    String serverReply = impl.bookMovieTickets(customerID, movieID, movieName, numberOfTickets,true);
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("cancelMovieTickets")) {
                    String serverReply = impl.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("getBookingSchedule")) {
                    String serverReply = impl.getBookingSchedule(customerID,true);
                    return serverReply;
                }
                if (methodName.equalsIgnoreCase("exchangeTickets")) {
                    String serverReply = impl.exchangeTickets(customerID, movieName, movieID, newMovieID, newMovieName,
                            numberOfTickets);
                    return serverReply;
                }
                break;
        }

        return "No response";
    }

    private static void sendToFrontend(String dataReceived, String ipAddress) {
        System.out.println("Trying Unicast - " + dataReceived);
        int FEport = Config.FRONTEND_PORT;
        int RMport = Config.RM1_PORT_FE;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            byte[] arr = dataReceived.getBytes();
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

}