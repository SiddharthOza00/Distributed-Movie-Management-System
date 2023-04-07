package com.Client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.FrontEnd.FrontendInterface;
import com.Replica1.LoggerInfo;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainClient {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws MalformedURLException {

        // Client code
        System.out.println("**************Welcome to the Distributed Movie Ticket Booking System**************");
        Boolean outOfLoop = false;
        while (true) {
            if (outOfLoop) {
                break;
            } else {
                System.out.println("Please enter your user ID: ");

                String userID = sc.nextLine();
                userID = userID.trim();

                try {
                    LoggerInfo userLogger = new LoggerInfo(userID + ".txt", userID);
                    userLogger.logger.setLevel(Level.ALL);
                    userLogger.logger.info("-----User " + userID + " has logged in-----");

                    String place = userID.substring(0, 3);
                    switch (place) {
                        case "ATW":
                            if (userID.charAt(3) == 'A') {
                                // User is an admin
                                System.out.println("You are an admin from Atwater");
                                userLogger.logger.info("User is an admin from Atwater");
                                operations(1, "Atwater", outOfLoop, userID, userLogger);
                            } else {
                                // User is a customer
                                System.out.println("You are a customer from Atwater");
                                userLogger.logger.info("User is a customer from Atwater");
                                operations(0, "Atwater", outOfLoop, userID, userLogger);
                            }
                            break;

                        case "VER":
                            if (userID.charAt(3) == 'A') {
                                // User is an admin
                                System.out.println("You are an admin from Verdun");
                                userLogger.logger.info("User is an admin from Verdun");
                                operations(1, "Verdun", outOfLoop, userID, userLogger);
                            } else {
                                // User is a customer
                                System.out.println("You are a customer from Verdun");
                                userLogger.logger.info("User is a customer from Verdun");
                                operations(0, "Verdun", outOfLoop, userID, userLogger);
                            }
                            break;

                        case "OUT":
                            if (userID.charAt(3) == 'A') {
                                // User is an admin
                                System.out.println("You are an admin from Outremont");
                                userLogger.logger.info("User is an admin from Outremont");
                                operations(1, "Outremont", outOfLoop, userID, userLogger);
                            } else {
                                // User is a customer
                                System.out.println("You are a customer from Outremont");
                                userLogger.logger.info("User is a customer from Outremont");
                                operations(0, "Outremont", outOfLoop, userID, userLogger);
                            }
                            break;
                    }
                } catch (Exception e) {

                }
            }
        }

        System.out
                .println("**************Thankyou for using the Distributed Movie Ticket Booking System**************");

    }

    private static void operations(int user, String serverName, Boolean outOfLoop, String customerID,
            LoggerInfo userLogger) {
        System.out.println("Enter the operation you would like to perform: ");
        try {
            
            // URL url = null;
            // QName qName = null;
            // switch (serverName) {
            //     case "Atwater":
            //     url = new URL("http://localhost:8080/atwater?wsdl");
            //     qName = new QName("http://webservices.com/", "FrontendInterfaceImplService");
            //     break;

            //     case "Verdun":
            //     url = new URL("http://localhost:8081/verdun?wsdl");
            //     qName = new QName("http://webservices.com/", "FrontendInterfaceImplService");
            //     break;

            //     case "Outremont":
            //     url = new URL("http://localhost:8082/outremont?wsdl");
            //     qName = new QName("http://webservices.com/", "FrontendInterfaceImplService");
            //     break;
                
            // }

            URL url = new URL("http://localhost:8080/frontend?wsdl");
            QName qName = new QName("http://FrontEnd.com/", "FrontendImplService");
            
            Service service = Service.create(url, qName);
            FrontendInterface helloService = service.getPort(FrontendInterface.class);

            if (user == 1) {
                // USer is an admin
                System.out.println(
                        "1. Add Movie Slots\n2. Remove Movie Slots\n3. List Movie Shows Availability\n4. Book Movie Tickets\n5. Get Booking Schedule\n6. Cancel Movie Tickets\n7. Exchange Tickets\n8. Exit");
                int operation = sc.nextInt();
                sc.nextLine();
                switch (operation) {
                    case 1:
                        // call add movie slots for the server
                        System.out.println("Enter the movie name: ");
                        String movieName = sc.nextLine();
                        System.out.println("Enter the movie ID: ");
                        String movieID = sc.nextLine();
                        System.out.println("Enter the number of slots you want to add: ");
                        String tickets = sc.nextLine();
                        userLogger.logger.info("User has called addMovieSlots for the movie " + movieName + " ,movieID "
                                + movieID + " and number of tickets " + tickets);
                        String res = helloService.addMovieSlots(customerID, movieID, movieName, Integer.parseInt(tickets));
                        if (res!=null && res.equals("Success")) {
                            System.out.println("Successsfully added movie slots");
                        } else {
                            System.out.println("Could not add movie slots");
                        }
                        break;
                    case 2:
                        // call remove movie slots for the server
                        System.out.println("Enter the movie name to remove: ");
                        String movieNameToRemove = sc.nextLine();
                        System.out.println("Enter the movie ID to remove: ");
                        String movieIDToRemove = sc.nextLine();
                        userLogger.logger.info("User has called removeMovieSlots for the movie " + movieNameToRemove
                                + " ,movieID " + movieIDToRemove);
                        String resForRemove = helloService.removeMovieSlots(customerID, movieIDToRemove, movieNameToRemove);
                        if (resForRemove.equals("Success")) {
                            System.out.println("Successsfully removed movie slots!!");
                        } else {
                            System.out.println("Could not remove movie slots");
                        }

                        break;
                    case 3:
                        // call list shows for the server
                        System.out.println("Enter the movie name: ");
                        String name = sc.nextLine();
                        userLogger.logger.info("User has called listMovieShowsAvailability for the movie " + name);
                        String finalRes = helloService.listMovieShowsAvailability(name, true);
                        if (finalRes == null || finalRes.equals("")) {
                            System.out.println("There are no movie slots for this movie in any server");
                        } else {
                            System.out.println(
                                    "List of all the movies shows for the movie " + name + " in all the servers.");
                            System.out.println("Final result - " + finalRes);
                            String[] movieSlotsAvailable = finalRes.split(",");

                            for (String r : movieSlotsAvailable) {
                                String id = r.split("-")[0];
                                String capacity = r.split("-")[1];
                                System.out.println("MOVIE SLOT -> " + id + " , CAPACITY -> " + capacity);
                            }
                        }

                        break;

                    case 4:
                        System.out.println("Enter the movie name: ");
                        String movieNameToBook = sc.nextLine();
                        System.out.println("Enter the movie ID: ");
                        String movieIDToBook = sc.nextLine();
                        System.out.println("Enter the number of tickets you want to book: ");
                        String ticketsToBook = sc.nextLine();
                        userLogger.logger.info("User has called bookMovieTickets for the movie " + movieNameToBook
                                + " ,movieID " + movieIDToBook + " and number of tickets " + ticketsToBook);
                        String resultOfBooking = helloService.bookMovieTickets(customerID, movieIDToBook,
                                movieNameToBook,
                                Integer.parseInt(ticketsToBook), true);
                        System.out.println("resultOfBooking " + resultOfBooking);

                        break;

                    case 5:
                        userLogger.logger.info("User has called getBookingSchedule");
                        String bookingsResultForClient = helloService.getBookingSchedule(customerID, true);

                        // String[] bookingResults = bookingsResultForClient.split(";"); COMMA
                        String[] bookingResults = bookingsResultForClient.split(",");
                        System.out.println(
                                "result size - " + bookingResults.length + " and result as array = " + bookingResults);
                        if (bookingsResultForClient.equals("") || bookingsResultForClient == null) {
                            System.out.println("There are no bookings for the user.");
                        } else {
                            for (String s : bookingResults) {
                                // String[] movieName2 = s.split("|");
                                String[] movieName2 = s.split(";");
                                String movieNameStr = movieName2[0];
                                String idTickets = movieName2[1];
                                String[] idTicketsArr = idTickets.split("/");
                                String id = idTicketsArr[0];
                                String tickets2 = idTicketsArr[1];
                                System.out.println(
                                        "MOVIE - " + movieNameStr + " ID - " + id + " TICKETS BOOKED - " + tickets2);
                            }
                        }

                        // System.out.println("Bookings for the client are "+ bookingsResultForClient);
                        // String[] bookingResults = bookingsResultForClient.split(";");
                        // System.out.println("Size after splitting "+bookingResults.length);
                        // for(String s:bookingResults) {
                        // System.out.println(s);
                        // }
                        break;

                    case 6:
                        System.out.println("Enter the movie name: ");
                        String movieNameToCancel = sc.nextLine();
                        System.out.println("Enter the movie ID: ");
                        String movieIDToCancel = sc.nextLine();
                        System.out.println("Enter the number of tickets you want to cancel: ");
                        String ticketsToCancel = sc.nextLine();
                        userLogger.logger.info("User has called cancelMovieTickets for the movie " + movieNameToCancel
                                + " ,movieID " + movieIDToCancel + " and number of tickets " + ticketsToCancel);
                        String cancel = helloService.cancelMovieTickets(customerID, movieIDToCancel, movieNameToCancel,
                                Integer.parseInt(ticketsToCancel));
                        System.out.println("Cancelled Movie status - " + cancel);
                        break;
                    case 7:
                        System.out.println("Enter the current movie name: ");
                        String currentMovieName = sc.nextLine();
                        System.out.println("Enter the new movie name: ");
                        String newMovieName = sc.nextLine();
                        System.out.println("Enter the current movie ID: ");
                        String currentMovieID = sc.nextLine();
                        System.out.println("Enter the new movie ID: ");
                        String newMovieID = sc.nextLine();
                        System.out.println("Enter the number of tickets you want to exchange: ");
                        String ticketsToExchange = sc.nextLine();

                        String exchangeTickets = helloService.exchangeTickets(customerID, currentMovieName,
                                currentMovieID, newMovieID, newMovieName, Integer.parseInt(ticketsToExchange));
                        System.out.println("Result of exchange tickets is: " + exchangeTickets);
                        break;
                    case 8:
                        outOfLoop = true;
                        break;
                }
            } else {
                // Customer
                System.out.println(
                        "1. Book Movie Tickets.\n2. Get Movie Schedule.\n3. Cancel Movie Tickets\n4. Exchange Tickets\n5. Exit");
                int operation = sc.nextInt();
                sc.nextLine();

                switch (operation) {
                    case 1:
                        System.out.println("Enter the movie name: ");
                        String movieNameToBook = sc.nextLine();
                        System.out.println("Enter the movie ID: ");
                        String movieIDToBook = sc.nextLine();
                        System.out.println("Enter the number of tickets you want to book: ");
                        String ticketsToBook = sc.nextLine();
                        userLogger.logger.info("User has called bookMovieTickets for the movie " + movieNameToBook
                                + " ,movieID " + movieIDToBook + " and number of tickets " + ticketsToBook);
                        String resultOfBooking = helloService.bookMovieTickets(customerID, movieIDToBook,
                                movieNameToBook,
                                Integer.parseInt(ticketsToBook), true);
                        System.out.println("resultOfBooking " + resultOfBooking);
                        break;

                    case 2:

                        userLogger.logger.info("User has called getBookingSchedule");
                        String bookingsResultForClient = helloService.getBookingSchedule(customerID, true);
                        System.out.println("Bookings for the user are " + bookingsResultForClient);

                        // String[] bookingResults = bookingsResultForClient.split(";"); COMMA
                        String[] bookingResults = bookingsResultForClient.split(",");
                        // System.out.println("result size - "+bookingResults.length+" and result as
                        // array = "+bookingResults);
                        if (bookingsResultForClient.equals("") || bookingsResultForClient == null) {
                            System.out.println("There are no bookings for the user.");
                        } else {
                            // String[] moviesPrint; String[]
                            for (String s : bookingResults) {
                                // System.out.println("s is - "+s);
                                String[] movieName = s.split("_");
                                String movieNameStr = movieName[0];
                                // System.out.println("Movie name is - "+movieNameStr);
                                String idTickets = movieName[1];
                                // System.out.println("for a movie id and tickets - "+idTickets);
                                String[] idTicketsArr = idTickets.split("-");

                                for (String idTicketStr : idTicketsArr) {
                                    // System.out.println("id&ticket -- "+idTicketStr);
                                    String onlyId = idTicketStr.split("/")[0];
                                    String onlyTickets = idTicketStr.split("/")[1];
                                    System.out.println("MOVIE - " + movieNameStr + " ID - " + onlyId + " TICKETS - "
                                            + onlyTickets);
                                }

                                // String id = idTicketsArr[0];
                                // String tickets = idTicketsArr[1];
                                // System.out.println("MOVIE - "+movieNameStr+" ID - "+id+" TICKETS BOOKED -
                                // "+tickets);
                            }
                        }

                        break;

                    case 3:
                        System.out.println("Enter the movie name: ");
                        String movieNameToCancel = sc.nextLine();
                        System.out.println("Enter the movie ID: ");
                        String movieIDToCancel = sc.nextLine();
                        System.out.println("Enter the number of tickets you want to cancel: ");
                        String ticketsToCancel = sc.nextLine();
                        userLogger.logger.info("User has called cancelMovieTickets for the movie " + movieNameToCancel
                                + " ,movieID " + movieIDToCancel + " and number of tickets " + ticketsToCancel);
                        String cancel = helloService.cancelMovieTickets(customerID, movieIDToCancel, movieNameToCancel,
                                Integer.parseInt(ticketsToCancel));
                        System.out.println("Cancelled Movie status - " + cancel);
                        break;
                    case 4:
                        System.out.println("Enter the current movie name: ");
                        String currentMovieName = sc.nextLine();
                        System.out.println("Enter the new movie name: ");
                        String newMovieName = sc.nextLine();
                        System.out.println("Enter the current movie ID: ");
                        String currentMovieID = sc.nextLine();
                        System.out.println("Enter the new movie ID: ");
                        String newMovieID = sc.nextLine();
                        System.out.println("Enter the number of tickets you want to exchange: ");
                        String ticketsToExchange = sc.nextLine();

                        String exchangeTickets = helloService.exchangeTickets(customerID, currentMovieName,
                                currentMovieID, newMovieID, newMovieName, Integer.parseInt(ticketsToExchange));
                        System.out.println("Result of exchange tickets is: " + exchangeTickets);
                        break;
                    case 5:
                        outOfLoop = true;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
