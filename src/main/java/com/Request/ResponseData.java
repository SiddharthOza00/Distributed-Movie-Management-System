package com.Request;

public class ResponseData {
    private String result;
    private String senderReplica;
    private Integer sequenceID;

    public ResponseData() {
    }

    public ResponseData(String result, String senderReplica, Integer sequenceID) {
        this.result = result;
        this.senderReplica = senderReplica;
        this.sequenceID = sequenceID;
    }

    
}
