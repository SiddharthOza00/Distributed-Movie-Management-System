package com.FrontEnd;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import com.Request.RequestData;

@WebService(endpointInterface = "com.webservices.FrontendInterface")
@SOAPBinding(style = Style.RPC)
public class FrontendImpl implements FrontendInterface {

    public FrontendImpl() {
        super();
    }

    @Override
    public String addMovieSlots(String customerID, String movieID, String movieName, int bookingCapacity) {
        RequestData requestData = new RequestData("addMovieSlots",customerID,movieID,movieName,null,null,bookingCapacity,0);
        FrontEnd frontend = new FrontEnd();
        // frontend
        return null;
    }

    @Override
    public String removeMovieSlots(String customerID, String movieID, String movieName) {
        RequestData requestData = new RequestData("removeMovieSlots",customerID,movieID,movieName,null,null,0,0);
        return null;
    }

    @Override
    public String listMovieShowsAvailability(String movieName, boolean isOwnClient) {
        RequestData requestData = new RequestData("listMovieShowsAvailability",null,null,movieName,null,null,0,0);
        return null;
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets,
            boolean isOwnClient) {
                RequestData requestData = new RequestData("bookMovieTickets",customerID,movieID,movieName,null,null,numberOfTickets,0);
                return null;
    }

    @Override
    public String getBookingSchedule(String customerID, boolean isOwnClient) {
        RequestData requestData = new RequestData("getBookingSchedule",customerID,null,null,null,null,0,0);
        return null;
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        RequestData requestData = new RequestData("cancelMovieTickets",customerID,movieID,movieName,null,null,numberOfTickets,0);
        return null;
    }

    @Override
    public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID,
            String newMovieName, int numberOfTickets) {
            RequestData requestData = new RequestData("exchangeTickets",customerID,movieID,oldMovieName,newMovieID,newMovieName,numberOfTickets,0);
            return null;
    }

    
    
}
