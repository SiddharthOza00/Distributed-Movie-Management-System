package com.Replica2.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static void clientLog(String clientID, String action, String requestParams, String response) throws IOException {
        FileWriter fileWriter = new FileWriter(getFileName(clientID, 0), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " Client Action: " + action + " | RequestParameters: " + requestParams + " | Server Response: " + response);

        printWriter.close();
    }

    public static void clientLog(String clientID, String msg) throws IOException {
        FileWriter fileWriter = new FileWriter(getFileName(clientID, 0), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void serverLog(String serverID, String clientID, String requestType, String requestParams, String serverResponse) throws IOException {

        if (clientID.equals("null")) {
            clientID = "Admin";
        }
        FileWriter fileWriter = new FileWriter(getFileName(serverID, 1), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " ClientID: " + clientID + " | RequestType: " + requestType + " | RequestParameters: " + requestParams + " | ServerResponse: " + serverResponse);

        printWriter.close();
    }

    public static void serverLog(String serverID, String msg) throws IOException {

        FileWriter fileWriter = new FileWriter(getFileName(serverID, 1), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void deleteALogFile(String ID) throws IOException {

        String fileName = getFileName(ID, 0);
        File file = new File(fileName);
        file.delete();
    }

    private static String getFileName(String ID, int logType) {
        final String dir = System.getProperty("user.dir");
        String fileName = dir;
        if (logType == 1) {
            if (ID.equalsIgnoreCase("ATW")) {
                fileName = dir + "/src/Logger/Server/Atwater.txt";
            } else if (ID.equalsIgnoreCase("OUT")) {
                fileName = dir + "/src/Logger/Server/Outremont.txt";
            } else if (ID.equalsIgnoreCase("VER")) {
                fileName = dir + "/src/Logger/Server/Verdun.txt";
            }
        } else {
            fileName = dir + "/src/Logger/Client/" + ID + ".txt";
        }
        return fileName;
    }

    private static String getFormattedDate() {
        Date date = new Date();

        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        return dateFormat.format(date);
    }

}
