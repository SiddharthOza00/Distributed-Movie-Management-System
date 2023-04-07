package com.Replica3;
import com.Replica3.LoggerClass.LogData;
import com.Replica3.Impl.BookingImpl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.xml.ws.Endpoint;

public class AllServerTemplate {

    private String serverID;
    private String serverName;
    private int UDPPort;
    private String endpoint;

    public AllServerTemplate(String serverID) throws Exception {

        setPortsAndEndpoints(serverID);
        BookingImpl impl = new BookingImpl(serverID, serverName);
        this.serverID = serverID;
        testData(impl);

        try {
            Endpoint endPt = Endpoint.publish(endpoint, impl);
            System.out.println(serverName + " server has started successfully ");
            LogData.serverLog(serverID, " server has started successfully ");

            new Thread(() -> {
                UDPListener(impl, UDPPort, serverName, serverID);
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void setPortsAndEndpoints(String serverID) {
        if(serverID.equals("ATW")) {
            serverName = "Atwater";
            UDPPort = 3344;
            endpoint = "http://localhost:8080/ServerAtwater";
            return;
        }
        if(serverID.equals("VER")) {
            serverName = "Verdun";
            UDPPort = 4455;
            endpoint = "http://localhost:8080/ServerVerdun";
            return;
        }
        if(serverID.equals("OUT")) {
            serverName = "Outremont";
            UDPPort = 5566;
            endpoint = "http://localhost:8080/ServerOutremont";
            return;
        }
    }

    private void testData(BookingImpl impl) {
        switch (serverID) {
            case "ATW":
                impl.addMovieSlots("ATWM250323", "Avatar", 100);
                impl.addMovieSlots("ATWA250323", "Avatar", 100);
                impl.addMovieSlots("ATWE250323", "Avatar", 100);
                impl.addMovieSlots("ATWM260323", "Avatar", 100);
                break;

            case "VER":
                impl.addMovieSlots("VERM270323", "Avengers", 100);
                impl.addMovieSlots("VERA270323", "Avengers", 100);
                impl.addMovieSlots("VERE270323", "Avatar", 100);
                impl.addMovieSlots("VERM280323", "Avatar", 100);
                break;

            case "OUT":
                impl.addMovieSlots("OUTM290323", "Titanic", 100);
                impl.addMovieSlots("OUTA290323", "Titanic", 100);
                impl.addMovieSlots("OUTE290323", "Avatar", 100);
                impl.addMovieSlots("OUTM300323", "Avatar", 100);
                break;
        }
    }

    private static void UDPListener(BookingImpl obj, int sUDPPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String answer = "";
        try {
            aSocket = new DatagramSocket(sUDPPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP started ");
            LogData.serverLog(serverID, " UDP started ");
            while (true) {
                DatagramPacket incomingReq = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(incomingReq);
                String sentence = new String(incomingReq.getData(), 0, incomingReq.getLength());
                System.out.println(sentence);
                String[] splitting = sentence.split("_");
                String method = splitting[0];
                String customerID = splitting[1];
                String movieName = splitting[2];
                String movieID = splitting[3];
                int numOfTickets = Integer.parseInt(splitting[4]);
                if (method.equalsIgnoreCase("removeMovieSlots")) {
                    LogData.serverLog(serverID, customerID, " incoming UDP " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", " ");
                    String result = obj.removeMovieSlotsUDP(movieID, movieName, customerID);
                    answer = result + "_";
                } else if (method.equalsIgnoreCase("listMovieShowsAvailability")) {
                    LogData.serverLog(serverID, customerID, " incoming UDP " + method + " ", " movieName: " + movieName + " ", " ");
                    String result = obj.listMovieShowsAvailabilityUDP(movieName);
                    answer = result + "_";
                } else if (method.equalsIgnoreCase("bookMovieTickets")) {
                    LogData.serverLog(serverID, customerID, " incoming UDP " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " " + numOfTickets, " ");
                    String result = obj.bookMovieTickets(customerID, movieID, movieName, numOfTickets);
                    answer = result + "_";
                } else if (method.equalsIgnoreCase("cancelMovieTickets")) {
                    LogData.serverLog(serverID, customerID, " incoming UDP " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " " + numOfTickets + " ", " ");
                    String result = obj.cancelMovieTickets(customerID, movieID, movieName, numOfTickets);
                    answer = result + "_";
                }
                byte[] dataToBeSent = answer.getBytes();
                DatagramPacket reply = new DatagramPacket(dataToBeSent, answer.length(), incomingReq.getAddress(),
                        incomingReq.getPort());
                aSocket.send(reply);
                LogData.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", answer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}