package com.FrontEnd;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import com.Request.RequestData;
import com.Request.ResponseData;

@WebService(endpointInterface = "com.FrontEnd.FrontendInterface")
@SOAPBinding(style = Style.RPC)
public class FrontendImpl implements FrontendInterface {


        public static final String sequencerIP = "192.168.247.53";
        public static final int sequencerPort = 2233;

        private int responseCounter = 0;
        private List<String> responses = new ArrayList<>(4);
        

        public int getResponseCounter() {
                return responseCounter;
        }

        public void setResponseCounter(int responseCounter) {
                this.responseCounter = responseCounter;
        }

        public List<String> getResponses() {
                return responses;
        }

        public void setResponses(List<String> responses) {
                this.responses = responses;
        }

        public FrontendImpl() {
        }

        @Override
        public String addMovieSlots(String customerID, String movieID, String movieName, int bookingCapacity) {
                RequestData requestData = new RequestData("addMovieSlots", customerID, movieID, movieName, null, null,
                                bookingCapacity);
                sendRequestToSequencer(requestData);
                boolean timerOver = false;
                startTimer(5000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout "+totalResponses);
                // String finalResponse = compareResultsAndSendFinalResult();
                
                if(totalResponses < 4){
                        //TODO: Send crash failure
                        System.out.println("Sending crash failure message");
                        // sendErrorMessageToRM();
                }else{
                        //TODO: Get final result
                        System.out.println("Got three responses well in time and now sending to client");
                }
                
                return "Success";
        }

        @Override
        public String removeMovieSlots(String customerID, String movieID, String movieName) {
                RequestData requestData = new RequestData("removeMovieSlots", customerID, movieID, movieName, null,
                                null, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String listMovieShowsAvailability(String customerID, String movieName, boolean isOwnClient) {
                RequestData requestData = new RequestData("listMovieShowsAvailability", customerID, null, movieName,
                                null,
                                null, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets,
                        boolean isOwnClient) {
                RequestData requestData = new RequestData("bookMovieTickets", customerID, movieID, movieName, null,
                                null, numberOfTickets);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String getBookingSchedule(String customerID, boolean isOwnClient) {
                RequestData requestData = new RequestData("getBookingSchedule", customerID, null, null, null, null, 0);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
                RequestData requestData = new RequestData("cancelMovieTickets", customerID, movieID, movieName, null,
                                null, numberOfTickets);
                sendRequestToSequencer(requestData);
                return null;
        }

        @Override
        public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID,
                        String newMovieName, int numberOfTickets) {
                RequestData requestData = new RequestData("exchangeTickets", customerID, movieID, oldMovieName,
                                newMovieID, newMovieName, numberOfTickets);
                sendRequestToSequencer(requestData);
                return null;
        }

        public void sendRequestToSequencer(RequestData requestData) {
                System.out.println("Sending request to sequncer-- " + requestData);
                DatagramSocket aSocket = null;
                try {
                        aSocket = new DatagramSocket(2234);
                        String dataFromClient = requestData.toString();
                        byte[] message = dataFromClient.getBytes();
                        InetAddress aHost = InetAddress.getByName(sequencerIP);
                        DatagramPacket requestToSequencer = new DatagramPacket(message, dataFromClient.length(), aHost,
                                        sequencerPort);

                        aSocket.send(requestToSequencer);

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


        private void startTimer(int timeout, boolean timerOver) {
                try {
                        CountDownLatch latch;
                        latch = new CountDownLatch(1);
                        boolean timeoutReached = latch.await(timeout, TimeUnit.MILLISECONDS);
                        timerOver = true;
                } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("FrontEndImpl_startTimer: " + e);
                }
        }

        
        public void responseUpdateFromFrontend(String response) {
                
                List<String> existingResponses = getResponses();
                existingResponses.add(response);
                setResponses(existingResponses);
        }

        private String compareResultsAndSendFinalResult() {
                List<String> responses = getResponses();
                int count = responses.size();
                String response1,ip1,seq1,response2,ip2,seq2,response3,ip3,seq3,response4,ip4,seq4;

                

                return "whatevs";
        }

}
