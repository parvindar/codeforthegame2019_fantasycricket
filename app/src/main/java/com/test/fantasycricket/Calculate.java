package com.test.fantasycricket;

import android.util.Log;

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
        runs=(double) ((Integer) player.get("R")).intValue();
        Log.d("points"," runs "+String.valueOf(runs));
//        minutes_played=Double.parseDouble(player.get("M").toString());
        balls_played=(double)((Integer)player.get("B")).intValue();
        fours=(double)((Integer)player.get("4s")).intValue();
        sixes=(double)((Integer)player.get("6s")).intValue();
        strike_rate=(double)((Integer)player.get("SR")).intValue();

        point+=(runs*0.5);
        point+=(fours*0.5);
        point+=(sixes);
        point+=((int)(runs)/50)*2;
        point+=((int)runs/100)*2;

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
        }else if(strike_rate >=80)
        {
            point++;
        }


        return  point;
    }

    public static double points_bowling(JSONObject player) throws JSONException {
/*
            "O": "11",                  // -----------------> Overs bowled (decimal value)
            "M": "1",                   // -----------------> Maidens bowled
            "R": "42",                  // -----------------> Runs conceded
            "W": "1",                   // -----------------> Wickets taken
            "Econ": "3.81",             // -----------------> Economy of runs per 6 balls
            "0s": "48"                  // -----------------> Dot balls bowled*/

        double point=0;

        double overs_bowled,maidens,runs_conceded,wickets_taken,econ,dot_balls;

        overs_bowled =Double.parseDouble((String)player.get("O"));
        maidens=Double.parseDouble((String)player.get("M"));
        runs_conceded=Double.parseDouble((String)player.get("R"));
        wickets_taken=Double.parseDouble((String)player.get("W"));
        econ=Double.parseDouble((String)player.get("Econ"));
        dot_balls=(double)((Integer)player.get("0s")).intValue();

        point+=(wickets_taken)*12;
        point+=((int)wickets_taken/4)*2;
        point+=((int)wickets_taken/5)*2;
        //maidens+=(wickets_taken)*2;

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
            else if(econ>=2.5&& econ < 3.5)
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
                "runout":
                "lbw": 4,                  // -----------------> LBWs
                "stumped": 2,                  // -----------------> Stumped
                "bowled": 0                  // -----------------> Bowled
    */
            double catchball,lbw,stumped,bowled,runout;

            catchball=(double)((Integer)player.get("catch")).intValue();
            lbw=(double)((Integer)player.get("lbw")).intValue();
            stumped=(double)((Integer)player.get("stumped")).intValue();
            bowled=(double)((Integer)player.get("bowled")).intValue();
            runout =(double)((Integer)player.get("runout")).intValue();


            point+=catchball*4;
            point+=lbw*2;
            point+=stumped*6;
            point+=bowled*6;


            return  point;

        }




}
