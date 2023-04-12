package com.Replica1;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@WebService(endpointInterface = "com.Replica1.Webservice")
@SOAPBinding(style = Style.RPC)
public class WebserviceImpl implements Webservice {

    public WebserviceImpl() {
        super();
    }

    private Map<String, Map<String, List<String>>> movieData;
    private Map<String, Map<String, Map<String, Integer>>> clientData;
    private String serverName;

    public Map<String, Map<String, List<String>>> getMovieData() {
        return movieData;
    }

    public void setMovieData(Map<String, Map<String, List<String>>> movieData) {
        this.movieData = movieData;
    }

    public Map<String, Map<String, Map<String, Integer>>> getClientData() {
        return clientData;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setClientData(Map<String, Map<String, Map<String, Integer>>> clientData) {
        this.clientData = clientData;
    }

    @Override
    public boolean addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        System.out.println("Called addMovieSlots from ServerIDLImpl for " + getServerName() + " server");
        boolean addedSlots = false;
        Integer bookingCapacityInt = bookingCapacity;

        try {
            if (movieID.startsWith(((Character) getServerName().charAt(0)).toString())) {
                System.out.println("Admin is booking slots for the same server.");

                // comparing date of the movie show
                LocalDate date1 = getLocalDateFromMovieID(movieID);
                date1 = date1.minusDays(7);
                LocalDate todayDate = LocalDate.now();
                System.out.println("Checking if today - " + todayDate + " is after " + date1);

                if (todayDate.isAfter(date1)) {
                    // ADmin can book tickets
                    System.out.println("Movie id lies within 7 days");
                    if (getMovieData().get(movieName) != null) {
                        System.out.println("Movie exists!");
                        Map<String, List<String>> movieMap = movieData.get(movieName);
                        System.out.println("Old list of all movie shows of " + movieName);
                        printMovieDataForAMovie(movieName);
                        if (movieMap.containsKey(movieID)) { // that show exists for the movie
                            System.out
                                    .println("Slots already exist for " + movieID
                                            + ". Updating the capacity of the existing slot...");

                            List<String> list = movieMap.get(movieID);
                            Integer newCapacity = bookingCapacityInt;
                            list.set(0, newCapacity.toString());

                            System.out.println("Capacity updated! ");
                            movieMap.put(movieID, list);
                            printMovieDataForAMovie(movieName);

                        } else {
                            System.out.println("This movie id does not exist for " + movieName + " Adding slot....");

                            List<String> list = new ArrayList<String>(2);
                            list.add(0, bookingCapacityInt.toString());
                            list.add(1, "");
                            movieMap.put(movieID, list);

                            System.out.println("Added movie ID to the database!");
                            printMovieDataForAMovie(movieName);

                        }
                        addedSlots = true;
                    } else {
                        System.out.println("Movie does not exist! Adding movie to the database");
                        getMovieData().put(movieName, new ConcurrentHashMap<String, List<String>>() {
                            {
                                put(movieID, new ArrayList<String>() {
                                    {
                                        add(bookingCapacityInt.toString());
                                        add("");
                                    }
                                });
                            }
                        });
                        System.out.println("Added " + movieName + " to the database!");
                        printMovieDataForAMovie(movieName);
                        addedSlots = true;
                    }
                } else {
                    System.out.println("Tickets which are more than a week from today cannot be booked!");
                    addedSlots = false;
                }
            } else {
                System.out.println("Sorry! Admin cannot book slots for different server");
                addedSlots = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return addedSlots;
        // throw new UnsupportedOperationException("Unimplemented method
        // 'addMovieSlots'");
    }

    @Override
    public boolean removeMovieSlots(String movieID, String movieName) {
        System.out.println("Called removeMovieSlots from ServerIDLImpl for " + getServerName() + " server");
        boolean movieSlotsRemoved = false;

        if (movieID.startsWith(((Character) getServerName().charAt(0)).toString())) {
            System.out.println("Admin is removing slots for the same server.");

            if (getMovieData().get(movieName) != null) {
                System.out.println("Movie exists!");
                Map<String, List<String>> movieMap = movieData.get(movieName);
                if (movieMap.get(movieID) != null) {
                    System.out.println("Movie slot exists!");
                    // ATWM160523
                    LocalDate today = LocalDate.now();
                    System.out.println("Today's date = " + today);
                    ;

                    String monthStr = movieID.substring(6, 8);
                    String ms = new String();
                    if (monthStr.charAt(0) == '0') {
                        ms = monthStr.substring(1, 2);
                    } else {
                        ms = monthStr;
                    }
                    String ds = new String();
                    String dateStr = movieID.substring(4, 6);
                    if (dateStr.charAt(0) == '0') {
                        ds = dateStr.substring(1, 2);
                    } else {
                        ds = dateStr;
                    }

                    String yearStr = movieID.substring(8, movieID.length());

                    yearStr = "20" + yearStr;
                    Integer year = Integer.parseInt(yearStr);
                    Integer month = Integer.parseInt(ms);
                    Integer dateM = Integer.parseInt(ds);

                    System.out.println("Checking if admin is deleting date after today's date");
                    System.out.println("year - " + year + " month - " + month + " date - " + dateM);

                    LocalDate date2 = LocalDate.of(year, month, dateM);
                    System.out.println("Second date is - " + date2);

                    if (date2.isAfter(today)) {
                        System.out.println("Admin can remove");
                        List<String> custDetails = new ArrayList<String>();
                        custDetails = movieMap.get(movieID);
                        movieMap.remove(movieID);

                        System.out.println("Movie slot successfully removed!");
                        printMovieDataForAMovie(movieName);

                        // Looking for next show
                        String[] custIds = custDetails.get(1).split(",");
                        List<String> customerIdsArr = Arrays.asList(custIds);
                        if (custIds.length > 0) {
                            // Removing from client data

                            // Calculating total capacity of all clients
                            // int totalCapacity = 0;
                            // for(String clients:customerIdsArr) {
                            // int capacityOFOneClient =
                            // getClientData().get(clients).get(movieName).get(movieID);
                            // totalCapacity+=capacityOFOneClient;
                            // }

                            // List<String> clientDetailsWithTickets = new ArrayList<String>();
                            Map<String, Integer> clientDetailsWithTickets = new ConcurrentHashMap<String, Integer>();

                            System.out.println("Client details with tickets are -- ");
                            for (String onlyCustDetails : customerIdsArr) {
                                clientDetailsWithTickets.put(onlyCustDetails,
                                        getClientData().get(onlyCustDetails).get(movieName).get(movieID));
                                System.out.println("Customer - " + onlyCustDetails + " capacity - "
                                        + getClientData().get(onlyCustDetails).get(movieName).get(movieID));

                            }

                            // System.out.println("All data of old customers - "+clientDetailsWithTickets);
                            // System.out.println("Total capacity of that movie show - "+totalCapacity);

                            // Canceling movie tickets for the customer
                            System.out.println("Removing from client database");
                            for (String customers : customerIdsArr) {
                                getClientData().get(customers).get(movieName).remove(movieID);
                            }

                            printClientDatabase();

                            // Looking for next show
                            List<String> sortedSlots = new ArrayList<String>();
                            for (Entry<String, List<String>> map : movieMap.entrySet()) {
                                sortedSlots.add(map.getKey());
                            }
                            sortedSlots.add(movieID);

                            Collections.sort(sortedSlots, new Comparator<String>() {

                                @Override
                                public int compare(String o1, String o2) {
                                    String date1s = o1.substring(4, o1.length());
                                    String date2s = o2.substring(4, o2.length());

                                    System.out.println("Dates to be parsed - " + date1s + " " + date2s);

                                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");

                                    Date date1, date2;
                                    try {
                                        date1 = sdf.parse(date1s);
                                        date2 = sdf.parse(date2s);

                                        if (date1.compareTo(date2) < 0) {
                                            return -1;
                                        } else if (date1.compareTo(date2) > 0) {
                                            return 1;
                                        } else {
                                            // Character[] charArr = new Character[]{'M','A','E'};
                                            List<Character> chararr = new ArrayList<Character>(
                                                    Arrays.asList('M', 'A', 'E'));
                                            Character day1c = o1.charAt(3);
                                            Character day2c = o2.charAt(3);
                                            if (chararr.indexOf(day1c) < chararr.indexOf(day2c)) {
                                                return -1;
                                            } else {
                                                return 1;
                                            }

                                        }
                                    } catch (ParseException e) {

                                        e.printStackTrace();
                                    }
                                    return 0;
                                }
                            });

                            System.out.println("Sorted slots printing-----");
                            for (String s : sortedSlots) {
                                System.out.println(s);
                            }

                            String slotDeleted = movieID;
                            int ind = sortedSlots.indexOf(slotDeleted);

                            List<String> custIdsForBooking = new ArrayList<String>(customerIdsArr);
                            // for(int i=ind+1;i<sortedSlots.size();i++) {
                            // List<String> ls = movieMap.get(sortedSlots.get(i));
                            // String newSlotToBeBookedAt = sortedSlots.get(i);
                            //// int newCapa = Integer.parseInt(ls.get(0));
                            //// if(totalCapacity <= newCapa) {
                            // System.out.println("Booking the tickets for customers- "+custDetails.get(1)+"
                            // from deleted slot "+slotDeleted+" to "+sortedSlots.get(i));
                            //
                            // //CallBookMovieShow
                            // for(String customersToBook:custIdsForBooking) {
                            // System.out.println("TRying to book "+customersToBook+" for
                            // "+newSlotToBeBookedAt);
                            // String resultOfBooking = bookMovieTickets(customersToBook,
                            // newSlotToBeBookedAt, movieName,
                            // clientDetailsWithTickets.get(customersToBook),false);
                            // if(resultOfBooking.contains("Success")) {
                            // System.out.println("Booking done for "+customersToBook+" customer");
                            // custIdsForBooking.remove(customersToBook);
                            // }
                            // }
                            // }

                            for (String customersToBook : custIdsForBooking) {
                                boolean booked = false;
                                for (int i = ind + 1; i < sortedSlots.size(); i++) {
                                    String newSlotToBeBookedAt = sortedSlots.get(i);
                                    System.out.println(
                                            "TRying to book " + customersToBook + " for " + newSlotToBeBookedAt);
                                    String resultOfBooking = bookMovieTickets(customersToBook, newSlotToBeBookedAt,
                                            movieName,
                                            clientDetailsWithTickets.get(customersToBook), false);
                                    if (resultOfBooking.contains("Success")) {
                                        System.out.println(
                                                "Booking done for " + customersToBook + " customer in the slot "
                                                        + newSlotToBeBookedAt);
                                        booked = true;
                                        break;
                                    }
                                }
                                if (!booked) {
                                    System.out.println("Sorry! booking could not be done for " + customersToBook);
                                }

                            }

                            // if(custIdsForBooking.size()==0) {
                            // System.out.println("All customers are booked!!");
                            // }else {
                            // System.out.println("All customers are not booked!! Not booked customers are -
                            // ");
                            // for(String s:custIdsForBooking) {
                            // System.out.println(s);
                            // }
                            // }

                        }

                        movieSlotsRemoved = true;
                    } else {
                        System.out.println("Admin cannot remove as the date is before today's date");
                        movieSlotsRemoved = false;
                    }

                } else {
                    System.out.println("Movie ID not exist for this show! Deletion could not be performed");
                    movieSlotsRemoved = false;
                }
            } else {
                System.out.println("Movie does not exist for this show! Deletion could not be performed");
                movieSlotsRemoved = false;
            }
        } else {
            System.out.println("Sorry! Admin cannot remove slots for different server");
            movieSlotsRemoved = false;
        }

        return movieSlotsRemoved;
        // throw new UnsupportedOperationException("Unimplemented method
        // 'removeMovieSlots'");
    }

    @Override
    public String listMovieShowsAvailability(String movieName, boolean isOwnClient) {
        Map<String, List<String>> movieMap = getMovieData().get(movieName);
        String finalResultStr = new String();

        /*********** new code ***********/
        if (isOwnClient) {
            String res2 = new String();
            List<String> res = new ArrayList<String>();

            if (movieMap == null) {
                System.out.println("For server " + getServerName() + " . This movie is not available");
            } else {
                for (Entry<String, List<String>> e : movieMap.entrySet()) {
                    StringBuilder sb = new StringBuilder();
                    String movieID = e.getKey(); // movieId
                    String capacity = e.getValue().get(0);
                    sb.append(movieID);
                    sb.append("-");
                    sb.append(capacity);
                    res.add(sb.toString());
                }
            }
            // Look in other servers
            String arg = "listMovieShowsAvailability," + movieName;
            switch (getServerName()) {
                case "Atwater":
                    res2 = callOtherServers("Atwater", arg);
                    break;

                case "Verdun":
                    res2 = callOtherServers("Verdun", arg);
                    break;

                case "Outremont":
                    res2 = callOtherServers("Outremont", arg);
                    break;
            }
            if (res2 == null || res2.equals("")) {
                finalResultStr = String.join(",", res);
            } else {
                String temp = String.join(",", res);
                StringBuilder tempResult = new StringBuilder(temp);
                tempResult = tempResult.append("," + res2);
                finalResultStr = tempResult.toString();
            }

        } else {
            // Only need local server's data
            List<String> res = new ArrayList<String>();

            if (movieMap == null) {
                System.out.println("For server " + getServerName() + " . This movie is not available");
            } else {
                for (Entry<String, List<String>> e : movieMap.entrySet()) {
                    StringBuilder sb = new StringBuilder();
                    String movieID = e.getKey(); // movieId
                    String capacity = e.getValue().get(0);
                    sb.append(movieID);
                    sb.append("-");
                    sb.append(capacity);
                    res.add(sb.toString());
                }
                finalResultStr = String.join(";", res);
            }
        }
        return finalResultStr;
        // throw new UnsupportedOperationException("Unimplemented method
        // 'listMovieShowsAvailability'");
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets,
            boolean isOwnClient) {
        String id = movieID;
        // ATWM040223
        String server = id.substring(0, 3);
        String thisServer = getServerName().substring(0, 3).toUpperCase();

        if (server.equals(thisServer)) {
            // Tickets to be booked in it's own server
            if (getMovieData().containsKey(movieName)) {
                Map<String, List<String>> movieMap = getMovieData().get(movieName);
                if (movieMap.containsKey(movieID)) {
                    List<String> custDetails = movieMap.get(movieID);
                    Integer remainingCapacity = Integer.parseInt(custDetails.get(0)) - numberOfTickets;

                    if (remainingCapacity >= 0) {
                        custDetails.set(0, remainingCapacity.toString());
                        movieMap.put(movieID, custDetails);

                        // checking if the customer has already bought tickets in this show
                        List<String> custs = new ArrayList<String>();
                        String temp = custDetails.get(1);
                        custs = Arrays.asList(temp.split(","));
                        if (custs.contains(customerID)) {
                            System.out.println("Customer already has booked before so entty present");
                        } else {
                            System.out.println("Customer needs to be added to the database");
                            StringBuilder sb = new StringBuilder();
                            sb.append(custDetails.get(1) + "," + customerID);
                            custDetails.set(1, sb.toString());
                            movieMap.replace(movieID, custDetails);
                        }

                        printMovieDataForAMovie("Avatar");

                        // Putting in the client map
                        Map<String, Map<String, Map<String, Integer>>> clientData = getClientData();
                        if (clientData.containsKey(customerID)) {
                            Map<String, Map<String, Integer>> movieData = clientData.get(customerID);
                            if (movieData.containsKey(movieName)) {
                                Map<String, Integer> showData = movieData.get(movieName);
                                if (showData.containsKey(movieID)) {
                                    showData.replace(movieID, showData.get(movieID) + numberOfTickets);
                                } else {
                                    // Create the movie ID
                                    showData.put(movieID, numberOfTickets);
                                }
                            } else {
                                // Create movieName
                                movieData.put(movieName, new ConcurrentHashMap<String, Integer>() {
                                    {
                                        put(movieID, numberOfTickets);

                                    }
                                });
                            }
                        } else {
                            // Create customer
                            clientData.put(customerID, new ConcurrentHashMap<String, Map<String, Integer>>() {
                                {
                                    put(movieName, new HashMap<String, Integer>() {
                                        {
                                            put(movieID, numberOfTickets);
                                        }
                                    });
                                }
                            });
                        }

                    } else {
                        System.out.println("Sorry! No capacity left to book the tickets!");
                        return "Failure";
                    }

                    printMovieDataForAClient(customerID);
                    return "Success";
                } else {
                    System.out.println("This show is not available!");
                    return "Failure";
                }
            } else {
                System.out.println("This movie does not exist");
                return "Failure";
            }
        } else {
            // Call other server
            String bookingOtherServer = null;
            String otherBookings = getBookingSchedule(customerID, true);
            int count = 0;
            if (otherBookings == null || otherBookings.equals("")) {
                // do nothing
                System.out.println("Got no bookings from other servers. Can book");
            } else {
                System.out.println("Got other bookings-- ");
                /************** Check for 7 days max 3 tickets ********************/

                // Calculating the date to compare with
                String movieIDMonthStr = movieID.substring(6, 8);
                String movieIDMonth = new String();
                if (movieIDMonthStr.charAt(0) == '0') {
                    movieIDMonth = movieIDMonthStr.substring(1, 2);
                } else {
                    movieIDMonth = movieIDMonthStr;
                }
                String movieIDDateStr = movieID.substring(4, 6);
                String movieIDDate = new String();
                if (movieIDDateStr.charAt(0) == '0') {
                    movieIDDate = movieIDDateStr.substring(1, 2);
                } else {
                    movieIDDate = movieIDDateStr;
                }
                String movieIDYearStr = movieID.substring(8, movieID.length());
                movieIDYearStr = "20" + movieIDYearStr;
                System.out.println("Final dates---- " + movieIDMonth + " " + movieIDDate);
                Integer yearForMovieID = Integer.parseInt(movieIDYearStr);
                Integer monthForMovieID = Integer.parseInt(movieIDMonth);
                Integer dateForMovieID = Integer.parseInt(movieIDDate);

                // Checking if admin is deleting date after today's date
                System.out.println("Checking if client has bookings more than 3 wali thingy");
                System.out.println(
                        "year - " + yearForMovieID + " month - " + monthForMovieID + " date - " + dateForMovieID);

                LocalDate date1 = LocalDate.of(yearForMovieID, monthForMovieID, dateForMovieID);
                System.out.println("This movie ID's show date is - " + date1);

                date1 = date1.minusDays(6);

                String[] otherBookingArr = otherBookings.split(";");
                for (String s : otherBookingArr) {
                    System.out.println("Bookings of other server - " + s);
                    String info = s;
                    String shows = info.split("_")[1];
                    // ATWM040223/2-ATWM020223/3
                    String[] splitShows = shows.split("-"); // ATWM040223/2 , ATWM020223/3
                    for (String showsStr : splitShows) {
                        // ATWM040223/2
                        String onlyShow = showsStr.split("/")[0];
                        String ticketsNum = showsStr.split("/")[1];
                        // ATWM040223

                        // Calculating client's other booking movie id's date
                        String monthStr = onlyShow.substring(6, 8);
                        String ms = new String();
                        if (monthStr.charAt(0) == '0') {
                            ms = monthStr.substring(1, 2);
                        } else {
                            ms = monthStr;
                        }
                        String ds = new String();
                        String dateStr = onlyShow.substring(4, 6);
                        if (dateStr.charAt(0) == '0') {
                            ds = dateStr.substring(1, 2);
                        } else {
                            ds = dateStr;
                        }
                        String yearStr = onlyShow.substring(8, onlyShow.length());
                        yearStr = "20" + yearStr;
                        System.out.println("Final dates---- " + ms + " " + ds);
                        Integer year = Integer.parseInt(yearStr);
                        Integer month = Integer.parseInt(ms);
                        Integer dateM = Integer.parseInt(ds);

                        // Checking if admin is deleting date after today's date
                        System.out.println("Checking if client has bookings more than 3 wali thingy");
                        System.out.println("year - " + year + " month - " + month + " date - " + dateM);

                        LocalDate date2 = LocalDate.of(year, month, dateM);
                        System.out.println("Client show date is - " + date2);

                        if (date1.compareTo(date2) < 0) {
                            System.out.println("This date " + date2 + " is within 7 days");
                            String serverComp1 = onlyShow.substring(0, 1);
                            String serverComp2 = getServerName().substring(0, 1);
                            if (serverComp1.equals(serverComp2)) {
                                System.out.println("In the same server no need to add");
                            } else {
                                int tick = Integer.parseInt(ticketsNum);
                                count += tick;
                                System.out.println("Value of count: " + count);
                            }

                        }

                    }
                }

            }

            if (count + numberOfTickets > 3) {
                System.out.println(
                        "Cannot book ticket in other server because you already have 3 tickets in the past 1 week");
            } else {
                System.out.println("Can book tickets in other server");
                bookingOtherServer = CallSpecificServer(server,
                        "bookMovieTickets," + customerID + "-" + movieID + "-" + movieName + "-" + numberOfTickets,
                        getServerName());
            }

            return bookingOtherServer;
        }
        // throw new UnsupportedOperationException("Unimplemented method
        // 'bookMovieTickets'");
    }

    @Override
    public String getBookingSchedule(String customerID, boolean isOwnClient) {
        Map<String, Map<String, Integer>> clientData = getClientData().get(customerID);
        List<String> result = new ArrayList<String>();
        String finalResultStr = new String();

        if (clientData != null) {
            System.out.println("*****Inside client data is not null");
            for (Entry<String, Map<String, Integer>> itr : clientData.entrySet()) {
                // StringBuilder sb = new StringBuilder();
                String movieName = new String();
                movieName = itr.getKey();
                Map<String, Integer> m = itr.getValue();
                StringBuilder sb = new StringBuilder();
                for (Entry<String, Integer> i : m.entrySet()) {
                    sb.append(i.getKey());
                    sb.append("/");
                    sb.append(i.getValue());
                    sb.append("-");
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }

                // New code -
                if (!sb.toString().equals("")) {
                    result.add(movieName + "_" + sb.toString());
                    System.out.println("*****Added to result - " + movieName + "," + sb.toString());

                    // finalResultStr = String.join(";", result);
                    finalResultStr = String.join(",", result);
                }

            }
        }

        if (isOwnClient) {
            String argPassed = "getBookingSchedule," + customerID;
            System.out.println("***Inside isownclient and now calling other servers " + argPassed);

            String detailsFromOtherServers = callOtherServers(getServerName(), argPassed);
            if (detailsFromOtherServers == null || detailsFromOtherServers.equals("")) {

            } else {
                System.out.println("***Booking schedule we got from other servers for " + customerID + " is--");
                // for (String o : detailsFromOtherServers) {
                // System.out.println("This is it " + o);
                // result.add(o);
                // }
                if (finalResultStr.equals("") || finalResultStr == null) {
                    // do not append ;
                    finalResultStr = detailsFromOtherServers;
                } else {
                    // already other booking exist, append ;
                    StringBuilder temp = new StringBuilder(finalResultStr);
                    // temp = temp.append(";" + detailsFromOtherServers); COMMA
                    temp = temp.append("," + detailsFromOtherServers);
                    finalResultStr = temp.toString();
                }

            }

        }

        return finalResultStr;
        // throw new UnsupportedOperationException("Unimplemented method
        // 'getBookingSchedule'");
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String result = "";
        // Cancelling from moviedata

        if (getServerName().substring(0, 1).equals(movieID.substring(0, 1))) {
            // Same server
            if (getMovieData().containsKey(movieName)) {
                Map<String, List<String>> movieMap = getMovieData().get(movieName);
                if (movieMap.containsKey(movieID)) {
                    List<String> cdata = movieMap.get(movieID);
                    String clients = cdata.get(1);
                    List<String> lc = Arrays.asList(clients.split(","));
                    if (lc.contains(customerID)) {

                        // Canceling from the client data
                        Map<String, Map<String, Map<String, Integer>>> clientData = getClientData();

                        Integer c = clientData.get(customerID).get(movieName).get(movieID);
                        System.out.println("Deleting " + c + " tickets from " + numberOfTickets);
                        if (c.equals(numberOfTickets) || c == -1) {
                            System.out.println("Customer is cancelling all the tickets so remove the customer");
                            // From the moviedata hashmap
                            List<String> listStr = new ArrayList<String>(lc);
                            listStr.remove(customerID);

                            String newClients = String.join(",", listStr);
                            System.out.println("New clients after removing - " + newClients);
                            // lc.set(1, newClients);
                            Integer newCapa = Integer.parseInt(cdata.get(0)) + numberOfTickets;
                            // lc.set(0, newCapa.toString());
                            System.out.println("Movie tickets cancelled!!");

                            cdata.set(1, newClients);
                            cdata.set(0, newCapa.toString());

                            // From the client data hashmap
                            clientData.get(customerID).get(movieName).remove(movieID);
                            result = "Success";
                        } else if (c > numberOfTickets) {
                            System.out.println("Only a few tickets have been cancelled");
                            // Updating in the client data
                            Integer newcapa = c - numberOfTickets;
                            Map<String, Integer> m = clientData.get(customerID).get(movieName);
                            m.replace(movieID, newcapa);

                            // Updating in the moviedata
                            Integer newCapa = Integer.parseInt(cdata.get(0)) + numberOfTickets;
                            // lc.set(0, newCapa.toString());
                            cdata.set(0, newCapa.toString());
                            result = "Success";
                        } else {
                            System.out.println("You cannot remove more slots than booked!!");
                            result = "Failure";
                        }

                    } else {
                        System.out.println("This movie show is not booked by this customer!!");
                        result = "Failure";
                    }
                } else {
                    System.out.println("This movie ID does not exist!");
                    result = "Failure";
                }
            } else {
                System.out.println("No bookings are done for this movie!");
                result = "Failure";
            }
        } else {
            // Different server
            result = CallSpecificServer(movieID.substring(0, 3),
                    "cancelMovieTickets," + customerID + "-" + movieID + "-" + movieName + "-" + numberOfTickets,
                    getServerName());
        }

        printMovieDataForAMovie(movieName);
        printMovieDataForAClient(customerID);

        return result;
        // throw new UnsupportedOperationException("Unimplemented method
        // 'cancelMovieTickets'");
    }

    @Override
    public String exchangeTickets(String customerID, String oldMovieName, String movieID, String newMovieID,
            String newMovieName, int numberOfTickets) {
        System.out.println("Inside exchange tickets called from Implementation");
        String exchangeResult = null;

        /************** APPROACH 1 *************/
        // booking the new slot
        // String bookingResult = bookMovieTickets(customerID, newMovieID, newMovieName,
        // numberOfTickets, true);
        // if (bookingResult.equals("Successfully booked!")) {
        // // Now can cancel
        // String cancelResult = cancelMovieTickets(customerID, movieID, oldMovieName,
        // numberOfTickets);
        // if (cancelResult.equals("Success")) {
        // exchangeResult = "Success";
        // } else {
        // // cancel prev
        // String secondCancel = cancelMovieTickets(customerID, newMovieID,
        // newMovieName, numberOfTickets);
        // if (secondCancel.equals("Success")) {
        // exchangeResult = "Fail";
        // } else {
        // exchangeResult = "Fail";
        // }
        // }
        // } else {
        // exchangeResult = "Fail";
        // }

        /************** APPROACH 2 *************/
        // Canceling first
        String cancelResult = cancelMovieTickets(customerID, movieID, oldMovieName, numberOfTickets);
        if (cancelResult.equals("Success")) {
            // Now trying to book the new slot
            String bookingResult = bookMovieTickets(customerID, newMovieID, newMovieName, numberOfTickets, true);
            if (bookingResult.equals("Successfully booked!")) {
                exchangeResult = "Success";
            } else {
                // will have to book again
                String bookAgain = bookMovieTickets(customerID, movieID, oldMovieName, numberOfTickets, true);
                exchangeResult = "Failure";
            }
        } else {
            exchangeResult = "Failure";
        }

        return exchangeResult;
    }

    // Helper methods--
    private void printMovieDataForAMovie(String movieName) {
        System.out.println("Called printMovieDataForAMovie-------");
        Map<String, List<String>> movieMap = getMovieData().get(movieName);
        System.out.println("Printing information about: " + movieName);
        if (movieMap == null) {
            System.out.println("This movie does not exist in this server!");
        } else {
            for (Entry<String, List<String>> map : movieMap.entrySet()) {
                System.out.println("[Movie id : " + map.getKey());
                List<String> moviedb = map.getValue();
                System.out.print(" || Movie capacity for this movie slot: " + moviedb.get(0));
                System.out.print(" || Clients who booked this show are - " + moviedb.get(1) + "]");
            }
        }

    }

    private void printMovieDataForAClient(String custNo) {
        System.out.println("Called printMovieDataForAClient-------");

        Map<String, Map<String, Map<String, Integer>>> clientData = this.getClientData();
        Map<String, Map<String, Integer>> movieData = clientData.get(custNo);

        if (movieData == null) {
            System.out.println("No movie data for this customer");
        } else {
            for (Entry<String, Map<String, Integer>> itr : movieData.entrySet()) {
                System.out.println("Movie : " + itr.getKey());
                Map<String, Integer> m = itr.getValue();
                System.out.println("MovieShows booked are - ");
                for (Entry<String, Integer> i : m.entrySet()) {
                    System.out.println("Show is " + i.getKey() + " capacity " + i.getValue());
                }
            }
        }

    }

    private void printClientDatabase() {
        System.out.println("Called printClientDatabase-------");

        Map<String, Map<String, Map<String, Integer>>> clientData = this.getClientData();

        for (Entry<String, Map<String, Map<String, Integer>>> itr : clientData.entrySet()) {
            System.out.println("Client : " + itr.getKey());
            Map<String, Map<String, Integer>> dataForClient = itr.getValue();
        }

    }

    private LocalDate getLocalDateFromMovieID(String movieID) {
        String movieIDMonthStr = movieID.substring(6, 8);
        String movieIDMonth = new String();
        if (movieIDMonthStr.charAt(0) == '0') {
            movieIDMonth = movieIDMonthStr.substring(1, 2);
        } else {
            movieIDMonth = movieIDMonthStr;
        }
        String movieIDDateStr = movieID.substring(4, 6);
        String movieIDDate = new String();
        if (movieIDDateStr.charAt(0) == '0') {
            movieIDDate = movieIDDateStr.substring(1, 2);
        } else {
            movieIDDate = movieIDDateStr;
        }
        String movieIDYearStr = movieID.substring(8, movieID.length());
        movieIDYearStr = "20" + movieIDYearStr;
        System.out.println("Final dates---- " + movieIDMonth + " " + movieIDDate);
        Integer yearForMovieID = Integer.parseInt(movieIDYearStr);
        Integer monthForMovieID = Integer.parseInt(movieIDMonth);
        Integer dateForMovieID = Integer.parseInt(movieIDDate);

        LocalDate date1 = LocalDate.of(yearForMovieID, monthForMovieID, dateForMovieID);
        System.out.println("This movie ID's " + movieID + " show date is - " + date1);

        return date1;
    }

    private String callOtherServers(String serverName, String method) {
        System.out.println("Inside callOtherServer. This server name is --- " + serverName);
        List<String> result = new ArrayList<String>();
        String finalResultStr = new String();

        try {
            DatagramSocket socket1 = new DatagramSocket();
            DatagramSocket socket2 = new DatagramSocket();

            byte[] b = method.getBytes();
            InetAddress ip = InetAddress.getLocalHost();

            int port1 = 0, port2 = 0;

            switch (serverName) {
                case "Atwater":
                    port1 = 8001;
                    port2 = 44555;
                    break;

                case "Verdun":
                    port1 = 44551;
                    port2 = 44556;
                    break;

                case "Outremont":
                    port1 = 44552;
                    port2 = 44554;
                    break;
            }

            // int port1 = 44551;
            // int port2 = 44552;

            System.out.println("Port 1 is-----" + port1);
            System.out.println("Port 2 is-----" + port2);

            DatagramPacket packet1 = new DatagramPacket(b, b.length, ip, port1);
            DatagramPacket packet2 = new DatagramPacket(b, b.length, ip, port2);

            socket1.send(packet1);
            socket2.send(packet2);
            System.out.println("VIA UDP - Packet sent from server " + getServerName() + " to server!!!");

            // Receive from both the servers
            byte[] b1 = new byte[1024];
            byte[] b2 = new byte[1024];
            DatagramPacket packetFrom1 = new DatagramPacket(b1, b1.length);
            DatagramPacket packetFrom2 = new DatagramPacket(b2, b2.length);
            socket1.receive(packetFrom1);
            socket2.receive(packetFrom2);

            System.out.println("VIA UDP - Packet received in " + getServerName() + "server from other two servers!");

            String res1 = new String(packetFrom1.getData()).trim();
            String res2 = new String(packetFrom2.getData()).trim();

            if ((res1 != null && !res1.equals("")) && (res2 != null && !res2.equals(""))) {
                StringBuilder temp = new StringBuilder(res1);
                temp.append("," + res2);
                finalResultStr = temp.toString();
            } else {
                if (res1 == null || res1.equals("")) {
                    finalResultStr = res2;
                } else {
                    finalResultStr = res1;
                }
            }

            // ByteArrayInputStream bais = new ByteArrayInputStream(packetFrom1.getData());
            // DataInputStream in = new DataInputStream(bais);
            // while (in.available() > 0) {
            // String element = in.readUTF();
            // if(element==null || element.equals("")) {
            // break;
            // }
            // System.out.println("FFFFFFfffinallllll result -- "+element);
            // result.add(element);
            // }

            // ByteArrayInputStream bais2 = new ByteArrayInputStream(packetFrom2.getData());
            // DataInputStream in2 = new DataInputStream(bais2);
            // while (in2.available() > 0) {
            // String element = in2.readUTF();
            // if(element==null || element.equals("")) {
            // break;
            // }
            // System.out.println("FFFFFFfffinallllll result number 2 -- "+element);
            // result.add(element);
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // if(result.size()==0) {
        // return null;
        // }else {
        // return result;
        // }
        return finalResultStr;
    }

    private String CallSpecificServer(String server, String method, String fromServer) {
        System.out.println("Called CallSpecificServer. Method to be called is --- " + method);
        String packetFromOtherServer = null;
        try {
            /************ Now sending packet **************/
            DatagramSocket socket = new DatagramSocket();

            byte[] b = method.getBytes();
            InetAddress ip = InetAddress.getLocalHost();

            int port = 0;

            switch (server) {
                case "ATW":
                    if (fromServer.equals("Verdun")) {
                        port = 44551;
                    } else {
                        port = 44552;
                    }
                    break;

                case "VER":
                    if (fromServer.equals("Atwater")) {
                        port = 8001;
                    } else {
                        port = 44554;
                    }
                    break;

                case "OUT":
                    if (fromServer.equals("Atwater")) {
                        port = 44555;
                    } else {
                        port = 44556;
                    }
                    break;
            }

            DatagramPacket packet = new DatagramPacket(b, b.length, ip, port);

            socket.send(packet);
            System.out.println("VIA UDP - Packet sent from server " + getServerName() + " to server " + server);

            /************ Now receiving packet **************/
            byte[] bRec = new byte[1024];
            DatagramPacket packetFrom = new DatagramPacket(bRec, bRec.length);
            socket.receive(packetFrom);

            System.out.println("VIA UDP - Packet received in " + getServerName() + "server from other two servers!");

            packetFromOtherServer = new String(packetFrom.getData()).trim();
            System.out.println("Got packet from specific server - " + packetFromOtherServer);

            // return packetFromOtherServer;

            // ByteArrayInputStream bais = new ByteArrayInputStream(packetFrom.getData());
            // DataInputStream in = new DataInputStream(bais);
            //// List<String> result = new ArrayList<String>();
            // int c = 0;
            // while (in.available() > 0) {
            // if(c==0) {
            // c=1;
            // String methodThatWasCalled = in.readUTF();
            // System.out.println("Method that was called "+methodThatWasCalled);
            // }else {
            // String element = in.readUTF();
            // if(element==null || element.equals("")) {
            // break;
            // }
            // System.out.println("FFFFFFfffinallllll result -- "+element);
            // result.add(element);
            // }
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return packetFromOtherServer;
    }

}
