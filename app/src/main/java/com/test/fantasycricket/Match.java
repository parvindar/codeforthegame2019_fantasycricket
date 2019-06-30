package com.test.fantasycricket;

public class Match {
    String team1;
    String team2;
    public String date;
    public String time;
    public String timeleft;
    public boolean started;
    public String matchtype;
    public String uniqueid;
    public Boolean open=true;
    String toss_winner;
    String winner_team;

    public Match()
    {

    }

    public Match(String uniqueid,String team1, String team2, String date,String matchtype ,Boolean started) {
        this.team1 = team1;
        this.team2 = team2;
        this.date = date;
        this.started = started;
        this.matchtype = matchtype;
        this.uniqueid = uniqueid;
        this.open=true;
        toss_winner="";
        winner_team="";
    }
}
