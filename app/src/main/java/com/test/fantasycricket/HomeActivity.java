package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
import java.util.Map;
import java.util.TimeZone;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ListView matchlist;
    ArrayList<Match> matches;
    JSONObject matchlistobject;
    MatchListAdaptor matchListAdaptor;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseApp.initializeApp(getApplicationContext());


        //AUTO LOG-IN FUNCTION.
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
                                    Toast.makeText(getApplicationContext(), "Welcome "+ user.get("Name"), Toast.LENGTH_LONG).show();
                                    UserInfo.login(user.get("UserType").toString(),user.get("Username").toString(),user.get("Name").toString(),user.get("Email").toString(),Double.parseDouble(user.get("Cash").toString()),Integer.parseInt(user.get("Winnings").toString()),Integer.parseInt(user.get("xp").toString()));

                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Auto-login failed. Login manually", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "There was some error in auto-login. Login manually", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

            }
        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        matchlist = findViewById(R.id.lv_matchlist);

//        Date temp = fromISO8601UTC("2019-06-19T00:00:00.000Z");
//        Log.d("DATE --- ",temp.toString());

        new getmatchestask().execute();


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
                    Toast.makeText(HomeActivity.this,"You need to login first!",Toast.LENGTH_LONG).show();
                }
            }
        });




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
            }
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            this.finish();

        } else if (id == R.id.nav_mycontests) {

            if(UserInfo.logined)
            {

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
                Intent intent = new Intent(this,LoginActivity.class);
                startActivity(intent);

            }

        } else if (id == R.id.nav_register) {
            if(UserInfo.logined)
            {
                Toast.makeText(HomeActivity.this,"You are logged In, logout to register a new account",Toast.LENGTH_LONG).show();
            }
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);
            this.finish();


        } else if (id == R.id.nav_share) {

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
                String team1,team2,date,matchtype,uniqueid;
                Boolean matchstarted;

                for(int i = 0 ;i<matchesjsonArray.length();i++)
                {
                    JSONObject match = matchesjsonArray.getJSONObject(i);
                    matchtype = match.getString("type");

                    if(!match.getString("type").equals("ODI"))
                    {
                        continue;
                    }
                    team1 = match.getString("team-1");
                    team2 = match.getString("team-2");
                    date = match.getString("dateTimeGMT");
                    uniqueid = match.getString("unique_id");
                    matchstarted = match.getBoolean("matchStarted");
                    Date d = fromISO8601UTC(date);
                    long mills = d.getTime() - Calendar.getInstance().getTime().getTime();
                    long hours = mills/(1000 * 60 * 60);
                    long mins = (mills/(1000*60)) % 60;
                    String timeremaining;
                    if(hours>48)
                    {
                        timeremaining = hours/24 +" days\nremaining";
                    }
                    else if(mills<0)
                    {
                        timeremaining = "Match\nStarted";
                    }
                    else if(hours==0)
                    {
                        timeremaining = mins +" mins.\nremaining";
                    }
                    else
                    {
                        timeremaining = hours + " hrs.\nremaining";
                    }
                    Match newmatch = new Match(uniqueid,team1,team2,date,matchtype,matchstarted);
                    newmatch.timeleft=timeremaining;
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



            matchListAdaptor = new MatchListAdaptor(getApplicationContext(),R.layout.matchlist_element_layout,matches);



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
            matchlist.setAdapter(matchListAdaptor);

        }


    }



}
