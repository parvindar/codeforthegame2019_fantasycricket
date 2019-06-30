package com.test.fantasycricket;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Constants {

    public static String getTeamShortName(String fullname)
    {
        String[] s= fullname.split(" ");
        if(s.length==1)
        {
            try
            {
                return s[0].substring(0,3).toUpperCase();
            }
            catch (Exception e)
            {
                return s[0].substring(0,2).toUpperCase();
            }
        }
        else {
                String ok ="";
                for(String k : s)
                {
                    boolean roman = k.matches("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$");
                    if(roman)
                    {
                        ok = ok +k;
                    }
                    else {
                        ok = ok + k.charAt(0);
                    }
                }
                return ok.toUpperCase();
            }


    }

    public static String INR = "\u20B9 ";
    public static DecimalFormat dec = new DecimalFormat("#0.00");


//    public static String API_KEY="VdUTmLVaoVNmU4V8wnQR6LBnezo2";
    public static String API_KEY="ETR53oAOSeT5gdo0EywqfdrUFW72";


    public static String API_URL_NEWMATCHES="https://cricapi.com/api/matches?apikey="+API_KEY;

    private static String API_URL_SQUAD="https://cricapi.com/api/fantasySquad?apikey="+API_KEY+"&unique_id=";
    private static String API_URL_SCORE ="https://cricapi.com/api/cricketScore?apikey="+API_KEY+"&unique_id=";
    private static String API_URL_FANTASY="https://cricapi.com/api/fantasySummary?apikey="+API_KEY+"&unique_id=";

    public static String getApiUrlSquad(String matchid) {
        return API_URL_SQUAD+matchid;
    }

    public static String getApiUrlScore(String matchid) {
        return API_URL_SCORE+matchid;
    }

    public static String getApiUrlFantasy(String matchid) {
        return API_URL_FANTASY+matchid;
    }

    public static void updateApiUrls()
    {
        API_URL_NEWMATCHES="https://cricapi.com/api/matches?apikey="+API_KEY;
        API_URL_SQUAD="https://cricapi.com/api/fantasySquad?apikey="+API_KEY+"&unique_id=";
        API_URL_SCORE ="https://cricapi.com/api/cricketScore?apikey="+API_KEY+"&unique_id=";
        API_URL_FANTASY="https://cricapi.com/api/fantasySummary?apikey="+API_KEY+"&unique_id=";
    }

}

