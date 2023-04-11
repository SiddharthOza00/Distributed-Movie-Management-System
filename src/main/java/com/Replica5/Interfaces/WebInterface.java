package com.Replica5.Interfaces;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WebInterface {

    String addMovieSlots(String movieID, String movieName, int bookingCapacity);

    String removeMovieSlots(String movieID, String movieName);

    String listMovieShowsAvailability(String movieName);

    String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets);

    String getBookingSchedule(String customerID);

    String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets);

    String exchangeTickets(String customerID, String newMovieID, String newMovieName, String oldMovieID, String oldMovieName, int numberOfTickets);

}
