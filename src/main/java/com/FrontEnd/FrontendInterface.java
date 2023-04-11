package com.FrontEnd;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import com.Request.RequestData;

@WebService
@SOAPBinding(style=Style.RPC)
public interface FrontendInterface {

    String addMovieSlots(String customerID, String movieID, String movieName, int bookingCapacity);
    String removeMovieSlots(String customerID, String movieID, String movieName);
    String listMovieShowsAvailability(String customerID, String movieName, boolean isOwnClient);
    String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets, boolean isOwnClient);
    String getBookingSchedule(String customerID, boolean isOwnClient);
    String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets);
    String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID, String newMovieName, int numberOfTickets);
    void sendRequestToSequencer(RequestData requestData);
}