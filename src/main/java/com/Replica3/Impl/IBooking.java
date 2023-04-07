package com.Replica3.Impl;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)

public interface IBooking {

    public String addMovieSlots(String movieID, String movieName, int bookingCapacity);

    public String removeMovieSlots(String movieID, String movieName);

    public String listMovieShowsAvailability(String movieName);

    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets);

    public String getBookingSchedule(String customerID);

    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets);

    public String exchangeTickets(String customerID, String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets);

}
