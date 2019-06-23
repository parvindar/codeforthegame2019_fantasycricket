package com.test.fantasycricket;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.internal.JsonParser;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                    if(!UserInfo.logined)
                    {
                        Toast.makeText(CreateTeamActivity.this,"You need to login to register in contest.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(price<UserInfo.cash){


                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CreateTeamActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        final View dialogView = inflater.inflate(R.layout.wallet_payment_layout, null);
                        dialogBuilder.setView(dialogView);
                        final AlertDialog b = dialogBuilder.create();
                        b.show();

                        TextView dialogteam1tv = dialogView.findViewById(R.id.tv_team1);
                        TextView dialogteam2tv = dialogView.findViewById(R.id.tv_team2);
                        TextView dialogpricetv = dialogView.findViewById(R.id.tv_pay_price);
                        TextView dialogcashtv = dialogView.findViewById(R.id.tv_pay_cash);
                        dialogteam1tv.setText(team1);
                        dialogteam2tv.setText(team2);
                        dialogpricetv.setText(Constants.INR+String.valueOf(price));
                        dialogcashtv.setText(Constants.INR+String.valueOf(UserInfo.cash));

                        Button dialogcancelbtn = dialogView.findViewById(R.id.btn_cancel);
                        Button dialogsubmitbtn = dialogView.findViewById(R.id.btn_pay_submit);

                        dialogcancelbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                b.dismiss();
                            }
                        });

                        dialogsubmitbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                UserInfo.cash -= price;
                                Map<String, Object> teamobject= new HashMap<>();
                                Map<String,Object> player =new HashMap<>();
                                ArrayList<Map<String,Object>> teamlist_maplist = new ArrayList<>();
                                for(Player p : myteam)
                                {
                                    player= new HashMap<>();
                                    player.put("name",p.name);
                                    player.put("credits",p.credits);
                                    player.put("pid",p.pid);
                                    player.put("points",p.points);
                                    player.put("team",p.team);
                                    teamlist_maplist.add(player);

                                }


                                    teamobject.put("Team",teamlist_maplist);

                                    db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(UserInfo.username+String.valueOf(UserInfo.xp)+String.valueOf(UserInfo.cash)).set(teamobject).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CreateTeamActivity.this,"Registered Successfully",Toast.LENGTH_LONG).show();
                                        }
                                    });


                                db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Map<String,Object> contestidobject = new HashMap<>();

                                        if(documentSnapshot.exists())
                                        {
                                            Map<String,Object> data = documentSnapshot.getData();
                                            ArrayList<String> contestsarraylist = new ArrayList<>();
                                            contestsarraylist=(ArrayList<String>) data.get("contests");
                                            contestsarraylist.add(contestid);
                                            contestidobject.put("contests",contestsarraylist);
                                            db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).set(contestidobject);
                                        }
                                        else
                                        {
                                            ArrayList<String> contestsarraylist = new ArrayList<>();
                                            contestsarraylist.add(contestid);
                                            contestidobject.put("contests",contestsarraylist);

                                            db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).set(contestidobject);
                                        }
                                    }
                                });
                                db.collection("Users").document(UserInfo.username).update("Cash",UserInfo.cash);

                                b.dismiss();
                                CreateTeamActivity.this.finish();
                            }
                        });


                    }
                    else
                    {
                        Toast.makeText(CreateTeamActivity.this,"You don't have enough cash in your wallet!",Toast.LENGTH_LONG).show();

                    }

                    //db.collection("Matches").document(matchid).collection("Contests")

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



    class Player {
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
        }
    }



    public class PlayerListAdaptor extends ArrayAdapter<Player> {
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
                    ll.setBackgroundColor(Color.parseColor("#73c2fb"));
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
