package com.Replica3.Impl;

import java.io.IOException;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.Replica3.MovieTemplate.Movie;
import com.Replica3.LoggerClass.LogData;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService(endpointInterface = "Impl.IBooking")
@SOAPBinding(style = Style.RPC)

public class BookingImpl implements IBooking {

    Map<String, Map<String, Movie>> movieMap;

    Map<String, Map<String, Map<String, Integer>>> customerBookingMap;


    String areaID;
    String areaName;

    //UDP ports
    public static final int portATW = 3344;
    public static final int portVER = 4455;
    public static final int portOUT = 5566;

    public BookingImpl(String areaID, String areaName) {
        movieMap = new ConcurrentHashMap<>();
        customerBookingMap = new ConcurrentHashMap<>();

        movieMap.put("Avatar", new ConcurrentHashMap<>());
        movieMap.put("Avengers", new ConcurrentHashMap<>());
        movieMap.put("Titanic", new ConcurrentHashMap<>());

        this.areaID = areaID;
        this.areaName = areaName;
    }
    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        // TODO Auto-generated method stub
        Date date = new Date();

        String strDateFormat = "dd-MM-yyyy";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String today = dateFormat.format(date);
        int currDay = Integer.parseInt(today.substring(0,2));
        int bookDay = Integer.parseInt(movieID.substring(4,6));

        if((bookDay < currDay) || ((bookDay-currDay) >7 )) {
            String serverReply = "Cant add slots!";
            try {
                LogData.serverLog(areaID, " ", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }

        //checking if the movie already exists or not
        if(movieMap.get(movieName).containsKey(movieID)) {

            //checking if old capacity is more than the new one
            if(movieMap.get(movieName).get(movieID).getMovieCapacity() > bookingCapacity) {
                String serverReply = "New booking capacity is less than the set capacity. Operation not done!";
                try {
                    LogData.serverLog(areaID, "null", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
            //this block is used to increase movie capacity
            else {
                movieMap.get(movieName).get(movieID).setMovieCapacity(bookingCapacity);
                String serverReply = "Successfully added more seats to the movie!";
                try {
                    LogData.serverLog(areaID, "null", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
        }
        else {

            if(Movie.separateArea(movieID).equalsIgnoreCase(areaName)){ //to check if movie is part of the the same server or not
                //adding a new Movie
                Map<String, Movie> tempMap = movieMap.get(movieName);
                tempMap.put(movieID, new Movie(movieName, movieID, bookingCapacity));
                String serverReply = "Added a new movie!";
                movieMap.put(movieName, tempMap);
                try {
                    LogData.serverLog(areaID, "null", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
            else {
                String serverReply = "Cannot add a movie in other servers";
                try {
                    LogData.serverLog(areaID, "null", " addMovieSlots ", " movieID: " + movieID + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
        }
    }
    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        // TODO Auto-generated method stub
        String serverReply;
        //checking if server is same or different
        if (Movie.detectServer(movieID).equals(areaName)) {
            //checking if booking is there or not
            if (movieMap.get(movieName).containsKey(movieID)) {
                //getting the list of booked customer IDs
                Map<String, Integer> bookedSlots = movieMap.get(movieName).get(movieID).getBookedSlots();
                movieMap.get(movieName).remove(movieID);
                removeCustomers(movieID, movieName, bookedSlots);
                serverReply = "Congrats ! Movie slots were removed";
                try {
                    LogData.serverLog(areaID, " ", " removeMovieSlots ", " movieID: " + movieID + " movieName: " + movieName +  " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
            else {
                serverReply = "Sorry ! Movie Does Not Exist";
                try {
                    LogData.serverLog(areaID, " ", " removeMovieSlots ", " movieID: " + movieID + " movieName: " + movieName +  " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
        } else {
            serverReply = "Sorry ! you cannot remove slots from other servers";
            try {
                LogData.serverLog(areaID, " ", " removeMovieSlots "," movieID: " + movieID + " movieName: " + movieName +  " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }
    }
    @Override
    public String listMovieShowsAvailability(String movieName) {
        // TODO Auto-generated method stub
        Map<String, Movie> movies = movieMap.get(movieName);
        StringBuilder sb = new StringBuilder();
        String server2, server3;

        //for movies from current server
        sb.append(areaName + " Server " + movieName + ":\n");
        if (movies.size() == 0) {
            sb.append("No movie show called " + movieName + "\n");
        } else {
            for (Movie movie :
                    movies.values()) {
                sb.append(movie.toString() + " ### ");
            }
            sb.append("\n ------------------------------ \n");
        }

        //for movies from other servers:
        if (areaID.equals("ATW")) {
            server2 = udpRequest(portVER, "listMovieShowsAvailability", "null", movieName, "null", 0);
            server3 = udpRequest(portOUT, "listMovieShowsAvailability", "null", movieName, "null", 0);
        }
        else if (areaID.equals("VER")) {
            server2 = udpRequest(portOUT, "listMovieShowsAvailability", "null", movieName, "null", 0);
            server3 = udpRequest(portATW, "listMovieShowsAvailability", "null", movieName, "null", 0);
        }
        else {
            server2 = udpRequest(portATW, "listMovieShowsAvailability", "null", movieName, "null", 0);
            server3 = udpRequest(portVER, "listMovieShowsAvailability", "null", movieName, "null", 0);
        }
        sb.append(server2).append(server3);
        String serverReply = "";
        serverReply = sb.toString();
        try {
            LogData.serverLog(areaID, "null", " listMovieShowsAvailability ", " movieName: " + movieName + " ", serverReply);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverReply;
    }
    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        // TODO Auto-generated method stub
        if(!customerBookingMap.containsKey(customerID)) {
            Map<String, Map<String, Integer>> temp = new ConcurrentHashMap<>();
            customerBookingMap.put(customerID, temp);
        }
        String serverReply;
        if (Movie.detectServer(movieID).equalsIgnoreCase(areaName)) {
            if(movieMap.get(movieName).get(movieID) == null) {
                serverReply = "Sorry ! Movie" + movieID + " doesn't exist";
                try {
                    LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
            Movie newBooking = movieMap.get(movieName).get(movieID);
            if (!newBooking.isFilled()) {
                //if customer is already there
                if(customerBookingMap.containsKey(customerID)) {
                    //if customer has already booked movie with same name
                    if (customerBookingMap.get(customerID).containsKey(movieName)) {
                        //if customer has already booked the movie ID
                        if(customerBookingMap.get(customerID).get(movieName).containsKey(movieID)) {
                            //checking if number of tickets is less or not
                            if(newBooking.getMovieRemainCapacity() > numberOfTickets) {
                                int currTick = newBooking.currTickForCustomer(customerID);
                                currTick = currTick + numberOfTickets;
                                customerBookingMap.get(customerID).get(movieName).put(movieID, currTick);
                            }
                            else {
                                serverReply = "Sorry ! Movie" + movieID + " doesnt have enough capacity ";
                                try {
                                    LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return serverReply;
                            }
                        }
                        else {
                            if(newBooking.getMovieRemainCapacity() > numberOfTickets) {
                                customerBookingMap.get(customerID).get(movieName).put(movieID, numberOfTickets);
                            }
                            else {
                                serverReply = "Sorry ! Movie" + movieID + " doesnt have enough capacity ";
                                try {
                                    LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return serverReply;
                            }
                        }
                    }
                    else {
                        if(newBooking.getMovieRemainCapacity() > numberOfTickets) {
                            Map<String, Integer> temp = new ConcurrentHashMap<>();
                            temp.put(movieID, numberOfTickets);
                            customerBookingMap.get(customerID).put(movieName, temp);
                        }
                        else {
                            serverReply = "Sorry ! Movie" + movieID + " doesnt have enough capacity ";
                            try {
                                LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return serverReply;
                        }
                    }
                }
                else {
                    if(newBooking.getMovieRemainCapacity() > numberOfTickets) {
                        Map<String, Map<String, Integer>> temp = new ConcurrentHashMap<>();
                        Map<String, Integer> temp2 = new ConcurrentHashMap<>();
                        temp2.put(movieID, numberOfTickets);
                        temp.put(movieName, temp2);
                        customerBookingMap.put(customerID, temp);
                    }
                    else {
                        serverReply = "Sorry ! Movie" + movieID + " doesnt have enough capacity ";
                        try {
                            LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return serverReply;
                    }
                }
                //checking if the customer is added to the Movie Class object or not
                if (movieMap.get(movieName).get(movieID).addCustomerID(customerID, numberOfTickets) == 1) {
                    serverReply = "Congrats ! Movie " + movieID + " Booked Successfully";
                }
                else {
                    serverReply = "Sorry Movie show " + movieID + " is Full";
                }
                try {
                    LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " " + numberOfTickets, serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
            else {
                serverReply = "Sorry ! Movie show " + movieID + " is Full";
                try {
                    LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
        }
        //for bookings in another server
        else {
            //checking if customer has booked more than 3 times in the same week or not
            if (!checkIfLimitExceeds(customerID, movieID.substring(4))) {
                String reply = udpRequest(getServerPort(movieID.substring(0, 3)), "bookMovieTickets", customerID, movieName, movieID, numberOfTickets);
                if (reply.startsWith("Congrats")) {
                    if (customerBookingMap.get(customerID).containsKey(movieName)) {
                        customerBookingMap.get(customerID).get(movieName).put(movieID, numberOfTickets);
                    } else {
                        Map<String, Integer> toBeAdded = new ConcurrentHashMap<>();
                        toBeAdded.put(movieID, numberOfTickets);
                        customerBookingMap.get(customerID).put(movieName, toBeAdded);
                    }
                }
                try {
                    LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " " + numberOfTickets + " ", reply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return reply;
            } else {
                serverReply = "Sorry ! You cannot book more than 3 movies in other servers";
                try {
                    LogData.serverLog(areaID, customerID, " bookMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverReply;
            }
        }
    }
    @Override
    public String getBookingSchedule(String customerID) {
        // TODO Auto-generated method stub
        String serverReply;
        if (!customerBookingMap.containsKey(customerID)) {
            Map<String, Map<String, Integer>> temp = new ConcurrentHashMap<>();
            customerBookingMap.put(customerID, temp);
            serverReply = "No Bookings for ID : " + customerID;
            try {
                LogData.serverLog(areaID, customerID, " getBookingSchedule ", " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }
        //getting moive list from customer map
        Map<String, Map<String, Integer>> movieList = customerBookingMap.get(customerID);
        if(movieList.size() != 0) {
            StringBuilder sb = new StringBuilder();
            for (String name : movieList.keySet()) {
                sb.append(name + ":\n");
                for (String movieID : movieList.get(name).keySet()) {
                    sb.append(movieID + " Tickets: " + movieList.get(name).get(movieID) + "   ### ");
                }
                sb.append("\n");
                sb.append("------------------------------");
                sb.append("\n");
            }
            serverReply = sb.toString();
            try {
                LogData.serverLog(areaID, customerID, " getBookingSchedule ", " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }
        else {
            serverReply = "No Bookings for ID : " + customerID;
            try {
                LogData.serverLog(areaID, customerID, " getBookingSchedule ", " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }
    }
    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        // TODO Auto-generated method stub
        String serverReply;

        //checking if the server is same as movie ID or not
        if (Movie.detectServer(movieID).equals(areaName)) {
            if (customerID.substring(0, 3).equals(areaID)) {
                if (customerBookingMap.get(customerID).get(movieName).containsKey(movieID)) {

                    //canceling the amount of tickets entered by the user
                    if(movieMap.get(movieName).get(movieID).currTickForCustomer(customerID) >= numberOfTickets) {
                        int oldTick = movieMap.get(movieName).get(movieID).currTickForCustomer(customerID);
                        int currTick = oldTick - numberOfTickets;
                        System.out.println("Old tick: " + oldTick + " to be removed: " + numberOfTickets + " curr tick: " + currTick);
                        if(currTick == 0) {
                            customerBookingMap.get(customerID).get(movieName).remove(movieID);
                            movieMap.get(movieName).get(movieID).removeCustomer(customerID);
                        }
                        else {
                            customerBookingMap.get(customerID).get(movieName).put(movieID, currTick);
                            movieMap.get(movieName).get(movieID).removeCustomerTickets(customerID, currTick);
                        }

                        serverReply = "Congrats ! Movie tickets " + movieID + " canceled for " + customerID;

                        try {
                            LogData.serverLog(areaID, customerID, " cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " Tickets:" + numberOfTickets + " ", serverReply);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return serverReply;
                    }
                    else
                    {
                        serverReply = "Sorry ! We can't cancel the tickets";
                        try {
                            LogData.serverLog(areaID, customerID, " cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " Tickets:"+ numberOfTickets + " ", serverReply);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return serverReply;
                    }
                }
                else {
                    serverReply = "Sorry you " + customerID + " have not booked this movie " + movieID;
                    try {
                        LogData.serverLog(areaID, customerID, " cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " Tickets:"+ numberOfTickets + " ", serverReply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return serverReply;
                }
            }
            else
            {
                if(movieMap.get(movieName).get(movieID).hasCustomerBooked(customerID)) {
                    int currTick = movieMap.get(movieName).get(movieID).currTickForCustomer(customerID);
                    currTick = currTick - numberOfTickets;
                    movieMap.get(movieName).get(movieID).removeCustomerTickets(customerID, numberOfTickets);
                    serverReply = "Congrats ! Movie tickets " + movieID + " canceled for " + customerID;
                    try {
                        LogData.serverLog(areaID, customerID, " cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " Tickets:" + numberOfTickets + " ", serverReply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return serverReply;
                }
                else {
                    serverReply = "Sorry you " + customerID + " have not booked this movie " + movieID;
                    try {
                        LogData.serverLog(areaID, customerID, " cancelMovieTickets ", " movieID: " + movieID + " movieName: " + movieName + " Tickets:"+ numberOfTickets + " ", serverReply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return serverReply;
                }
            }
        }
        else {
            if (customerID.substring(0, 3).equals(areaID)) {
                if (customerBookingMap.get(customerID).get(movieName).containsKey(movieID)) {
                    int currTick = customerBookingMap.get(customerID).get(movieName).get(movieID);
                    currTick = currTick - numberOfTickets;
                    if(currTick == 0) {
                        customerBookingMap.get(customerID).get(movieName).remove(movieID);
                    } 
                    else if(currTick < 0) {
                        return "Sorry ! Failed " + areaID + areaName ;
                    }
                    else {
                        customerBookingMap.get(customerID).get(movieName).put(movieID, currTick);
                    }
                    return udpRequest(getServerPort(movieID.substring(0, 3)), "cancelMovieTickets", customerID, movieName, movieID, currTick);
                }
            }
            return "Sorry ! Failed " + areaID + areaName ;
        }
    }

    @Override
    public String exchangeTickets(String customerID, String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets) {
        // TODO Auto-generated method stub
        String serverReply = "";
        String isBookingSuccessful = "";
        String isCancelSuccessful = "";

        String bookedCheck = getBookingSchedule(customerID);
        if(!bookedCheck.contains(movieID)) {
            System.out.println(bookedCheck.contains(movieID));
            isCancelSuccessful = "Sorry not successful, you haven't booked the old movie";
            serverReply = serverReply + isBookingSuccessful + isCancelSuccessful;
            try {
                LogData.serverLog(areaID, customerID, " exchangeTickets ", " old_movieName: " + old_movieName + " movieID: " + movieID + " new_movieID: " + new_movieID + " new_movieName: " + new_movieName + " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }

        synchronized(this) {
            isBookingSuccessful = bookMovieTickets(customerID, new_movieID, new_movieName, numberOfTickets);
            System.out.println(isBookingSuccessful);
            if(isBookingSuccessful.startsWith("Congrats")) {
                isCancelSuccessful = cancelMovieTickets(customerID, movieID, old_movieName, numberOfTickets);
                System.out.println(isCancelSuccessful);
            }
        }

        if(isBookingSuccessful.startsWith("Congrats") && isCancelSuccessful.startsWith("Congrats")) {
            serverReply = "Congrats ! movieID: " + movieID + " swapped with " + new_movieID;
            try {
                LogData.serverLog(areaID, customerID, " exchangeTickets ", " old_movieName: " + old_movieName + " movieID: " + movieID + " new_movieID: " + new_movieID + " new_movieName: " + new_movieName + " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }
        else if(isCancelSuccessful.startsWith("Sorry")) {
            cancelMovieTickets(customerID, new_movieID, new_movieName, numberOfTickets);
            serverReply = "Sorry ! Cannot swap (Canceling tickets was not successful)";
            try {
                LogData.serverLog(areaID, customerID, " exchangeTickets ", " old_movieName: " + old_movieName + " movieID: " + movieID + " new_movieID: " + new_movieID + " new_movieName: " + new_movieName + " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }
        else {
            serverReply = "Sorry ! Cannot swap (Booking was not succesful)";
            try {
                LogData.serverLog(areaID, customerID, " exchangeTickets ", " old_movieName: " + old_movieName + " movieID: " + movieID + " new_movieID: " + new_movieID + " new_movieName: " + new_movieName + " ", serverReply);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverReply;
        }
    }

    private static int getServerPort(String branchChaString) {
        if (branchChaString.equalsIgnoreCase("ATW")) {
            return portATW;
        } else if (branchChaString.equalsIgnoreCase("VER")) {
            return portVER;
        } else if (branchChaString.equalsIgnoreCase("OUT")) {
            return portOUT;
        }
        return 1;
    }

    private String udpRequest(int port, String methodName, String customerID, String movieName, String movieID, int numOfTickets) {
        DatagramSocket aSocket = null;
        String answer = "";
//        System.out.println("Reached here");
        String dataToBeSent = methodName + "_" + customerID + "_" + movieName + "_" + movieID + "_" + numOfTickets;
        try {
            LogData.serverLog(areaID, customerID, " UDP to be sent -> " + methodName + " ", " movieID: " + movieID + " movieName: " + movieName + " " + numOfTickets + " ", " ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataToBeSent.getBytes();
            InetAddress aHost = InetAddress.getLocalHost();
            DatagramPacket request = new DatagramPacket(message, dataToBeSent.length(), aHost, port);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            answer = new String(reply.getData());
            // System.out.println("Answer = " + answer);
            String[] splitting = answer.split("_");
            answer = splitting[0];
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        try {
            LogData.serverLog(areaID, customerID, " UDP reply success -> " + methodName + " ", " movieID: " + movieID + " movieName: " + movieName + " ", answer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    public String removeMovieSlotsUDP(String movieID, String movieName, String customerID) {
        if (!customerBookingMap.containsKey(customerID))
        {
            return "Sorry ! You are not registsered in this movie";
        }
        else {
            if (customerBookingMap.get(customerID).get(movieName).containsKey(movieID)) {
                customerBookingMap.get(customerID).get(movieName).remove(movieID);
                return "Congrats! movie tickets were removed from schedule";
            } else {
                return "Sorry ! You are not registered in this movie";
            }
        }
    }

    public String listMovieShowsAvailabilityUDP(String movieName) {
        Map<String, Movie> movies = movieMap.get(movieName);
        StringBuilder sb = new StringBuilder();
        sb.append(areaName + " Server " + movieName + ":\n");
        if (movies.size() == 0) {
            sb.append("Can't find events " + movieName);
        } else {
            for (Movie movie :
                    movies.values()) {
                sb.append(movie.toString() + " || ");
            }
        }
        sb.append("\n ################################# \n");
        return sb.toString();
    }


    private void removeCustomers(String movieID, String movieName, Map<String, Integer> bookedSlots) {
        for(String customerID : bookedSlots.keySet()) {
            if (customerID.substring(0, 3).equals(areaID)) {
                customerBookingMap.get(customerID).get(movieName).remove(movieID);
            }
            else
            {
                udpRequest(getServerPort(customerID.substring(0, 3)), "removeMovieSlots", customerID, movieName, movieID, 0);
            }
        }
    }

    private boolean checkIfLimitExceeds(String customerID, String mDate) {
        int limit = 0;
        for (int i = 0; i < 3; i++) {
            List<String> fullListOfID = new ArrayList<>();
            switch (i) {
                case 0:
                    if (customerBookingMap.get(customerID).containsKey("Avatar")) {
                        for(String x : customerBookingMap.get(customerID).get("Avatar").keySet()) {
                            if(!Movie.detectServer(x).equals(areaName)) {
                                fullListOfID.add(x);
                            }
                        }
                    }
                    break;
                case 1:
                    if (customerBookingMap.get(customerID).containsKey("Avengers")) {
                        for(String x : customerBookingMap.get(customerID).get("Avengers").keySet()) {
                            if(!Movie.detectServer(x).equals(areaName)) {
                                fullListOfID.add(x);
                            }
                        }
                    }
                    break;
                case 2:
                    if (customerBookingMap.get(customerID).containsKey("Titanic")) {
                        for(String x : customerBookingMap.get(customerID).get("Titanic").keySet()) {
                            if(!Movie.detectServer(x).equals(areaName)) {
                                fullListOfID.add(x);
                            }
                        }
                    }
                    break;
            }
            for (String eachMovieID : fullListOfID) {
                if (eachMovieID.substring(6, 8).equals(mDate.substring(2, 4)) && eachMovieID.substring(8, 10).equals(mDate.substring(4, 6))) {
                    int checkWeekRegistered = Integer.parseInt(eachMovieID.substring(4, 6)) / 7;
                    int checkWeekCurrent = Integer.parseInt(mDate.substring(0, 2)) / 7;
                    if (checkWeekRegistered == checkWeekCurrent) {
                        limit++;
                        System.out.println(" - - - -- - -  " + limit + " - - - - - - - - ");
                    }
                }
                if (limit == 3) {
                    return true;
                }
            }
        }
        return false;
    }

}