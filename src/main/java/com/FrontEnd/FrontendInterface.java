package com.FrontEnd;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import com.Request.RequestData;

@WebService
@SOAPBinding(style=Style.RPC)
public interface FrontendInterface {

    public String addMovieSlots(String customerID, String movieID, String movieName, int bookingCapacity);
    public String removeMovieSlots(String customerID, String movieID, String movieName);
    public String listMovieShowsAvailability(String customerID, String movieName, boolean isOwnClient);
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets, boolean isOwnClient);
    public String getBookingSchedule(String customerID, boolean isOwnClient);
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets);
    public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID, String newMovieName, int numberOfTickets);
    public void sendRequestToSequencer(RequestData requestData);
}