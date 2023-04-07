package com.Replica3;
import com.Replica3.Impl.BookingImpl;
import com.Replica3.Impl.IBooking;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.Replica3.LoggerClass.LogData;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/*
    targetNamespace="http://Impl/"
    name="BookingImplService"
 */


public class Client {

    private static IBooking impl;
    private static Service sAtwater, sVerdun, sOutremont;

    public static void main(String[] args) throws Exception {
        createService();
        //calling the CLI function
        CmdLineInterface();
    }

    private static void createService() throws MalformedURLException {
        URL url1 = new URL("http://localhost:8080/ServerAtwater/?wsdl");
        URL url2 = new URL("http://localhost:8080/ServerVerdun/?wsdl");
        URL url3 = new URL("http://localhost:8080/ServerOutremont/?wsdl");
        QName qName1 = new QName("http://Impl/","BookingImplService");
        QName qName2 = new QName("http://Impl/","BookingImplService");
        QName qName3 = new QName("http://Impl/","BookingImplService");

        sAtwater = Service.create(url1, qName1);
        sVerdun = Service.create(url2, qName2);
        sOutremont = Service.create(url3, qName3);
    }

    public static void CmdLineInterface() throws Exception {

        Scanner sc = new Scanner(System.in);
        String userID;
        System.out.println("Enter your ID:");
        userID = sc.next().trim().toUpperCase();
        LogData.clientLog(userID, " tried to log in ");

        switch (checkUserType(userID)) {
            //Customer
            case 1:
                try {
                    System.out.println("Logged in!");
                    LogData.clientLog(userID, " Customer has logged in ");
                    customer(userID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //Admin
            case 2:
                try {
                    System.out.println("Admin Logged in!");
                    LogData.clientLog(userID, " Admin has logged in ");
                    admin(userID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //if ID is wrong
            default:
                System.out.println("wrong user ID entered");
                LogData.clientLog(userID, " wrong user ID entered ");
                LogData.deleteLog(userID);
                CmdLineInterface();

        }

    }

    private static int checkUserType(String userID) {
        /*
         * checks is ID is customer or Admin
         */
        if (userID.length() == 8) {
            if (userID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    userID.substring(0, 3).equalsIgnoreCase("VER") ||
                    userID.substring(0, 3).equalsIgnoreCase("OUT"))
            {
                if (userID.substring(3, 4).equalsIgnoreCase("C")) {
                    return 1;
                } else if (userID.substring(3, 4).equalsIgnoreCase("A")) {
                    return 2;
                }
            }
        }
        return 0;
    }

    private static String serverName(String userID) {
        if(userID.substring(0,3).equals("ATW")) {
            return userID.substring(0, 3);
        }
        else if (userID.substring(0,3).equals("VER")) {
            return userID.substring(0,3);
        }
        else if (userID.substring(0,3).equals("OUT")) {
            return userID.substring(0,3);
        }
        return null;
    }

    private static void customer(String customerID) throws Exception {
        /*
         * CLI for customer
         */
        Scanner sc = new Scanner(System.in);

        String serverID = customerID.substring(0,3);
        if(serverID.equalsIgnoreCase("ATW")){
            impl = sAtwater.getPort(IBooking.class);
        }
        else if(serverID.equalsIgnoreCase("VER")){
            impl = sVerdun.getPort(IBooking.class);
        }
        else if(serverID.equalsIgnoreCase("OUT")){
            impl = sOutremont.getPort(IBooking.class);
        }
        else CmdLineInterface();

        display(1);
        int choice = sc.nextInt();
        String movieName; String new_movieName; String old_movieName;
        String movieID; String new_movieID;
        int numOfTickets; int numberOfTickets;
        String serverReply;
        switch (choice) {
            //bookMovieTickets
            case 1:
                movieName = selectMovieName();
                movieID = getMovieID();
                numOfTickets = getTickets();
                serverReply = impl.bookMovieTickets(customerID, movieID, movieName, numOfTickets);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " bookMovieTickets", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                break;
            //getBookingSchedule
            case 2:
                serverReply = impl.getBookingSchedule(customerID);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " getBookingSchedule ", " null ", serverReply);
                break;
            //cancelMovieTickets
            case 3:
                movieName = selectMovieName();
                movieID = getMovieID();
                numOfTickets = getTickets();
                serverReply = impl.cancelMovieTickets(customerID, movieID, movieName, numOfTickets);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " cancelMovieTickets "," movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                break;
            //exchangeTickets
            case 4:
                System.out.print("Old movie ID - ");
                movieID = getMovieID();
                System.out.print("Old movie name - ");
                old_movieName = selectMovieName();
                System.out.print("New movie ID - ");
                new_movieID = getMovieID();
                System.out.print("New movie name - ");
                new_movieName = selectMovieName();
                System.out.println("Number of tickets - ");
                numberOfTickets = getTickets();
                serverReply = impl.exchangeTickets(customerID, old_movieName, movieID, new_movieID, new_movieName, numberOfTickets);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " exchangeTickets", " Old name:" + old_movieName + " Old id:" + movieID + " New name:" + new_movieName + " New id:" + new_movieID, serverReply);
                break;
            //logout
            case 5:
                LogData.clientLog(customerID, " Logged out ");
                CmdLineInterface();
                return;
        }
        customer(customerID);
    }

    private static void admin(String adminID) throws Exception {
        /*
         * CLI for admin
         */
        Scanner sc = new Scanner(System.in);

        String serverID = adminID.substring(0,3);
        if(serverID.equalsIgnoreCase("ATW")){
            impl = sAtwater.getPort(IBooking.class);
        }
        else if(serverID.equalsIgnoreCase("VER")){
            impl = sVerdun.getPort(IBooking.class);
        }
        else if(serverID.equalsIgnoreCase("OUT")){
            impl = sOutremont.getPort(IBooking.class);
        }
        else CmdLineInterface();

        display(2);
        String movieName; String new_movieName;
        String movieID; String new_movieID;
        String serverReply;
        int capacity;
        int numOfTickets;
        String customerID;
        int choice = sc.nextInt();
        switch (choice) {
            //addMovieSlots
            case 1:
                movieName = selectMovieName();
                movieID = getMovieID();
                capacity = getCapacity();
                LogData.clientLog(adminID, " addMovieSlots ");
                serverReply = impl.addMovieSlots(movieID, movieName, capacity);
                System.out.println(serverReply);
                LogData.clientLog(adminID, " addMovieSlots", " movieID: " + movieID + " movieName: " + movieName + " movieCapacity: " + capacity + " ", serverReply);
                break;
            //removeMovieSlots
            case 2:
                movieName = selectMovieName();
                movieID = getMovieID();
                LogData.clientLog(adminID, " removeMovieSlots ");
                serverReply = impl.removeMovieSlots(movieID, movieName);
                System.out.println(serverReply);
                LogData.clientLog(adminID, " removeEvent", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                break;
            //listMovieShowsAvailability
            case 3:
                movieName = selectMovieName();
                LogData.clientLog(adminID, " listMovieShowsAvailability ");
                serverReply = impl.listMovieShowsAvailability(movieName);
                System.out.println(serverReply);
                LogData.clientLog(adminID, " listMovieShowsAvailability", " movieName: " + movieName + " ", serverReply);
                break;
            //Book movie
            case 4:
                customerID = getCustID();
                movieName = selectMovieName();
                movieID = getMovieID();
                numOfTickets = getTickets();
                serverReply = impl.bookMovieTickets(customerID, movieID, movieName, numOfTickets);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " bookMovieTickets", " movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                break;
            //cancelMovieTickets
            case 5:
                customerID = getCustID();
                movieName = selectMovieName();
                movieID = getMovieID();
                numOfTickets = getTickets();
                serverReply = impl.cancelMovieTickets(customerID, movieID, movieName, numOfTickets);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " cancelMovieTickets "," movieID: " + movieID + " movieName: " + movieName + " ", serverReply);
                break;
            //getBookingSchedule
            case 6:
                customerID = getCustID();
                serverReply = impl.getBookingSchedule(customerID);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " getBookingSchedule ", " null ", serverReply);
                break;
            //exchangeTickets
            case 7:
                customerID = getCustID();
                System.out.print("Old movie ID - ");
                movieID = getMovieID();
                System.out.print("Old movie name - ");
                movieName = selectMovieName();
                System.out.print("New movie ID - ");
                new_movieID = getMovieID();
                System.out.print("New movie name - ");
                new_movieName = selectMovieName();
                System.out.println("Number of tickets - ");
                numOfTickets = getTickets();
                serverReply = impl.exchangeTickets(customerID, movieName, movieID, new_movieID, new_movieName, numOfTickets);
                System.out.println(serverReply);
                LogData.clientLog(customerID, " exchangeTickets", " Old name:" + movieName + " Old id:" + movieID + " New name:" + new_movieName + " New id:" + new_movieID, serverReply);
                break;
            //Log out
            case 8:
                LogData.clientLog(adminID, " Logged out ");
                CmdLineInterface();
                return;
        }
        admin(adminID);
    }

    private static String getCustID() {
        Scanner sc = new Scanner(System.in);
        System.out.println("#################################");
        System.out.println("Please enter the Customer ID:");
        return sc.next();
    }

    private static void display(int userType) {
        System.out.println("#################################");
        System.out.println("Which operation do you want to perform?");
        if (userType == 1) {
            System.out.println("1. bookMovieTickets");
            System.out.println("2. getBookingSchedule");
            System.out.println("3. cancelMovieTickets");
            System.out.println("4. exchangeTickets");
            System.out.println("5. Logout");
        } else if (userType == 2) {
            System.out.println("1. addMovieSlots");
            System.out.println("2. removeMovieSlots");
            System.out.println("3. listMovieShowsAvailability");
            System.out.println("4. bookMovieTickets");
            System.out.println("5. getBookingSchedule");
            System.out.println("6. cancelMovieTickets");
            System.out.println("7. exchangeTickets");
            System.out.println("8. Logout");
        }
    }

    private static String getMovieID() {
        /*
         * For entering movie ID - loops again if wrongly entered
         */
        Scanner sc = new Scanner(System.in);
//        System.out.println("#################################");
        // System.out.println();
        System.out.println("Enter movie ID");
        String eventID = sc.next().trim().toUpperCase();
        if (eventID.length() == 10) {
            if (eventID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    eventID.substring(0, 3).equalsIgnoreCase("VER") ||
                    eventID.substring(0, 3).equalsIgnoreCase("OUT")) {
                if (eventID.substring(3, 4).equalsIgnoreCase("M") ||
                        eventID.substring(3, 4).equalsIgnoreCase("A") ||
                        eventID.substring(3, 4).equalsIgnoreCase("E")) {
                    return eventID;
                }
            }
        }
        return getMovieID();
    }

    private static int getCapacity() {
        Scanner sc = new Scanner(System.in);
        System.out.println("#################################");
        System.out.println("Please enter the booking capacity:");
        return sc.nextInt();
    }

    static String selectMovieName() {
        Scanner sc = new Scanner(System.in);
        System.out.println("#################################");
        System.out.println("Which movie?");
        System.out.println("1.Avatar");
        System.out.println("2.Avengers");
        System.out.println("3.Titanic");
        int choice = sc.nextInt();
        if(choice == 1) return "Avatar";
        else if(choice ==2) return "Avengers";
        else if(choice ==3) return "Titanic";
        else return selectMovieName();
    }

    static int getTickets() {
        Scanner sc = new Scanner(System.in);
        System.out.println("#################################");
        System.out.println("Enter number of tickets ");
        return sc.nextInt();
    }
}
