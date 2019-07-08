package com.test.fantasycricket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.test.fantasycricket.Constants.dec;

public class ContestActivity extends AppCompatActivity {
    static String matchid;
    static String team1;
    static String team2;
    static FirebaseFirestore db=FirebaseFirestore.getInstance();
    static boolean started;
    static TextView team1scoretv;
    static TextView team2scoretv;
    static String winner_team="";



    static SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onResume() {
        super.onResume();

        ContestsFragment.refreshFunction(this);


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contest_activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CreateTeamActivity.matchid = matchid;
        db=FirebaseFirestore.getInstance();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container_contestactivity);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs_contestactivity);
        tabLayout.setupWithViewPager(mViewPager);

        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv = findViewById(R.id.tv_team2);
        TextView team1fulltv = findViewById(R.id.tv_team1_full);
        TextView team2fulltv = findViewById(R.id.tv_team2_full);
        CircularImageView team1flag = findViewById(R.id.img_team1);
        CircularImageView team2flag = findViewById(R.id.img_team2);

        team1scoretv = findViewById(R.id.tv_team1_score);
        team2scoretv = findViewById(R.id.tv_team2_score);

        team1=getIntent().getStringExtra("team1");
        team2=getIntent().getStringExtra("team2");
        winner_team = getIntent().getStringExtra("winner_team");
        started = getIntent().getBooleanExtra("started",false);

        Log.d("debug","team1 "+team1);

        team1tv.setText(Constants.getTeamShortName(team1));
        team2tv.setText(Constants.getTeamShortName(team2));

        team1fulltv.setText(team1);
        team2fulltv.setText(team2);

        String uri1 = "https://github.com/parvindar/codeforthegame2019_fantasycricket/blob/master/app/src/main/res/drawable/"+Constants.getTeamShortName(team1).toLowerCase()+".png?raw=true";
        Picasso.get().load(uri1).placeholder(R.drawable.trophy).into(team1flag);
        String uri2 = "https://github.com/parvindar/codeforthegame2019_fantasycricket/blob/master/app/src/main/res/drawable/"+Constants.getTeamShortName(team2).toLowerCase()+".png?raw=true";
        Picasso.get().load(uri2).placeholder(R.drawable.trophy).into(team2flag);

        if(!winner_team.isEmpty() && (winner_team.equals(team1)||winner_team.equals(team2)))
        {
            team1scoretv.setText(getIntent().getStringExtra("team1_score"));
            team2scoretv.setText(getIntent().getStringExtra("team2_score"));

            if(winner_team.equals(team1))
            {
                team1scoretv.setTextColor(0xFF00A70B);
            }
            else if(winner_team.equals(team2))
            {
                team2scoretv.setTextColor(0xFF00A70B);
            }

        }
        else {
            new getmatchscoretask().execute();

        }


    }










    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ContestsFragment extends Fragment {

        static ListView contestlist;
        static ArrayList<Contest> contestArrayList = new ArrayList<>();
        String contestname,totalprize,totalspots,price;
        Double gain,gainfactor=20.0;
        static ArrayList<String> mycontests;

        static SwipeRefreshLayout mSwipeRefreshLayout;




        /**
         * The fragment argument representing the section number for this
         * fragment.
         *
         *
         */

        static void refreshFunction(final Context mcontext)
        {
            try {

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Matches").document(matchid).collection("Contests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        contestArrayList.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String prize, price, spotsfilled, totalspots;
                            String contestname, id;
                            boolean contestfinished;

                            prize = documentSnapshot.getData().get("TotalPrize").toString();
                            price = documentSnapshot.getData().get("Price").toString();
                            spotsfilled = (documentSnapshot.getData().get("SpotsFilled").toString());
                            totalspots = (documentSnapshot.getData().get("TotalSpots").toString());
                            contestfinished = (boolean) documentSnapshot.getData().get("Finished");
                            id = documentSnapshot.getId();
                            contestname = documentSnapshot.getString("ContestName");
                            Contest currcontest = new Contest(id, Double.valueOf(prize), Double.valueOf(price), Integer.valueOf(spotsfilled), Integer.valueOf(totalspots));
                            currcontest.contestname = contestname;
                            currcontest.finished = contestfinished;
                            contestArrayList.add(currcontest);

                        }

                        ContestListAdaptor contestListAdaptor = new ContestListAdaptor(mcontext, R.layout.contest, contestArrayList);
                        try {
                            contestlist.setAdapter(contestListAdaptor);

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        try {
                            mSwipeRefreshLayout.setRefreshing(false);

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                });


                if (UserInfo.logined) {
                    db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            Intent intent;
                            intent = new Intent(mcontext, CreateTeamActivity.class);

                            if (documentSnapshot.exists()) {
                                Map<String, Object> contestobject = documentSnapshot.getData();

                                mycontests = (ArrayList<String>) contestobject.get("contests");

                                ContestListAdaptor contestListAdaptor = new ContestListAdaptor(mcontext, R.layout.contest, contestArrayList);
                                try {
                                    contestlist.setAdapter(contestListAdaptor);

                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }

                        }
                    });

                }
                new getmatchscoretask().execute();


            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d("debug","error calling refresh in contest");
            }


        }



        private static final String ARG_SECTION_NUMBER = "section_number";

        public ContestsFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ContestsFragment newInstance(int sectionNumber) {
            ContestsFragment fragment = new ContestsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);




            contestlist=getView().findViewById(R.id.lv_contests);
            FloatingActionButton createcontestbtn = getView().findViewById(R.id.fab_createcontest);
            contestArrayList = new ArrayList<>();
//            TextView team1tv= getView().findViewById(R.id.tv_team1);
//            TextView team2tv = getView().findViewById(R.id.tv_team2);
            team1=getActivity().getIntent().getStringExtra("team1");
            team2=getActivity().getIntent().getStringExtra("team2");
            started = getActivity().getIntent().getBooleanExtra("started",false);

            Log.d("debug","team1 "+team1);

//            team1tv.setText(team1);
//            team2tv.setText(team2);
            Log.d("debug","mathid "+matchid);

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

                    ContestListAdaptor contestListAdaptor = new ContestListAdaptor(getContext(),R.layout.contest,contestArrayList);
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
                        intent = new Intent(getContext(),CreateTeamActivity.class);

                        if(documentSnapshot.exists())
                        {
                            Map<String,Object> contestobject = documentSnapshot.getData();

                            mycontests = (ArrayList<String>) contestobject.get("contests");

                            ContestListAdaptor contestListAdaptor = new ContestListAdaptor(getContext(),R.layout.contest,contestArrayList);
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
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
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
                                Toast.makeText(getContext(),"Fill the details.",Toast.LENGTH_LONG).show();
                                return;
                            }

                            if(Integer.parseInt(totalspots)<5)
                            {
                                Toast.makeText(getContext(),"Minimum 5 spots",Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(getContext(),"Contest Created Successfully",Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(getContext(),"You can't set Prize greater than 10000",Toast.LENGTH_LONG).show();
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
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.activity_main_swipe_refresh_layout);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    refreshFunction(getContext());


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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_contest, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
// ONCREATE ======






            //ONCREATE =======
            return rootView;
        }





        private static class Contest{
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


        public static class ContestListAdaptor extends ArrayAdapter<Contest> {
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
                                missedtv.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
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
                                    intent = new Intent(getContext(),CurrentPointsActivity.class);
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

                                    getContext().startActivity(intent);
                                    return;

                                }

                                if(started && !UserInfo.logined)
                                {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                                    LayoutInflater inflater = LayoutInflater.from(mContext);
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

                                            UserInfo.instantLogin(getContext());
                                            Toast.makeText(getContext(),"You need to login to see the leaderboard.",Toast.LENGTH_LONG).show();
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

                                if(!started )
                                {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                                    LayoutInflater inflater = LayoutInflater.from(mContext);
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
                                                UserInfo.instantLogin(getContext());
                                                return;
                                            }

                                            db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                    Intent intent;
                                                    intent = new Intent(getContext(),CreateTeamActivity.class);

                                                    if(documentSnapshot.exists())
                                                    {
                                                        Map<String,Object> contestobject = documentSnapshot.getData();
                                                        if(contestobject.keySet().contains(getItem(position).id))
                                                        {
                                                            Toast.makeText(getContext(),"You have already participated in this contest",Toast.LENGTH_LONG).show();
                                                            intent = new Intent(getContext(),CurrentPointsActivity.class);
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

                                                            getContext().startActivity(intent);
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
                                                    getContext().startActivity(intent);

                                                    b.dismiss();


                                                }
                                            });



                                        }
                                    });


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
                                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                                                LayoutInflater inflater =LayoutInflater.from(mContext);
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

                                                        Intent intent1 = new Intent(getContext(),LeaderboardActivity.class);
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

                                                        getContext().startActivity(intent1);
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
                                                intent3 = new Intent(getContext(),CurrentPointsActivity.class);
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
                                                getContext().startActivity(intent3);

                                            }

                                        }
                                    });





                                }



/*
                                Toast.makeText(ContestActivity.this,"Contest is already running, You have missed it.",Toast.LENGTH_LONG).show();
                                return;
*/






                            }

                        }
                    });


                }
                return convertView;

            }



        }






    }


    public static class LiveUpdatesFragment extends Fragment {
        ListView lv_batting_team1,lv_batting_team2,lv_bowling_team1,lv_bowling_team2;
        ArrayList<Player> batting_list_team1,batting_list_team2,bowling_list_team1,bowling_list_team2;
        BattingListAdaptor battingListAdaptor_team1,battingListAdaptor_team2;
        BowlingListAdaptor bowlingListAdaptor_team1,bowlingListAdaptor_team2;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public LiveUpdatesFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static LiveUpdatesFragment newInstance(int sectionNumber) {
            LiveUpdatesFragment fragment = new LiveUpdatesFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            final Button btn_team1= getView().findViewById(R.id.btn_team1);
            final Button btn_team2= getView().findViewById(R.id.btn_team2);
            btn_team1.setText(team1);
            btn_team2.setText(team2);

            lv_batting_team1 = getView().findViewById(R.id.lv_batting_team1);
            lv_bowling_team1= getView().findViewById(R.id.lv_bowling_team1);
            lv_batting_team2 = getView().findViewById(R.id.lv_batting_team2);
            lv_bowling_team2 = getView().findViewById(R.id.lv_bowling_team2);
            batting_list_team1 = new ArrayList<>();
            bowling_list_team1 = new ArrayList<>();
            batting_list_team2 = new ArrayList<>();
            bowling_list_team2 = new ArrayList<>();

            battingListAdaptor_team1 = new BattingListAdaptor(getContext(),R.layout.batting_list_element,batting_list_team1);
            battingListAdaptor_team2 = new BattingListAdaptor(getContext(),R.layout.batting_list_element,batting_list_team2);
            bowlingListAdaptor_team1 = new BowlingListAdaptor(getContext(),R.layout.bowling_list_element,bowling_list_team1);
            bowlingListAdaptor_team2 = new BowlingListAdaptor(getContext(),R.layout.bowling_list_element,bowling_list_team2);

            lv_batting_team1.setAdapter(battingListAdaptor_team1);
            lv_bowling_team1.setAdapter(bowlingListAdaptor_team1);
            lv_batting_team2.setAdapter(battingListAdaptor_team2);
            lv_bowling_team2.setAdapter(bowlingListAdaptor_team2);



            new getteamtask().execute();


            lv_batting_team2.setVisibility(View.GONE);
            lv_bowling_team2.setVisibility(View.GONE);
            btn_team1.setBackgroundColor(getResources().getColor(R.color.colorAccent));


            btn_team1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lv_batting_team2.setVisibility(View.GONE);
                    lv_bowling_team2.setVisibility(View.GONE);
                    lv_batting_team1.setVisibility(View.VISIBLE);
                    lv_bowling_team1.setVisibility(View.VISIBLE);
                    btn_team1.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    btn_team2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                }
            });


            btn_team2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lv_batting_team2.setVisibility(View.VISIBLE);
                    lv_bowling_team2.setVisibility(View.VISIBLE);
                    lv_batting_team1.setVisibility(View.GONE);
                    lv_bowling_team1.setVisibility(View.GONE);
                    btn_team1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    btn_team2.setBackgroundColor(getResources().getColor(R.color.colorAccent));


                }
            });





        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.match_live_updates, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));



            return rootView;
        }





        private class Player {
            String name;
            String pid;
            public Double points=0.0;
            Double credits;
            String team;
            String type;
            String runs,overs_played,fours,sixes,wicket_taken,strike_rake,dissmissal_info,bowled,overs_bowled,maidens,economy,catchball,runout,lbw,runs_conceded,stumped;
            String batting_points,bowling_points,fielding_points;

            public Player(){

            }

            public Player(String name, String pid, Double credits) {
                this.name = name;
                this.pid = pid;
                this.credits = credits;
                this.type = "";
            }

            public void fillbattingstats(String runs,String fours,String sixes,String strike_rake,String overs_played,String dissmissal_info)
            {

                this.runs = runs;
                this.fours = fours;
                this.sixes = sixes;
                this.strike_rake = strike_rake;
                this.overs_played= overs_played;
                this.dissmissal_info=dissmissal_info;
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


        private class BattingListAdaptor extends ArrayAdapter<Player> {
            private static final String TAG = "BattingListAdaptor";
            private Context mContext;
            private int mResource;

            public BattingListAdaptor(Context context, int resource, List<Player> objects) {
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
                    TextView runs = convertView.findViewById(R.id.tv_runs);
                    TextView fours = convertView.findViewById(R.id.tv_fours);
                    TextView balls_played = convertView.findViewById(R.id.tv_balls_played);
                    TextView sixes = convertView.findViewById(R.id.tv_sixes);
                    TextView strike_rate = convertView.findViewById(R.id.tv_strike_rate);
                    TextView dismissalinfo= convertView.findViewById(R.id.tv_dismissal_info);

                    if(getItem(position).name.equals(""))
                    {
                        dismissalinfo.setVisibility(View.GONE);
                    }

                    playername_tv.setText(getItem(position).name);
                    runs.setText(getItem(position).runs);
                    fours.setText(getItem(position).fours);
                    sixes.setText((getItem(position)).sixes);
                    strike_rate.setText((getItem(position)).strike_rake);
                    balls_played.setText(getItem(position).overs_played);
                    dismissalinfo.setText(getItem(position).dissmissal_info);



                }
                return convertView;

            }

        }



        private class BowlingListAdaptor extends ArrayAdapter<Player> {
            private static final String TAG = "BowlingListAdaptor";
            private Context mContext;
            private int mResource;

            public BowlingListAdaptor(Context context, int resource, List<Player> objects) {
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
                    TextView overs_bowled = convertView.findViewById(R.id.tv_overs_bowled);
                    TextView maidens = convertView.findViewById(R.id.tv_maidens);
                    TextView wicket_taken = convertView.findViewById(R.id.tv_wickets_taken);
                    TextView runs_conceded = convertView.findViewById(R.id.tv_runs_conceded);
                    TextView economy = convertView.findViewById(R.id.tv_economy);
                    overs_bowled.setText((getItem(position)).overs_bowled);
                    maidens.setText((getItem(position)).maidens);
                    wicket_taken.setText((getItem(position)).wicket_taken);
                    runs_conceded.setText((getItem(position)).runs_conceded);
                    economy.setText((getItem(position)).economy);
                    playername_tv.setText(getItem(position).name);
                    if(getItem(position).name.equals(""))
                    {
                        economy.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f);
                    }


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
                        data = main_object.getJSONObject("data");
                        JSONArray batting_arr_object = data.getJSONArray("batting");
                        JSONArray bowling_arr_object = data.getJSONArray("bowling");
                        JSONArray fielding_arr_object = data.getJSONArray("fielding");
                        batting_list_team1.clear();
                        bowling_list_team1.clear();
                        batting_list_team2.clear();
                        bowling_list_team2.clear();
                        Player batterhead = new Player("","",0.0);
                        batterhead.fillbattingstats("R","4s","6s","S/R","B","");

                        Player bowlerhead = new Player("","",0.0);
                        bowlerhead.fillbowlingstats("O","W","M","Econ","R");

                        batting_list_team1.add(batterhead);
                        batting_list_team2.add(batterhead);

                        bowling_list_team1.add(bowlerhead);
                        bowling_list_team2.add(bowlerhead);



                        for(int i=0 ;i<batting_arr_object.length();i++)
                        {

                            JSONObject batting_object = batting_arr_object.getJSONObject(i);
                            JSONArray batting_scores = batting_object.getJSONArray("scores");
                            String  batting_team = batting_object.getString("title");

                            if(batting_team.contains(team1))
                            {
                                for(int j=0;j<batting_scores.length();j++)
                                {
                                    JSONObject p = batting_scores.getJSONObject(j);


                                    Player player = new Player(String.valueOf(p.get("batsman")),String.valueOf(p.get("pid")),8.0);
                                    boolean alreadythere = false;
                                    for(Player batter : batting_list_team1)
                                    {
                                        if(batter.pid.equals(player.pid))
                                        {
                                            batting_list_team1.remove(batter);
                                            alreadythere=true;
                                            break;
                                        }
                                    }
                                    Log.d("points ",p.toString());
                                    if(alreadythere)
                                    {
                                        player.fillbattingstats(String.valueOf(Double.valueOf(player.runs)+Double.valueOf(String.valueOf(p.get("R")))),String.valueOf(Double.valueOf(player.fours)+Double.valueOf(String.valueOf(p.get("4s")))),String.valueOf(Double.valueOf(player.sixes)+Double.valueOf(String.valueOf(p.get("6s")))),String.valueOf(Double.valueOf(player.strike_rake)+Double.valueOf(String.valueOf(p.get("SR")))),String.valueOf(Double.valueOf(player.overs_played)+Double.valueOf(String.valueOf(p.get("B")))),String.valueOf(p.get("dismissal-info")));

                                    }
                                    else
                                    {
                                        player.fillbattingstats(String.valueOf(p.get("R")),String.valueOf(p.get("4s")),String.valueOf(p.get("6s")),String.valueOf(p.get("SR")),String.valueOf(p.get("B")),String.valueOf(p.get("dismissal-info")));

                                    }

                                    batting_list_team1.add(player);
                                }
                            }
                            else if(batting_team.contains(team2))
                            {
                                for(int j=0;j<batting_scores.length();j++)
                                {
                                    JSONObject p = batting_scores.getJSONObject(j);


                                    Player player = new Player(String.valueOf(p.get("batsman")),String.valueOf(p.get("pid")),8.0);
                                    boolean alreadythere = false;
                                    for(Player batter : batting_list_team2)
                                    {
                                        if(batter.pid.equals(player.pid))
                                        {
                                            batting_list_team2.remove(batter);
                                            alreadythere=true;
                                            break;
                                        }
                                    }
                                    Log.d("points ",p.toString());
                                    if(alreadythere)
                                    {
                                        player.fillbattingstats(String.valueOf(Double.valueOf(player.runs)+Double.valueOf(String.valueOf(p.get("R")))),String.valueOf(Double.valueOf(player.fours)+Double.valueOf(String.valueOf(p.get("4s")))),String.valueOf(Double.valueOf(player.sixes)+Double.valueOf(String.valueOf(p.get("6s")))),String.valueOf(Double.valueOf(player.strike_rake)+Double.valueOf(String.valueOf(p.get("SR")))),String.valueOf(Double.valueOf(player.overs_played)+Double.valueOf(String.valueOf(p.get("B")))),String.valueOf(p.get("dismissal-info")));

                                    }
                                    else
                                    {
                                        player.fillbattingstats(String.valueOf(p.get("R")),String.valueOf(p.get("4s")),String.valueOf(p.get("6s")),String.valueOf(p.get("SR")),String.valueOf(p.get("B")),String.valueOf(p.get("dismissal-info")));

                                    }
                                    batting_list_team2.add(player);


                                }
                            }

                        }


                        for(int i=0 ;i<bowling_arr_object.length();i++)
                        {

                            JSONObject bowling_object = bowling_arr_object.getJSONObject(i);
                            JSONArray bowling_scores = bowling_object.getJSONArray("scores");
                            String  bowling_team = bowling_object.getString("title");

                            if(bowling_team.contains(team1))
                            {
                                for(int j=0;j<bowling_scores.length();j++)
                                {
                                    JSONObject p = bowling_scores.getJSONObject(j);


                                    Player player = new Player(String.valueOf(p.get("bowler")),String.valueOf(p.get("pid")),8.0);
                                    boolean alreadythere = false;
                                    for(Player bowler : bowling_list_team1)
                                    {
                                        if(bowler.pid.equals(player.pid))
                                        {
                                            bowling_list_team1.remove(bowler);
                                            alreadythere=true;
                                            break;
                                        }
                                    }
                                    Log.d("points ",p.toString());
                                    if(alreadythere)
                                    {
                                        player.fillbowlingstats(String.valueOf(Double.valueOf(player.overs_bowled)+Double.valueOf(String.valueOf(p.get("O")))),String.valueOf(Double.valueOf(player.wicket_taken)+Double.valueOf(String.valueOf(p.get("W")))),String.valueOf(Double.valueOf(player.maidens)+Double.valueOf(String.valueOf(p.get("M")))),String.valueOf(p.get("Econ")),String.valueOf(Double.valueOf(player.runs_conceded)+Double.valueOf(String.valueOf(p.get("R")))));

                                    }
                                    else
                                    {
                                        player.fillbowlingstats(String.valueOf(p.get("O")),String.valueOf(p.get("W")),String.valueOf(p.get("M")),String.valueOf(p.get("Econ")),String.valueOf(p.get("R")));

                                    }

                                    bowling_list_team1.add(player);
                                }
                            }
                            else if(bowling_team.contains(team2))
                            {
                                for(int j=0;j<bowling_scores.length();j++)
                                {
                                    JSONObject p = bowling_scores.getJSONObject(j);


                                    Player player = new Player(String.valueOf(p.get("bowler")),String.valueOf(p.get("pid")),8.0);
                                    boolean alreadythere = false;
                                    for(Player bowler : bowling_list_team2)
                                    {
                                        if(bowler.pid.equals(player.pid))
                                        {
                                            bowling_list_team2.remove(bowler);
                                            alreadythere=true;
                                            break;
                                        }
                                    }
                                    Log.d("points ",p.toString());
                                    if(alreadythere)
                                    {
                                        player.fillbowlingstats(String.valueOf(Double.valueOf(player.overs_bowled)+Double.valueOf(String.valueOf(p.get("O")))),String.valueOf(Double.valueOf(player.wicket_taken)+Double.valueOf(String.valueOf(p.get("W")))),String.valueOf(Double.valueOf(player.maidens)+Double.valueOf(String.valueOf(p.get("M")))),String.valueOf(p.get("Econ")),String.valueOf(Double.valueOf(player.runs_conceded)+Double.valueOf(String.valueOf(p.get("R")))));
                                    }
                                    else
                                    {
                                        player.fillbowlingstats(String.valueOf(p.get("O")),String.valueOf(p.get("W")),String.valueOf(p.get("M")),String.valueOf(p.get("Econ")),String.valueOf(p.get("R")));

                                    }
                                    bowling_list_team2.add(player);
                                }
                            }

                        }




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

                bowlingListAdaptor_team1.notifyDataSetChanged();
                battingListAdaptor_team1.notifyDataSetChanged();
                bowlingListAdaptor_team2.notifyDataSetChanged();
                battingListAdaptor_team2.notifyDataSetChanged();



            }

        }






    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position==0)
            {
                return  ContestsFragment.newInstance(position);
            }
            if(position==1)
            {
                return  LiveUpdatesFragment.newInstance(position);
            }
            return ContestsFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            switch (position) {
                case 0:
                    return "Contests";
                case 1:
                    return "Live Updates";
                default:
                    return null;
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




    static class getmatchscoretask extends AsyncTask<String, Boolean, Boolean> {
        String team1_score="",team2_score="";

        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)

                        try {
                            JSONObject scoreobject = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlScore(matchid));
                            String score = scoreobject.getString("score");
                            score = score.trim();
                            score = score.replaceAll(team1,"").replaceAll(team2,"").trim();
                            team1_score = score.split("v")[0].trim();
                            team2_score = score.split("v")[1].trim();
                            Log.d("debug",score);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }


            return true;
        }

        //        @Override
//        public void progressUpdate(Integer progress) {
//            //Update progress bar (ui thread)
//
//
//        }
        @Override
        protected void onPostExecute(Boolean result) {
            //Call your next task (ui thread)

            team1scoretv.setText(team1_score);
            team2scoretv.setText(team2_score);



            if(mSwipeRefreshLayout!=null&&mSwipeRefreshLayout.isRefreshing())
            {
                mSwipeRefreshLayout.setRefreshing(false);
            }


        }


    }






}
