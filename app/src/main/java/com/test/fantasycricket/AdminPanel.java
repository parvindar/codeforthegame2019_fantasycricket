package com.test.fantasycricket;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminPanel extends AppCompatActivity {

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        db = FirebaseFirestore.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button manage_user_btn = findViewById(R.id.btn_admin_manageusers);
        Button change_api_key = findViewById(R.id.btn_admin_changeapikey);

        manage_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanel.this,AdminUserListActivity.class);
                startActivity(intent);
            }
        });

        change_api_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AdminPanel.this);
                LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.admin_api_key_change, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                final EditText apikey_et = dialogView.findViewById(R.id.et_api_key);


                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                Button nobtn = dialogView.findViewById(R.id.btn_no);

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String apikey = apikey_et.getText().toString();
                        if(apikey.isEmpty())
                        {
                            Toast.makeText(AdminPanel.this,"Enter the new api key to update",Toast.LENGTH_LONG).show();
                            b.dismiss();
                            return;
                        }

                        db.collection("Projectinfo").document("cricket-api").update("API_KEY_APP",apikey).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AdminPanel.this,"API-KEY updated successfully",Toast.LENGTH_LONG).show();
                                b.dismiss();
                            }
                        });



                    }
                });

                nobtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        b.dismiss();
                    }
                });

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
