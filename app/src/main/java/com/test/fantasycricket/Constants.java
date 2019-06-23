package com.test.fantasycricket;

public class Constants {

    public static String INR = "\u20B9 ";

    public static String API_KEY="VdUTmLVaoVNmU4V8wnQR6LBnezo2";

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

