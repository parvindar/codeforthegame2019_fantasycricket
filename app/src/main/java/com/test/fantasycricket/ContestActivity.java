package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.DeadObjectException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.test.fantasycricket.Constants.dec;

public class ContestActivity extends AppCompatActivity {
    ListView contestlist;
    static String matchid;
    String team1,team2;
    ArrayList<Contest> contestArrayList;
    String contestname,totalprize,totalspots,price;
    boolean started;
    Double gain,gainfactor=20.0;
    FirebaseFirestore db;
    ArrayList<String> mycontests;

    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onResume() {
        super.onResume();

        refreshFunction();


    }

    void refreshFunction()
    {
        db.collection("Matches").document(matchid).collection("Contests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                contestArrayList.clear();
                for( DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments())
                {
                    String prize,price,spotsfilled,totalspots;
                    String contestname,id;
                    boolean contestfinished;

                    prize = documentSnapshot.getData().get("TotalPrize").toString();
                    price = documentSnapshot.getData().get("Price").toString();
                    spotsfilled = (documentSnapshot.getData().get("SpotsFilled").toString());
                    totalspots = (documentSnapshot.getData().get("TotalSpots").toString());
                    contestfinished=(boolean)documentSnapshot.getData().get("Finished");
                    id= documentSnapshot.getId();
                    contestname = documentSnapshot.getString("ContestName");
                    Contest currcontest = new Contest(id,Double.valueOf(prize),Double.valueOf(price),Integer.valueOf(spotsfilled),Integer.valueOf(totalspots));
                    currcontest.contestname = contestname;
                    currcontest.finished=contestfinished;
                    contestArrayList.add(currcontest);

                }

                ContestListAdaptor contestListAdaptor = new ContestListAdaptor(ContestActivity.this,R.layout.contest,contestArrayList);
                contestlist.setAdapter(contestListAdaptor);

                mSwipeRefreshLayout.setRefreshing(false);

            }
        });


        if(UserInfo.logined)
        {
            db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    Intent intent;
                    intent = new Intent(ContestActivity.this,CreateTeamActivity.class);

                    if(documentSnapshot.exists())
                    {
                        Map<String,Object> contestobject = documentSnapshot.getData();

                        mycontests = (ArrayList<String>) contestobject.get("contests");

                        ContestListAdaptor contestListAdaptor = new ContestListAdaptor(ContestActivity.this,R.layout.contest,contestArrayList);
                        contestlist.setAdapter(contestListAdaptor);

                    }

                }
            });

        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CreateTeamActivity.matchid = matchid;
        db=FirebaseFirestore.getInstance();

        contestlist=findViewById(R.id.lv_contests);
        FloatingActionButton createcontestbtn = findViewById(R.id.fab_createcontest);
        contestArrayList = new ArrayList<>();
        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv = findViewById(R.id.tv_team2);
        team1=getIntent().getStringExtra("team1");
        team2=getIntent().getStringExtra("team2");
        started = getIntent().getBooleanExtra("started",false);


        team1tv.setText(team1);
        team2tv.setText(team2);

        db.collection("Matches").document(matchid).collection("Contests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                contestArrayList.clear();
                for( DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments())
                {
                    String prize,price,spotsfilled,totalspots;
                    String contestname,id;
                    boolean contestfinished,contestawarded;

                    prize = documentSnapshot.getData().get("TotalPrize").toString();
                    price = documentSnapshot.getData().get("Price").toString();
                    spotsfilled = (documentSnapshot.getData().get("SpotsFilled").toString());
                    totalspots = (documentSnapshot.getData().get("TotalSpots").toString());
                    contestfinished=(boolean)documentSnapshot.getData().get("Finished");
                    id= documentSnapshot.getId();
                    contestname = documentSnapshot.getString("ContestName");
                    try{
                        contestawarded = documentSnapshot.getBoolean("Awarded");
                    }catch (Exception e)
                    {
                        contestawarded=false;
                    }

                    Contest currcontest = new Contest(id,Double.valueOf(prize),Double.valueOf(price),Integer.valueOf(spotsfilled),Integer.valueOf(totalspots));
                    currcontest.contestname = contestname;
                    currcontest.finished=contestfinished;
                    currcontest.awarded = contestawarded;
                    contestArrayList.add(currcontest);

                }

                ContestListAdaptor contestListAdaptor = new ContestListAdaptor(ContestActivity.this,R.layout.contest,contestArrayList);
                contestlist.setAdapter(contestListAdaptor);

            }
        });

        //=====


        if(UserInfo.logined)
        {
            db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    Intent intent;
                    intent = new Intent(ContestActivity.this,CreateTeamActivity.class);

                    if(documentSnapshot.exists())
                    {
                        Map<String,Object> contestobject = documentSnapshot.getData();

                        mycontests = (ArrayList<String>) contestobject.get("contests");

                        ContestListAdaptor contestListAdaptor = new ContestListAdaptor(ContestActivity.this,R.layout.contest,contestArrayList);
                        contestlist.setAdapter(contestListAdaptor);

                    }

                }
            });

        }


//
//    }
//});



//        ContestListAdaptor contestListAdaptor = new ContestListAdaptor(this,R.layout.contest,contestArrayList);
//        contestlist.setAdapter(contestListAdaptor);


        createcontestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContestActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.createcontest, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                final EditText et_contestname = dialogView.findViewById(R.id.et_contestname);
                final EditText et_totalprize = dialogView.findViewById(R.id.et_totalprize);
                final EditText et_totalspots = dialogView.findViewById(R.id.et_totalspots);
                final TextView tv_price = dialogView.findViewById(R.id.tv_price);
                TextView dialogteam1 = dialogView.findViewById(R.id.tv_team1);
                TextView dialogteam2 = dialogView.findViewById(R.id.tv_team2);
                dialogteam1.setText(team1);
                dialogteam2.setText(team2);
                contestname="";
                Button btn_createcontest = dialogView.findViewById(R.id.btn_submit);

                btn_createcontest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contestname = et_contestname.getText().toString();
                        totalprize = et_totalprize.getText().toString();
                        totalspots = et_totalspots.getText().toString();
//                        price = tv_price.getText().toString();

                        if(totalspots.isEmpty() || totalprize.isEmpty() || price.isEmpty())
                        {
                            Toast.makeText(ContestActivity.this,"Fill the details.",Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(Integer.parseInt(totalspots)<5)
                        {
                            Toast.makeText(ContestActivity.this,"Minimum 5 spots",Toast.LENGTH_LONG).show();
                            return;
                        }

                        Map<String,Object> contest = new HashMap<>();
                        contest.put("ContestName","Private: "+contestname);
                        contest.put("TotalPrize",totalprize);
                        contest.put("TotalSpots",totalspots);
                        contest.put("Price",price);
                        contest.put("SpotsFilled",0);
                        contest.put("Finished",false);
                        contest.put("Awarded",false);
                        db.collection("Matches").document(matchid).collection("Contests").add(contest).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(ContestActivity.this,"Contest Created Successfully",Toast.LENGTH_LONG).show();
                                b.dismiss();

                            }
                        });

                    }
                });


                et_totalprize.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().isEmpty() && !et_totalspots.getText().toString().isEmpty())
                        {
                            if(Double.valueOf(s.toString())>10000 && UserInfo.usertype.equals("user"))
                            {
                                et_totalprize.setText("10000");
                                Toast.makeText(ContestActivity.this,"You can't set Prize greater than 10000",Toast.LENGTH_LONG).show();
                            }

                            TextView tv_price =(TextView)dialogView.findViewById(R.id.tv_price);

                            if( Double.parseDouble(et_totalspots.getText().toString())==0)
                            {
                                return;
                            }
                            String prize = s.toString();
                            Double prizeamt= Double.parseDouble(prize);
                            Double totalspots = Double.parseDouble(et_totalspots.getText().toString());
                            gain = prizeamt*(gainfactor/100);
                            price =String.valueOf (dec.format((prizeamt + gain)/totalspots));
                            if(Double.parseDouble(price)<1)
                            {
                                if(Double.parseDouble(prize)==0)
                                {
                                    price="0";
                                    gain=0.0;
                                }
                                else
                                {
                                    price="1";
                                    gain = totalspots-prizeamt;
                                }
                            }
                            tv_price.setText(UserInfo.INR+price);

                        }

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                et_totalspots.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        if(!et_totalprize.getText().toString().isEmpty() &&  !s.toString().isEmpty())
                        {

                            if(Double.parseDouble(s.toString())==0 )
                            {
                                return;
                            }

                             String prize = et_totalprize.getText().toString();
                             Double prizeamt= Double.parseDouble(prize);
                             Double totalspots = Double.parseDouble(s.toString());
                             gain = prizeamt*(gainfactor/100);
                            price =String.valueOf (dec.format((prizeamt + gain)/totalspots));
                            if(Double.parseDouble(price)<1)
                            {
                                if(Double.parseDouble(prize)==0)
                                {
                                    price="0";
                                    gain=0.0;
                                }
                                else
                                {
                                    price="1";
                                    gain = totalspots-prizeamt;
                                }
                            }
                             TextView tv_price =(TextView)dialogView.findViewById(R.id.tv_price);
                             tv_price.setText(UserInfo.INR+price);

                        }

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });


        //  swipe to refresh ===========================================================
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshFunction();

/*
                db.collection("Matches").document(matchid).collection("Contests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        contestArrayList.clear();
                        for( DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments())
                        {
                            String prize,price,spotsfilled,totalspots;
                            String contestname,id;
                            boolean contestfinished;

                            prize = documentSnapshot.getData().get("TotalPrize").toString();
                            price = documentSnapshot.getData().get("Price").toString();
                            spotsfilled = (documentSnapshot.getData().get("SpotsFilled").toString());
                            totalspots = (documentSnapshot.getData().get("TotalSpots").toString());
                            contestfinished=(boolean)documentSnapshot.getData().get("Finished");
                            id= documentSnapshot.getId();
                            contestname = documentSnapshot.getString("ContestName");
                            Contest currcontest = new Contest(id,Double.valueOf(prize),Double.valueOf(price),Integer.valueOf(spotsfilled),Integer.valueOf(totalspots));
                            currcontest.contestname = contestname;
                            currcontest.finished=contestfinished;
                            contestArrayList.add(currcontest);

                        }

                        ContestListAdaptor contestListAdaptor = new ContestListAdaptor(ContestActivity.this,R.layout.contest,contestArrayList);
                        contestlist.setAdapter(contestListAdaptor);

                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                });


                db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Intent intent;
                        intent = new Intent(ContestActivity.this,CreateTeamActivity.class);

                        if(documentSnapshot.exists())
                        {
                            Map<String,Object> contestobject = documentSnapshot.getData();

                            mycontests = (ArrayList<String>) contestobject.get("contests");

                            ContestListAdaptor contestListAdaptor = new ContestListAdaptor(ContestActivity.this,R.layout.contest,contestArrayList);
                            contestlist.setAdapter(contestListAdaptor);

                        }

                    }
                });
*/

            }
        });

        //=========================================================================





    }



    private class Contest{
        Integer spotsfilled,totalspots;
        Double prize,price;
        String contestname;
        public String id;
        public boolean finished=false;
        public boolean awarded = false;

        public Contest(String id,Double prize, Double price, Integer spotsfilled, Integer totalspots) {
            this.prize = prize;
            this.price = price;
            this.spotsfilled = spotsfilled;
            this.totalspots = totalspots;
            this.contestname ="";
            this.id = id;
        }
    }


    public class ContestListAdaptor extends ArrayAdapter<Contest> {
        private static final String TAG = "ContestListAdaptor";
        private Context mContext;
        private int mResource;

        public ContestListAdaptor(Context context, int resource, List<Contest> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(getItem(position)!=null) {

                Double prize = getItem(position).prize;
                final Double price = getItem(position).price;
                Integer spotsfilled = getItem(position).spotsfilled;
                Integer totalspots = getItem(position).totalspots;

                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView prizetv = convertView.findViewById(R.id.tv_prize);
                TextView pricetv = convertView.findViewById(R.id.tv_price);
                TextView spotslefttv = convertView.findViewById(R.id.tv_spotsleft);
                TextView totalspotstv = convertView.findViewById(R.id.tv_totalspots);
                ProgressBar spotsfilledpb = convertView.findViewById(R.id.pb_spotsfilled);
                TextView contestnametv = convertView.findViewById(R.id.tv_contestname);
                TextView missedtv = convertView.findViewById(R.id.tv_missed);
                if(started)
                {
                    if(mycontests == null || !mycontests.contains(getItem(position).id))
                    {
                        missedtv.setText("Missed");
                        missedtv.setTextColor(Color.RED);
                    }
                    else
                    {
                        if(getItem(position).finished)
                        {
                            missedtv.setText("Finished");
                            missedtv.setTextColor(0xFF00A70B);
                        }
                        else
                        {
                            missedtv.setText("Running");
                            missedtv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    }
                }
                else
                {

                    if(mycontests != null && mycontests.contains(getItem(position).id))
                    {
                        missedtv.setText("You're In");
                        missedtv.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        missedtv.setVisibility(View.INVISIBLE);
                    }
                }




                pricetv.setText(UserInfo.INR+String.valueOf(price));
                prizetv.setText(UserInfo.INR+String.valueOf(prize));

                if(prize/10000000>=1)
                {
                    Double _prizeunit = prize/10000000;
                    prizetv.setText(Constants.INR+String.valueOf(_prizeunit)+" Cr.");
                }

                totalspotstv.setText(String.valueOf(totalspots));
                spotslefttv.setText(String.valueOf(totalspots-spotsfilled));
                spotsfilledpb.setProgress((100*spotsfilled)/totalspots);

                contestnametv.setText(getItem(position).contestname);
                if(prize==0 && !getItem(position).contestname.isEmpty())
                {
                    if(getItem(position).contestname.length()>13)
                    {
                        prizetv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f);
                    }
                    prizetv.setText(getItem(position).contestname);


                    contestnametv.setText("");
                }





                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {
                            if(UserInfo.logined && mycontests!=null && mycontests.contains(getItem(position).id))
                            {

                                //=========================

                                        Intent intent;
                                        intent = new Intent(ContestActivity.this,CurrentPointsActivity.class);
                                        intent.putExtra("ParticipantID",UserInfo.username);
                                        intent.putExtra("started",started);
                                        intent.putExtra("finishedforall",getItem(position).finished);
                                        intent.putExtra("awarded",getItem(position).awarded);
                                        intent.putExtra("team1",team1);
                                        intent.putExtra("team2",team2);
                                        intent.putExtra("matchid",matchid);
                                        intent.putExtra("contestid",getItem(position).id);
                                        intent.putExtra("price",getItem(position).price);
                                        intent.putExtra("contestname",getItem(position).contestname);
                                        intent.putExtra("totalspots",getItem(position).totalspots);

                                        startActivity(intent);
                                        return;

                            }

                            if(started && !UserInfo.logined)
                            {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContestActivity.this);
                                LayoutInflater inflater = getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                                dialogBuilder.setView(dialogView);
                                final AlertDialog b = dialogBuilder.create();
                                b.show();

                                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                                TextView yousuretv = dialogView.findViewById(R.id.tv_dialog_areyousure);
                                yousuretv.setVisibility(View.GONE);
                                title_tv.setText("Registrations are over");
                                detail_tv.setText("Contest has already started, You have missed this contest.\nYou can always see the leaderboard.");

                                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                                Button nobtn = dialogView.findViewById(R.id.btn_no);

                                yesbtn.setText("Leaderboard");
                                nobtn.setText("Cancel");

                                yesbtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        UserInfo.instantLogin(ContestActivity.this);
                                        Toast.makeText(ContestActivity.this,"You need to login to see the leaderboard.",Toast.LENGTH_LONG).show();
                                        b.dismiss();
                                    }

                                });

                                nobtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        b.dismiss();
                                    }
                                });


                                return;
                            }

                            if(started && UserInfo.logined)
                            {

                                db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Log.d("debug","started and logined");


                                            Map<String,Object> contestobject = documentSnapshot.getData();
                                            if(!documentSnapshot.exists() || !contestobject.keySet().contains(getItem(position).id))
                                            {
                                                Log.d("debug","document not exist OR contest not in my contests");
                                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContestActivity.this);
                                                LayoutInflater inflater = getLayoutInflater();
                                                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                                                dialogBuilder.setView(dialogView);
                                                final AlertDialog b = dialogBuilder.create();
                                                b.show();

                                                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                                                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                                                TextView yousuretv = dialogView.findViewById(R.id.tv_dialog_areyousure);
                                                yousuretv.setVisibility(View.GONE);

                                                if(getItem(position).finished)
                                                {
                                                    title_tv.setText("Contest is finished");
                                                    detail_tv.setText("Contest has finished, You have missed this contest.\nYou can always see the leaderboard.");
                                                }
                                                else {
                                                    title_tv.setText("Registrations are over");
                                                    detail_tv.setText("Contest has already started, You have missed this contest.\nYou can always see the leaderboard.");
                                                }

                                                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                                                Button nobtn = dialogView.findViewById(R.id.btn_no);

                                                yesbtn.setText("Leaderboard");
                                                nobtn.setText("Cancel");

                                                yesbtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent intent1 = new Intent(ContestActivity.this,LeaderboardActivity.class);
                                                        intent1.putExtra("notregistered",true);
                                                        intent1.putExtra("matchid",matchid);
                                                        intent1.putExtra("contestid",getItem(position).id);
                                                        intent1.putExtra("team1",team1);
                                                        intent1.putExtra("team2",team2);
                                                        intent1.putExtra("participantID",UserInfo.username);
                                                        intent1.putExtra("contestname",getItem(position).contestname);
                                                        intent1.putExtra("totalspots",getItem(position).totalspots);
                                                        intent1.putExtra("finishedforall",getItem(position).finished);
                                                        intent1.putExtra("started",started);

                                                        startActivity(intent1);
                                                        b.dismiss();
                                                    }

                                                });

                                                nobtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        b.dismiss();
                                                    }
                                                });


                                            }
                                            else
                                            {

                                                Log.d("debug","document exist & contest is in my contests");

                                                Intent intent3;
                                                intent3 = new Intent(ContestActivity.this,CurrentPointsActivity.class);
                                                intent3.putExtra("ParticipantID",UserInfo.username);
                                                intent3.putExtra("started",started);
                                                intent3.putExtra("finishedforall",getItem(position).finished);
                                                intent3.putExtra("awarded",getItem(position).awarded);
                                                intent3.putExtra("team1",team1);
                                                intent3.putExtra("team2",team2);
                                                intent3.putExtra("matchid",matchid);
                                                intent3.putExtra("contestid",getItem(position).id);
                                                intent3.putExtra("price",getItem(position).price);
                                                intent3.putExtra("contestname",getItem(position).contestname);
                                                intent3.putExtra("totalspots",getItem(position).totalspots);
                                                startActivity(intent3);

                                            }

                                    }
                                });





                            }



/*
                                Toast.makeText(ContestActivity.this,"Contest is already running, You have missed it.",Toast.LENGTH_LONG).show();
                                return;
*/


                            if(!started)
                            {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContestActivity.this);
                                LayoutInflater inflater = getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.participate_contest_dialog, null);
                                dialogBuilder.setView(dialogView);
                                final AlertDialog b = dialogBuilder.create();
                                b.show();

                                TextView prizetv= dialogView.findViewById(R.id.tv_dialog_prize);
                                TextView pricetv = dialogView.findViewById(R.id.tv_dialog_price);
                                TextView contestname = dialogView.findViewById(R.id.tv_dialog_contestname);
                                Button btn_participate = dialogView.findViewById(R.id.btn_participate);
                                Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);
                                prizetv.setText(UserInfo.INR+getItem(position).prize.toString());
                                pricetv.setText(UserInfo.INR+ getItem(position).price.toString());
                                contestname.setText(getItem(position).contestname);

                                btn_cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        b.dismiss();
                                    }
                                });

                                btn_participate.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if(!UserInfo.logined)
                                        {
                                            UserInfo.instantLogin(ContestActivity.this);
                                            return;
                                        }

                                        db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                Intent intent;
                                                intent = new Intent(ContestActivity.this,CreateTeamActivity.class);

                                                if(documentSnapshot.exists())
                                                {
                                                    Map<String,Object> contestobject = documentSnapshot.getData();
                                                    if(contestobject.keySet().contains(getItem(position).id))
                                                    {
                                                        Toast.makeText(ContestActivity.this,"You have already participated in this contest",Toast.LENGTH_LONG).show();
                                                        intent = new Intent(ContestActivity.this,CurrentPointsActivity.class);
                                                        Map<String,Object> contestdetail = (Map<String,Object>)contestobject.get(getItem(position).id);
                                                        intent.putExtra("ParticipantID",contestdetail.get("ParticipantID").toString());
                                                        intent.putExtra("started",started);
                                                        intent.putExtra("finishedforall",getItem(position).finished);
                                                        intent.putExtra("awarded",getItem(position).awarded);
                                                        intent.putExtra("team1",team1);
                                                        intent.putExtra("team2",team2);
                                                        intent.putExtra("matchid",matchid);
                                                        intent.putExtra("contestid",getItem(position).id);
                                                        intent.putExtra("price",getItem(position).price);
                                                        intent.putExtra("contestname",getItem(position).contestname);
                                                        intent.putExtra("totalspots",getItem(position).totalspots);

                                                        startActivity(intent);
                                                    }


                                                }

                                                intent.putExtra("team1",team1);
                                                intent.putExtra("team2",team2);
                                                intent.putExtra("matchid",matchid);
                                                intent.putExtra("contestid",getItem(position).id);
                                                intent.putExtra("price",getItem(position).price);
                                                intent.putExtra("contestname",getItem(position).contestname);
                                                intent.putExtra("totalspots",getItem(position).totalspots);

                                                CreateTeamActivity.contestid=getItem(position).id;
                                                CreateTeamActivity.matchid=matchid;
                                                startActivity(intent);

                                                b.dismiss();


                                            }
                                        });



                                    }
                                });


                            }



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
