package com.Replica4.Server;


import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
public interface WebInterface {

    @WebMethod
    public String addMovieSlots(String movieId, String movieName, int bookingCapacity);
    @WebMethod
    public String removeMovieSlots(String movieId, String movieName);
    @WebMethod
    public String listMovieShowsAvailability(String movieName);
    @WebMethod
    public String bookMoviesTickets(String customerId, String movieId, String movieName, int numberOfTickets);
    @WebMethod
    public String getBookingSchedule(String customerId);
    @WebMethod
    public String cancelMovieTickets(String customerId, String movieId, String movieName, int numberOfTickets);
    @WebMethod
    public String exchangeTickets(String customerID, String movieID, String old_movieName,String new_movieID, String new_movieName, int numberOfTickets);


}


