package com.Replica2.Service;

import com.Replica2.Logger.Logger;
import com.Replica2.Interfaces.Implementation;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
public class ServerInstance {

    private String serverID;
    private String serverName;
    private String serverEndpoint;
    private int serverUdpPort;

    public ServerInstance(String serverID, String[] args) throws Exception {
        this.serverID = serverID;
        switch (serverID) {
            case "ATW":
                serverName = "Atwater";
                serverUdpPort = Implementation.Atwater_Server_Port;
                serverEndpoint = "http://localhost:8080/atwater";
                break;
            case "OUT":
                serverName = "Outremont";
                serverUdpPort = Implementation.Outremont_Server_Port;
                serverEndpoint = "http://localhost:8080/outremont";
                break;
            case "VER":
                serverName = "Verdun";
                serverUdpPort = Implementation.Verdun_Server_Port;
                serverEndpoint = "http://localhost:8080/verdun";
                break;
        }

        try {
            Implementation service = new Implementation(serverID, serverName);

            Endpoint endpoint = Endpoint.publish(serverEndpoint, service);

            System.out.println(serverName + " Server is Up & Running");
            Logger.serverLog(serverID, " Server is Up & Running");
            Runnable task = () -> {
                listenForRequest(service, serverUdpPort, serverName, serverID);
            };
            Thread thread = new Thread(task);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            Logger.serverLog(serverID, "Exception: " + e);
        }
    }


    private static void listenForRequest(Implementation obj, int serverUdpPort, String serverName, String serverID) {
        String sendingResult = "";
        try (DatagramSocket aSocket = new DatagramSocket(serverUdpPort)) {
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String movieName = parts[2];
                String movieID = parts[3];
                int numberOfTickets = Integer.parseInt(parts[4]);
                if (method.equalsIgnoreCase("bookMovieTickets")) {
                    String result = obj.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
                    Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", sendingResult);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listMovieShowsAvailability")) {
                    String result = obj.listMovieShowsAvailabilityUDP(movieName);
                    Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", sendingResult);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase(("removeMovieSlots"))) {
                    String result = obj.removeMovieSlots(movieID, movieName);
                    Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", sendingResult);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelMovieTickets")) {
                    String result = obj.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                    Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", sendingResult);
                    sendingResult = result + ";";
                }
                sendingResult = sendingResult.trim();
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(), request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", sendingResult);
            }
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}