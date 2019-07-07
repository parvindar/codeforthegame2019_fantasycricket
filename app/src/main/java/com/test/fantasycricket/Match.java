package com.test.fantasycricket;

import java.util.Date;

public class Match {
    String team1;
    String team2;
    public String date;
    public String time;
    public Date realdate;
    public String timeleft;
    public boolean started;
    public String matchtype;
    public String uniqueid;
    public Boolean open=true;
    public String team1_score;
    public String team2_score;
    String toss_winner;
    String winner_team;

    public Match()
    {

    }

    public Match(String uniqueid,String team1, String team2, String date,Date realdate,String matchtype ,Boolean started) {
        this.team1 = team1;
        this.team2 = team2;
        this.date = date;
        this.started = started;
        this.matchtype = matchtype;
        this.uniqueid = uniqueid;
        this.open=true;
        toss_winner="";
        winner_team="";
        team1_score="";
        team2_score="";
        this.realdate=realdate;

    }
}
