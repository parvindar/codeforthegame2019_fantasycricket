package com.test.fantasycricket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyMatchesActivity extends AppCompatActivity {

    ListView lv;
    FirebaseFirestore db ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        lv=findViewById(R.id.lv_mymatches_list);

        db.collection("Users").document(UserInfo.username).collection("Matches").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<String> mymatches_arrlist=new ArrayList<>();
                ArrayList<Match> mymatcheslist=new ArrayList<>();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                {
                    mymatches_arrlist.add(documentSnapshot.getId());
                }

                try {

                    JSONObject matchlistobject = HomeActivity.getJSONObjectFromURL(Constants.API_URL_NEWMATCHES);
                    JSONArray matchesjsonArray =matchlistobject.getJSONArray("matches");
                    String team1,team2,date,matchtype,uniqueid;
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
                        uniqueid = match.getString("unique_id");
                        matchstarted = match.getBoolean("matchStarted");
                        Date d = HomeActivity.fromISO8601UTC(date);
                        long mills = d.getTime() - Calendar.getInstance().getTime().getTime();
                        long hours = mills/(1000 * 60 * 60);
                        long mins = (mills/(1000*60)) % 60;
                        String timeremaining;
                        if(hours>48)
                        {
                            timeremaining = hours/24 +" days\nremaining";
                        }
                        else if(mills<0)
                        {
                            timeremaining = "Match\nStarted";
                        }
                        else if(hours==0)
                        {
                            timeremaining = mins +" mins.\nremaining";
                        }
                        else
                        {
                            timeremaining = hours + " hrs.\nremaining";
                        }
                        Match newmatch = new Match(uniqueid,team1,team2,date,matchtype,matchstarted);
                        newmatch.timeleft=timeremaining;
                        if(hours>72)
                        {
                            newmatch.open=false;
                        }

                        mymatcheslist.add(newmatch);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }



                MatchListAdaptor matchListAdaptor = new MatchListAdaptor(getApplicationContext(),R.layout.matchlist_element_layout,mymatcheslist);
                lv.setAdapter(matchListAdaptor);

            }
        });







    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
