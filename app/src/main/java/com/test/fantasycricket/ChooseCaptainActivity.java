package com.test.fantasycricket;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.test.fantasycricket.Constants.dec;

public class ChooseCaptainActivity extends AppCompatActivity {

    ListView lv ;
    static ArrayList<CreateTeamActivity.Player> myteamlist;
    String captain="",vicecaptain="";
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_captain);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = FirebaseFirestore.getInstance();

        final String team1,team2,contestid,matchid;
        final double price;

        lv = findViewById(R.id.lv_teamlist);
        team1 = getIntent().getStringExtra("team1");
        team2 = getIntent().getStringExtra("team2");
        contestid = getIntent().getStringExtra("contestid");
        matchid = getIntent().getStringExtra("matchid");
        price = getIntent().getDoubleExtra("price",1000d);
        TextView team1_tv = findViewById(R.id.tv_team1);
        TextView team2_tv = findViewById(R.id.tv_team2);
        team1_tv.setText(team1);
        team2_tv.setText(team2);

  /*      Bundle args = getIntent().getBundleExtra("BUNDLE");
        myteamlist = (ArrayList<CreateTeamActivity.Player>) args.getSerializable("ARRAYLIST");
*/

        PlayerListAdaptor playerListAdaptor = new PlayerListAdaptor(ChooseCaptainActivity.this,R.layout.choose_c_vc_layout,myteamlist);

        lv.setAdapter(playerListAdaptor);


        Button participatebtn = findViewById(R.id.btn_participate);

        participatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!UserInfo.logined)
                {
                    Toast.makeText(ChooseCaptainActivity.this,"You need to login to register in contest.",Toast.LENGTH_LONG).show();
                    return;
                }

                if(captain.isEmpty() || vicecaptain.isEmpty())
                {
                    Toast.makeText(ChooseCaptainActivity.this,"Select the best captain and vice captain for your team.",Toast.LENGTH_LONG).show();
                    return;
                }

                if(captain.equals(vicecaptain))
                {
                    Toast.makeText(ChooseCaptainActivity.this,"Select different captain and vice captain for your team.",Toast.LENGTH_LONG).show();
                    return;
                }

                if(price<UserInfo.cash){


                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ChooseCaptainActivity.this);
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

                    dialogpricetv.setText(Constants.INR+dec.format(price));
                    dialogcashtv.setText(Constants.INR+dec.format(UserInfo.cash));

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
                            for(CreateTeamActivity.Player p : myteamlist)
                            {
                                player= new HashMap<>();
                                player.put("name",p.name);
                                player.put("credits",p.credits);
                                player.put("pid",p.pid);
                                player.put("points",p.points);
                                player.put("team",p.team);
                                if(p.pid.equals(captain))
                                {
                                    player.put("captain",true);
                                }
                                if(p.pid.equals(vicecaptain))
                                {
                                    player.put("vicecaptain",true);
                                }
                                teamlist_maplist.add(player);

                            }


                            teamobject.put("Team",teamlist_maplist);
                            teamobject.put("Points",0d);
                            String participantID = db.collection("Matches").document().getId();
                            participantID = participantID.substring(0,10);
                            participantID=UserInfo.username + String.valueOf(UserInfo.xp) + participantID;

                            db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").document(participantID).set(teamobject).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ChooseCaptainActivity.this,"Registered Successfully",Toast.LENGTH_LONG).show();
                                }
                            });

                            db.collection("Matches").document(matchid).collection("Contests").document(contestid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    Long spotsfilled = (Long) documentSnapshot.get("SpotsFilled");
                                    spotsfilled++;
                                    db.collection("Matches").document(matchid).collection("Contests").document(contestid).update("SpotsFilled",spotsfilled);

                                }
                            });

                            // Adding contest details to user's data
                            final String finalParticipantID = participantID;
                            db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Map<String,Object> contestidobject = new HashMap<>();

                                    if(documentSnapshot.exists())
                                    {
                                        contestidobject = documentSnapshot.getData();
                                        ArrayList<String> contestsarraylist = new ArrayList<>();
                                        contestsarraylist=(ArrayList<String>) contestidobject.get("contests");
                                        contestsarraylist.add(contestid);

                                        Map<String,Object> mycontestdetail = new HashMap<>();
                                        mycontestdetail.put("ParticipantID", finalParticipantID);
                                        mycontestdetail.put("TotalPoints",0);

                                        contestidobject.put("contests",contestsarraylist);
                                        contestidobject.put(contestid,mycontestdetail);
                                        db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).set(contestidobject);
                                    }
                                    else
                                    {
                                        ArrayList<String> contestsarraylist = new ArrayList<>();
                                        contestsarraylist.add(contestid);
                                        Map<String,Object> mycontestdetail = new HashMap<>();
                                        mycontestdetail.put("ParticipantID", finalParticipantID);
                                        mycontestdetail.put("TotalPoints",0);

                                        contestidobject.put("contests",contestsarraylist);
                                        contestidobject.put(contestid,mycontestdetail);

                                        db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).set(contestidobject);
                                    }
                                }
                            });

                            // updating user's cash in wallet.
                            db.collection("Users").document(UserInfo.username).update("Cash",UserInfo.cash);

                            b.dismiss();
                            ChooseCaptainActivity.this.finish();
                            CreateTeamActivity.fa.finish();
                        }
                    });


                }
                else
                {
                    Toast.makeText(ChooseCaptainActivity.this,"You don't have enough cash in your wallet!",Toast.LENGTH_LONG).show();

                }

                //db.collection("Matches").document(matchid).collection("Contests")




            }
        });


    }





    private class PlayerListAdaptor extends ArrayAdapter<CreateTeamActivity.Player> {
        private static final String TAG = "PlayerListAdaptor";
        private Context mContext;
        private int mResource;

        public PlayerListAdaptor(Context context, int resource, List<CreateTeamActivity.Player> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(getItem(position)!=null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView playername_tv = convertView.findViewById(R.id.tv_player_name);
                TextView playerteam_tv = convertView.findViewById(R.id.tv_playerteam);
                TextView player_credits_tv = convertView.findViewById(R.id.tv_player_credits);
                final TextView captaintv = convertView.findViewById(R.id.tv_captain);
                final TextView vicecaptaintv = convertView.findViewById(R.id.tv_vicecaptain);

                playername_tv.setText(getItem(position).name);
                playerteam_tv.setText(getItem(position).team);
                player_credits_tv.setText(String.valueOf(getItem(position).credits));

                final TextView captain_tv = findViewById(R.id.tv_captain);
                final TextView vicecaptain_tv = findViewById(R.id.tv_vicecaptain);


                if(getItem(position).pid.equals(vicecaptain))
                {
                    vicecaptaintv.setBackgroundColor(Color.parseColor("#80D94D1B"));
                    vicecaptaintv.setTextColor(Color.WHITE);
                }
                if(getItem(position).pid.equals(captain))
                {
                    captaintv.setBackgroundColor(Color.parseColor("#FFD94D1B"));
                    captaintv.setTextColor(Color.WHITE);

                }


                captaintv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        captain_tv.setText(getItem(position).name);
                        captain=getItem(position).pid;
                        if(getItem(position).pid.equals(vicecaptain))
                        {
                            vicecaptain="";
                            vicecaptain_tv.setText("Vice Captain");

                        }

                        notifyDataSetChanged();

                    }
                });

                vicecaptaintv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vicecaptain_tv.setText(getItem(position).name);
                        vicecaptain=getItem(position).pid;
                        if(getItem(position).pid.equals(captain))
                        {
                            captain="";
                            captain_tv.setText("Vice Captain");

                        }

                        notifyDataSetChanged();

                    }
                });


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
