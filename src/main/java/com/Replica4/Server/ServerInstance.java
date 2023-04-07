package com.Replica4.Server;

import Client.Client;
import Log.Logger;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class ServerInstance {
    private String serverId;
    private String serverName;
    private int serverRegPort;
    private int serverUDPPort;
    private int serverWebPort;

    public ServerInstance(String serverId, String[] args) throws Exception {
        this.serverId = serverId;
        if (serverId.equals("ATW")) {
            serverName = "ATWATER";
            serverRegPort = Client.ATWATER_SERV;
            serverUDPPort = BookingImplementation.ATW_SERV_PORT;
            serverWebPort = 7300;
        } else if (serverId.equals("VER")) {
            serverName = "VERDUN";
            serverRegPort = Client.VERDUN_SERV;
            serverUDPPort = BookingImplementation.VER_SERV_PORT;
            serverWebPort = 7400;
        } else if (serverId.equals("OUT")) {
            serverName = "OUTREMONT";
            serverRegPort = Client.OUTREMONT_SERV;
            serverUDPPort = BookingImplementation.OUT_SERV_PORT;
            serverWebPort = 7500;
        }

        BookingImplementation rObj = new BookingImplementation(serverId, serverName);
        Endpoint ep = Endpoint.publish("http://localhost:" + serverWebPort + "/" + serverId.toUpperCase(), rObj);

//        ORB orb = ORB.init(args,null);
//        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
//        rootPOA.the_POAManager().activate();
//
//        BookingImplementation servant = new BookingImplementation(serverId, serverName);
//        servant.setORB(orb);
//
//        org.omg.CORBA.Object ref = rootPOA.servant_to_reference(servant);
//        InterfaceIDL href = InterfaceIDLHelper.narrow(ref);
//
//        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//        NamingContextExt ncref = NamingContextExtHelper.narrow(objRef);
//
//        NameComponent path[] = ncref.to_name(serverId);
//        ncref.rebind(path,href);


//        Registry registry = LocateRegistry.createRegistry(serverRegPort);
//        registry.bind("MOVIE_MANAGEMENT", rObj);
        System.out.println(serverName + " Server is Up and Running");
        Logger.serverLog(serverId, " Server is Up and Running");

        Runnable task = () -> {
            listenForRequest(rObj, serverUDPPort, serverName, serverId);
        };
        Thread thread = new Thread(task);
        thread.start();
//        orb.run();
    }

    private static void listenForRequest(BookingImplementation obj, int serverUdpPort, String serverName, String serverId) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Logger.serverLog(serverId, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String movieName = parts[2];
                String movieId = parts[3];
                Integer qTickets = Integer.valueOf(parts[4]);
                //int noOfTickets = Integer.valueOf(parts[4]);
                if (method.equalsIgnoreCase("removeMovie")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieName: " + movieName + " movieId: " + movieId + " ", " ...");                    String result = obj.removeMovieUDP(movieId, movieName, customerID);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listMovieAvailability")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieName: " + movieName + " movieId: " + movieId + " ", " ...");                    String result = obj.listMovieAvailabilityUDP(movieName);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookMovie")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieName: " + movieName + " movieId: " + movieId + " ", " ...");                    String result = obj.bookMoviesTickets(customerID, movieId, movieName,qTickets);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelMovie")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieId: " + movieId + " movieName: " + movieName + " ", " ...");
                    String result = obj.cancelMovieTickets(customerID, movieId, movieName,qTickets);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverId, customerID, " UDP reply sent " + method + " ", " movieId: " + movieId + " movieName: " + movieName + " ", sendingResult);
            }


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

