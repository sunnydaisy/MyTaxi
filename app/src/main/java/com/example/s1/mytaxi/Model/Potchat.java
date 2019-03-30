package com.example.s1.mytaxi.Model;

public class Potchat {
    private String sender;
    private String pot_name;
    private String message;
    private String url;
    private String username;
    private boolean isseen;

    public Potchat(String sender, String receiver, String message, boolean isseen, String url, String username) {
        this.sender = sender;
        this.pot_name = receiver;
        this.message = message;
        this.isseen = isseen;
        this.url = url;
        this.username = username;
    }

    public Potchat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return pot_name;
    }

    public void setReceiver(String receiver) {
        this.pot_name = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getUrl() {return url;}

    public void setUrl(String url) {this.url = url;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}
}
