package com.test.fantasycricket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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

    //---

    //---

    FirebaseFirestore db;
    ListView team1list ;
    ListView team2list ;

    PlayerListAdaptor team1listadaptor,team2listadaptor;
    Double price=0d;

    TextView tv_credits;
    String team1="okk",team2="achha";
    Integer count_team1=0,count_team2=0;
    Double credits = 100.0;
    TextView totalplayerselectedtv;
    Integer player_selected_num=0;
    static String matchid;
    static String contestid;

    public static Activity fa;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fa= this;

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);


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

//        TextView team1listtv = findViewById(R.id.tv_team1list);
//        TextView team2listtv = findViewById(R.id.tv_team2list);
//        team1listtv.setText(team1);
//        team2listtv.setText(team2);
//
//        team1list = findViewById(R.id.lv_team1list);
//        team2list = findViewById(R.id.lv_team2list);

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





    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public static ListView team1_lv;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_choose_team_tab, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            team1_lv = rootView.findViewById(R.id.lv_teamlist);


            return rootView;
        }
    }


    public static class PlaceholderFragment2 extends Fragment {
        public static ListView team2_lv;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment2() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment2 newInstance(int sectionNumber) {
            PlaceholderFragment2 fragment = new PlaceholderFragment2();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_choose_team_tab, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            team2_lv = rootView.findViewById(R.id.lv_teamlist);



            return rootView;
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
                return  PlaceholderFragment.newInstance(position);
            }
            if(position==1)
            {
                return  PlaceholderFragment2.newInstance(position);
            }
            return PlaceholderFragment.newInstance(position + 1);
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
                    return getIntent().getStringExtra("team1");
                case 1:
                    return getIntent().getStringExtra("team2");

                default:
                    return null;
            }
        }
    }




    class getteamtask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)
            final ArrayList<Player> team1_arrlist = new ArrayList<>();
            final ArrayList<Player> team2_arrlist = new ArrayList<>();

            try {

                JSONObject squadobject = HomeActivity.getJSONObjectFromURL(Constants.getApiUrlSquad(matchid));
                JSONArray jsonsquad =squadobject.getJSONArray("squad");
                JSONObject team1object =jsonsquad.getJSONObject(0);
                JSONObject team2object =jsonsquad.getJSONObject(1);
                JSONArray team1_json = team1object.getJSONArray("players");
                JSONArray team2_json = team2object.getJSONArray("players");

                for (int i=0;i<team1_json.length();i++)
                {
                    Double p_credits=8.0;
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
                    Double p_credits=8.0;
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

                db.collection("Team").document(team1).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String,Object> data = documentSnapshot.getData();
                        String captain,vicecaptain;
                        ArrayList<String> good_players,bad_players;
                        try {
                            good_players = (ArrayList<String>) data.get("good_players");
                            if(good_players==null)
                            {
                                good_players = new ArrayList<>();
                            }

                        }
                        catch (Exception e)
                        {
                            good_players = new ArrayList<>();
                        }

                        try {
                            bad_players = (ArrayList<String>) data.get("bad_players");
                            if(bad_players==null)
                            {
                                bad_players = new ArrayList<>();
                            }

                        }
                        catch (Exception e)
                        {
                            bad_players = new ArrayList<>();
                        }


                        try{
                            captain =(String) data.get("captain");
                        }
                        catch (Exception e )
                        {
                            e.printStackTrace();
                            captain ="";
                        }
                        try{
                            vicecaptain = (String) data.get("vice_captain");                    }
                        catch (Exception e )
                        {
                            e.printStackTrace();
                            vicecaptain ="";
                        }
                        String manofthematch;
                        try{
                            manofthematch =data.get("man_of_the_match").toString();
                        }catch (Exception e)
                        {
                            manofthematch ="";
                        }

                        for(Player player : team1_arrlist)
                        {
                            if(good_players.contains(player.name))
                            {
                                player.credits = 8.5;
                            }

                            if(bad_players.contains(player.name))
                            {
                                player.credits = 7.5;
                            }

                            if(player.name.equals(vicecaptain))
                            {
                                player.credits = 10.0;
                            }

                            if(player.name.equals(manofthematch))
                            {
                                player.credits = 11.0;
                            }

                            if(player.name.equals(captain))
                            {
                                player.credits = 14.0;
                            }
                        }
                        team1listadaptor = new PlayerListAdaptor(CreateTeamActivity.this,R.layout.player_element,team1_arrlist);
                        PlaceholderFragment.team1_lv.setAdapter(team1listadaptor);


                    }
                });


            db.collection("Team").document(team2).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Map<String,Object> data = documentSnapshot.getData();
                    ArrayList<String> good_players,bad_players;
                    try {
                        good_players = (ArrayList<String>) data.get("good_players");

                        Log.d("creditcal","good player "+good_players.size());
                        if(good_players==null)
                        {
                            good_players = new ArrayList<>();
                        }
                    }
                    catch (Exception e)
                    {
                        good_players = new ArrayList<>();
                    }

                    try {
                        bad_players = (ArrayList<String>) data.get("bad_players");
                        if(bad_players==null)
                        {
                            bad_players = new ArrayList<>();
                        }

                    }
                    catch (Exception e)
                    {
                        bad_players = new ArrayList<>();
                    }



                    String captain,vicecaptain;
                    try{
                       captain =(String) data.get("captain");
                    }
                    catch (Exception e )
                    {
                        e.printStackTrace();
                        captain ="";
                    }
                    try{
                        vicecaptain = (String) data.get("vice_captain");                    }
                    catch (Exception e )
                    {
                        e.printStackTrace();
                        vicecaptain ="";
                    }

                    String manofthematch;
                    try{
                        manofthematch = data.get("man_of_the_match").toString();
                    }catch (Exception e)
                    {
                        manofthematch ="";
                    }

                    for(Player player : team2_arrlist)
                    {

                        if(good_players.contains(player.name))
                        {
                            player.credits = 8.5;
                        }

                        if(bad_players.contains(player.name))
                        {
                            player.credits = 7.5;
                        }

                        if(player.name.equals(vicecaptain))
                        {
                            player.credits = 10.0;
                        }
                        if(player.name.equals(manofthematch))
                        {
                            player.credits = 11.0;
                        }


                        if(player.name.equals(captain))
                        {
                            player.credits = 14.0;
                        }

                    }
                    team2listadaptor = new PlayerListAdaptor(CreateTeamActivity.this,R.layout.player_element,team2_arrlist);
                    PlaceholderFragment2.team2_lv.setAdapter(team2listadaptor);


                }
            });



            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            //Call your next task (ui thread)
//            team1list.setAdapter(team1listadaptor);
//            team2list.setAdapter(team2listadaptor);

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
                //pointstv.setText(String.valueOf(getItem(position).points));
                pointstv.setText("");
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
                                if(credits<getItem(position).credits)
                                {
                                    Toast.makeText(mContext,"Not enough credits, try selecting a different combination.",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(getItem(position).team.equals(team1))
                                {
                                    if(count_team1==8)
                                    {
                                        Toast.makeText(mContext,"Max. 8 players from single team!",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    else
                                    {
                                        count_team1++;
                                    }
                                }
                                else
                                {

                                    if(count_team2==8)
                                    {
                                        Toast.makeText(mContext,"Max. 8 players from single team!",Toast.LENGTH_LONG).show();
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
