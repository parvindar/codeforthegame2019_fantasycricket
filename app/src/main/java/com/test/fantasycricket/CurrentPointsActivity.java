package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
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
    boolean started = false,finishedforall=false,awarded=false;
    Map<String,Object> teamobject;
    ArrayList<Player> playerArrayList;
    String man_of_the_match;
    PlayerListAdaptor playerListAdaptor;
    TextView totalpoints_tv;
    String contestname;
    Integer totalspots;
    String captain,vicecaptain;
    String team1,team2;
    Long time=0l;


    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean contestfinished_forme=false;

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
        final TextView contestname_tv = findViewById(R.id.tv_contestname);
        TextView totalspots_tv = findViewById(R.id.tv_spots);
        final TextView conteststatus_tv = findViewById(R.id.tv_conteststatus);


        team1 = getIntent().getStringExtra("team1");
        team2 = getIntent().getStringExtra("team2");
        matchid = getIntent().getStringExtra("matchid");
        contestid = getIntent().getStringExtra("contestid");
        participantID = getIntent().getStringExtra("ParticipantID");
        started = getIntent().getBooleanExtra("started",false);
        contestname = getIntent().getStringExtra("contestname");
        totalspots = getIntent().getIntExtra("totalspots",0);
        finishedforall = getIntent().getBooleanExtra("finishedforall",false);
        awarded = getIntent().getBooleanExtra("awarded",false);

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
                    try{
                        if(teamobject.get("Finished")!=null && (boolean)teamobject.get("Finished"))
                        {
                            contestfinished_forme = true;
                        }
                    }
                    catch (Exception e)
                    {
                        contestfinished_forme=false;
                        e.printStackTrace();
                    }


                    try {
                        totalpoints_tv.setText(Constants.dec.format((double) teamobject.get("Points")));
                    }
                    catch (Exception e)
                    {
                        totalpoints_tv.setText(Constants.dec.format((double)((Long)teamobject.get("Points")).intValue()));

                    }

                    for(Map<String,Object> p : playerobject)
                    {
                        Player player = new Player(p.get("name").toString(),p.get("pid").toString(),Double.parseDouble(p.get("credits").toString()));
                        player.points = Double.parseDouble(p.get("points").toString());
                        player.team = p.get("team").toString();
                        try{
                            player.type=p.get("type").toString();
                        }
                        catch (Exception e)
                        {
                            player.type ="";
                        }
                        if(p.get("captain")!=null && (boolean)p.get("captain"))
                        {
                            captain = player.pid;
                        }
                        if(p.get("vicecaptain")!=null && (boolean)p.get("vicecaptain"))
                        {
                            vicecaptain = player.pid;
                        }

                        playerArrayList.add(player);
                    }

                    Collections.sort(playerArrayList,new Comparator<Player>()
                    {
                        @Override
                        public int compare(Player o1, Player o2)
                        {
                            return (int)(o2.points-o1.points);
                        }
                    });


                    playerListAdaptor = new PlayerListAdaptor(CurrentPointsActivity.this,R.layout.player_element_curr_points,playerArrayList);

                    lv.setAdapter(playerListAdaptor);

                    if(!contestfinished_forme)
                    {
                        new getteamtask().execute();
                        if(started)
                        {
                            conteststatus_tv.setText("Contest is running");
                        }
                        else
                        {
                            conteststatus_tv.setText("Contest will start soon");
                        }

                    }
                    else
                    {
                        conteststatus_tv.setText("Contest is finished");
                    }

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
                intent.putExtra("started",started);
                intent.putExtra("finished",contestfinished_forme);
                intent.putExtra("finishedforall",finishedforall);
                intent.putExtra("awarded",awarded);


                startActivity(intent);
            }
        });



        //  swipe to refresh ===========================================================
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if((Calendar.getInstance().getTimeInMillis()-time<5000))
                    {
                        mSwipeRefreshLayout.setRefreshing(false);
                        return;
                    }
                    else {
                        time= Calendar.getInstance().getTimeInMillis();
                    }


                }


                db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(participantID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists())
                        {
                            playerArrayList=new ArrayList<>();
                            teamobject = documentSnapshot.getData();
                            ArrayList<Map<String,Object>> playerobject = (ArrayList<Map<String,Object>>)teamobject.get("Team");
                            try {
                                totalpoints_tv.setText(Constants.dec.format((double) teamobject.get("Points")));
                            }
                            catch (Exception e)
                            {
                                totalpoints_tv.setText(Constants.dec.format((double)((Long)teamobject.get("Points")).intValue()));
                            }

                            try{
                                if(teamobject.get("Finished")!=null && (boolean)teamobject.get("Finished"))
                                {
                                    contestfinished_forme = true;
                                }
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                                contestfinished_forme=false;
                            }

                            for(Map<String,Object> p : playerobject)
                            {
                                Player player = new Player(p.get("name").toString(),p.get("pid").toString(),Double.parseDouble(p.get("credits").toString()));
                                player.points = Double.parseDouble(p.get("points").toString());
                                player.team = p.get("team").toString();
                                player.type=p.get("type").toString();
                                if(p.get("captain")!=null && (boolean)p.get("captain"))
                                {
                                    captain = player.pid;
                                }
                                if(p.get("vicecaptain")!=null && (boolean)p.get("vicecaptain"))
                                {
                                    vicecaptain = player.pid;
                                }

                                playerArrayList.add(player);
                            }


                            Collections.sort(playerArrayList,new Comparator<Player>()
                            {
                                @Override
                                public int compare(Player o1, Player o2)
                                {
                                    return (int)(o2.points-o1.points);
                                }
                            });

                            playerListAdaptor = new PlayerListAdaptor(CurrentPointsActivity.this,R.layout.player_element_curr_points,playerArrayList);

                            lv.setAdapter(playerListAdaptor);

                            if(!contestfinished_forme)
                            {
                                new getteamtask().execute();
                                if(started)
                                {
                                    conteststatus_tv.setText("Contest is running");
                                }
                                else
                                {
                                    conteststatus_tv.setText("Contest will start soon");
                                }

                            }
                            else
                            {
                                mSwipeRefreshLayout.setRefreshing(false);
                                conteststatus_tv.setText("Contest is finished");

                            }
                        }
                    }
                });

            }
        });

        //=========================================================================



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
            this.type = "";
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
                TextView playerstatus_tv = convertView.findViewById(R.id.tv_playerstatus);
                playernametv.setText(getItem(position).name);
                pointstv.setText(String.valueOf(getItem(position).points));
                playerteam_tv.setText(getItem(position).team);
                playertype_tv.setText(getItem(position).type);

                if(getItem(position).pid.equals(captain))
                {
                    playerstatus_tv.setText("C");
                }
                if(getItem(position).pid.equals(vicecaptain))
                {
                    playerstatus_tv.setText("VC");
                    playerstatus_tv.setTextColor(Color.parseColor("#FFE7410F"));
                }

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
        boolean finishedforme=false;

        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)

            if(started)
            {

                try {


                    JSONObject main_object = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlFantasy(matchid));
                    Log.d("apicall","api is called in leaderboard");

                    JSONObject data = main_object.getJSONObject("data");
                    try{
                        man_of_the_match = data.getString("man-of-the-match");
                    }
                    catch (Exception e)
                    {
                        Log.e("error",e.toString());
                    }
                    JSONArray batting_arr_object = data.getJSONArray("batting");
                    JSONArray bowling_arr_object = data.getJSONArray("bowling");
                    JSONArray fielding_arr_object = data.getJSONArray("fielding");

                    try{
                        if(data.keys()!=null && (data.getString("winner_team").equals(team1) || data.getString("winner_team").equals(team2)))
                        {
                            finishedforme=true;
                        }

                    }
                    catch (Exception e)
                    {
                        finishedforme=false;

                        Log.d("error","team winner wala error"+e.toString());
                    }


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

                                    if(player.pid.equals(captain))
                                    {
                                        points=points*(2.0);
                                    }
                                    if(player.pid.equals(vicecaptain))
                                    {
                                        points=points*(1.5);
                                    }

                                    player.points += points;
                                    if(points>25d)
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

                                    if(player.pid.equals(captain))
                                    {
                                        points=points*(2.0);
                                    }
                                    if(player.pid.equals(vicecaptain))
                                    {
                                        points=points*(1.5);
                                    }



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


                                    if(player.pid.equals(captain))
                                    {
                                        points=points*(2.0);
                                    }
                                    if(player.pid.equals(vicecaptain))
                                    {
                                        points=points*(1.5);
                                    }


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

                    totalpoints = 0d;
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

            playerListAdaptor = new PlayerListAdaptor(CurrentPointsActivity.this,R.layout.player_element_curr_points,playerArrayList);
            lv.setAdapter(playerListAdaptor);
            totalpoints_tv.setText(Constants.dec.format(totalpoints));
            if(mSwipeRefreshLayout.isRefreshing())
            {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            Log.d("calculated"," calculated, now in postexecute ");

            if(started)
            {

                if(finishedforme)
                {
                    teamobject.put("Finished",true);
                    db.collection("Users").document(UserInfo.username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            double xp = Double.valueOf(String.valueOf(documentSnapshot.get("xp")));
                            xp=xp+totalpoints;
                            db.collection("Users").document(UserInfo.username).update("xp",xp);
                        }
                    });
                }

                teamobject.put("Points",totalpoints);

                ArrayList<Map<String,Object>> playerobject = (ArrayList<Map<String,Object>>)teamobject.get("Team");
                for (Player player : playerArrayList)
                {
                    for(Map<String,Object> p : playerobject)
                    {
                        if(p.get("pid").equals(player.pid))
                        {
                            p.put("points",player.points);
                            p.put("type",player.type);
                        }
                    }
                }

                teamobject.put("Team",playerobject);

                db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(participantID).set(teamobject);

            }

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
