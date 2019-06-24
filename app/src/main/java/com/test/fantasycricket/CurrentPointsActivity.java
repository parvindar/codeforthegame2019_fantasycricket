package com.test.fantasycricket;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_points);
        db=FirebaseFirestore.getInstance();
        lv= findViewById(R.id.lv_currentpoints_playerlist);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        TextView totalpoints_tv = findViewById(R.id.tv_totalpoints);
        TextView participantid_tv = findViewById(R.id.tv_participantid);
        TextView team1_tv = findViewById(R.id.tv_team1);
        TextView team2_tv = findViewById(R.id.tv_team2);

        String team1,team2;
        team1 = getIntent().getStringExtra("team1");
        team2 = getIntent().getStringExtra("team2");
        matchid = getIntent().getStringExtra("matchid");
        contestid = getIntent().getStringExtra("contestid");
        participantID = getIntent().getStringExtra("ParticipantID");

        totalpoints_tv.setText(String.valueOf(totalpoints));
        participantid_tv.setText(participantID);
        team1_tv.setText(team1);
        team2_tv.setText(team2);

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

                        player.type="Batsman";

                        playerArrayList.add(player);
                    }

                    PlayerListAdaptor playerListAdaptor = new PlayerListAdaptor(CurrentPointsActivity.this,R.layout.player_element_curr_points,playerArrayList);
                    lv.setAdapter(playerListAdaptor);

                }
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
                try {

                    String man_of_the_match;
                    JSONObject main_object = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlFantasy(matchid));
                    JSONObject data = main_object.getJSONObject("data");
                    man_of_the_match = data.getString("man-of-the-match");
                    JSONArray batting_arr_object = data.getJSONArray("batting");
                    JSONArray bowling_arr_object = data.getJSONArray("bowling");
                    JSONArray fielding_arr_object = data.getJSONArray("fielding");




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
