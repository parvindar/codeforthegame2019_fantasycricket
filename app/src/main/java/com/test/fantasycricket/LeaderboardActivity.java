package com.test.fantasycricket;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static android.view.Gravity.BOTTOM;

public class LeaderboardActivity extends AppCompatActivity {

    ListView lv ;
    String matchid,contestid;
    String participantID;
    String team1,team2,contestname;
    Integer totalspots;
    String captain,vicecaptain;
    TextView points_tv ;
    TextView rank_tv ;
    TextView conteststatus_tv;
    boolean notregistered;
    double contestprize;

    FirebaseFirestore db;
    boolean started,finishedforme,finishedforall=false,awarded=false;

    SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();

        lv = findViewById(R.id.lv_leaderboardlist);
        matchid = getIntent().getStringExtra("matchid");
        contestid = getIntent().getStringExtra("contestid");
        team1 = getIntent().getStringExtra("team1");
        team2 = getIntent().getStringExtra("team2");
        participantID = getIntent().getStringExtra("participantID");
        contestname = getIntent().getStringExtra("contestname");
        totalspots = getIntent().getIntExtra("totalspots",0);
        started = getIntent().getBooleanExtra("started",false);
        finishedforme = getIntent().getBooleanExtra("finished",false);
        finishedforall=getIntent().getBooleanExtra("finishedforall",false);
        awarded = getIntent().getBooleanExtra("awarded",false);
        notregistered = getIntent().getBooleanExtra("notregistered",false);

        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv = findViewById(R.id.tv_team2);
        TextView contestname_tv = findViewById(R.id.tv_contestname);
        TextView totalspots_tv = findViewById(R.id.tv_spots);
        TextView participantid_tv = findViewById(R.id.tv_participantid);
        conteststatus_tv = findViewById(R.id.tv_conteststatus);
        points_tv = findViewById(R.id.tv_points);
        rank_tv = findViewById(R.id.tv_rank);

        team1tv.setText(team1);
        team2tv.setText(team2);
        contestname_tv.setText(contestname);
        totalspots_tv.setText(totalspots+" Spots");
        participantid_tv.setText(participantID);
        if(notregistered)
        {
            points_tv.setText("?");
            rank_tv.setText("?");
        }


        // calculate points of 10 users before and 10 users after and the user itself and then show it on leaderboard.

        new getteamtask().execute();




//  swipe to refresh ===========================================================
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                new getteamtask().execute();

//                db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").orderBy("Points", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        ArrayList<Participant> participants = new ArrayList<>();
//                        int i =0;
//                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
//                        {
//                            i++;
//                            String id = documentSnapshot.getId();
//                            double points;
//                            try{
//                                points = (double)documentSnapshot.get("Points");
//
//                            }catch (Exception e)
//                            {
//                                points = (double)(((Long)documentSnapshot.get("Points")).intValue());
//                            }
//                            participants.add(new Participant(id,points));
//                            if(id.equals(participantID))
//                            {
//                                points_tv.setText(Constants.dec.format(points));
//                                rank_tv.setText(String.valueOf(i));
//                            }
//
//                        }
//
//                        ParticipantListAdaptor participantListAdaptor = new ParticipantListAdaptor(LeaderboardActivity.this,R.layout.leaderboard_element,participants);
//                        lv.setAdapter(participantListAdaptor);
//                        mSwipeRefreshLayout.setRefreshing(false);
//
//
//                    }
//                });


            }
        });

        //=========================================================================





    }

    class Participant{
        String id;
        double points;
        int rank;

        public Participant(String id, double points,int rank) {
            this.id = id;
            this.points = points;
            this.rank=rank;

        }
    }





    private class ParticipantListAdaptor extends ArrayAdapter<Participant> {
        private static final String TAG = "ParticipantListAdaptor";
        private Context mContext;
        private int mResource;

        public ParticipantListAdaptor(Context context, int resource, List<Participant> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(getItem(position)!=null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView Participantid_tv = convertView.findViewById(R.id.tv_participantid);
                TextView pointstv = convertView.findViewById(R.id.tv_points);
                TextView ranktv = convertView.findViewById(R.id.tv_rank);

                if(getItem(position).id.equals(participantID))
                {
                    Participantid_tv.setTextColor(Color.BLACK);
                    pointstv.setTextColor(Color.BLACK);

                    Participantid_tv.setTypeface(null, Typeface.BOLD);
                    pointstv.setTypeface(null, Typeface.BOLD);
                    ranktv.setTypeface(null,Typeface.BOLD);
                }
                Participantid_tv.setText(getItem(position).id);
                pointstv.setText(Constants.dec.format(getItem(position).points));
                ranktv.setText(String.valueOf(getItem(position).rank));


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LeaderboardActivity.this);
                            LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                            final View dialogView = inflater.inflate(R.layout.otherplayer_playerlist_layout, null);
                            dialogBuilder.setView(dialogView);
                            final AlertDialog b = dialogBuilder.create();
                            b.show();

//                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                            lp.copyFrom(b.getWindow().getAttributes());
//                            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//                            b.show();
//                            b.getWindow().setGravity(Gravity.CENTER);
//                            b.getWindow().setAttributes(lp);

                            TextView participantid_tv = dialogView.findViewById(R.id.tv_participantid);
                            TextView contestname_tv = dialogView.findViewById(R.id.tv_contestname);
                            TextView totalspots_tv = dialogView.findViewById(R.id.tv_spots);
//                            TextView team1_tv = dialogView.findViewById(R.id.tv_team1);
//                            TextView team2_tv = dialogView.findViewById(R.id.tv_team2);
                            final TextView othertotalpoints_tv = dialogView.findViewById(R.id.tv_totalpoints);
                            participantid_tv.setText(getItem(position).id);
                            othertotalpoints_tv.setText(Constants.dec.format(getItem(position).points));
                            contestname_tv.setText(contestname);
                            totalspots_tv.setText(String.valueOf(totalspots)+" Spots");
//                            team1_tv.setText(team1);
//                            team2_tv.setText(team2);

                            final ListView lv_otherplayerteam = dialogView.findViewById(R.id.lv_currentpoints_playerlist);

                            db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(getItem(position).id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists())
                                    {
                                        captain="";
                                        vicecaptain="";
                                        ArrayList<Player> otherplayerArrayList=new ArrayList<>();
                                        Map<String,Object> otherteamobject = documentSnapshot.getData();
                                        ArrayList<Map<String,Object>> otherplayerobject = (ArrayList<Map<String,Object>>)otherteamobject.get("Team");

                                        try {
                                            othertotalpoints_tv.setText(Constants.dec.format((double) otherteamobject.get("Points")));
                                        }
                                        catch (Exception e)
                                        {
                                            othertotalpoints_tv.setText(Constants.dec.format((double)((Long)otherteamobject.get("Points")).intValue()));

                                        }
                                        for(Map<String,Object> p : otherplayerobject)
                                        {
                                            Player player = new Player(p.get("name").toString(),p.get("pid").toString(),Double.parseDouble(p.get("credits").toString()));
                                            player.points = Double.parseDouble(p.get("points").toString());
                                            player.team = p.get("team").toString();
                                            try{
                                                player.type=p.get("type").toString();
                                            }
                                            catch (Exception e)
                                            {
                                                player.type = "";
                                            }
                                            if(p.get("captain")!=null && (boolean)p.get("captain"))
                                            {
                                                captain = player.pid;
                                            }
                                            if(p.get("vicecaptain")!=null && (boolean)p.get("vicecaptain"))
                                            {
                                                vicecaptain = player.pid;
                                            }

                                            otherplayerArrayList.add(player);
                                        }

                                        PlayerListAdaptor playerListAdaptor = new PlayerListAdaptor(LeaderboardActivity.this,R.layout.player_element_curr_points,otherplayerArrayList);

                                        lv_otherplayerteam.setAdapter(playerListAdaptor);

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
            this.type="";
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
        boolean finished=false;
        String man_of_the_match;
        private double totalpoints;
        private String captain,vicecaptain;
        boolean finishedforall1 = false,finishedforall2=false;
        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)

            if(!notregistered && started && !finishedforall)
            {
                Log.d("apicall","inside calculation area");

                try {


                   JSONObject main_object = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlFantasy(matchid));

                   Log.d("apicall","api is called in leaderboard");
                    final JSONObject data = main_object.getJSONObject("data");
                    try
                    {
                        man_of_the_match = data.getJSONObject("man-of-the-match").getString("name");
                        Log.d("manofthematch","man found man = "+man_of_the_match);
                    }
                    catch (Exception e)
                    {
                        e.toString();
                        man_of_the_match="";
                    }


                    final JSONArray batting_arr_object = data.getJSONArray("batting");
                    final JSONArray bowling_arr_object = data.getJSONArray("bowling");
                    final JSONArray fielding_arr_object = data.getJSONArray("fielding");
                    JSONArray team1arrjson,team2arrjson;

                    if(!man_of_the_match.isEmpty())
                    {
                        String teamofman="";
                        JSONArray team  = data.getJSONArray("team");
                        JSONObject team1ob = team.getJSONObject(0);
                        JSONObject team2ob = team.getJSONObject(1);
                        if(!team1ob.getString("name").equals(team1))
                        {
                            team1ob = team.getJSONObject(1);
                            team2ob = team.getJSONObject(0);
                        }

                        team1arrjson = team1ob.getJSONArray("players");
                        team2arrjson = team2ob.getJSONArray("players");

                        for(int i =0;i<team1arrjson.length();i++)
                        {
                            JSONObject p = team1arrjson.getJSONObject(i);
                            if(p.getString("name").equals(man_of_the_match)){
                                teamofman=team1;
                                break;
                            }
                        }
                        if(teamofman.isEmpty())
                        {
                            for(int i =0;i<team2arrjson.length();i++)
                            {
                                JSONObject p = team2arrjson.getJSONObject(i);
                                if(p.getString("name").equals(man_of_the_match)){
                                    teamofman=team2;
                                    break;
                                }
                            }
                        }

                        if(!teamofman.isEmpty())
                        {
                            db.collection("Team").document(teamofman).update("man_of_the_match",man_of_the_match);
                        }

                    }

                    try{
                        if(data.keys()!=null && (data.getString("winner_team").equals(team1) || data.getString("winner_team").equals(team2)))
                        {
                            finished=true;
                        }

                    }
                    catch (Exception e)
                    {
                        finished=false;

                        Log.d("error","team winner wala error"+e.toString());
                    }

                    // calculating points for 25 participants after the user and the user.
                    db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").orderBy(FieldPath.documentId()).startAt(participantID).whereEqualTo("Finished",false).limit(26).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            ArrayList<Player> playerArrayList = new ArrayList<>();
                            if(queryDocumentSnapshots.isEmpty()){
                                finishedforall1=true;
                                Log.d("debug","before finisheforall1 true "+finishedforall1);

                            }
                            Log.d("debug","after me document size "+queryDocumentSnapshots.size());

                            for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                try {
                                    playerArrayList.clear();
                                    String id = documentSnapshot.getId();
                                    Log.d("calculate"," after me : "+id);

                                    Map<String, Object> teamdata = documentSnapshot.getData();
                                    ArrayList<Map<String, Object>> playerobject = (ArrayList<Map<String, Object>>) teamdata.get("Team");


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
                                            player.type="";
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

                                    for (Player player : playerArrayList) {
                                        player.points = 0d;
                                    }

                                    for (int i = 0; i < batting_arr_object.length(); i++) {
                                        JSONObject batting_object = batting_arr_object.getJSONObject(i);
                                        JSONArray batting_scores = batting_object.getJSONArray("scores");
                                        for (int j = 0; j < batting_scores.length(); j++) {
                                            double points;

                                            JSONObject p = batting_scores.getJSONObject(j);

                                            for (Player player : playerArrayList) {
                                                if ((player.pid).equals(p.getString("pid"))) {
                                                    points = Calculate.points_batting(p);

                                                    if (player.pid.equals(captain)) {
                                                        points = points * (2.0);
                                                    }
                                                    if (player.pid.equals(vicecaptain)) {
                                                        points = points * (1.5);
                                                    }

                                                    player.points += points;
                                                    if (points > 25d) {
                                                        player.type = "Batting";
                                                    }
                                                    break;
                                                }
                                            }

                                        }

                                    }

//                                    Log.d("calculate"," calculated batting  "+id);


                                    for (int i = 0; i < bowling_arr_object.length(); i++) {
                                        JSONObject bowling_object = bowling_arr_object.getJSONObject(i);
                                        JSONArray bowling_scores = bowling_object.getJSONArray("scores");
                                        for (int j = 0; j < bowling_scores.length(); j++) {
                                            double points ;
                                            JSONObject p = bowling_scores.getJSONObject(j);

                                            for (Player player : playerArrayList) {
                                                if ((player.pid).equals(p.getString("pid"))) {
                                                    points = Calculate.points_bowling(p);

                                                    if (player.pid.equals(captain)) {
                                                        points = points * (2.0);
                                                    }
                                                    if (player.pid.equals(vicecaptain)) {
                                                        points = points * (1.5);
                                                    }


                                                    player.points += points;
                                                    if (player.type.equals("Batting") && points >20) {
                                                        player.type = "All-Rounder";
                                                    } else if(points > 25){
                                                        player.type = "Bowling";
                                                    }
                                                    break;
                                                }
                                            }

                                        }

                                    }

//                                    Log.d("calculate"," calculated bowling  "+id);



                                    for (int i = 0; i < fielding_arr_object.length(); i++) {
                                        JSONObject fielding_object = fielding_arr_object.getJSONObject(i);
                                        JSONArray fielding_scores = fielding_object.getJSONArray("scores");
                                        for (int j = 0; j < fielding_scores.length(); j++) {
                                            double points = 0;
                                            JSONObject p = fielding_scores.getJSONObject(j);

                                            for (Player player : playerArrayList) {
                                                if ((player.pid).equals(p.getString("pid"))) {
                                                    points = Calculate.points_fielding(p);


                                                    if (player.pid.equals(captain)) {
                                                        points = points * (2.0);
                                                    }
                                                    if (player.pid.equals(vicecaptain)) {
                                                        points = points * (1.5);
                                                    }


                                                    player.points += points;
                                                    if (Integer.parseInt(p.getString("stumped")) > 0) {
                                                        player.type = "Wicket-Keeper";
                                                    } else if (player.type.equals("") && points>25) {
                                                        player.type = "Fielding";
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }


//                                    Log.d("calculate"," calculated feilding  "+id);

                                    totalpoints = 0d;
                                    Log.d("finalpoints", "size of playerarraylist "+id + " "+playerArrayList.size());

                                    for (Player player : playerArrayList) {

                                        totalpoints += player.points;
                                        Log.d("finalpoints", "Calculating totalpoints for "+id + " "+player.name+" - "+player.points+" total - "+totalpoints);
                                    }


                                    Log.d("final", "FINAL TOTAL SCORE "+id+" " + String.valueOf(totalpoints));
                                    teamdata.put("Points",totalpoints);

                                    if(finished)
                                    {
                                        teamdata.put("Finished",true);
                                    }

                                    for (Player player : playerArrayList)
                                    {
                                        for(Map<String,Object> p : playerobject)
                                        {
                                            if(p.get("pid").equals(player.pid))
                                            {
                                                p.put("points",player.points);
                                                p.put("type",player.type);
                                                Log.d("finalpoints", id+" copying to map "+p.get("name")+" "+p.get("type")+" " + String.valueOf(p.get("points")));
                                            }

                                        }

                                    }



                                    teamdata.put("Team",playerobject);

                                    db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(id).set(teamdata);

                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            if(!finishedforall&&finishedforall1 && finishedforall2)
                            {
                                finishedforall=true;
                                db.collection("Matches").document(matchid).collection("Contests").document(contestid).update("Finished",true);
                            }

                            updateContestStatustext();


                        }
                    });


                    // calculating points for 25 participants before the user.
                    db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").orderBy(FieldPath.documentId()).endBefore(participantID).whereEqualTo("Finished",false).limit(25).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            ArrayList<Player> playerArrayList = new ArrayList<>();
                            if(queryDocumentSnapshots.isEmpty()){
                                finishedforall2=true;
                                Log.d("debug","before finisheforall2 true "+finishedforall2);

                            }

                            Log.d("debug","before me document size "+queryDocumentSnapshots.size());
                            for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                try {
                                    String id = documentSnapshot.getId();
                                    Log.d("calculate","before me : calculating points for "+id);

                                    Map<String, Object> teamdata = documentSnapshot.getData();
                                    ArrayList<Map<String, Object>> playerobject = (ArrayList<Map<String, Object>>) teamdata.get("Team");


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
                                            player.type="";
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


                                    for (Player player : playerArrayList) {
                                        player.points = 0d;
                                    }

                                    for (int i = 0; i < batting_arr_object.length(); i++) {
                                        JSONObject batting_object = batting_arr_object.getJSONObject(i);
                                        JSONArray batting_scores = batting_object.getJSONArray("scores");
                                        for (int j = 0; j < batting_scores.length(); j++) {
                                            double points;

                                            JSONObject p = batting_scores.getJSONObject(j);
                                            Log.d("points ", p.toString());


                                            for (Player player : playerArrayList) {
                                                if ((player.pid).equals(p.getString("pid"))) {
                                                    points = Calculate.points_batting(p);
                                                    Log.d("points ", "calculated point bat " + String.valueOf(points));

                                                    if (player.pid.equals(captain)) {
                                                        points = points * (2.0);
                                                    }
                                                    if (player.pid.equals(vicecaptain)) {
                                                        points = points * (1.5);
                                                    }

                                                    player.points += points;
                                                    if (points > 25) {
                                                        player.type = "Batting";
                                                    }
                                                    break;
                                                }
                                            }

                                        }

                                    }

                                    for (int i = 0; i < bowling_arr_object.length(); i++) {
                                        JSONObject bowling_object = bowling_arr_object.getJSONObject(i);
                                        JSONArray bowling_scores = bowling_object.getJSONArray("scores");
                                        for (int j = 0; j < bowling_scores.length(); j++) {
                                            double points ;
                                            JSONObject p = bowling_scores.getJSONObject(j);

                                            for (Player player : playerArrayList) {
                                                if ((player.pid).equals(p.getString("pid"))) {
                                                    points = Calculate.points_bowling(p);

                                                    if (player.pid.equals(captain)) {
                                                        points = points * (2.0);
                                                    }
                                                    if (player.pid.equals(vicecaptain)) {
                                                        points = points * (1.5);
                                                    }


                                                    player.points += points;
                                                    if (player.type.equals("Batting")) {
                                                        player.type = "All-Rounder";
                                                    } else {
                                                        player.type = "Bowling";
                                                    }
                                                    break;
                                                }
                                            }

                                        }

                                    }


                                    for (int i = 0; i < fielding_arr_object.length(); i++) {
                                        JSONObject fielding_object = fielding_arr_object.getJSONObject(i);
                                        JSONArray fielding_scores = fielding_object.getJSONArray("scores");
                                        for (int j = 0; j < fielding_scores.length(); j++) {
                                            double points = 0;
                                            JSONObject p = fielding_scores.getJSONObject(j);

                                            for (Player player : playerArrayList) {
                                                if ((player.pid).equals(p.getString("pid"))) {
                                                    points = Calculate.points_fielding(p);


                                                    if (player.pid.equals(captain)) {
                                                        points = points * (2.0);
                                                    }
                                                    if (player.pid.equals(vicecaptain)) {
                                                        points = points * (1.5);
                                                    }


                                                    player.points += points;
                                                    if (Integer.parseInt(p.getString("stumped")) > 0) {
                                                        player.type = "Wicket-Keeper";
                                                    } else if (player.type.equals("") && points>25) {
                                                        player.type = "Fielding";
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    totalpoints = 0d;
                                    for (Player player : playerArrayList) {

                                        totalpoints += player.points;

                                    }



                                    teamdata.put("Points",totalpoints);

                                    if(finished)
                                    {
                                        teamdata.put("Finished",true);
                                    }

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

                                    teamdata.put("Team",playerobject);

                                    db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(id).set(teamdata);







                                }
                                catch(Exception e)
                                {

                                    e.printStackTrace();
                                    Log.d("debug",e.toString());
                                }
                            }

                            if(!finishedforall&&finishedforall1 && finishedforall2)
                            {
                                finishedforall=true;
                                db.collection("Matches").document(matchid).collection("Contests").document(contestid).update("Finished",true);
                            }

                            updateContestStatustext();



                        }

                    });






                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("debug",e.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("debug",e.toString());

                }


            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            //Call your next task (ui thread)

            updateContestStatustext();

            db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").orderBy("Points", Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    ArrayList<Participant> participants = new ArrayList<>();
                    int i =0;
                    for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                    {

                        String id = documentSnapshot.getId();
                        double points;
                        try{
                            points = (double)documentSnapshot.get("Points");

                        }catch (Exception e)
                        {
                            points = (double)(((Long)documentSnapshot.get("Points")).intValue());
                        }
                        if(i==0)
                        {
                            i++;
                        }
                        else
                        {
                            if(participants.get(i-1).points != points)
                            {
                                i++;
                            }
                        }
                        participants.add(new Participant(id,points,i));

                        if(id.equals(participantID))
                        {
                            points_tv.setText(Constants.dec.format(points));
                            rank_tv.setText(String.valueOf(i));
                        }

                    }

                    ParticipantListAdaptor participantListAdaptor = new ParticipantListAdaptor(LeaderboardActivity.this,R.layout.leaderboard_element,participants);
                    lv.setAdapter(participantListAdaptor);

                    if(mSwipeRefreshLayout.isRefreshing())
                    {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }


                }
            });

            if(finishedforall && !awarded)
            {

                db.collection("Matches").document(matchid).collection("Contests").document(contestid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        try
                        {
                            awarded = documentSnapshot.getBoolean("Awarded");
                        }
                        catch (Exception io)
                        {
                            awarded = false;
                        }

                        if(!awarded)
                        {
                            awarded=true;
                            db.collection("Matches").document(matchid).collection("Contests").document(contestid).update("Awarded",true);

                            db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").orderBy("Points", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    double points=0;
                                    int i=0;

                                    for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                                    {
                                        points = documentSnapshot.getDouble("Points");
                                        break;
                                    }

                                    final double finalPoints = points;
                                    db.collection("Matches").document(matchid).collection("Contests").document(contestid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                            contestprize =Double.valueOf(documentSnapshot.getString("TotalPrize"));
                                            Log.d("award", "contest prize value "+contestprize);
                                            db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").whereEqualTo("Points", finalPoints).limit(100).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    final double prizeforeach = contestprize/(double)(queryDocumentSnapshots.size());
                                                    Log.d("award", "contest prize value per person "+prizeforeach);

                                                    for(DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots)
                                                    {
                                                        final String id = documentSnapshot1.getId();
                                                        Log.d("award", "winner id  "+id);

                                                        db.collection("Users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                double winnings =Double.valueOf(String.valueOf(documentSnapshot.get("Winnings")));
                                                                Log.d("award", "winnings of person "+winnings+" "+prizeforeach);

                                                                winnings = winnings+prizeforeach;
                                                                db.collection("Users").document(id).update("Winnings",winnings);
                                                            }
                                                        });
                                                    }

                                                }
                                            });


                                        }
                                    });

                                }
                            });
                        }

                    }
                });

            }

        }


        void updateContestStatustext()
        {

            if(finishedforall)
            {
                conteststatus_tv.setText("Contest is finished, rankings are final");
                conteststatus_tv.setTextColor(Color.RED);
            }
            else if(finished)
            {
                conteststatus_tv.setText("Match is finished, points are still being calculated");
                conteststatus_tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

            }
            else if(started)
            {
                conteststatus_tv.setText("Contest is running");
                conteststatus_tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
            else
            {
                conteststatus_tv.setText("Contest will start soon");
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
