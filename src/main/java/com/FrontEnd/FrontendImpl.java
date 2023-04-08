package com.FrontEnd;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.Request.RequestData;

@WebService(endpointInterface = "com.FrontEnd.FrontendInterface")
@SOAPBinding(style = Style.RPC)
public class FrontendImpl implements FrontendInterface {

        public static final String sequencerIP = "192.168.247.53";
        public static final int sequencerPort = 2233;

        public FrontendImpl() {
        }

        @Override
        public String addMovieSlots(String customerID, String movieID, String movieName, int bookingCapacity) {
                RequestData requestData = new RequestData("addMovieSlots", customerID, movieID, movieName, null, null,
                                bookingCapacity, 0);
                sendRequestToSequencer(requestData);
                return "Success";
        }

        @Override
        public String removeMovieSlots(String customerID, String movieID, String movieName) {
                RequestData requestData = new RequestData("removeMovieSlots", customerID, movieID, movieName, null,
                                null, 0, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String listMovieShowsAvailability(String movieName, boolean isOwnClient) {
                RequestData requestData = new RequestData("listMovieShowsAvailability", null, null, movieName, null,
                                null, 0, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets,
                        boolean isOwnClient) {
                RequestData requestData = new RequestData("bookMovieTickets", customerID, movieID, movieName, null,
                                null, numberOfTickets, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String getBookingSchedule(String customerID, boolean isOwnClient) {
                RequestData requestData = new RequestData("getBookingSchedule", customerID, null, null, null, null, 0,
                                0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
                RequestData requestData = new RequestData("cancelMovieTickets", customerID, movieID, movieName, null,
                                null, numberOfTickets, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID,
                        String newMovieName, int numberOfTickets) {
                RequestData requestData = new RequestData("exchangeTickets", customerID, movieID, oldMovieName,
                                newMovieID, newMovieName, numberOfTickets, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        public void sendRequestToSequencer(RequestData requestData) {
                System.out.println("Sending request to sequncer-- "+requestData);
                DatagramSocket aSocket = null;
                try {
                        aSocket = new DatagramSocket(2234);
                        String dataFromClient = requestData.toString();
                        byte[] message = dataFromClient.getBytes();
                        InetAddress aHost = InetAddress.getByName(sequencerIP);
                        DatagramPacket requestToSequencer = new DatagramPacket(message, dataFromClient.length(), aHost,
                                        sequencerPort);

                        aSocket.send(requestToSequencer);

                        // aSocket.setSoTimeout(1000);
                        // // Set up an UPD packet for recieving
                        // byte[] buffer = new byte[1000];
                        // DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                        // // Try to receive the response from the ping
                        // aSocket.receive(response);
                        // String sentence = new String(response.getData(), 0,
                        // response.getLength());
                        // System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>" +
                        // sentence);
                        // int sequenceID = Integer.parseInt(sentence.trim());
                        // System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>SequenceID:"
                        // + sequenceID);
                } catch (SocketException e) {
                        // System.out.println("Failed: " + requestFromClient.noRequestSendError());
                        System.out.println("Socket: " + e.getMessage());
                } catch (IOException e) {
                        // System.out.println("Failed: " + requestFromClient.noRequestSendError());
                        e.printStackTrace();
                        System.out.println("IO: " + e.getMessage());
                } finally {
                        if (aSocket != null)
                                aSocket.close();
                }
        }

}
