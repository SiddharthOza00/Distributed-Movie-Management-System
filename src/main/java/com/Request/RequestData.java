package com.Request;

public class RequestData {
    private String method;
    private String customerID;
    private String movieID;
    private String movieName;
    private String newMovieID;
    private String newMovieName;
    private Integer numberOfTickets;
    private Integer sequenceId;
    
    public RequestData() {
    }

    public RequestData(String method, String customerID, String movieID, String movieName,
            String newMovieID, String newMovieName, int numberOfTickets) {
        this.method = method;
        this.customerID = customerID;
        this.movieID = movieID;
        this.movieName = movieName;
        this.newMovieID = newMovieID;
        this.newMovieName = newMovieName;
        this.numberOfTickets = numberOfTickets;
    }
    

    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getCustomerID() {
        return customerID;
    }
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
    public String getMovieID() {
        return movieID;
    }
    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }
    public String getMovieName() {
        return movieName;
    }
    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
    public String getnewMovieID() {
        return newMovieID;
    }
    public void setnewMovieID(String newMovieID) {
        this.newMovieID = newMovieID;
    }
    public String getnewMovieName() {
        return newMovieName;
    }
    public void setnewMovieName(String newMovieName) {
        this.newMovieName = newMovieName;
    }
    public int getnumberOfTickets() {
        return numberOfTickets;
    }
    public void setnumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }
    public int getSequenceId() {
        return sequenceId;
    }
    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }
    
    @Override
    public String toString() {
        return getMethod()+","+getCustomerID()+","+getMovieID()+","+getMovieName()+","+getnewMovieID()+","+getnewMovieName()+","+getnumberOfTickets()+","+getSequenceId();
    }

}
