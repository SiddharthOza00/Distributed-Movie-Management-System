package com.Replica4.Server;

//import Interface.BookingInterface;

import com.Replica4.Interface.Interface;
import com.Replica4.Log.Logger;
import com.Replica4.Model.ClientModel;
import com.Replica4.Model.MovieModel;
import org.omg.CORBA.ORB;

import javax.jws.WebService;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebService(endpointInterface = "com.Replica4.Server.WebInterface")
public class BookingImplementation implements WebInterface {
    public static final int ATW_SERV_PORT = 9955;
    public static final int VER_SERV_PORT = 9944;
    public static final int OUT_SERV_PORT = 9933;
    public static final int MINVALUE = Integer.MIN_VALUE;
    private String serverId;
    private String serverName;
    private Map<String, Map<String, MovieModel>> moviesEvents;
    private Map<String, Map<String, List<String>>> clientEvents;
    private Map<String, ClientModel> serverClients;
    private Map<String, Integer> movieBookings;

    private ORB orb;

    public void setORB(ORB orb_val){
        orb = orb_val;
    }


    public BookingImplementation(String serverId, String serverName)  {
        super();
        this.serverId = serverId;
        this.serverName = serverName;
        moviesEvents = new ConcurrentHashMap<>();
        moviesEvents.put("Avatar",new ConcurrentHashMap<>());
        moviesEvents.put("Avengers",new ConcurrentHashMap<>());
        moviesEvents.put("Titanic",new ConcurrentHashMap<>());
        clientEvents = new ConcurrentHashMap<>();
        serverClients = new ConcurrentHashMap<>();
        movieBookings = new ConcurrentHashMap<>();
    }


    private static int getServerPort(String serverBranch){
        if(serverBranch.equalsIgnoreCase("ATW")){
            return ATW_SERV_PORT;
        }
        else if(serverBranch.equalsIgnoreCase("VER")){
            return VER_SERV_PORT;
        } else if (serverBranch.equalsIgnoreCase("OUT")) {
            return OUT_SERV_PORT;
        }
        return 1;
    }



    public String addMovieSlots(String movieId, String movieName, int bookingCapacity)  {
        String response;

        Date date = new Date();
        String strDateFormat = "yyMMdd";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String dateToday = dateFormat.format(date);
        int today = Integer.parseInt(dateToday);
        String dateOfMovie = movieId.substring(8,10)+""+movieId.substring(6,8)+""+movieId.substring(4,6);
        int movieDate = Integer.parseInt(dateOfMovie);
        if(movieDate-today>7 || movieDate-today<0){
            response = "Failure";
            try {
                Logger.serverLog(movieId, "null", " RMI addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            }catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        if (moviesEvents.get(movieName).containsKey(movieId)) {
            if (moviesEvents.get(movieName).get(movieId).getMovieCapacity() <= bookingCapacity) {
                moviesEvents.get(movieName).get(movieId).setMovieCapacity(bookingCapacity);
                response = "Success";
                try {
                    Logger.serverLog(movieId, "null", " RMI addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            else{
                response = "Failure";
                try {
                    Logger.serverLog(movieId, "null", " RMI addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            return response;
        }
        if(MovieModel.findMovieServer(movieId).equals(serverName)){
            MovieModel movieModel = new MovieModel(movieName, movieId, bookingCapacity);
            Map<String, MovieModel> moviesHashMap = moviesEvents.get(movieName);
            moviesHashMap.put(movieId, movieModel);
            moviesEvents.put(movieName, moviesHashMap);
            response = "Success";
            try {
                Logger.serverLog(movieId, "null", " RMI addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            response = "Failure";
            try {
                Logger.serverLog(movieId, "null", " RMI addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private synchronized boolean movieSlotExists(String movieName, String movieId){
        return moviesEvents.get(movieName).containsKey(movieId);
    }
    private synchronized boolean isSameServerMovieSlot(String movieId){
        return MovieModel.findMovieServer(movieId).equals(serverName);
    }


    public String removeMovieSlots(String movieId, String movieName) {
        String response;
        if(MovieModel.findMovieServer(movieId).equals(serverName)){
            if(moviesEvents.get(movieName).containsKey(movieId)){
                List<String> clientsList = moviesEvents.get(movieName).get(movieId).getRegisteredClients();
                moviesEvents.get(movieName).remove(movieId);
                addCustomersToNextMovieSlot(movieId, movieName, clientsList);
                response = "Success";
                try {
                    Logger.serverLog(movieId, "null", " RMI removeMovieSlots ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                response = "Failure";
                try {
                    Logger.serverLog(movieId, "null", " RMI removeMovieSlots ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
        else {
            response = "Failure";
            try {
                Logger.serverLog(movieId, "null", " RMI removeMovieSlots ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

//    private void addCustomersToNextMovieSlot(String movieId, String movieName, List<String> clientsList)  {
//        for (String customerID : clientsList) {
//            if (customerID.substring(0, 3).equals(movieId.substring(0,3))) {
//                clientEvents.get(customerID).get(movieName).remove(movieId);
//                String nextSameEventResult = getNextSameEvent(moviesEvents.get(movieName).keySet(), movieName, movieId);
//                if (nextSameEventResult.equals("Failure")) {
//                    return;
//                } else {
//                    bookMoviesTickets(customerID, nextSameEventResult, movieName,0);
//                }
//            } else {
//                sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeEvent", customerID, movieName, movieId,MINVALUE );
//            }
//        }
//    }

    private void addCustomersToNextMovieSlot(String movieId, String movieName, List<String> clientsList) {
        String response;
        for (String customerID : clientsList) {
            if (customerID.substring(0, 3).equals(movieId.substring(0,3))) {
                int tickets = movieBookings.get(customerID+movieId+movieName);
                removeMovieIfAlreadyExists(customerID,movieName,movieId);
                String nextAvailableSlot = getNextSameEvent(moviesEvents.get(movieName).keySet(),movieName, movieId);
                if (nextAvailableSlot.equals("Failure")) {
                    response= "Getting next available slot for you" + nextAvailableSlot;
                    try {
                        Logger.serverLog(serverId, customerID, "addCustomersToNextMovieSlot", "old movieId: "+movieId+ "movieName: "+ movieName + " ",response );
                    }
                    catch( Exception e){
                        e.printStackTrace();
                    }return;
                } else {
                    bookMoviesTickets(customerID, nextAvailableSlot, movieName, tickets);
                }
            } else {
                sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeEvent", customerID, movieName, movieId,MINVALUE );
            }
        }
    }

    private String getNextSameEvent(Set<String> keySet, String movieName, String movieId) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(movieId);
        Collections.sort(sortedIDs, new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                Integer timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(movieId) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!moviesEvents.get(movieName).get(sortedIDs.get(i)).isHouseful()) {
                return sortedIDs.get(i);
            }
        }
        return "Failure";
    }

    @Override
    public String listMovieShowsAvailability(String movieName) {
        String response;
        Map<String,MovieModel> slots = moviesEvents.get(movieName);
        StringBuffer sb = new StringBuffer();
//        sb.append(serverName+" Server "+ movieName+":\n");
//        if(slots.size()==0){
//            sb.append("No Movie Slots for Movie: "+ movieName);
//        }
//        else{
//            for(MovieModel movies: slots.values()){
//                sb.append(" "+movies.toString()+" || ");
//            }
//            sb.append("\n=====================================\n");
//        }

        String server1, server2;

        if(slots.size() != 0){
            for(MovieModel movies: slots.values()){
                sb.append(movies.toString()+";");
            }
            sb.deleteCharAt(sb.length()-1);
        }
        if(serverId.equals("ATW")){
            server1 = sendUDPMessage(VER_SERV_PORT,"listMovieAvailability","null",movieName,"null",MINVALUE);
            server2 = sendUDPMessage(OUT_SERV_PORT,"listMovieAvailability","null",movieName,"null",MINVALUE);
        }
        else if(serverId.equals("VER")){
            server1 = sendUDPMessage(ATW_SERV_PORT,"listMovieAvailability","null",movieName,"null",MINVALUE);
            server2 = sendUDPMessage(OUT_SERV_PORT,"listMovieAvailability","null",movieName,"null",MINVALUE);
        }
        else
        {
            server1 = sendUDPMessage(ATW_SERV_PORT,"listMovieAvailability","null",movieName,"null",MINVALUE);
            server2 = sendUDPMessage(VER_SERV_PORT,"listMovieAvailability","null",movieName,"null",MINVALUE);
        }
        sb.append(server1).append(server2);

        response = sb.toString();
        try {
            Logger.serverLog(serverId, "null", " RMI listMovieShowsAvailability ", " movieName: " + movieName +  " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String bookMoviesTickets(String customerId, String movieId, String movieName, int numberOfTickets) {
        String response;
        checkIfClientExists(customerId);
        boolean bookingAgain = false;
        if (isSameServerMovieSlot(movieId)) {
            MovieModel bookedEvent = moviesEvents.get(movieName).get(movieId);

            if ((!bookedEvent.isHouseful()) && numberOfTickets <= bookedEvent.getMovieCapacity()) {
                if (clientEvents.containsKey(customerId)) {
                    if (clientEvents.get(customerId).containsKey(movieName)) {
                        if (clientHasSlot(customerId,movieName,movieId)) {
                            bookingAgain = true;
                        } else {
                            clientEvents.get(customerId).get(movieName).add(movieId);
                        }
                    }else{
                        if(isCustomerOfThisServer(customerId)){
                            addMovieNameAndMovie(customerId,movieName,movieId);
                        }
                    }
                }else {
                    if (!isCustomerOfThisServer(customerId)){
                        addCustomerAndMovie(customerId,movieName,movieId);
                    }
                }
                if(bookingAgain){
                    int oldQty = movieBookings.get(customerId+movieId+movieName);
                    response = "Success";
                    movieBookings.put(customerId+movieId+movieName,numberOfTickets+oldQty);
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() - (oldQty+numberOfTickets));

                    return response;
                }
                else if (moviesEvents.get(movieName).get(movieId).addRegisteredClientId(customerId) == MovieModel.SUCCESS) {
                    response = "Success";
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() - numberOfTickets);
                    movieBookings.put(customerId+movieId+movieName,numberOfTickets);
                } else if (moviesEvents.get(movieName).get(movieId).addRegisteredClientId(customerId) == MovieModel.HOUSE_FULL) {
                    response = "Failure";
                } else {
                    response = "Failure";
                }
                try {
                    Logger.serverLog(serverId, customerId, " CORBA bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                response = "Failure";
                try {
                    Logger.serverLog(serverId, customerId, " CORBA bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } return response;
        }else {
            if(clientHasSlot(customerId,movieName,movieId)){
                int oldQty = movieBookings.get(customerId+movieId+movieName);
                response = "Success";
                movieBookings.put(customerId+movieId+movieName,numberOfTickets+oldQty);
                moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() - (oldQty+numberOfTickets));
                return response;
            }
            if (!exceedWeeklyLimit(customerId)) {
                String serverResponse = sendUDPMessage(getServerPort(movieId.substring(0, 3)), "bookMovie", customerId, movieName, movieId,numberOfTickets);
                if (serverResponse.startsWith("Success:")) {
                    if (clientEvents.get(customerId).containsKey(movieName)) {
                        clientEvents.get(customerId).get(movieName).add(movieId);
                    } else {
                        List<String> temp = new ArrayList<>();
                        temp.add(movieId);
                        clientEvents.get(customerId).put(movieName, temp);
                    }
                }
                try {
                    Logger.serverLog(serverId, customerId, " CORBA bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                movieBookings.put(customerId+movieId+movieName,numberOfTickets);


                System.out.println(movieBookings);
                return serverResponse;
            } else {
                response = "Failure";
                try {
                    Logger.serverLog(serverId, customerId, " CORBA bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    private synchronized void addCustomerAndMovie(String customerId, String movieName, String movieId) {
        Map<String,List<String>> temp = new ConcurrentHashMap<>();
        List<String> temp2 = new ArrayList<>();
        temp2.add(movieId);
        temp.put(movieName,temp2);
        clientEvents.put(customerId,temp);
    }

    private synchronized void addMovieNameAndMovie(String customerId, String movieName, String movieId) {
        List<String> temp = new ArrayList<>();
        temp.add(movieId);
        clientEvents.get(customerId).put(movieName,temp);
    }

    private boolean isCustomerOfThisServer(String customerId) {
        return customerId.substring(0, 3).equals(serverId);
    }
    private synchronized boolean clientHasSlot(String customerId, String movieName, String movieId){
        if (clientEvents.get(customerId).containsKey(movieName)) {
            return  clientEvents.get(customerId).get(movieName).contains(movieId);
        }else{
            return false;
        }
    }
    private synchronized boolean checkIfClientExists(String customerId) {
        if(!serverClients.containsKey(customerId)){
            addNewCustomerToClients(customerId);
            return false;
        }else{
            return true;
        }
    }

    private boolean exceedWeeklyLimit(String movieId){
        int count=0;
        for(String index: clientEvents.get(movieId).keySet()){
            for(String mIndex : clientEvents.get(movieId).get(index))
            {
                if(!mIndex.substring(0, 3).equals(movieId.substring(0, 3))){
                    count++;
                    if(count>=3){
                        return true;
                    }
                }
            }
        }
        return false;
    }


//    private boolean exceedWeeklyLimit(String customerId, String movieDate) {
//        int limit = 0;
//        for(int i =0;i<3;i++){
//            List<String> registeredIds = new ArrayList<>();
//            switch (i) {
//                case 0:
//                    if (clientEvents.get(customerId).containsKey("Avatar")) {
//                        registeredIds = clientEvents.get(customerId).get("Avatar");
//                    }
//                    break;
//                case 1:
//                    if (clientEvents.get(customerId).containsKey("Avengers")) {
//                        registeredIds = clientEvents.get(customerId).get("Avengers");
//                    }
//                    break;
//                case 2:
//                    if (clientEvents.get(customerId).containsKey("Titanic")) {
//                        registeredIds = clientEvents.get(customerId).get("Titanic");
//                    }
//                    break;
//            }
//            for (String movieId : registeredIds) {
//                if (movieId.substring(6, 8).equals(movieDate.substring(2, 4)) && movieId.substring(8, 10).equals(movieDate.substring(4, 6))) {
//                    int week1 = Integer.parseInt(movieDate.substring(4, 6)) / 7;
//                    int week2 = Integer.parseInt(movieDate.substring(0, 2)) / 7;
////                    int diff = Math.abs(day2 - day1);
//                    if (week1 == week2) {
//                        limit++;
//                    }
//                }
//                if (limit == 3)
//                    return true;
//            }
//        }
//        return false;
//    }

    private String getNextAvailableSlot(Set<String> keySet, String movieName, String movieId) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(movieId);
        Collections.sort(sortedIDs, new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                Integer timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(movieId) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!moviesEvents.get(movieName).get(sortedIDs.get(i)).isHouseful()) {
                return sortedIDs.get(i);
            }
        }
        return "Failure";
    }



    @Override
    public String getBookingSchedule(String customerId) {
        String response;
        if(!checkIfClientExists(customerId)){
            response = "Booking Schedule Empty For " + customerId;
            try {
                Logger.serverLog(serverId, customerId, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        Map<String, List<String>> movies = clientEvents.get(customerId);
        if (movies.size() == 0) {
            response = "Booking Schedule Empty For " + customerId;
            try {
                Logger.serverLog(serverId, customerId, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        StringBuffer builder = new StringBuffer();
        for (String movieNames :
                movies.keySet()) {
            builder.append(movieNames + ":\n");
            for (String movieId :
                    movies.get(movieNames)) {

                builder.append(movieId).append("\t").append(movieBookings.get(customerId+movieId+movieNames));
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
        try {
            Logger.serverLog(serverId, customerId, " CORBA getBookingSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String cancelMovieTickets(String customerId, String movieId, String movieName, int numberOfTickets)  {
        String response;
        int qty = movieBookings.get(customerId+movieId+movieName);
        if (MovieModel.findMovieServer(movieId).equals(serverName)) {
            if (customerId.substring(0, 3).equals(serverId)) {
                if (!serverClients.containsKey(customerId)) {
                    addNewCustomerToClients(customerId);
                    response = "FAILED";
                    try {
                        Logger.serverLog(serverId, customerId, " RMI cancelMovieTickets ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(numberOfTickets>qty){
                    response = "Failure";
                }
                else if(numberOfTickets<qty){
                    movieBookings.put(customerId+movieId+movieName,qty-numberOfTickets);
                    response = "Success";
                }

                else{
                    if (clientEvents.get(customerId).get(movieName).remove(movieId)) {


                        moviesEvents.get(movieName).get(movieId).removeRegisteredClientId(customerId);
                        movieBookings.remove(customerId+movieId+movieName);
                        clientEvents.get(customerId).get(movieName).remove(movieId);
                        response = "Success";
                        try {
                            Logger.serverLog(serverId, customerId, " RMI cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        response = "Failure";
                        try {
                            Logger.serverLog(serverId, customerId, " RMI cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            else {
                if (moviesEvents.get(movieName).get(movieId).removeRegisteredClientId(customerId)) {
                    movieBookings.remove(customerId+movieId+movieName);
                    response = "Success";
                    try {
                        Logger.serverLog(serverId, customerId, " RMI cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    response = "Failure";
                    try {
                        Logger.serverLog(serverId, customerId, " RMI cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response;
        } else {
            if (customerId.substring(0, 3).equals(serverId)) {
                if (!serverClients.containsKey(customerId)) {
                    addNewCustomerToClients(customerId);
                } else {
                    if (clientEvents.get(customerId).get(movieName).remove(movieId)) {
                        return sendUDPMessage(getServerPort(movieId.substring(0, 3)), "cancelMovie", customerId, movieName, movieId,numberOfTickets);
                    }
                }
            }
            return "Failure";
        }

    }

    @Override
    public String exchangeTickets(String customerId, String oldMovieName, String newMovieId, String movieId, String newMovieName, int numberOfTickets) {
        String response;
        String resCancel="",resBook="";
        if(!checkIfClientExists(customerId)){
            response = "Failure";
            try{
                Logger.serverLog(serverId,customerId," CORBA Exchange Tickets", "oldMovieId: "+movieId+" oldMovieName: "+oldMovieName+" newMovieId: "+newMovieId+" newMovieName "+ newMovieName+" numberOfTickets: "+numberOfTickets,response);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }else {
            if (clientHasSlot(customerId,oldMovieName,movieId)) {
                synchronized (this) {
                    resCancel = cancelMovieTickets(customerId, movieId, oldMovieName, numberOfTickets);
                    if (resCancel.startsWith("Success:")) {
                        resBook = bookMoviesTickets(customerId, newMovieId, newMovieName, numberOfTickets);
                    } else {
                        resBook = bookMoviesTickets(customerId, newMovieId, newMovieName, numberOfTickets);
                        if (resBook.startsWith("Success:")) {
                            resCancel = cancelMovieTickets(customerId, movieId, oldMovieName, numberOfTickets);
                        }
                    }

                }
                if (resBook.startsWith("Success:") && resCancel.startsWith("Success")) {
                    response = "Success";
                } else if (resBook.startsWith("Failure:") && resCancel.startsWith("Success:")) {
                    String res = bookMoviesTickets(customerId, movieId, oldMovieName, numberOfTickets);
                    response = "Failure";
                } else if (resBook.startsWith("Success:") && resCancel.startsWith("FAILED:")) {
                    String res = cancelMovieTickets(customerId, newMovieId, newMovieName, numberOfTickets);
                    response = "Failure";
                } else {
                    response = "Failure";
                }


            }else{
                if(!clientHasSlot(customerId,oldMovieName,movieId)){
                    response = "Failure";
                }
                else if(moviesEvents.get(newMovieName).get(newMovieId).getMovieCapacity() < numberOfTickets){
                    response = "New Booking Server Cannot facilitate "+numberOfTickets+" Tickets. Less Capacity Available";
                }else{
                    response = "Cannot Exchange Tickets";
                }

            }
            try {
                Logger.serverLog(serverId, customerId, " CORBA Exchange Tickets", "oldMovieId: " + movieId + " oldMovieName: " + oldMovieName + " newMovieId: " + newMovieId + " newMovieName " + newMovieName + " numberOfTickets: " + numberOfTickets,response);
            }catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }
    }


    private String sendUDPMessage(int serverPort, String method, String customerId, String movieName, String  movieId, Integer value) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerId + ";" + movieName + ";" + movieId+";"+value;
        try {
            Logger.serverLog(serverId, customerId, " UDP request sent " + method + " ", " movieId: " + movieId + " movieName: " + movieName + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        try {
            Logger.serverLog(serverId, customerId, " UDP reply received" + method + " ", " movieId: " + movieId + " movieName: " + movieName + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    public String removeMovieUDP(String oldMovieId, String movieName, String customerId) {
        if (!serverClients.containsKey(customerId)) {
            addNewCustomerToClients(customerId);
            return "Failure";
        } else {
            if (clientEvents.get(customerId).get(movieName).remove(oldMovieId)) {
                return "Success";
            } else {
                return "Failure";
            }
        }
    }

    public String listMovieAvailabilityUDP(String movieName) {
        Map<String, MovieModel> movies = moviesEvents.get(movieName);
        StringBuffer builder = new StringBuffer();
//        builder.append("\n");
//        builder.append("\n"+serverName + " Server " + movieName + ":\n");
//        if (movies.size() == 0) {
//            builder.append("No Events of Type " + movieName);
//        } else {
//            for (MovieModel movie :
//                    movies.values()) {
//                builder.append(movie.toString() + " || ");
//            }
//        }
//        builder.append("\n=====================================\n");
        if(movies.size() != 0){
            for(MovieModel model : movies.values()){
                builder.append(movies.toString()+";");
            }
            builder.deleteCharAt(builder.length()-1);
        }
        return builder.toString();
    }

    private synchronized boolean checkClientExists(String customerID) {
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
            return false;
        } else {
            return true;
        }
    }


    private synchronized boolean clientHasMovie(String customerID, String eventType, String eventID) {
        if (clientEvents.get(customerID).containsKey(eventType)) {
            return clientEvents.get(customerID).get(eventType).contains(eventID);
        } else {
            return false;
        }
    }

    private synchronized boolean removeMovieIfAlreadyExists(String customerId, String movieName, String movieId){
        if(clientEvents.get(customerId).containsKey(movieName)){
            return clientEvents.get(customerId).get(movieName).remove(movieId);
        }
        else{
            return false;
        }
    }

//    @Override
//    public void shutdown() {
//        orb.shutdown(false);
//    }


    private boolean onTheSameWeek(String newEventDate, String eventID) {
        if (eventID.substring(6, 8).equals(newEventDate.substring(2, 4)) && eventID.substring(8, 10).equals(newEventDate.substring(4, 6))) {
            int week1 = Integer.parseInt(eventID.substring(4, 6)) / 7;
            int week2 = Integer.parseInt(newEventDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(day2 - day1);
            return week1 == week2;
        } else {
            return false;
        }
    }


    public void addNewCustomerToClients(String customerId) {
        ClientModel newCustomer = new ClientModel(customerId);
        serverClients.put(newCustomer.getClientId(), newCustomer);
        clientEvents.put(newCustomer.getClientId(), new ConcurrentHashMap<>());
    }
}

