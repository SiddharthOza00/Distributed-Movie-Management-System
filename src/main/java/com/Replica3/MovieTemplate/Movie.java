package com.Replica3.MovieTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Movie {
    String movieID;
    String movieName;
    int movieCapacity;
    Map<String, Integer> bookedSlots;
    String date;
    String timeSlot;
    String serverName;

    int totBooked;

    public Movie(String movieName, String movieID, int movieCapacity) {
        this.movieName = movieName;
        this.movieID = movieID;
        this.movieCapacity = movieCapacity;

        bookedSlots = new ConcurrentHashMap<>();

        this.date = separateDate(movieID);
        this.timeSlot = separateSlot(movieID);
        this.serverName = separateArea(movieID);
    }

    @Override
    public String toString() {
        return " ID: " + getMovieID() + " during " + getTimeSlot() + " of date " + getDate() + " Total Capacity: " + getMovieCapacity() + " Remaining seats: " + getMovieRemainCapacity() + " ";
    }

    public String getMovieID() {
        return movieID;
    }

    public String getMovieName() {
        return movieName;
    }

    public int getMovieCapacity() {
        return movieCapacity;
    }

    public Map<String,Integer> getBookedSlots() {
        return bookedSlots;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getServerName() {
        return serverName;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public void setMovieCapacity(int movieCapacity) {
        this.movieCapacity = movieCapacity;
    }

    public void setBookedSlots(Map<String,Integer> bookedSlots) {
        this.bookedSlots = bookedSlots;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getMovieRemainCapacity() {
        // Checks for remaining capacity by iterating through the hashmap
        totBooked = 0;
        bookedSlots.forEach(
                (key, value)
                        -> totBooked = totBooked + value);
        return movieCapacity - totBooked;
    }

    public boolean hasCustomerBooked(String customerID) {
        // Checks if customer has booked or not
        if(bookedSlots.containsKey(customerID)) return true;

        return false;
    }

    public int addCustomerID(String newID, int numberOfTickets) {
        // adding new customer to the hashmap
        if (!isFilled() && !bookedSlots.containsKey(newID)) {
            bookedSlots.put(newID, numberOfTickets);
            return 1;
        }
        else if(bookedSlots.containsKey(newID)) {
            addMoreTickets(newID, numberOfTickets) ;
            return 1;
        }
        else {
            return -1;
        }
    }

    public void removeCustomerTickets(String customerID, int numberOfTickets) {
        //remove set amount of tickets from the hashmap
        bookedSlots.put(customerID, numberOfTickets);
    }

    public void removeCustomer(String customerID) {
        bookedSlots.remove(customerID);
    }

    private void addMoreTickets(String customerID, int numberOfTickets) {
        //Adds tickets to old booking
        int tickets = bookedSlots.get(customerID);
        tickets = tickets + numberOfTickets;
        bookedSlots.put(customerID, tickets);
    }

    public int currTickForCustomer(String customerID) {
        //get current amount of tickets
        return bookedSlots.get(customerID);
    }

    public static String separateDate(String movieID) {
        String returnDate = movieID.substring(4,6)+"/"+movieID.substring(6,8)+"/"+movieID.substring(8);
        return returnDate;
    }

    public static String separateSlot(String movieID) {
        //get which slot is booked
        String slot = movieID.substring(3,4);
        if(slot.equalsIgnoreCase("M")) return "Morning";
        else if(slot.equalsIgnoreCase("A")) return "Afternoon";
        else return "Evening";
    }

    public static String separateArea(String movieID) {
        //get server area of the booking
        String area = movieID.substring(0,3);
        if(area.equalsIgnoreCase("ATW")) return "Atwater";
        else if(area.equalsIgnoreCase("VER")) return "Verdun";
        else return "Outremont";
    }

    public static String detectServer(String movieID) {
        //get server name from movie ID
        if (movieID.substring(0, 3).equalsIgnoreCase("ATW")) {
            return "Atwater";
        } else if (movieID.substring(0, 3).equalsIgnoreCase("VER")) {
            return "Verdun";
        } else {
            return "Outremont";
        }
    }

    public boolean isFilled() {
        //to check if slots are filled or not
        if(getMovieRemainCapacity() == 0) return true;
        else return false;
    }
}
