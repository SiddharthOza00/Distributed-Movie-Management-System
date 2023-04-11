package com.Replica5.Object;

public class ClientObject {
    private String clientType;
    private String clientID;
    private String clientServer;

    public ClientObject(String clientID) {
        this.clientID = clientID;
        this.clientType = detectClientType();
        this.clientServer = detectClientServer();
    }

    private String detectClientServer() {
        if (clientID.substring(0, 3).equalsIgnoreCase("ATW")) {
            return "Atwater";
        } else if (clientID.substring(0, 3).equalsIgnoreCase("OUT")) {
            return "Outremont";
        } else {
            return "Verdun";
        }
    }

    private String detectClientType() {
        if (clientID.substring(3, 4).equalsIgnoreCase("A")) {
            return "Admin";
        } else {
            return "Customer";
        }
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientServer() {
        return clientServer;
    }

    public void setClientServer(String clientServer) {
        this.clientServer = clientServer;
    }

    @Override
    public String toString() {
        return getClientType() + "(" + getClientID() + ") on " + getClientServer() + " Server.";
    }
}