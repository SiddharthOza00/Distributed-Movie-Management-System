package com.Replica3.LoggerClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogData {
    public static final int SERVER = 1;
    public static final int CLIENT = 0;

    public static void deleteLog(String ID) throws IOException {

        String fileName = getDir(ID, CLIENT);
        File file = new File(fileName);
        file.delete();
    }

    private static String getDir(String ID, int logType) {
        /*
         * Get directory name
         */
        final String dir = System.getProperty("user.dir");
        String fileName = dir;
        // System.out.println(dir);
        if (logType == SERVER) {
            if (ID.equalsIgnoreCase("ATW")) {
                fileName = dir + "\\Logs\\Server\\Atwater.txt";
            } else if (ID.equalsIgnoreCase("VER")) {
                fileName = dir + "\\Logs\\Server\\Verdun.txt";
            } else if (ID.equalsIgnoreCase("OUT")) {
                fileName = dir + "\\Logs\\Server\\Outremont.txt";
            }
        } else {
            fileName = dir + "\\Logs\\Client\\" + ID + ".txt";
        }
        return fileName;
    }

    private static String getDate() {
        /*
         * get date
         */
        Date date = new Date();

        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        return dateFormat.format(date);
    }

    public static void clientLog(String clientID, String action, String requestParams, String response) throws IOException {
        /*
         * Log file for client with requested parameters
         */
        FileWriter fileWriter = new FileWriter(getDir(clientID, CLIENT), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getDate() + " Client Action: " + action + " | RequestParameters: " + requestParams + " | Server Response: " + response);

        printWriter.close();
    }

    public static void clientLog(String clientID, String msg) throws IOException {
        /*
         * Log file for client without parameters
         */
        FileWriter fileWriter = new FileWriter(getDir(clientID, CLIENT), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getDate() + " " + msg);

        printWriter.close();
    }

    public static void serverLog(String serverID, String clientID, String requestType, String requestParams, String serverResponse) throws IOException {
        /*
         * Server log file with requested parameters
         */
        if (clientID.equals("null")) {
            clientID = "Admin";
        }
        FileWriter fileWriter = new FileWriter(getDir(serverID, SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getDate() + " ClientID: " + clientID + " | RequestType: " + requestType + " | RequestParameters: " + requestParams + " | ServerResponse: " + serverResponse);

        printWriter.close();
    }

    public static void serverLog(String serverID, String msg) throws IOException {
        /*
         * Server log file without parameters
         */
        FileWriter fileWriter = new FileWriter(getDir(serverID, SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getDate() + " " + msg);

        printWriter.close();
    }
}

