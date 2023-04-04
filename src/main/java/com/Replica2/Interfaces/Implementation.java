package com.Replica2.Interfaces;

import com.Replica2.Logger.Logger;
import com.Replica2.Object.ClientObject;
import com.Replica2.Object.MovieObject;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebService(endpointInterface = "com.Replica2.Interfaces.WebInterface")

@SOAPBinding(style = SOAPBinding.Style.RPC)

public class Implementation implements WebInterface {

    public static final int Atwater_Server_Port = 1111;
    public static final int Verdun_Server_Port = 2222;
    public static final int Outremont_Server_Port = 3333;
    private final String serverID;
    private final String serverName;
    private final Map<String, Map<String, MovieObject>> allMovies;
    private final Map<String, Map<String, Map<String, Integer>>> clientMovies;

    private final Map<String, ClientObject> serverClients;

    public Implementation(String serverID, String serverName) {
        super();
        this.serverID = serverID;
        this.serverName = serverName;
        allMovies = new ConcurrentHashMap<>();
        allMovies.put("Avatar", new ConcurrentHashMap<>());
        allMovies.put("Avengers", new ConcurrentHashMap<>());
        allMovies.put("Titanic", new ConcurrentHashMap<>());
        clientMovies = new ConcurrentHashMap<>();
        serverClients = new ConcurrentHashMap<>();
    }

    private static int getServerPort(String branchID) {
        if (branchID.equalsIgnoreCase("ATW")) {
            return Atwater_Server_Port;
        } else if (branchID.equalsIgnoreCase("OUT")) {
            return Outremont_Server_Port;
        } else if (branchID.equalsIgnoreCase("VER")) {
            return Verdun_Server_Port;
        }
        return 1;
    }

    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        String response;
        if (isMovieOfThisServer(movieID)) {
            if (MovieExists(movieName, movieID)) {
                if (allMovies.get(movieName).get(movieID).getMovieCapacity() <= bookingCapacity) {
                    allMovies.get(movieName).get(movieID).setMovieCapacity(bookingCapacity);
                    response = "Success: Movie " + movieID + " Capacity increased to " + bookingCapacity;
                    try {
                        Logger.serverLog(serverID, "null", " addMovieSlot ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    response = "Failed: MovieAlready Exists, Cannot Decrease Booking Capacity";
                    try {
                        Logger.serverLog(serverID, "null", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            } else {
                if (MovieObject.checkMovieData(movieID)) {
                    MovieObject Movie = new MovieObject(movieName, movieID, bookingCapacity);
                    Map<String, MovieObject> MovieHashMap = allMovies.get(movieName);
                    MovieHashMap.put(movieID, Movie);
                    allMovies.put(movieName, MovieHashMap);
                    response = "Success: Movie " + movieID + " added successfully";
                    try {
                        Logger.serverLog(serverID, "null", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
                response = "Failed: Cannot Add Movie as it has the wrong movie date";
                try {
                    Logger.serverLog(serverID, "null", " addMovieSlot ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity: " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            response = "Failed: Cannot Add Movie to servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        String response;
        if (isMovieOfThisServer(movieID)) {
            if (MovieExists(movieName, movieID)) {
                Map<String, Integer> registeredClients = allMovies.get(movieName).get(movieID).getRegisteredClientIDs();
                allMovies.get(movieName).remove(movieID);
                addCustomersToNextSameMovie(movieID, movieName, registeredClients);
                response = "Success: Movie " + movieID + " Removed Successfully";
                try {
                    Logger.serverLog(serverID, "null", " removeMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Movie " + movieID + " Does Not Exist";
                try {
                    Logger.serverLog(serverID, "null", " removeMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            response = "Failed: Cannot Remove Movie from servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " removeMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String listMovieShowsAvailability(String movieName) {
        String response;
        Map<String, MovieObject> movies = allMovies.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName).append(" Server ").append(movieName).append(":\n");
        if (movies.size() == 0) {
            builder.append("No Movies of Type ").append(movieName).append("\n");
        } else {
            for (MovieObject movie :
                    movies.values()) {
                builder.append(movie.toString()).append(" || ");
            }
        }
        builder.append("\n=====================================\n");
        String otherServer1, otherServer2;
        if (serverID.equals("ATW")) {
            otherServer1 = sendUDPMessage(Outremont_Server_Port, "listMovieShowsAvailability", "null", movieName, "null", 0);
            otherServer2 = sendUDPMessage(Verdun_Server_Port, "listMovieShowsAvailability", "null", movieName, "null", 0);
        } else if (serverID.equals("OUT")) {
            otherServer1 = sendUDPMessage(Verdun_Server_Port, "listMovieShowsAvailability", "null", movieName, "null", 0);
            otherServer2 = sendUDPMessage(Atwater_Server_Port, "listMovieShowsAvailability", "null", movieName, "null", 0);
        } else {
            otherServer1 = sendUDPMessage(Atwater_Server_Port, "listMovieShowsAvailability", "null", movieName, "null", 0);
            otherServer2 = sendUDPMessage(Outremont_Server_Port, "listMovieShowsAvailability", "null", movieName, "null", 0);
        }
        builder.append(otherServer1).append(otherServer2);
        response = builder.toString();
        try {
            Logger.serverLog(serverID, "null", " listMovieShowsAvailability ", " movieName: " + movieName + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }

    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String response;
        checkClientExists(customerID);
        if (isMovieOfThisServer(movieID)) {
            MovieObject bookedMovie = allMovies.get(movieName).get(movieID);
            if (bookedMovie == null) {
                response = "Failed: Movie " + movieID + " Does not exists";
                try {
                    Logger.serverLog(serverID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
            if (!bookedMovie.isFull(numberOfTickets)) {
                if (clientMovies.containsKey(customerID)) {
                    if (clientMovies.get(customerID).containsKey(movieName)) {
                        if (!clientHasMovie(customerID, movieName, movieID)) {
                            if (isCustomerOfThisServer(customerID))
                                clientMovies.get(customerID).get(movieName).put(movieID, numberOfTickets);
                        } else {
                            response = "Failed: Movie " + movieID + " Already Booked";
                            try {
                                Logger.serverLog(serverID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return response;
                        }
                    } else {
                        if (isCustomerOfThisServer(customerID))
                            addMovieTypeAndMovie(customerID, movieName, movieID, numberOfTickets);
                    }
                } else {
                    if (isCustomerOfThisServer(customerID))
                        addCustomerAndMovie(customerID, movieName, movieID, numberOfTickets);
                }
                if (allMovies.get(movieName).get(movieID).addRegisteredClientID(customerID, numberOfTickets) == MovieObject.ADD_SUCCESS) {
                    response = "Success: Movie " + movieID + " Booked Successfully";
                } else if (allMovies.get(movieName).get(movieID).addRegisteredClientID(customerID, numberOfTickets) == MovieObject.MOVIE_FULL) {
                    response = "Failed: Movie " + movieID + " is Full";
                } else {
                    response = "Failed: Cannot Add You To Movie " + movieID;
                }
                try {
                    Logger.serverLog(serverID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Movie " + movieID + " is Full";
                try {
                    Logger.serverLog(serverID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            if (clientHasMovie(customerID, movieName, movieID)) {
                String serverResponse = "Failed: Movie " + movieID + " Already Booked";
                try {
                    Logger.serverLog(serverID, customerID, " CORBA bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverResponse;
            }
            if (exceedWeeklyLimit(customerID, movieID.substring(4))) {
                String serverResponse = sendUDPMessage(getServerPort(movieID.substring(0, 3)), "bookMovieTickets", customerID, movieName, movieID, numberOfTickets);
                if (serverResponse.startsWith("Success:")) {
                    if (clientMovies.get(customerID).containsKey(movieName)) {
                        clientMovies.get(customerID).get(movieName).put(movieID, numberOfTickets);
                    } else {
                        Map<String, Integer> temp = new ConcurrentHashMap<>();
                        temp.put(movieID, numberOfTickets);
                        clientMovies.get(customerID).put(movieName, temp);
                    }
                }
                try {
                    Logger.serverLog(serverID, customerID, " CORBA bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverResponse;
            } else {
                response = "Failed: You Cannot Book Movie in Other Servers For This Week(Max Weekly Limit = 3)";
                try {
                    Logger.serverLog(serverID, customerID, " CORBA bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    @Override
    public String getBookingSchedule(String customerID) {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        Map<String, Map<String, Integer>> movies = clientMovies.get(customerID);
        if (movies.size() == 0) {
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        StringBuilder builder = new StringBuilder();
        for (String movieName : movies.keySet()) {
            builder.append(movieName).append(":\n");
            for (String movieID : movies.get(movieName).keySet()) {
                builder.append(movieID).append(" - ").append(movies.get(movieName).get(movieID)).append(" Tickets ||");
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
        try {
            Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String response;
        if (isMovieOfThisServer(movieID)) {
            if (isCustomerOfThisServer(customerID)) {
                if (!checkClientExists(customerID)) {
                    response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    if (removeMovieIfExists(customerID, movieName, movieID, numberOfTickets)) {
                        allMovies.get(movieName).get(movieID).removeRegisteredClientID(customerID);
                        response = "Success: Movie " + movieID + " Canceled for " + customerID;
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    } else {
                        response = "Failed: You " + customerID + " Are Not Registered or you are booked for less tickets in " + movieID;
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            } else {
                if (allMovies.get(movieName).get(movieID).removeRegisteredClientID(customerID)) {
                    response = "Success: Movie " + movieID + " Canceled for " + customerID;
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            }
        } else {
            if (isCustomerOfThisServer(customerID)) {
                if (checkClientExists(customerID)) {
                    if (removeMovieIfExists(customerID, movieName, movieID, numberOfTickets)) {
                        response = sendUDPMessage(getServerPort(movieID.substring(0, 3)), "cancelMovieTickets", customerID, movieName, movieID, numberOfTickets);
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            }
            response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    public String exchangeTickets(String customerID, String newMovieID, String newMovieName, String oldMovieID, String oldMovieName, int numberOfTickets) {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + oldMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        } else {
            if (clientHasMovie(customerID, oldMovieName, oldMovieID)) {
                String bookResp = "Failed: did not send book request for your newMovie " + newMovieID;
                String cancelResp = "Failed: did not send cancel request for your oldMovie " + oldMovieID;
                synchronized (this) {
                    if (onTheSameWeek(newMovieID.substring(4), oldMovieID) && !exceedWeeklyLimit(customerID, newMovieID.substring(4))) {
                        cancelResp = cancelMovieTickets(customerID, oldMovieID, oldMovieName, numberOfTickets);
                        if (cancelResp.startsWith("Success:")) {
                            bookResp = bookMovieTickets(customerID, newMovieID, newMovieName, numberOfTickets);
                        }
                    } else {
                        bookResp = bookMovieTickets(customerID, newMovieID, newMovieName, numberOfTickets);
                        if (bookResp.startsWith("Success:")) {
                            cancelResp = cancelMovieTickets(customerID, oldMovieID, oldMovieName, numberOfTickets);
                        }
                    }
                }
                if (bookResp.startsWith("Success:") && cancelResp.startsWith("Success:")) {
                    response = "Success: Movie " + oldMovieID + " exchanged with " + newMovieID;
                } else if (bookResp.startsWith("Success:") && cancelResp.startsWith("Failed:")) {
                    cancelMovieTickets(customerID, newMovieID, newMovieName, numberOfTickets);
                    response = "Failed: Your oldMovie " + oldMovieID + " Could not be Canceled reason: " + cancelResp;
                } else if (bookResp.startsWith("Failed:") && cancelResp.startsWith("Success:")) {
                    String resp1 = bookMovieTickets(customerID, oldMovieID, oldMovieName, numberOfTickets);
                    response = "Failed: Your newMovie " + newMovieID + " Could not be Booked reason: " + bookResp + " And your old Movie Rolling back: " + resp1;
                } else {
                    response = "Failed: on newMovie " + newMovieID + " Booking reason: " + bookResp + " and oldMovie " + oldMovieID + " Canceling reason: " + cancelResp;
                }
                try {
                    Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + oldMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
                try {
                    Logger.serverLog(serverID, customerID, " CORBA exchangeTicket ", " oldMovieID: " + oldMovieID + " oldMovieName: " + oldMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    public String listMovieShowsAvailabilityUDP(String movieName) {
        Map<String, MovieObject> movies = allMovies.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName).append(" Server ").append(movieName).append(":\n");
        if (movies.size() == 0) {
            builder.append("No Movies of Type ").append(movieName);
        } else {
            for (MovieObject movie :
                    movies.values()) {
                builder.append(movie.toString()).append(" || ");
            }
        }
        builder.append("\n=====================================\n");
        return builder.toString();
    }

    private String sendUDPMessage(int serverPort, String method, String customerID, String movieName, String movieID, int numberOfTickets) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieName + ";" + movieID + ";" + (numberOfTickets);
        try {
            Logger.serverLog(serverID, customerID, " UDP request sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        try {
            Logger.serverLog(serverID, customerID, " UDP reply received" + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getNextSameMovie(Set<String> keySet, String movieName, String oldMovieID, int numberOfTickets) {
        List<String> sortedIDs = new ArrayList<>(keySet);
        sortedIDs.add(oldMovieID);
        sortedIDs.sort((ID1, ID2) -> {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                int timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
        });
        int index = sortedIDs.indexOf(oldMovieID) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!allMovies.get(movieName).get(sortedIDs.get(i)).isFull(numberOfTickets)) {
                return sortedIDs.get(i);
            }
        }
        return "Failed";
    }

    private boolean exceedWeeklyLimit(String customerID, String movieDate) {
        int limit = 0;
        for (int i = 0; i < 3; i++) {
            List<String> registeredIDs = new ArrayList<>();
            switch (i) {
                case 0:
                    if (clientMovies.get(customerID).containsKey("Avatar")) {
                        registeredIDs = new ArrayList<>(clientMovies.get(customerID).get("Avatar").keySet());
                    }
                    break;
                case 1:
                    if (clientMovies.get(customerID).containsKey("Avengers")) {
                        registeredIDs = new ArrayList<>(clientMovies.get(customerID).get("Avengers").keySet());
                    }
                    break;
                case 2:
                    if (clientMovies.get(customerID).containsKey("Titanic")) {
                        registeredIDs = new ArrayList<>(clientMovies.get(customerID).get("Titanic").keySet());
                    }
                    break;
            }
            for (String movieID :
                    registeredIDs) {
                if (onTheSameWeek(movieDate, movieID) && !isMovieOfThisServer(movieID)) {
                    limit++;
                }
                if (limit == 3)
                    return false;
            }
        }
        return true;
    }

    private void addCustomersToNextSameMovie(String oldMovieID, String movieName, Map<String, Integer> registeredClients) {
        String response;
        for (String customerID : registeredClients.keySet()) {
            if (customerID.substring(0, 3).equals(serverID)) {
                removeMovieIfExists(customerID, movieName, oldMovieID, registeredClients.get(customerID));
                String nextSameMovieResult = getNextSameMovie(allMovies.get(movieName).keySet(), movieName, oldMovieID, registeredClients.get(customerID));
                if (nextSameMovieResult.equals("Failed")) {
                    response = "Acquiring nextSameMovie :" + nextSameMovieResult;
                    try {
                        Logger.serverLog(serverID, customerID, " addCustomersToNextSameMovie ", " oldMovieID: " + oldMovieID + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                } else {
                    bookMovieTickets(customerID, nextSameMovieResult, movieName, registeredClients.get(customerID));
                }
            } else {
                sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeMovieSlot", customerID, movieName, oldMovieID, registeredClients.get(customerID));
            }
        }

    }

    private synchronized boolean MovieExists(String movieName, String movieID) {
        return allMovies.get(movieName).containsKey(movieID);
    }

    private synchronized boolean isMovieOfThisServer(String movieID) {
        return MovieObject.detectMovieServer(movieID).equals(serverName);
    }

    private synchronized boolean checkClientExists(String customerID) {
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
            return false;
        } else {
            return true;
        }
    }

    private synchronized boolean clientHasMovie(String customerID, String movieName, String movieID) {
        if (clientMovies.get(customerID).containsKey(movieName)) {
            return clientMovies.get(customerID).get(movieName).containsKey(movieID);
        } else {
            return false;
        }
    }

    private boolean removeMovieIfExists(String customerID, String movieName, String movieID, int numberOfTickets) {
        if (clientMovies.get(customerID).containsKey(movieName)) {
            if (clientMovies.get(customerID).get(movieName).get(movieID) == numberOfTickets) {
                return clientMovies.get(customerID).get(movieName).get(movieID).equals(clientMovies.get(customerID).get(movieName).remove(movieID));
            } else if (clientMovies.get(customerID).get(movieName).get(movieID) > numberOfTickets) {
                int temp = clientMovies.get(customerID).get(movieName).get(movieID);
                clientMovies.get(customerID).get(movieName).put(movieID, temp - numberOfTickets);
                return true;
            }
        } else {
            return false;
        }
        return false;
    }



    private synchronized void addCustomerAndMovie(String customerID, String movieName, String movieID, int numberOfTickets) {
        Map<String, Map<String, Integer>> temp = new ConcurrentHashMap<>();
        Map<String, Integer> temp2 = new ConcurrentHashMap<>();
        temp2.put(movieID, numberOfTickets);
        temp.put(movieName, temp2);
        clientMovies.put(customerID, temp);
    }

    private synchronized void addMovieTypeAndMovie(String customerID, String movieName, String movieID, int numberOfTickets) {
        Map<String, Integer> temp = new ConcurrentHashMap<>();
        temp.put(movieID, numberOfTickets);
        clientMovies.get(customerID).put(movieName, temp);
    }

    private boolean isCustomerOfThisServer(String customerID) {
        return customerID.substring(0, 3).equals(serverID);
    }

    private boolean onTheSameWeek(String newMovieDate, String movieID) {
        if (movieID.substring(6, 8).equals(newMovieDate.substring(2, 4)) && movieID.substring(8, 10).equals(newMovieDate.substring(4, 6))) {
            int week1 = Integer.parseInt(movieID.substring(4, 6)) / 7;
            int week2 = Integer.parseInt(newMovieDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(day2 - day1);
            return week1 == week2;
        } else {
            return false;
        }
    }

    
    public void addNewCustomerToClients(String customerID) {
        ClientObject newCustomer = new ClientObject(customerID);
        serverClients.put(newCustomer.getClientID(), newCustomer);
        clientMovies.put(newCustomer.getClientID(), new ConcurrentHashMap<>());
    }
}
