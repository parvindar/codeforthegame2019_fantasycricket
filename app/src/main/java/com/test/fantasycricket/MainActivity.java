package com.test.fantasycricket;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    ListView matchlist;
    ArrayList<Match> matches;
    JSONObject matchlistobject;
    MatchListAdaptor matchListAdaptor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        matchlist = findViewById(R.id.lv_matchlist);

//        Date temp = fromISO8601UTC("2019-06-19T00:00:00.000Z");
//        Log.d("DATE --- ",temp.toString());

        new getmatchestask().execute();



    }



    // Function to request json object from web.
    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }

    public static Date fromISO8601UTC(String dateStr) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);

        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }






    class getmatchestask extends AsyncTask<String, Boolean, Boolean> {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)
            matches = new ArrayList<>();

            try {
                matchlistobject = getJSONObjectFromURL("https://cricapi.com/api/matches?apikey=VdUTmLVaoVNmU4V8wnQR6LBnezo2");
                JSONArray matchesjsonArray =matchlistobject.getJSONArray("matches");
                String team1,team2,date,matchtype;
                Boolean matchstarted;

                for(int i = 0 ;i<matchesjsonArray.length();i++)
                {
                    JSONObject match = matchesjsonArray.getJSONObject(i);
                    matchtype = match.getString("type");

                    if(!match.getString("type").equals("ODI"))
                    {
                        continue;
                    }
                    team1 = match.getString("team-1");
                    team2 = match.getString("team-2");
                    date = match.getString("dateTimeGMT");
                    matchstarted = match.getBoolean("matchStarted");
                    Date d = fromISO8601UTC(date);
                    long mills = d.getTime() - Calendar.getInstance().getTime().getTime();
                    long hours = mills/(1000 * 60 * 60);
                    long mins = (mills/(1000*60)) % 60;
                    String timeremaining;
                    if(hours>24)
                    {
                        if(hours/24==1){
                            timeremaining = hours/24 +" day\nremaining";
                        }
                        else
                        {
                            timeremaining = hours/24 +" days\nremaining";
                        }
                    }
                    else if(hours==0)
                    {
                        timeremaining = mins +" mins.\nremaining";
                    }
                    else if(mills<0)
                    {
                        timeremaining = "Match\nStarted";
                    }
                    else
                    {
                        timeremaining = hours + " hrs.\nremaining";
                    }
                    Match newmatch = new Match(team1,team2,timeremaining,matchtype,matchstarted);


                    matches.add(newmatch);

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }



            matchListAdaptor = new MatchListAdaptor(getApplicationContext(),R.layout.matchlist_element_layout,matches);



            return true;
        }

//        @Override
//        public void progressUpdate(Integer progress) {
//            //Update progress bar (ui thread)
//
//
//        }
        @Override
        protected void onPostExecute(Boolean result) {
            //Call your next task (ui thread)
            matchlist.setAdapter(matchListAdaptor);

        }


}}
