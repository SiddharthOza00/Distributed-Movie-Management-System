package com.Replica4.Model;

public class ClientModel {
    private String serverName;
    private String clientServer;
    private String clientType;
    private String clientId;


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public ClientModel(String clientId) {
        this.clientId = clientId;
        this.clientType = findClientType();
        this.clientServer = findClientServer();
    }

    private String findClientType() {
        return clientId.charAt(3)=='A'? "ADMIN":"CUSTOMER";
    }

    private String findClientServer() {
        String serverSubstring = clientId.substring(0,3);
        if(serverSubstring.equals("ATW")){
            return "ATWATER";
        }else if(serverSubstring.equals("VER")){
            return "VERDUN";
        }else{
            return "OUTREMONT";
        }
    }

    @Override
    public String toString() {
        return "Accessing: "+clientServer+" Server By Client:"+clientType+" with ID:"+clientId;
    }
}


