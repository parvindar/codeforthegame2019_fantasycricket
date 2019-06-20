package com.test.fantasycricket;

public class Match {
    String team1;
    String team2;
    public String date;
    public String time;
    public String timeleft;
    public boolean started;
    public String matchtype;

    public Match()
    {

    }

    public Match(String team1, String team2, String date,String matchtype ,Boolean started) {
        this.team1 = team1;
        this.team2 = team2;
        this.date = date;
        this.started = started;
        this.matchtype = matchtype;
    }
}
