package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ListView matchlist;
    ArrayList<Match> matches;
    JSONObject matchlistobject;
    MatchListAdaptor matchListAdaptor;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SwipeRefreshLayout mSwipeRefreshLayout;
    Spinner spinner;
    String selectedtype="World Cup";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseApp.initializeApp(HomeActivity.this);

        String[] matchtypes={"World Cup","ODI","T20"};
        spinner = findViewById(R.id.spinner_matchtype);
        ArrayAdapter arrayAdapter = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_dropdown_item,matchtypes);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, View view, final int position, long id) {


                selectedtype = parent.getItemAtPosition(position).toString();

                if(!selectedtype.equals("World Cup")){

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                    dialogBuilder.setView(dialogView);
                    final AlertDialog b = dialogBuilder.create();
                    b.show();

                    TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                    TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                    title_tv.setText("This app is currently designed for ICC World Cup");
                    detail_tv.setText("This app is currently designed for ICC World Cup, you might not see the contests in other matches.");

                    Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                    Button nobtn = dialogView.findViewById(R.id.btn_no);

                    yesbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            new getmatchestask().execute();

                            b.dismiss();
                        }
                    });

                    nobtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedtype="World Cup";
                            spinner.setSelection(0);
                            b.dismiss();
                        }
                    });


                }
                else {
                    new getmatchestask().execute();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //AUTO LOG-IN FUNCTION. ==============================================================
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("app",Context.MODE_PRIVATE);
        Boolean islogined =sharedPref.getBoolean("logined",false);
        if(islogined)
        {
            final String username = sharedPref.getString("username",null);
            final String password = sharedPref.getString("password",null);
            if(username!=null && password!=null)
            {
                db.collection("Users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> user = new HashMap<>();
                                user = document.getData();
                                if(user.get("Password").equals(password)){
                                    Toast.makeText(HomeActivity.this, "Welcome "+ user.get("Name"), Toast.LENGTH_LONG).show();
                                    UserInfo.login(user.get("UserType").toString(),user.get("Username").toString(),user.get("Name").toString(),user.get("Email").toString(),Double.parseDouble(user.get("Cash").toString()),Double.parseDouble(user.get("Winnings").toString()),Double.parseDouble(user.get("xp").toString()));

                                }
                                else{
                                    Toast.makeText(HomeActivity.this, "Auto-login failed. Login manually", Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                    }
                });

            }
        }

        //=====================================================================================

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("CrickSkill");

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });



        // Setting Name and email in nav bar ==========================
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {

                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(HomeActivity.this);

                TextView nav_name = navigationView.findViewById(R.id.tv_name);
                TextView nav_email = navigationView.findViewById(R.id.tv_email);

                if(UserInfo.logined)
                {
                    nav_name.setText(UserInfo.name);
                    nav_email.setText(UserInfo.email);
                }
                else
                {
                    nav_name.setText("Anonymous");
                    nav_email.setText("");
                }

                super.onDrawerOpened(drawerView);
            }
        };


        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //==============================================================================

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


// list shows upcomig and live matches
        matchlist = findViewById(R.id.lv_matchlist);

    //    new getmatchestask().execute();

        // will use this code lated;
        db.collection("Projectinfo").document("cricket-api").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Constants.API_KEY = documentSnapshot.get("API_KEY_APP").toString();
                Constants.updateApiUrls();
                new getmatchestask().execute();

            }
        });


        //my account , shows current wallet balance ===========================================
        Button myaccountbtn = findViewById(R.id.btn_account);
        myaccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UserInfo.logined)
                {
                    Intent intent = new Intent(HomeActivity.this,MyAccountActivity.class);
                    startActivity(intent);
                }
                else
                {
                    UserInfo.instantLogin(HomeActivity.this);
                    Toast.makeText(HomeActivity.this,"You need to login first!",Toast.LENGTH_LONG).show();
                }
            }
        });
        //==========================================================


        // my contest ==========================
        Button mycontestsbtn = findViewById(R.id.btn_mymatches);
        mycontestsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UserInfo.logined)
                {
                    Intent intent = new Intent(HomeActivity.this,MyMatchesActivity.class);
                    startActivity(intent);
                }
                else
                {
                    UserInfo.instantLogin(HomeActivity.this);
                    Toast.makeText(HomeActivity.this,"You need to login first!",Toast.LENGTH_LONG).show();
                }
            }
        });

        //===============================================================

        Button notificationbtn = findViewById(R.id.btn_notifications);
        notificationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
            }
        });



//  swipe to refresh ===========================================================
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new getmatchestask().execute();
            }
        });

        //=========================================================================





    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            if(UserInfo.logined)
            {
                Toast.makeText(HomeActivity.this,"You are already Logged In",Toast.LENGTH_LONG).show();
                return false ;
            }
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            this.finish();

        } else if (id == R.id.nav_mycontests) {

            if(UserInfo.logined)
            {

                Intent intent = new Intent(HomeActivity.this,MyMatchesActivity.class);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(HomeActivity.this,"You need to login first!",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this,LoginActivity.class);
                startActivity(intent);

            }

        } else if (id == R.id.nav_account) {

            if(UserInfo.logined)
            {
                Intent intent = new Intent(HomeActivity.this,MyAccountActivity.class);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(HomeActivity.this,"You need to login first!",Toast.LENGTH_LONG).show();

            }

        } else if (id == R.id.nav_register) {
            if(UserInfo.logined)
            {
                Toast.makeText(HomeActivity.this,"You are logged In, logout to register a new account",Toast.LENGTH_LONG).show();
                return false;
            }
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_faq) {

            Intent intent = new Intent(HomeActivity.this,FAQActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {

            if(UserInfo.logined)
            {
                UserInfo.logout();
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("app",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("logined",false);
                editor.putString("password",null);
                editor.commit();

                Toast.makeText(HomeActivity.this,"Successfully logged out",Toast.LENGTH_LONG).show();

            }
            else {
                Toast.makeText(HomeActivity.this,"You are already Logged Out",Toast.LENGTH_LONG).show();
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }










    // Function to request json object from web.
    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }

    public static Date fromISO8601UTC(String dateStr) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);

        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }






    class getmatchestask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //Do Stuff that takes ages (background thread)
            matches = new ArrayList<>();

            try {

                matchlistobject = getJSONObjectFromURL(Constants.API_URL_NEWMATCHES);
                JSONArray matchesjsonArray =matchlistobject.getJSONArray("matches");
                String team1,team2,date,matchtype,uniqueid,winner_team,toss_winner_team;
                Boolean matchstarted;

                for(int i = 0 ;i<matchesjsonArray.length();i++)
                {
                    JSONObject match = matchesjsonArray.getJSONObject(i);
                    matchtype = match.getString("type");


                    team1 = match.getString("team-1");
                    team2 = match.getString("team-2");
                    if(selectedtype.equals("World Cup"))
                    {
                        if(!match.getString("type").equals("ODI") || !Constants.getWorldcupteams().contains(team1.toLowerCase()) || !Constants.getWorldcupteams().contains(team2.toLowerCase()) )
                        {
                            if(!team1.equals("TBA"))
                                continue;
                        }

                        matchtype = "ICC World Cup";
                    }
                    else if(selectedtype.equals("ODI"))
                    {
                        if(!match.getString("type").equals("ODI")  )
                        {
                            continue;
                        }
                        if(Constants.getWorldcupteams().contains(team1.toLowerCase()) && Constants.getWorldcupteams().contains(team2.toLowerCase()) || team1.equals("TBA"))
                        {
                            matchtype = "ICC World Cup";
                        }
                    }
                    else if(selectedtype.equals("T20"))
                    {
                        if(!match.getString("type").equals("Twenty20")  )
                        {
                            continue;
                        }
                    }


                    date = match.getString("dateTimeGMT");
                    uniqueid = match.getString("unique_id");
                    matchstarted = match.getBoolean("matchStarted");
                    try
                    {
                        winner_team = match.getString("winner_team");
                    }
                    catch (Exception e)
                    {
                        winner_team ="";
                    }
                    try
                    {
                        toss_winner_team = match.getString("toss_winner_team");
                    }
                    catch (Exception e)
                    {
                        toss_winner_team = "";
                    }
                    Date d = fromISO8601UTC(date);
                    long mills = d.getTime() - Calendar.getInstance().getTime().getTime();
                    long hours = mills/(1000 * 60 * 60);
                    long mins = (mills/(1000*60)) % 60;

                    if(selectedtype.equals("World Cup"))
                    {
                        if((team1.equals("TBA")&& team2.equals("TBA")) && (d.getDate()!= 14 || d.getHours()!=15))
                        {
                            continue;
                        }
                    }

                    if(selectedtype.equals("ODI"))
                    {
                        if((team1.equals("TBA")&& team2.equals("TBA")) && (d.getDate()!= 14 || d.getHours()!=15))
                        {
                            matchtype="ODI";
                        }
                    }

                        String timeremaining;
                    if(hours>48)
                    {
                        timeremaining = hours/24 +" days to go";
                    }
                    else if(mills<0)
                    {
                        timeremaining = "Match Started";
                    }
                    else if(hours==0)
                    {
                        timeremaining = mins +" mins. remaining";
                    }
                    else
                    {
                        timeremaining = hours + " hrs. " + mins+" mins. remaining";
                    }

                    String team1_score="",team2_score="";
                    if(!toss_winner_team.isEmpty())
                    {
                        try {
                            JSONObject scoreobject = getJSONObjectFromURL(Constants.getApiUrlScore(uniqueid));
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

                    }

                    Match newmatch = new Match(uniqueid,team1,team2,date,d,matchtype,matchstarted);
                    newmatch.timeleft=timeremaining;
                    newmatch.winner_team = winner_team;
                    newmatch.toss_winner = toss_winner_team;
                    newmatch.team1_score=team1_score;
                    newmatch.team2_score = team2_score;


                    if(hours>72)
                    {
                        newmatch.open=false;
                    }
                    matches.add(newmatch);

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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
            matchListAdaptor = new MatchListAdaptor(HomeActivity.this,R.layout.matchlist_element_layout,matches);
            matchlist.setAdapter(matchListAdaptor);

            if(mSwipeRefreshLayout.isRefreshing())
            {
                mSwipeRefreshLayout.setRefreshing(false);
            }

        }


    }






    private class Product {
        String name;
        long expirationTime;

        public Product(String name, long expirationTime) {
            this.name = name;
            this.expirationTime = expirationTime;
        }
    }


    public class CountdownAdapter extends ArrayAdapter<Product> {

        private LayoutInflater lf;
        private List<ViewHolder> lstHolders;
        private Handler mHandler = new Handler();
        private Runnable updateRemainingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (lstHolders) {
                    long currentTime = System.currentTimeMillis();
                    for (ViewHolder holder : lstHolders) {
                        holder.updateTimeRemaining(currentTime);
                    }
                }
            }
        };

        public CountdownAdapter(Context context, List<Product> objects) {
            super(context, 0, objects);
            lf = LayoutInflater.from(context);
            lstHolders = new ArrayList<>();
            startUpdateTimer();
        }

        private void startUpdateTimer() {
            Timer tmr = new Timer();
            tmr.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(updateRemainingTimeRunnable);
                }
            }, 1000, 1000);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = lf.inflate(R.layout.matchlist_element_layout, parent, false);
/*
                holder.tvProduct = (TextView) convertView.findViewById(R.id.tvProduct);
                holder.tvTimeRemaining = (TextView) convertView.findViewById(R.id.tvTimeRemaining);
*/
                convertView.setTag(holder);

                synchronized (lstHolders) {
                    lstHolders.add(holder);
                }
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.setData(getItem(position));

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvProduct;
        TextView tvTimeRemaining;
        Product mProduct;

        public void setData(Product item) {
            mProduct = item;
            tvProduct.setText(item.name);
            updateTimeRemaining(System.currentTimeMillis());
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff = mProduct.expirationTime - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);
                tvTimeRemaining.setText(hours + " hrs " + minutes + " mins " + seconds + " sec");
            } else {
                tvTimeRemaining.setText("Expired!!");
            }
        }
    }










}
