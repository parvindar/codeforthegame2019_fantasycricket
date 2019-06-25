package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CurrentPointsActivity extends AppCompatActivity {
    ListView lv;
    String matchid,contestid;

    FirebaseFirestore db;
    String participantID;
    Double totalpoints =0.0;
    boolean started = false;
    Map<String,Object> teamobject;
    ArrayList<Player> playerArrayList;
    String man_of_the_match;
    PlayerListAdaptor playerListAdaptor;
    TextView totalpoints_tv;
    String contestname;
    Integer totalspots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_points);
        db=FirebaseFirestore.getInstance();
        lv= findViewById(R.id.lv_currentpoints_playerlist);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        totalpoints_tv = findViewById(R.id.tv_totalpoints);
        TextView participantid_tv = findViewById(R.id.tv_participantid);
        TextView team1_tv = findViewById(R.id.tv_team1);
        TextView team2_tv = findViewById(R.id.tv_team2);
        TextView contestname_tv = findViewById(R.id.tv_contestname);
        TextView totalspots_tv = findViewById(R.id.tv_spots);


        final String team1,team2;

        team1 = getIntent().getStringExtra("team1");
        team2 = getIntent().getStringExtra("team2");
        matchid = getIntent().getStringExtra("matchid");
        contestid = getIntent().getStringExtra("contestid");
        participantID = getIntent().getStringExtra("ParticipantID");
        started = getIntent().getBooleanExtra("started",false);
        contestname = getIntent().getStringExtra("contestname");
        totalspots = getIntent().getIntExtra("totalspots",0);

        Log.d("current points","started "+started);

        totalpoints_tv.setText(String.valueOf(totalpoints));
        participantid_tv.setText(participantID);
        team1_tv.setText(team1);
        team2_tv.setText(team2);
        contestname_tv.setText(contestname);
        totalspots_tv.setText(totalspots+" Spots");

        db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(participantID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    playerArrayList=new ArrayList<>();
                    teamobject = documentSnapshot.getData();
                    ArrayList<Map<String,Object>> playerobject = (ArrayList<Map<String,Object>>)teamobject.get("Team");

                    for(Map<String,Object> p : playerobject)
                    {
                        Player player = new Player(p.get("name").toString(),p.get("pid").toString(),Double.parseDouble(p.get("credits").toString()));
                        player.points = Double.parseDouble(p.get("points").toString());
                        player.team = p.get("team").toString();
                        player.type="";

                        playerArrayList.add(player);
                    }

                    playerListAdaptor = new PlayerListAdaptor(CurrentPointsActivity.this,R.layout.player_element_curr_points,playerArrayList);

                    lv.setAdapter(playerListAdaptor);

                    new getteamtask().execute();
                }
            }
        });


        Button leaderboard_btn = findViewById(R.id.btn_leaderboard);

        leaderboard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CurrentPointsActivity.this,LeaderboardActivity.class);
                intent.putExtra("matchid",matchid);
                intent.putExtra("contestid",contestid);
                intent.putExtra("team1",team1);
                intent.putExtra("team2",team2);
                intent.putExtra("participantID",participantID);
                intent.putExtra("contestname",contestname);
                intent.putExtra("totalspots",totalspots);


                startActivity(intent);
            }
        });


        }

    private class Player {
        String name;
        String pid;
        public Double points=0.0;
        Double credits;
        String team;
        String type;

        public Player(){

        }

        public Player(String name, String pid, Double credits) {
            this.name = name;
            this.pid = pid;
            this.credits = credits;
        }
    }



    private class PlayerListAdaptor extends ArrayAdapter<Player> {
        private static final String TAG = "PlayerListAdaptor";
        private Context mContext;
        private int mResource;

        public PlayerListAdaptor(Context context, int resource, List<Player> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(getItem(position)!=null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView playernametv = convertView.findViewById(R.id.tv_player_name);
                TextView pointstv = convertView.findViewById(R.id.tv_points);
                TextView playertype_tv = convertView.findViewById(R.id.tv_playertype);
                TextView playerteam_tv = convertView.findViewById(R.id.tv_playerteam);

                playernametv.setText(getItem(position).name);
                pointstv.setText(String.valueOf(getItem(position).points));
                playerteam_tv.setText(getItem(position).team);
                playertype_tv.setText(getItem(position).type);


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {


                        }

                    }
                });


            }
            return convertView;

        }

    }




    class getteamtask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)


            if(started)
            {
                Log.d("current points ","i am inside");

                try {

                    JSONObject main_object = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlFantasy(matchid));
                    JSONObject data = main_object.getJSONObject("data");
                    man_of_the_match = data.getString("man-of-the-match");
                    JSONArray batting_arr_object = data.getJSONArray("batting");
                    JSONArray bowling_arr_object = data.getJSONArray("bowling");
                    JSONArray fielding_arr_object = data.getJSONArray("fielding");

                    for(Player player: playerArrayList)
                    {
                        player.points=0d;
                    }

                    for(int i=0 ;i<batting_arr_object.length();i++)
                    {
                        JSONObject batting_object = batting_arr_object.getJSONObject(i);
                        JSONArray batting_scores = batting_object.getJSONArray("scores");
                        for(int j=0;j<batting_scores.length();j++)
                        {
                            double points = 0;

                            JSONObject p = batting_scores.getJSONObject(j);
                            Log.d("points ",p.toString());



                            for(Player player : playerArrayList)
                            {
                                if((player.pid).equals(p.getString("pid")))
                                {
                                    points = Calculate.points_batting(p);
                                    Log.d("points ","calculated point bat "+ String.valueOf(points));

                                    player.points += points;
                                    if(points>30)
                                    {
                                        player.type = "Batting";
                                    }
                                    break;
                                }
                            }

                        }

                    }

                    for(int i=0 ;i<bowling_arr_object.length();i++)
                    {
                        JSONObject bowling_object = bowling_arr_object.getJSONObject(i);
                        JSONArray bowling_scores = bowling_object.getJSONArray("scores");
                        for(int j=0;j<bowling_scores.length();j++)
                        {
                            double points = 0;
                            JSONObject p = bowling_scores.getJSONObject(j);

                            for(Player player : playerArrayList)
                            {
                                if((player.pid).equals(p.getString("pid")))
                                {
                                    points = Calculate.points_bowling(p);

                                    player.points += points;
                                    if(player.type.equals("Batting"))
                                    {
                                        player.type="All-Rounder";
                                    }
                                    else
                                    {
                                        player.type="Bowling";
                                    }
                                    break;
                                }
                            }

                        }

                    }


                    for(int i=0 ;i<fielding_arr_object.length();i++)
                    {
                        JSONObject fielding_object = fielding_arr_object.getJSONObject(i);
                        JSONArray fielding_scores = fielding_object.getJSONArray("scores");
                        for(int j=0;j<fielding_scores.length();j++)
                        {
                            double points = 0;
                            JSONObject p = fielding_scores.getJSONObject(j);

                            for(Player player : playerArrayList)
                            {
                                if((player.pid).equals(p.getString("pid")))
                                {
                                    points = Calculate.points_fielding(p);

                                    player.points += points;
                                    if(Integer.parseInt(p.getString("stumped"))>0)
                                    {
                                        player.type="Wicket-Keeper";
                                    }
                                    else if (player.type.equals(""))
                                    {
                                        player.type="Fielding";
                                    }

                                    break;
                                }
                            }

                        }

                    }

                    totalpoints =0d;
                    for(Player player : playerArrayList)
                    {

                        totalpoints+=player.points;

                        Log.d("current points ","points of  player "+ String.valueOf(player.points));
                    }

                    Collections.sort(playerArrayList,new Comparator<Player>()
                    {
                        @Override
                        public int compare(Player o1, Player o2)
                        {
                            return (int)(o2.points-o1.points);
                        }
                    });




                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            //Call your next task (ui thread)

            playerListAdaptor.notifyDataSetChanged();
            totalpoints_tv.setText(Constants.dec.format(totalpoints));

            db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(participantID).update("Points",totalpoints);



                }


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
