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

import com.Request.Config;
import com.Request.RequestData;
import com.Request.ResponseData;

@WebService(endpointInterface = "com.FrontEnd.FrontendInterface")
@SOAPBinding(style = Style.RPC)
public class FrontendImpl implements FrontendInterface {

        public static final String sequencerIP = Config.SEQUENCER_IP;
        public static final int sequencerPort = Config.SEQUENCE_PORT;

        private int responseCounter = 0;
        private List<String> responses = new ArrayList<>();
        private int timeout = 10000;

        private int replica1Incorrect = 0;
        private int replica2Incorrect = 0;
        private int replica3Incorrect = 0;
        private int replica4Incorrect = 0;

        public int getTimeout() {
                return timeout;
        }

        public void setTimeout(int timeout) {
                this.timeout = timeout;
        }

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
                startTimer(10000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout " + totalResponses);
                return compareResultsAndSendFinalResult();
        }

        @Override
        public String removeMovieSlots(String customerID, String movieID, String movieName) {
                RequestData requestData = new RequestData("removeMovieSlots", customerID, movieID, movieName, null,
                                null, 0);
                sendRequestToSequencer(requestData);
                boolean timerOver = false;
                startTimer(5000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout " + totalResponses);
                return compareResultsAndSendFinalResult();
        }

        @Override
        public String listMovieShowsAvailability(String customerID, String movieName, boolean isOwnClient) {
                RequestData requestData = new RequestData("listMovieShowsAvailability", customerID, null, movieName,
                                null,
                                null, 0);
                sendRequestToSequencer(requestData);
                boolean timerOver = false;
                startTimer(5000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout " + totalResponses);
                return compareResultsAndSendFinalResult();
        }

        @Override
        public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets,
                        boolean isOwnClient) {
                RequestData requestData = new RequestData("bookMovieTickets", customerID, movieID, movieName, null,
                                null, numberOfTickets);
                sendRequestToSequencer(requestData);
                boolean timerOver = false;
                startTimer(5000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout " + totalResponses);
                return compareResultsAndSendFinalResult();
        }

        @Override
        public String getBookingSchedule(String customerID, boolean isOwnClient) {
                RequestData requestData = new RequestData("getBookingSchedule", customerID, null, null, null, null, 0);
                sendRequestToSequencer(requestData);
                boolean timerOver = false;
                startTimer(5000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout " + totalResponses);
                return compareResultsAndSendFinalResult();
        }

        @Override
        public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
                RequestData requestData = new RequestData("cancelMovieTickets", customerID, movieID, movieName, null,
                                null, numberOfTickets);
                sendRequestToSequencer(requestData);
                boolean timerOver = false;
                startTimer(5000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout " + totalResponses);
                return compareResultsAndSendFinalResult();
        }

        @Override
        public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID,
                        String newMovieName, int numberOfTickets) {
                RequestData requestData = new RequestData("exchangeTickets", customerID, movieID, oldMovieName,
                                newMovieID, newMovieName, numberOfTickets);
                sendRequestToSequencer(requestData);
                boolean timerOver = false;
                startTimer(5000, timerOver);
                System.out.println("After timer!!!");
                int totalResponses = getResponseCounter();
                System.out.println("Responses total that we got after timeout " + totalResponses);
                return compareResultsAndSendFinalResult();
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
                String finalResult = "";

                String response1 = "";
                String response2 = "";
                String response3 = "";
                String response4 = "";
                int sequenceID1 = -1;
                int sequenceID2 = -1;
                int sequenceID3 = -1;
                int sequenceID4 = -1;

                for (String rep : responses) {
                        if (rep.split(",")[1].equals("RM1")) {
                                response1 = rep.split(",")[0];
                                sequenceID1 = Integer.parseInt(rep.split(",")[2]);
                        } else if (rep.split(",")[1].equals("RM2")) {
                                response2 = rep.split(",")[0];
                                sequenceID2 = Integer.parseInt(rep.split(",")[2]);
                        } else if (rep.split(",")[1].equals("RM3")) {
                                response3 = rep.split(",")[0];
                                sequenceID3 = Integer.parseInt(rep.split(",")[2]);
                        } else if (rep.split(",")[1].equals("RM4")) {
                                response4 = rep.split(",")[0];
                                sequenceID4 = Integer.parseInt(rep.split(",")[2]);
                        }
                }

                if (!response2.equals("Success")) {
                        replica2Incorrect += 1;
                        if (replica2Incorrect == 3) {
                                sendErrorMessage("Software Failure", Config.RM2_PORT_FE, Config.RM2_IP);
                                System.out.println("Replica 2 gave incorrect answer");
                                finalResult = response1;
                        }

                }

                switch (responses.size()) {

                        case 4:
                                System.out.println("Got responses from all replicas!!");

                                if (response1.equals(response2)) {
                                        if (response2.equals(response3)) {
                                                if (response3.equals(response4)) {
                                                        finalResult = response1;
                                                } else {
                                                        // Replica 4 is incorrect
                                                        System.out.println("Replica 4 gave incorrect answer");
                                                        finalResult = response3;
                                                        int RMPort = Config.RM4_PORT_FE;
                                                        String RMIP = Config.RM4_IP;
                                                        sendErrorMessage("Software Failure", RMPort, RMIP);
                                                }
                                        } else {
                                                if (response2.equals(response4)) {
                                                        // REplica 3 is wrong
                                                        System.out.println("Replica 3 gave incorrect answer");
                                                        finalResult = response2;
                                                        int RMPort = Config.RM3_PORT_FE;
                                                        String RMIP = Config.RM3_IP;
                                                        sendErrorMessage("Software Failure", RMPort, RMIP);
                                                }
                                        }
                                } else {
                                        if (response3.equals(response1)) {
                                                // REsponse 2 is wrong
                                                System.out.println("Replica 2 gave incorrect answer");
                                                finalResult = response3;
                                                int RMPort = Config.RM2_PORT_FE;
                                                String RMIP = Config.RM2_IP;
                                                // sendErrorMessage("Software Failure", RMPort, RMIP);
                                        } else {
                                                // Response 1 is wrong
                                                System.out.println("Replica 1 gave incorrect answer");
                                                finalResult = response2;
                                                int RMPort = Config.RM1_PORT_FE;
                                                String RMIP = Config.RM1_IP;
                                                sendErrorMessage("Software Failure", RMPort, RMIP);
                                        }
                                }
                                break;
                        default:
                                System.out.println("Crash Failure");
                                if (response1.equals("")) {
                                        System.out.println("Replica 1 has crashed");
                                        finalResult = response2;
                                        sendErrorMessage("Crash Failure", Config.RM1_PORT_FE, Config.RM1_IP);
                                }
                                if (response2.equals("")) {
                                        System.out.println("Replica 2 hsa crashed");
                                        finalResult = response1;
                                        sendErrorMessage("Crash Failure", Config.RM2_PORT_FE, Config.RM2_IP);
                                }
                                if (response3.equals("")) {
                                        System.out.println("Replica 3 has crashed");
                                        finalResult = response1;
                                        sendErrorMessage("Crash Failure", Config.RM3_PORT_FE, Config.RM3_IP);
                                }
                                if (response4.equals("")) {
                                        System.out.println("Replica 4 has crashed");
                                        finalResult = response1;
                                        sendErrorMessage("Crash Failure", Config.RM4_PORT_FE, Config.RM4_IP);
                                }
                                break;
                }

                // Resetting the count
                setResponseCounter(0);
                responses.clear();

                return finalResult;
        }

        private void sendErrorMessage(String errorMessage, Integer replicaPort, String replicaIP) {
                System.out.println("Sending error message to RM-- " + replicaPort);
                DatagramSocket aSocket = null;
                try {
                        aSocket = new DatagramSocket(2234);
                        byte[] message = errorMessage.getBytes();
                        InetAddress aHost = InetAddress.getByName(replicaIP);
                        DatagramPacket errorResponseToRM = new DatagramPacket(message, errorMessage.length(), aHost,
                                        replicaPort);

                        aSocket.send(errorResponseToRM);

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
