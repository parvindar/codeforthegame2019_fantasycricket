package com.test.fantasycricket;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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






    class getmatchestask extends AsyncTask<String, Boolean, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)
            matches = new ArrayList<>();

            try {
                matchlistobject = getJSONObjectFromURL("https://cricapi.com/api/matches?apikey=VdUTmLVaoVNmU4V8wnQR6LBnezo2");
                JSONArray matchesjsonArray =matchlistobject.getJSONArray("matches");
                String team1,team2,date;
                Boolean matchstarted;

                for(int i = 0 ;i<matchesjsonArray.length();i++)
                {
                    JSONObject match = matchesjsonArray.getJSONObject(i);
                    team1 = match.getString("team-1");
                    team2 = match.getString("team-2");
                    date = match.getString("date");
                    matchstarted = match.getBoolean("matchStarted");
                    Match newmatch = new Match(team1,team2,date,matchstarted);
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
