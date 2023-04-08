package com.FrontEnd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrontendThread implements Runnable {

    int portNumber;
    FrontendImpl feImpl;

    // Default Constructor
    public FrontendThread() {
    }

    // Parameterised Constructor
    public FrontendThread(int portNumber, FrontendImpl feImpl) {
        this.portNumber = portNumber;
        this.feImpl = feImpl;
    }

    @Override
    public void run() {
        // UDP Implementation
        System.out
                .println("-------Inside-------- thread------ this server is Frontend and port number is " + portNumber);
        try {
            while (true) {
                DatagramSocket serverSocket = new DatagramSocket(portNumber);
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                serverSocket.receive(packet);

                System.out.println("VIA UDP -- Packets received at Frontend socket!!");

                String arr = new String(packet.getData()).trim();
                System.out.print("VIA UDP - Message received from port -- " + packet.getPort()+ " to Frontend server: " + arr);



                // String result = MethodMapper(arr, feImpl);
                // String result = "Hello check";

                // byte[] b = result.getBytes();

                //REsult to be checked
                // InetAddress ip = InetAddress.getLocalHost();
                // DatagramPacket packetResult = new DatagramPacket(b, b.length, ip, packet.getPort());
                // serverSocket.send(packetResult);
                // System.out.print("VIA UDP - Message sent from Frontend server to : " + packet.getPort());
                serverSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occured in Frontend Server. Error is: " + e.getMessage());
        }
    }

    // MapperMethod
    private static String MethodMapper(String arr, FrontendImpl methImpl) {
        String res = new String();
        if (arr.contains("listMovieShowsAvailability")) {
            System.out.println("Calling  server's listMovieShowsAvailability method");
            String[] meth = arr.split(",");
            String movieName = meth[1];
            //TODO: PASS CUSTOMER ID IN ARR
            String customerID = meth[2];
            try {
                res = methImpl.listMovieShowsAvailability(customerID, movieName, false);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(
                        "Exception ocuured in calling server's listMovieShowsAvailability method. Exception is "
                                + e.getMessage());
            }

        } else if (arr.contains("bookMovieTickets")) {
            res = new String();
            System.out.println("Book movie tickets of  needs to be called");
            System.out.println("Calling  server's bookMovieTickets method");
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
            System.out.println("Get booking schedule from  servers-- ");
            System.out.println("Calling  server's getBookingSchedule method");
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
