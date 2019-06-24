package com.test.fantasycricket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.test.fantasycricket.Constants.dec;

public class MyAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_my_account);
        TextView name = findViewById(R.id.tv_name);
        TextView username = findViewById(R.id.tv_username);
        TextView email = findViewById(R.id.tv_email);
        TextView winnings = findViewById(R.id.tv_winnings);
        TextView xp = findViewById(R.id.tv_xp);
        TextView cashtv = findViewById(R.id.tv_cash);
        name.setText(UserInfo.name);
        username.setText(UserInfo.username);
        email.setText(UserInfo.email);
        winnings.setText(String.valueOf(UserInfo.winnings));
        xp.setText(String.valueOf(UserInfo.xp));
        cashtv.setText(UserInfo.INR+dec.format(UserInfo.cash));

        Button addcashbtn =findViewById(R.id.btn_addcash);
        addcashbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
