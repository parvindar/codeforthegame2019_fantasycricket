package com.test.fantasycricket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class Calculate {
    public static double points_batting(JSONObject player ) throws JSONException {
        double point=0;
/*          "R": "7",        // -----------------> Runs
            "M": "14",       // -----------------> Minutes Batted
            "B": "13",       // -----------------> Balls Played
            "4s": "1",       // -----------------> FOURs hit
            "6s": "0",       // -----------------> SIXes hit
            "SR": "53.84"    // -----------------> Strike Rate / 100 Balls*/
        double runs,minutes_played,balls_played,fours,sixes,strike_rate;
        runs=Double.parseDouble(player.get("R").toString());
        minutes_played=Double.parseDouble(player.get("M").toString());
        balls_played=Double.parseDouble(player.get("B").toString());
        fours=Double.parseDouble(player.get("4s").toString());
        sixes=Double.parseDouble(player.get("6s").toString());
        strike_rate=Double.parseDouble(player.get("SR").toString());

        point+=(runs*0.5);
        point+=(fours*0.5);
        point+=(sixes);
        point+=((int)(runs)/50)*2;
        point+=((int)runs/100)*4;

        if(strike_rate<40)
        {
            point-=3;
        }
        else if(strike_rate<50)
        {
            point-=2;
        }
        else if(strike_rate<60)
        {
            point-=1;
        }else {
            point++;
        }




        return  point;
    }

    public static double points_bowling(JSONObject player) throws JSONException {
/*
            "O": "11",                  // -----------------> Overs bowled (decimal value)
            "M": "1",                  // -----------------> Maidens bowled
            "R": "42",                  // -----------------> Runs conceded
            "W": "1",                  // -----------------> Wickets taken
            "Econ": "3.81",                  // -----------------> Economy of runs per 6 balls
            "0s": "48"                  // -----------------> Dot balls bowled*/

        double point=0;

        double overs_bowled,maidens,runs_conceded,wickets_taken,econ,dot_balls;

        overs_bowled =Double.parseDouble(player.get("O").toString());
        maidens=Double.parseDouble(player.get("M").toString());
        runs_conceded=Double.parseDouble(player.get("R").toString());
        wickets_taken=Double.parseDouble(player.get("W").toString());
        econ=Double.parseDouble(player.get("Econ").toString());
        dot_balls=Double.parseDouble(player.get("0s").toString());

        point+=(wickets_taken)*12;
        point+=((int)wickets_taken/4)*2;
        point+=((int)wickets_taken/5)*2;
        maidens+=(wickets_taken)*2;
        if(overs_bowled>=5)
        {
            if(econ>9)
            {
                point-=3;
            }
            else if (econ >8)
            {
                point-=2;
            }
            else if (econ >7)
            {
                point -=1;
            }
            else if(econ>=3.5 && econ<4.5)
            {
                point+=1;
            }
            else if(econ>=2.5&& econ <3.5)
            {
                point+=2;
            }
            else if(econ<2.5)
            {
                point+=3;
            }
        }

        point+=maidens*2;
        point+=dot_balls*0.5;




        return  point;

    }

        public static double points_fielding(JSONObject player) throws JSONException {
            double point=0;

     /*         "catch": 3,                  // -----------------> Catches
                "lbw": 4,                  // -----------------> LBWs
                "stumped": 2,                  // -----------------> Stumped
                "bowled": 0                  // -----------------> Bowled
    */
            double catchball,lbw,stumped,bowled;

            catchball=Double.parseDouble(player.get("catch").toString());
            lbw=Double.parseDouble(player.get("lbw").toString());
            stumped=Double.parseDouble(player.get("stumped").toString());
            bowled=Double.parseDouble(player.get("bowled").toString());


            point+=catchball*4;
            point+=lbw*2;
            point+=stumped*6;
            point+=bowled*6;


            return  point;

        }

}
