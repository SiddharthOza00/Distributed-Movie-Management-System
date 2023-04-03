package com.Client;

import com.Replica2.Interfaces.WebInterface;
import com.Replica2.Logger.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Scanner;



public class Client {
    public static Service atwaterService;
    public static Service verdunService;
    public static Service outremontService;
    private static WebInterface obj;

    static Scanner sc;

    public static void main(String[] args) throws Exception {
        URL atwaterURL = new URL("http://localhost:8080/atwater?wsdl");
        QName atwaterQName = new QName("http://Interfaces.com/", "ImplementationService");
        atwaterService = Service.create(atwaterURL, atwaterQName);

        URL verdunURL = new URL("http://localhost:8080/verdun?wsdl");
        QName verdunQName = new QName("http://Interfaces.com/", "ImplementationService");
        verdunService = Service.create(verdunURL, verdunQName);

        URL outremontURL = new URL("http://localhost:8080/outremont?wsdl");
        QName outremontQName = new QName("http://Interfaces.com/", "ImplementationService");
        outremontService = Service.create(outremontURL, outremontQName);
        init();
    }

    public static void init() throws Exception {
        sc = new Scanner(System.in);
        String userID;
        System.out.println("Please Enter your UserID:");
        userID = sc.next().trim().toUpperCase();
        Logger.clientLog(userID, " login attempt");
        switch (checkUserType(userID)) {
            case 1:
                try {
                    System.out.println("Customer Login successful (" + userID + ")");
                    Logger.clientLog(userID, " Customer Login successful");
                    customer(userID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    System.out.println("Admin Login successful (" + userID + ")");
                    Logger.clientLog(userID, " Admin Login successful");
                    admin(userID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("UserID is not in correct format!");
                Logger.clientLog(userID, " UserID is not in correct format");
                Logger.deleteALogFile(userID);
                init();
        }
    }
    private static String getServerID(String userID) {
        String branchAcronym = userID.substring(0, 3);
        if (branchAcronym.equalsIgnoreCase("ATW")) {
            obj = atwaterService.getPort(WebInterface.class);
            return branchAcronym;
        } else if (branchAcronym.equalsIgnoreCase("VER")) {
            obj = verdunService.getPort(WebInterface.class);
            return branchAcronym;
        } else if (branchAcronym.equalsIgnoreCase("OUT")) {
            obj = outremontService.getPort(WebInterface.class);
            return branchAcronym;
        }
        return "1";
    }

    private static int checkUserType(String userID) {
        if (userID.length() == 8) {
            if (userID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    userID.substring(0, 3).equalsIgnoreCase("VER") ||
                    userID.substring(0, 3).equalsIgnoreCase("OUT")) {
                if (userID.substring(3, 4).equalsIgnoreCase("C")) {
                    return 1;
                } else if (userID.substring(3, 4).equalsIgnoreCase("A")) {
                    return 2;
                }
            }
        }
        return 0;
    }

    private static void customer(String customerID) throws Exception {
        String serverID = getServerID(customerID);
        if (serverID.equals("1")) {
            init();
        }
        boolean repeat = true;
        printMenu(1);
        int menuSelection = sc.nextInt();
        String movieName;
        String movieID;
        String serverResponse;
        int numberOfTickets;
        switch (menuSelection) {
            case 1:
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                System.out.println("Enter the number of tickets you want to book. ");
                numberOfTickets = sc.nextInt();
                Logger.clientLog(customerID, " attempting to bookMovie");
                serverResponse = obj.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " bookMovie", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                break;
            case 2:
                Logger.clientLog(customerID, " attempting to getBookingSchedule");
                serverResponse = obj.getBookingSchedule(customerID);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " bookMovie", " null ", serverResponse);
                break;
            case 3:
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                System.out.println("Enter the number of tickets you want to book. ");
                numberOfTickets = sc.nextInt();
                Logger.clientLog(customerID, " attempting to cancelMovie");
                serverResponse = obj.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " bookMovie", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                break;
            case 4:
                System.out.println("Please Enter the OLD Movie to be replaced");
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                System.out.println("Enter the number of tickets you want to exchange. ");
                numberOfTickets = sc.nextInt();
                System.out.println("Please Enter the NEW Movie to be replaced");
                String newmovieName = promptforMovieName();
                String newmovieID = promptForMovieID();
                Logger.clientLog(customerID, " attempting to exchangeTickets");
                serverResponse = obj.exchangeTickets(customerID, newmovieID, newmovieName, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " exchangeTickets", " oldMovieID: " + movieID + " oldMovieName: " + movieName + " newmovieID: " + newmovieID + " newmovieName: " + newmovieName + " ", serverResponse);
                break;
            case 5:
                repeat = false;
                Logger.clientLog(customerID, " attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            customer(customerID);
        }
    }

    private static void admin(String adminID) throws Exception {
        String serverID = getServerID(adminID);
        if (serverID.equals("1")) {
            init();
        }
        boolean repeat = true;
        printMenu(2);
        String movieName;
        String movieID;
        String serverResponse;
        int capacity;
        int numberOfTickets;
        int menuSelection = sc.nextInt();
        switch (menuSelection) {
            case 1:
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                capacity = promptForCapacity();
                Logger.clientLog(adminID, " attempting to addMovie");
                serverResponse = obj.addMovieSlots(movieID, movieName, capacity);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " addMovie", " movieID: " + movieID + " movieName: " + movieName + " MovieCapacity: " + capacity + " ", serverResponse);
                break;
            case 2:
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                Logger.clientLog(adminID, " attempting to removeMovie");
                serverResponse = obj.removeMovieSlots(movieID, movieName);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " removeMovie", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                break;
            case 3:
                movieName = promptforMovieName();
                Logger.clientLog(adminID, " attempting to listMovieShowsAvailability");
                serverResponse = obj.listMovieShowsAvailability(movieName);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " listMovieShowsAvailability", " movieName: " + movieName + " ", serverResponse);
                break;
            case 4:
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                System.out.println("Enter the number of tickets you want to book. ");
                numberOfTickets = sc.nextInt();
                Logger.clientLog(adminID, " attempting to bookMovie");
                serverResponse = obj.bookMovieTickets(adminID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " bookMovie", " customerID: " + adminID + " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                break;
            case 5:
                Logger.clientLog(adminID, " attempting to getBookingSchedule");
                serverResponse = obj.getBookingSchedule(adminID);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " getBookingSchedule", " customerID: " + adminID + " ", serverResponse);
                break;
            case 6:
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                System.out.println("Enter the number of tickets you want to book. ");
                numberOfTickets = sc.nextInt();
                Logger.clientLog(adminID, " attempting to cancelMovie");
                serverResponse = obj.cancelMovieTickets(adminID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " cancelMovie", " customerID: " + adminID + " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                break;
            case 7:
                System.out.println("Please Enter the OLD Movie to be exchanged");
                movieName = promptforMovieName();
                movieID = promptForMovieID();
                System.out.println("Enter the number of tickets you want to exchange. ");
                numberOfTickets = sc.nextInt();
                System.out.println("Please Enter the NEW Movie to be exchanged");
                String newmovieName = promptforMovieName();
                String newMovieID = promptForMovieID();
                Logger.clientLog(adminID, " attempting to exchangeTickets");
                serverResponse = obj.exchangeTickets(adminID, newMovieID, newmovieName, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " exchangeTickets", " customerID: " + adminID + " oldMovieID: " + movieID + " oldmovieName: " + movieName + " newMovieID: " + newMovieID + " newmovieName: " + newmovieName + " ", serverResponse);
                break;
            case 8:
                repeat = false;
                Logger.clientLog(adminID, "attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            admin(adminID);
        }
    }

    private static void printMenu(int userType) {
        System.out.println("*************************************");
        System.out.println("Please choose an option below:");
        if (userType == 1) {
            System.out.println("1.Book Movie Tickets");
            System.out.println("2.Get Booking Schedule");
            System.out.println("3.Cancel Movie Tickets");
            System.out.println("4.Exchange Tickets");
            System.out.println("5.Logout");
        } else if (userType == 2) {
            System.out.println("1.Add Movie Slots");
            System.out.println("2.Remove Movie Slots");
            System.out.println("3.List Movie Shows Availability");
            System.out.println("4.Book Movie Tickets");
            System.out.println("5.Get Booking Schedule");
            System.out.println("6.Cancel Movie Tickets");
            System.out.println("7.Exchange Tickets");
            System.out.println("8.Logout");
        }
    }

    private static int promptForCapacity() {
        System.out.println("-------------------------------------");
        System.out.println("Please enter the booking capacity:");
        return sc.nextInt();
    }

    private static String promptForMovieID() {
        System.out.println("-------------------------------------");
        System.out.println("Please enter the MovieID (e.g ATWM100223)");
        String movieID = sc.next().trim().toUpperCase();
        if (movieID.length() == 10) {
            if (movieID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    movieID.substring(0, 3).equalsIgnoreCase("VER") ||
                    movieID.substring(0, 3).equalsIgnoreCase("OUT")) {
                if (movieID.substring(3, 4).equalsIgnoreCase("M") ||
                        movieID.substring(3, 4).equalsIgnoreCase("A") ||
                        movieID.substring(3, 4).equalsIgnoreCase("E")) {
                    return movieID;
                }
            }
        }
        return promptForMovieID();
    }

    private static String promptforMovieName() {
        System.out.println("-------------------------------------");
        System.out.println("Please choose a Movie:");
        System.out.println("1.Avatar");
        System.out.println("2.Avengers");
        System.out.println("3.Titanic");
        switch (sc.nextInt()) {
            case 1:
                return "Avatar";
            case 2:
                return "Avengers";
            case 3:
                return "Titanic";
        }
        return promptforMovieName();
    }
}