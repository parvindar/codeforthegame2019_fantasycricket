package com.test.fantasycricket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.internal.JsonParser;

import static com.test.fantasycricket.Constants.dec;

public class CreateTeamActivity extends AppCompatActivity {

    ArrayList<Player> myteam;

    FirebaseFirestore db;
    ListView team1list ;
    ListView team2list ;
    PlayerListAdaptor team1listadaptor,team2listadaptor;
    Double price=0d;

    TextView tv_credits;
    String team1,team2;
    Integer count_team1=0,count_team2=0;
    Double credits = 100.0;
    TextView totalplayerselectedtv;
    Integer player_selected_num=0;
    static String matchid;
    static String contestid;

    public static Activity fa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fa= this;

        myteam = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        price = getIntent().getDoubleExtra("price",0);

        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv= findViewById(R.id.tv_team2);
        team1=getIntent().getStringExtra("team1");
        team2=getIntent().getStringExtra("team2");
        team1tv.setText(team1);
        team2tv.setText(team2);

        totalplayerselectedtv = findViewById(R.id.tv_total_player_selected);
        tv_credits = findViewById(R.id.tv_creditsleft);

        TextView team1listtv = findViewById(R.id.tv_team1list);
        TextView team2listtv = findViewById(R.id.tv_team2list);
        team1listtv.setText(team1);
        team2listtv.setText(team2);

        team1list = findViewById(R.id.lv_team1list);
        team2list = findViewById(R.id.lv_team2list);

        new getteamtask().execute();

        FloatingActionButton createteambtn = findViewById(R.id.fab_maketeam);

        createteambtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(myteam.size()!=11)
                {
                    Toast.makeText(CreateTeamActivity.this,"Select 11 players!",Toast.LENGTH_LONG).show();
                }
                else
                {

                    //---------------------------------------------------------------===============
                    Intent intent = new Intent(CreateTeamActivity.this, ChooseCaptainActivity.class);
                    ChooseCaptainActivity.myteamlist = myteam;
                    intent.putExtra("team1",team1);
                    intent.putExtra("team2",team2);
                    intent.putExtra("matchid",matchid);
                    intent.putExtra("contestid",contestid);
                    intent.putExtra("price",price);
                    startActivity(intent);
                    //-------------------------------------------------------------=============

                }
            }
        });




    }


        class getteamtask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)
            ArrayList<Player> team1_arrlist = new ArrayList<>();
            ArrayList<Player> team2_arrlist = new ArrayList<>();

            try {

                JSONObject squadobject = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlSquad(matchid));
                JSONArray jsonsquad =squadobject.getJSONArray("squad");
                JSONObject team1object =jsonsquad.getJSONObject(0);
                JSONObject team2object =jsonsquad.getJSONObject(1);
                JSONArray team1_json = team1object.getJSONArray("players");
                JSONArray team2_json = team2object.getJSONArray("players");

                for (int i=0;i<team1_json.length();i++)
                {
                    Double p_credits=9.0;
                    JSONObject player_json = team1_json.getJSONObject(i);
                    String pid,name;
                    name = player_json.getString("name");
                    pid = player_json.getString("pid");
                    Player player = new Player(name,pid,p_credits);
                    player.team=team1;
                    team1_arrlist.add(player);

                }

                for (int i=0;i<team2_json.length();i++)
                {
                    Double p_credits=9.0;
                    JSONObject player_json = team2_json.getJSONObject(i);
                    String pid,name;
                    name = player_json.getString("name");
                    pid = player_json.getString("pid");
                    Player player = new Player(name,pid,p_credits);
                    player.team=team2;
                    team2_arrlist.add(player);


                }




            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }



                team1listadaptor = new PlayerListAdaptor(CreateTeamActivity.this,R.layout.player_element,team1_arrlist);
                team2listadaptor = new PlayerListAdaptor(CreateTeamActivity.this,R.layout.player_element,team2_arrlist);

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            //Call your next task (ui thread)
            team1list.setAdapter(team1listadaptor);
            team2list.setAdapter(team2listadaptor);


        }


    }



    public class Player implements Serializable {
        String name;
        String pid;
        public Double points=0.0;
        Double credits;
        String team;

        public Player(){

        }

        public Player(String name, String pid, Double credits) {
            this.name = name;
            this.pid = pid;
            this.credits = credits;
            this.points=0.0;
            this.team="";
        }

        public String getName() {
            return name;
        }

        public String getPid() {
            return pid;
        }

        public Double getPoints() {
            return points;
        }

        public Double getCredits() {
            return credits;
        }

        public String getTeam() {
            return team;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public void setPoints(Double points) {
            this.points = points;
        }

        public void setCredits(Double credits) {
            this.credits = credits;
        }

        public void setTeam(String team) {
            this.team = team;
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
                TextView creditstv = convertView.findViewById(R.id.tv_player_credits);

                playernametv.setText(getItem(position).name);
                pointstv.setText(String.valueOf(getItem(position).points));
                creditstv.setText(String.valueOf(getItem(position).credits));
                final LinearLayout ll = convertView.findViewById(R.id.ll_player_element);
                if(myteam.contains(getItem(position)))
                {
                    ll.setBackgroundColor(Color.parseColor("#8073c2fb"));
                }
                else
                {
                    ll.setBackgroundColor(Color.WHITE);
                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {
                            if(!myteam.contains(getItem(position)))
                            {
                                if(myteam.size()==11)
                                {
                                    Toast.makeText(mContext,"Total 11 players can be selected!",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(getItem(position).team.equals(team1))
                                {
                                    if(count_team1==7)
                                    {
                                        Toast.makeText(mContext,"Max. 7 players from single team!",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    else
                                    {
                                        count_team1++;
                                    }
                                }
                                else
                                {
                                    if(count_team2==7)
                                    {
                                        Toast.makeText(mContext,"Max. 7 players from single team!",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    else
                                    {
                                        count_team2++;
                                    }
                                }
                                myteam.add(getItem(position));
                                credits = credits - getItem(position).credits;
                                tv_credits.setText(String.valueOf(credits));

                                player_selected_num++;
                                totalplayerselectedtv.setText(String.valueOf(player_selected_num)+"/11");


                            }
                            else
                            {

                                if(getItem(position).team.equals(team1))
                                {
                                    count_team1--;
                                }
                                else
                                {
                                    count_team2--;
                                }

                                myteam.remove(getItem(position));
                                credits = credits + getItem(position).credits;
                                tv_credits.setText(String.valueOf(credits));

                                player_selected_num--;
                                totalplayerselectedtv.setText(String.valueOf(player_selected_num)+"/11");

                            }

                            notifyDataSetChanged();


                        }

                    }
                });


            }
            return convertView;

        }



    }


    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
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
