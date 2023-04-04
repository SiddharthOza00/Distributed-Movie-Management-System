package com.Replica1;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style=Style.RPC)
public interface Webservice {

    public boolean addMovieSlots(String movieID, String movieName, int bookingCapacity);
    public boolean removeMovieSlots(String movieID, String movieName);
    public String listMovieShowsAvailability(String movieName, boolean isOwnClient);
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets, boolean isOwnClient);
    public String getBookingSchedule(String customerID, boolean isOwnClient);
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets);
    public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID, String newMovieName, int numberOfTickets);
}