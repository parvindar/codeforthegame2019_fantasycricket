package com.test.fantasycricket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                    try
                    {
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

//                        player.fillbattingstats(String.valueOf(p.get("runs")),String.valueOf(p.get("fours")),String.valueOf(p.get("sixes")),String.valueOf(p.get("strike_rate")));
//                        player.fillbowlingstats(String.valueOf(p.get("overs_bowled")),String.valueOf(p.get("wicket_taken")),String.valueOf(p.get("maidens")),String.valueOf(p.get("economy")),String.valueOf(p.get("runs_conceded")));
//                        player.fillfieldingstats(String.valueOf(p.get("catchball")),String.valueOf(p.get("runout")),String.valueOf(p.get("lbw")),String.valueOf(p.get("stumped")),String.valueOf(p.get("bowled")));
//
//                        player.batting_points = String.valueOf(p.get("batting_points"));
//                        player.bowling_points = String.valueOf(p.get("bowling_points"));
//                        player.fielding_points =String.valueOf(p.get("fielding_points"));

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

                            try
                            {
                                if(teamobject.get("Finished")!=null && (boolean)teamobject.get("Finished"))
                                {
                                    contestfinished_forme = true;
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                contestfinished_forme=false;
                            }

                            for(Map<String,Object> p : playerobject)
                            {
                                Player player = new Player(p.get("name").toString(),p.get("pid").toString(),Double.parseDouble(p.get("credits").toString()));
                                player.points = Double.parseDouble(p.get("points").toString());
                                player.team = p.get("team").toString();
                                try
                                {
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
                                mSwipeRefreshLayout.setRefreshing(false);
                                conteststatus_tv.setText("Contest is finished");

                                // displaying notification that contest is finished.
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CurrentPointsActivity.this);
                                LayoutInflater inflater = getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.contest_finished_notification, null);
                                dialogBuilder.setView(dialogView);
                                final AlertDialog b = dialogBuilder.create();
                                b.show();

                                TextView finalpointstv = dialogView.findViewById(R.id.tv_totalpoints);
                                TextView titletv = dialogView.findViewById(R.id.tv_participantid);
                                TextView contestnametv = dialogView.findViewById(R.id.tv_contestname);
                                TextView spotstv = dialogView.findViewById(R.id.tv_spots);
                                TextView evaluationpoints= dialogView.findViewById(R.id.tv_evaluationpoints);

                                finalpointstv.setText(String.valueOf(teamobject.get("Points")));
                                titletv.setText(participantID);
                                contestnametv.setText(contestname);
                                spotstv.setText(String.valueOf(totalspots)+"Spots");
                                evaluationpoints.setVisibility(View.GONE);

                            }
                        }
                    }
                });

            }
        });

        //=========================================================================

//test ===



    }

    private class Player {
        String name;
        String pid;
        public Double points=0.0;
        Double credits;
        String team;
        String type;
        String runs,fours,sixes,wicket_taken,strike_rake,bowled,overs_bowled,maidens,economy,catchball,runout,lbw,runs_conceded,stumped;
        String batting_points,bowling_points,fielding_points;

        public Player(){

        }

        public Player(String name, String pid, Double credits) {
            this.name = name;
            this.pid = pid;
            this.credits = credits;
            this.type = "";
        }

        public void fillbattingstats(String runs,String fours,String sixes,String strike_rake)
        {

            this.runs = runs;
            this.fours = fours;
            this.sixes = sixes;
            this.strike_rake = strike_rake;
        }

        public void fillbowlingstats(String overs_bowled,String wicket_taken,String maidens,String economy,String runs_conceded)
        {


            this.overs_bowled=overs_bowled;
            this.maidens = maidens;
            this.wicket_taken = wicket_taken;
            this.economy = economy;
            this.runs_conceded = runs_conceded;
        }

        public void fillfieldingstats(String catchball, String runout, String lbw, String stumped,String bowled) {
            this.catchball = catchball;
            this.runout = runout;
            this.lbw = lbw;
            this.stumped = stumped;
            this.bowled = bowled;
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

                final View finalConvertView = convertView;
                convertView.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {

                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CurrentPointsActivity.this);
                            LayoutInflater inflater =getLayoutInflater();
                            final View dialogView = inflater.inflate(R.layout.score_card, null);
                            dialogBuilder.setView(dialogView);
                            final AlertDialog b = dialogBuilder.create();
                            b.show();

                            TextView playername = dialogView.findViewById(R.id.tv_playername);
                            TextView team = dialogView.findViewById(R.id.tv_playerteam);

                            final TextView runs = dialogView.findViewById(R.id.tv_runs);
                            final TextView fours = dialogView.findViewById(R.id.tv_fours);

                            final TextView sixes = dialogView.findViewById(R.id.tv_sixes);
                            final TextView strike_rate = dialogView.findViewById(R.id.tv_strike_rate);
                            final TextView overs_bowled = dialogView.findViewById(R.id.tv_overs_bowled);
                            final TextView maidens = dialogView.findViewById(R.id.tv_maidens);
                            final TextView wicket_taken = dialogView.findViewById(R.id.tv_wickets_taken);
                            final TextView runs_conceded = dialogView.findViewById(R.id.tv_runs_conceded);
                            final TextView economy = dialogView.findViewById(R.id.tv_economy);
                            final TextView runout = dialogView.findViewById(R.id.tv_runout);
                            final TextView catchball = dialogView.findViewById(R.id.tv_catch);
                            final TextView stumped = dialogView.findViewById(R.id.tv_stumped);
                            final TextView bowled = dialogView.findViewById(R.id.tv_bowled);
                            final TextView lbw = dialogView.findViewById(R.id.tv_lbw);
                            final TextView battingpoints = dialogView.findViewById(R.id.tv_battingpoints);
                            final TextView bowlingpoints = dialogView.findViewById(R.id.tv_bowlingpoints);
                            final TextView fieldingpoints = dialogView.findViewById(R.id.tv_fieldingpoints);
                            final TextView pointstv = dialogView.findViewById(R.id.tv_playerpoints);
                            final ScrollView playerscoresview = dialogView.findViewById(R.id.scrollvies_player_scores);
                            final TextView playernotplayingview = dialogView.findViewById(R.id.tv_player_notplaying);


                            playername.setText(getItem(position).name);
                            team.setText(getItem(position).team);

                            db.collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    ArrayList<Map<String,Object>> player_global = (ArrayList<Map<String, Object>>) documentSnapshot.get("Team");
                                    boolean present = false;
                                    if(player_global==null || player_global.isEmpty())
                                    {
                                        playernotplayingview.setText("Match has not started yet.");
                                        return;
                                    }

                                    for(Map<String,Object> p : player_global)
                                    {
                                        if(getItem(position).pid.equals((String) p.get("pid")))
                                        {
                                            playerscoresview.setVisibility(View.VISIBLE);
                                            playernotplayingview.setVisibility(View.GONE);


                                            present = true;
                                            getItem(position).fillbattingstats(String.valueOf(p.get("runs")),String.valueOf(p.get("fours")),String.valueOf(p.get("sixes")),String.valueOf(p.get("strike_rate")));
                                            getItem(position).fillbowlingstats(String.valueOf(p.get("overs_bowled")),String.valueOf(p.get("wicket_taken")),String.valueOf(p.get("maidens")),String.valueOf(p.get("economy")),String.valueOf(p.get("runs_conceded")));
                                            getItem(position).fillfieldingstats(String.valueOf(p.get("catchball")),String.valueOf(p.get("runout")),String.valueOf(p.get("lbw")),String.valueOf(p.get("stumped")),String.valueOf(p.get("bowled")));

                                            getItem(position).batting_points = String.valueOf(p.get("batting_points"));
                                            getItem(position).bowling_points = String.valueOf(p.get("bowling_points"));
                                            getItem(position).fielding_points =String.valueOf(p.get("fielding_points"));

                                            runs.setText((getItem(position)).runs);
                                            fours.setText((getItem(position)).fours);
                                            sixes.setText((getItem(position)).sixes);
                                            strike_rate.setText((getItem(position)).strike_rake);
                                            overs_bowled.setText((getItem(position)).overs_bowled);
                                            maidens.setText((getItem(position)).maidens);
                                            wicket_taken.setText((getItem(position)).wicket_taken);
                                            runs_conceded.setText((getItem(position)).runs_conceded);
                                            economy.setText((getItem(position)).economy);
                                            runout.setText((getItem(position)).runout);
                                            catchball.setText((getItem(position)).catchball);
                                            stumped.setText((getItem(position)).stumped);
                                            bowled.setText((getItem(position)).bowled);
                                            lbw.setText((getItem(position)).lbw);



                                            if(getItem(position).batting_points.equals("null") || getItem(position).batting_points.isEmpty())
                                            {
                                                Log.d("scorecard","batting null");
                                                LinearLayout ll_batting = dialogView.findViewById(R.id.ll_batting);
                                                ll_batting.setVisibility(View.GONE);
                                            }
                                            if(getItem(position).bowling_points.equals("null")||getItem(position).bowling_points.isEmpty())
                                            {
                                                LinearLayout ll_bowling = dialogView.findViewById(R.id.ll_bowling);
                                                ll_bowling.setVisibility(View.GONE);
                                            }
                                            if(getItem(position).fielding_points.equals("null") || getItem(position).fielding_points.isEmpty())
                                            {
                                                LinearLayout ll_fielding = dialogView.findViewById(R.id.ll_fielding);
                                                ll_fielding.setVisibility(View.GONE);
                                            }
                                            battingpoints.setText((getItem(position)).batting_points+" Points");
                                            fieldingpoints.setText((getItem(position)).fielding_points+" Points");
                                            bowlingpoints.setText((getItem(position)).bowling_points+" Points");
                                            pointstv.setText(String.valueOf(getItem(position).points)+" Points");


                                            break;

                                        }


                                    }

                                    if(!present)
                                    {
                                        playerscoresview.setVisibility(View.GONE);
                                        playernotplayingview.setText(getItem(position).name + " is not in playing 11.");
                                    }


                                }
                            });





                        }

                    }
                });


            }
            return convertView;

        }

    }




    class getteamtask extends AsyncTask<String, Boolean, Boolean> {
        boolean finishedforme=false;
        JSONObject data;

        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)

            if(started)
            {

                try {


                    JSONObject main_object = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlFantasy(matchid));
                    Log.d("apicall","api is called in leaderboard");

                    data = main_object.getJSONObject("data");
                    try
                    {
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

                                    player.fillbattingstats(String.valueOf(p.get("R")),String.valueOf(p.get("4s")),String.valueOf(p.get("6s")),String.valueOf(p.get("SR")));
                                    player.batting_points= String.valueOf(points);
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

                                    player.fillbowlingstats(String.valueOf(p.get("O")),String.valueOf(p.get("W")),String.valueOf(p.get("M")),String.valueOf(p.get("Econ")),String.valueOf(p.get("R")));
                                    player.bowling_points= String.valueOf(points);

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

                                    player.fillfieldingstats(String.valueOf(p.get("catch")),String.valueOf(p.get("runout")),String.valueOf(p.get("lbw")),String.valueOf(p.get("stumped")),String.valueOf(p.get("bowled")));
                                    player.fielding_points = String.valueOf(points);
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

                    // displaying notification that contest is finished.
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CurrentPointsActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.contest_finished_notification, null);
                    dialogBuilder.setView(dialogView);
                    final AlertDialog b = dialogBuilder.create();
                    b.show();

                    TextView finalpointstv = dialogView.findViewById(R.id.tv_totalpoints);
                    TextView titletv = dialogView.findViewById(R.id.tv_participantid);
                    TextView contestnametv = dialogView.findViewById(R.id.tv_contestname);
                    TextView spotstv = dialogView.findViewById(R.id.tv_spots);

                    finalpointstv.setText(String.valueOf(totalpoints));
                    titletv.setText(participantID);
                    contestnametv.setText(contestname);
                    spotstv.setText(String.valueOf(totalspots)+"Spots");


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
                            p.put("team",player.team);

//                            p.put("runs",player.runs);
//                            p.put("runs_conceded",player.runs_conceded);
//                            p.put("fours",player.fours);
//                            p.put("sixes",player.sixes);
//                            p.put("strike_rate",player.strike_rake);
//                            p.put("overs_bowled",player.overs_bowled);
//                            p.put("maidens",player.maidens);
//                            p.put("wicket_taken",player.wicket_taken);
//                            p.put("bowled",player.bowled);
//                            p.put("runout",player.runout);
//                            p.put("stumped",player.stumped);
//                            p.put("lbw",player.lbw);
//                            p.put("catchball",player.catchball);
//                            p.put("economy",player.economy);
//                            p.put("batting_points",player.batting_points);
//                            p.put("bowling_points",player.bowling_points);
//                            p.put("fielding_points",player.fielding_points);

                        }
                    }
                }

                teamobject.put("Team",playerobject);

                db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(participantID).set(teamobject);
                db.collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists())
                        {
                            ArrayList<Map<String,Object>> team_global_db = (ArrayList<Map<String, Object>>) documentSnapshot.getData().get("Team");


                            for(Map<String,Object> p: team_global_db)
                            {
                                for(Player player : playerArrayList)
                                {
                                    if(p.get("pid").equals(player.pid))
                                    {
                                        p.put("points",player.points);
                                        p.put("type",player.type);
                                        p.put("team",player.team);
                                        p.put("runs",player.runs);
                                        p.put("runs_conceded",player.runs_conceded);
                                        p.put("fours",player.fours);
                                        p.put("sixes",player.sixes);
                                        p.put("strike_rate",player.strike_rake);
                                        p.put("overs_bowled",player.overs_bowled);
                                        p.put("maidens",player.maidens);
                                        p.put("wicket_taken",player.wicket_taken);
                                        p.put("bowled",player.bowled);
                                        p.put("runout",player.runout);
                                        p.put("stumped",player.stumped);
                                        p.put("lbw",player.lbw);
                                        p.put("catchball",player.catchball);
                                        p.put("economy",player.economy);
                                        p.put("batting_points",player.batting_points);
                                        p.put("bowling_points",player.bowling_points);
                                        p.put("fielding_points",player.fielding_points);

                                    }

                                }
                            }

                            Map<String,Object> team = new HashMap<>();
                            team.put("Team",team_global_db);
                            db.collection("Matches").document(matchid).set(team);
                        }
                        else
                        {
                            try {
                                JSONArray team = data.getJSONArray("team");
                                JSONObject team1ob = team.getJSONObject(0);
                                JSONObject team2ob = team.getJSONObject(1);
                                if (!team1ob.getString("name").equals(team1)) {
                                    team1ob = team.getJSONObject(1);
                                    team2ob = team.getJSONObject(0);
                                }

                                JSONArray team1arrjson = team1ob.getJSONArray("players");
                                JSONArray team2arrjson = team2ob.getJSONArray("players");

                                ArrayList<Map<String,Object>> team_global_db = new ArrayList<Map<String,Object>>();

                                for(int i =0 ;i<team1arrjson.length();i++)
                                {
                                    Map<String,Object> player = new HashMap<>();
                                    JSONObject p = team1arrjson.getJSONObject(i);

                                    player.put("pid",p.get("pid"));
                                    player.put("name",p.get("name"));

                                    team_global_db.add(player);
                                }

                                for(int i =0 ;i<team2arrjson.length();i++)
                                {
                                    Map<String,Object> player = new HashMap<>();
                                    JSONObject p = team2arrjson.getJSONObject(i);

                                    player.put("pid",p.get("pid"));
                                    player.put("name",p.get("name"));

                                    team_global_db.add(player);
                                }

                                for(Map<String,Object> p: team_global_db)
                                {
                                    for(Player player : playerArrayList)
                                    {
                                        if(p.get("pid").equals(player.pid))
                                        {
                                            p.put("points",player.points);
                                            p.put("type",player.type);
                                            p.put("team",player.team);
                                            p.put("runs",player.runs);
                                            p.put("runs_conceded",player.runs_conceded);
                                            p.put("fours",player.fours);
                                            p.put("sixes",player.sixes);
                                            p.put("strike_rate",player.strike_rake);
                                            p.put("overs_bowled",player.overs_bowled);
                                            p.put("maidens",player.maidens);
                                            p.put("wicket_taken",player.wicket_taken);
                                            p.put("bowled",player.bowled);
                                            p.put("runout",player.runout);
                                            p.put("stumped",player.stumped);
                                            p.put("lbw",player.lbw);
                                            p.put("catchball",player.catchball);
                                            p.put("economy",player.economy);
                                            p.put("batting_points",player.batting_points);
                                            p.put("bowling_points",player.bowling_points);
                                            p.put("fielding_points",player.fielding_points);

                                        }

                                    }
                                }
                                Map<String,Object> team_document = new HashMap<>();
                                team_document.put("Team",team_global_db);
                                db.collection("Matches").document(matchid).set(team_document);


                            }
                            catch ( Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });
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
