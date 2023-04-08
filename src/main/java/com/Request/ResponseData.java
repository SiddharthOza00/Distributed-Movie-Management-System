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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSenderReplica() {
        return senderReplica;
    }

    public void setSenderReplica(String senderReplica) {
        this.senderReplica = senderReplica;
    }

    public Integer getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(Integer sequenceID) {
        this.sequenceID = sequenceID;
    }

    @Override
    public String toString() {
        return getResult()+","+getSenderReplica()+","+getSequenceID();
    }    

    
}
