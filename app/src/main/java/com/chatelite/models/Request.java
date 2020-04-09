package com.chatelite.models;

public class Request {
    private String state, from, to;

    public String getType() {
        return state;
    }

    public void setType(String type) {
        this.state = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


}
