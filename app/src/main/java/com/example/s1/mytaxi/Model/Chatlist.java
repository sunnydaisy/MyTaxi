package com.example.s1.mytaxi.Model;

public class Chatlist {
    public String id;
    public String pot_name;

    public Chatlist(String id, String pot_name) {
        this.id = id;
        this.pot_name = pot_name;
    }

    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPot_name(){ return pot_name;}

    public void setPot_name(String pot_name){this.pot_name = pot_name;}
}
