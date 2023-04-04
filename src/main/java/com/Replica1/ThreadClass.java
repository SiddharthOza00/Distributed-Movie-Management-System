package com.Replica1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ThreadClass implements Runnable {

    int portNumber;
    LoggerInfo userLogger;
    WebserviceImpl serverImpl;
    
    //Default constructor
    public ThreadClass() {
    }

    //Parameterized constructor
    public ThreadClass(int portNumber, LoggerInfo userLogger, WebserviceImpl serverImpl) {
        this.portNumber = portNumber;
        this.userLogger = userLogger;
        this.serverImpl = serverImpl;
    }

    @Override
    public void run() {
        // UDP Implementation
        System.out.println("-------Inside-------- thread------ this server is "+serverImpl.getServerName()+" and port number is "+portNumber);
        try {
            while (true) {
                DatagramSocket serverSocket = new DatagramSocket(portNumber);
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                serverSocket.receive(packet);

                System.out.println("VIA UDP -- Packets received at " + serverImpl.getServerName() +" socket!!");
                userLogger.logger.info("--VIA UDP-- Packets received at " + serverImpl.getServerName() +" socket");

                String arr = new String(packet.getData()).trim();
                System.out.print("VIA UDP - Message received from port -- " + packet.getPort()
                        + " to " + serverImpl.getServerName() +" server: " + arr);
                userLogger.logger.info("VIA UDP - Message received from port -- " + packet.getPort()
                        + " to " + serverImpl.getServerName() +" server: " + arr);

                String result = MethodMapper(arr, serverImpl, userLogger);

                byte[] b = result.getBytes();

                InetAddress ip = InetAddress.getLocalHost();
                DatagramPacket packetResult = new DatagramPacket(b, b.length, ip, packet.getPort());
                serverSocket.send(packetResult);
                System.out.print("VIA UDP - Message sent from " + serverImpl.getServerName() +" server to : " + packet.getPort());
                userLogger.logger
                        .info("--VIA UDP-- Message sent from " + serverImpl.getServerName() +" server to port : " + packet.getPort());

                serverSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            userLogger.logger.info("Error occured in " + serverImpl.getServerName() +" Server. Error is: " + e.getMessage());
        }
        // throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    //MapperMethod
    private static String MethodMapper(String arr, WebserviceImpl methImpl, LoggerInfo userLogger) {
        String res = new String();
        if (arr.contains("listMovieShowsAvailability")) {
            userLogger.logger.info("Calling " + methImpl.getServerName() +" server's listMovieShowsAvailability method");
            String[] meth = arr.split(",");
            String movieName = meth[1];
            try {
                res = methImpl.listMovieShowsAvailability(movieName, false);
            } catch (Exception e) {
                e.printStackTrace();
                userLogger.logger.info(
                        "Exception ocuured in calling " + methImpl.getServerName() +" server's listMovieShowsAvailability method. Exception is "
                                + e.getMessage());
            }

        } else if (arr.contains("bookMovieTickets")) {
            res = new String();
            System.out.println("Book movie tickets of " + methImpl.getServerName() +" needs to be called");
            userLogger.logger.info("Calling " + methImpl.getServerName() +" server's bookMovieTickets method");
            String[] meth = arr.split(",");
            String[] arguments = meth[1].split("-");
            // String customerID, String movieID, String movieName, Integer numberOfTickets
            String customerID = arguments[0];
            String movieID = arguments[1];
            String movieName = arguments[2];
            Integer tickets = Integer.parseInt(arguments[3]);

            try {
                String resultTemp = methImpl.bookMovieTickets(customerID, movieID, movieName, tickets, false);
                res = resultTemp;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (arr.contains("getBookingSchedule")) {
            System.out.println("Get booking schedule from " + methImpl.getServerName() +" servers-- ");
            userLogger.logger.info("Calling " + methImpl.getServerName() +" server's getBookingSchedule method");
            String[] meth = arr.split(",");
            String arguments = meth[1];

            try {
                res = methImpl.getBookingSchedule(arguments, false);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return res;
    }
    
}
