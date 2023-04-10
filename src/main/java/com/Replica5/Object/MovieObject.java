package com.Replica5.Object;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MovieObject {

    public static final int MOVIE_FULL = -1;
    public static final int ADD_SUCCESS = 1;
    private String movieName;
    private String movieID;
    private String movieServer;
    private int movieCapacity;
    private int remainingCapacity;
    private String movieDate;
    private String movieTimeSlots;
    private Map<String, Integer> registeredClients;

    public MovieObject(String movieName, String movieID, int numberOfTickets) {
        this.movieID = movieID;
        this.movieName = movieName;
        this.movieCapacity = numberOfTickets;
        this.movieTimeSlots = detectMovieTimeSlot(movieID);
        this.movieServer = detectMovieServer(movieID);
        this.movieDate = detectMovieDate(movieID);
        registeredClients = new ConcurrentHashMap<>();
        this.remainingCapacity=numberOfTickets;
    }

    public static String detectMovieServer(String movieID) {
        if (movieID.substring(0, 3).equalsIgnoreCase("ATW")) {
            return "Atwater";
        } else if (movieID.substring(0, 3).equalsIgnoreCase("VER")) {
            return "Verdun";
        } else {
            return "Outremont";
        }
    }

    public static String detectMovieTimeSlot(String movieID) {
        if (movieID.substring(3, 4).equalsIgnoreCase("M")) {
            return "Morning";
        } else if (movieID.substring(3, 4).equalsIgnoreCase("A")) {
            return "Afternoon";
        } else {
            return "Evening";
        }
    }

    public static String detectMovieDate(String movieID) {
        return movieID.substring(4, 6) + "/" + movieID.substring(6, 8) + "/20" + movieID.substring(8, 10);
    }

    public static boolean checkMovieData(String movieID) {
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String todayDate = date.substring(0,2);
        String thisMonth = date.substring(3,5);
        String thisYear = date.substring(8,10);
        if (Integer.parseInt(thisYear) <= Integer.parseInt(movieID.substring(8,10))) {
            if (Integer.parseInt(thisMonth) <= Integer.parseInt(movieID.substring(6,8))) {
                return Integer.parseInt(todayDate) < Integer.parseInt(movieID.substring(4, 6));
            }
        }
        return false;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getMovieServer() {
        return movieServer;
    }

    public void setMovieServer(String movieServer) {
        this.movieServer = movieServer;
    }

    public int getMovieCapacity() {
        return movieCapacity;
    }

    public void setMovieCapacity(int movieCapacity) {
        this.movieCapacity = movieCapacity;
    }

    public int getMovieRemainCapacity() {
        int totalBooked = 0;
        for(int i : registeredClients.values()){
            totalBooked += i;
        }
        return movieCapacity - totalBooked;
    }

    public String getMovieDate() {
        return movieDate;
    }

    public void setMovieDate(String movieDate) {
        this.movieDate = movieDate;
    }

    public String getMovieTimeSlots() {
        return movieTimeSlots;
    }

    public void setMovieTimeSlots(String movieTimeSlots) {
        this.movieTimeSlots = movieTimeSlots;
    }

    public boolean isFull(int numberOfTickets) {
        if(getMovieRemainCapacity() >= numberOfTickets)
            return false;
        else
            return true;
    }

    public Map<String, Integer> getRegisteredClientIDs() {
        return registeredClients;
    }

    public int setRegisteredClient(String registeredCustomerID, int numberOfTickets) {
        if(!isFull(numberOfTickets)){
            if(registeredClients.keySet().contains(registeredCustomerID)){
                return -1;
            }else{
                registeredClients.put(registeredCustomerID, numberOfTickets);
                this.remainingCapacity -=numberOfTickets;
                return 1;
            }
        }else{
            return 3;
        }
    }

    public int setRemoveRegisteredClient(String registeredCustomerID, int numberOfTickets) {
        if(!isFull(numberOfTickets)){
            if(registeredClients.keySet().contains(registeredCustomerID)){
                return -1;
            }else{
                registeredClients.put(registeredCustomerID, numberOfTickets);
                this.remainingCapacity +=numberOfTickets;
                return 1;
            }
        }else{
            return 3;
        }
    }

    public int addRegisteredClientID(String registeredClientID, int numberOfTickets) {
        if (getMovieRemainCapacity()>numberOfTickets) {
            if (registeredClients.containsKey(registeredClientID)) {
                return 0;
            } else {
                registeredClients.put(registeredClientID, numberOfTickets);
                return ADD_SUCCESS;
            }
        } else {
            return -1;
        }
    }

    public boolean removeRegisteredClientID(String registeredCustomerID){
        if(registeredClients.remove(registeredCustomerID) != null){
            return true;
        }else{
            System.out.println("Customer has not booked any tickets for this show.");
            return false;
        }
    }

    @Override
    public String toString() {
        return " (" + getMovieID() + ") in the " + getMovieTimeSlots() + " of " + getMovieDate() + " Total[Remaining] Capacity: " + getMovieCapacity() + "[" + getMovieRemainCapacity() + "]";
    }
}
