package com.test.fantasycricket;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.test.fantasycricket.Constants.dec;

public class MyAccountActivity extends AppCompatActivity {

    SwipeRefreshLayout mSwipeRefreshLayout;
    FirebaseFirestore db;
    static TextView cashtv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        if (ContextCompat.checkSelfPermission(MyAccountActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MyAccountActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        final TextView name = findViewById(R.id.tv_name);
        final TextView username = findViewById(R.id.tv_username);
        final TextView email = findViewById(R.id.tv_email);
        final TextView winnings = findViewById(R.id.tv_winnings);
        final TextView xp = findViewById(R.id.tv_xp);
        cashtv = findViewById(R.id.tv_cash);
        name.setText(UserInfo.name);
        username.setText(UserInfo.username);
        email.setText(UserInfo.email);
        winnings.setText(String.valueOf(UserInfo.winnings));
        xp.setText(String.valueOf(UserInfo.xp));
        cashtv.setText(Constants.INR+dec.format(UserInfo.cash));


        Button admin_panel_btn = findViewById(R.id.btn_admin_panel);

        if(!UserInfo.usertype.equals("admin"))
        {
            admin_panel_btn.setVisibility(View.GONE);
        }
        Button addcashbtn =findViewById(R.id.btn_addcash);
        addcashbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserInfo.addCash(MyAccountActivity.this);


            }
        });

        admin_panel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyAccountActivity.this,AdminPanel.class);
                startActivity(intent);
            }
        });


//  swipe to refresh ===========================================================
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                db.collection("Users").document(UserInfo.username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> user = new HashMap<>();
                                user = document.getData();

                                UserInfo.login(user.get("UserType").toString(),user.get("Username").toString(),user.get("Name").toString(),user.get("Email").toString(),Double.parseDouble(user.get("Cash").toString()),Double.parseDouble(user.get("Winnings").toString()),Double.parseDouble(user.get("xp").toString()));

                                name.setText(UserInfo.name);
                                username.setText(UserInfo.username);
                                email.setText(UserInfo.email);
                                winnings.setText(String.valueOf(UserInfo.winnings));
                                xp.setText(String.valueOf(UserInfo.xp));
                                cashtv.setText(UserInfo.INR+dec.format(UserInfo.cash));

                                mSwipeRefreshLayout.setRefreshing(false);


                            }
                             else {
                                Toast.makeText(MyAccountActivity.this, "Username doesn't exist !", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });



            }
        });

        //=========================================================================


    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_edit_profile:
                Intent intent = new Intent(MyAccountActivity.this,EditProfileActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

}
