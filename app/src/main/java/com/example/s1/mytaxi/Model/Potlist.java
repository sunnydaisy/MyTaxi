package com.example.s1.mytaxi.Model;

public class Potlist {
    public String userid;
    public String dest;
    public String dep;
    public String date;
    public String username;
    public String male;
    public String search;
    public String time;
    public String lat;
    public String lon;

    public Potlist(String userid, String dest, String dep, String username, String male, String date, String search, String time, String lat, String lon) {
        this.userid = userid;
        this.lat = lat;
        this.lon = lon;
        this.dest = dest;
        this.dep = dep;
        this.username = username;
        this.male = male;
        this.date = date;
        this.search = search;
        this.time = time;
    }

    public Potlist() {
    }

    public String getId() {
        return userid;
    }

    public void setId(String id) {
        this.userid = id;
    }

    public String getDep() {
        return dep;
    }

    public void setDep(String dep) {
        this.dep = dep;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getusername() {
        return username;
    }

    public void setusername(String name) {
        this.username = name;
    }

    public String getMale() {
        return male;
    }

    public void setMale(String name) {
        this.male = name;
    }

    public String getDate(){ return date;  }

    public void setDate(String date){this.date = date;}

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getTime() {return time;}

    public void setTime(String time) { this.time = time;}

    public String getLat(){return lat;}

    public void setLat(String lat){this.lat = lat;}

    public String getLon(){return lon;}

    public void setLon(String lon){this.lon = lon;}
}
