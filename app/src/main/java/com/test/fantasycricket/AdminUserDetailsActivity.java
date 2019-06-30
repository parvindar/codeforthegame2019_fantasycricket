package com.test.fantasycricket;

import android.graphics.Color;
import android.provider.Contacts;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AdminUserDetailsActivity extends AppCompatActivity {

    String username;
    FirebaseFirestore db;
    boolean namechange=false,emailchange=false,winningschange=false,xpchange=false,cashchange=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        username = getIntent().getStringExtra("username");

        TextView title_tv = findViewById(R.id.tv_adminuserlist_title);
        TextView username_tv = findViewById(R.id.tv_username);
        title_tv.setText(username);
        username_tv.setText(username);

        final EditText name_et = findViewById(R.id.et_name);
        final EditText email_et = findViewById(R.id.et_email);
        final EditText cash_et = findViewById(R.id.et_cash);
        final EditText winnings_et = findViewById(R.id.et_winnings);
        final EditText xp_et = findViewById(R.id.et_xp);

        Button update_btn = findViewById(R.id.btn_update);
        Button delete_btn = findViewById(R.id.btn_delete_account);

        db.collection("Users").document(username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String,Object> data = documentSnapshot.getData();
                String name,email;
                Double cash,winnings,xp;

                name= (String) data.get("Name");
                email =(String) data.get("Email");
                try{
                    cash = (Double) data.get("Cash");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    cash = Double.parseDouble(String.valueOf(data.get("Cash")));
                }
                try {
                    xp = (Double)data.get("xp");
                }
                catch (Exception e)
                {
                    xp = Double.parseDouble(String.valueOf(data.get("xp")));
                    e.printStackTrace();
                }
                try {
                    winnings=(Double)data.get("Winnings");
                }
                catch (Exception e)
                {
                    winnings = Double.parseDouble(String.valueOf(data.get("Winnings")));
                    e.printStackTrace();
                }

                name_et.setText(name);
                cash_et.setText(String.valueOf(cash));
                winnings_et.setText(String.valueOf(winnings));
                email_et.setText(email);
                xp_et.setText(String.valueOf(xp));

            }
        });

        name_et.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                name_et.setInputType(InputType.TYPE_CLASS_TEXT);
                Log.d("longclick","okok name long click");
                namechange=true;
                return true;
            }
        });

        email_et.setInputType(InputType.TYPE_NULL);
        name_et.setInputType(InputType.TYPE_NULL);
        winnings_et.setInputType(InputType.TYPE_NULL);
        cash_et.setInputType(InputType.TYPE_NULL);
        xp_et.setInputType(InputType.TYPE_NULL);


        email_et.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                email_et.setInputType(InputType.TYPE_CLASS_TEXT);
                Log.d("longclick","okok name long click");
                emailchange=true;
                return false;
            }
        });

        cash_et.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cash_et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                cashchange=true;
                return false;
            }
        });

        winnings_et.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                winnings_et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                winningschange=true;
                return false;
            }
        });

        xp_et.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                xp_et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                xpchange=true;
                return false;
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AdminUserDetailsActivity.this);
                LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                title_tv.setText("Change account details");
                detail_tv.setText("You are changing the account details of "+username+".");

                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                Button nobtn = dialogView.findViewById(R.id.btn_no);

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        db.collection("Users").document(username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Map<String,Object> data = documentSnapshot.getData();
                                if(namechange && !name_et.getText().toString().isEmpty())
                                {
                                    data.put("Name",name_et.getText().toString());
                                    data.put("Name_insensitive",name_et.getText().toString().toLowerCase());
                                }

                                if(emailchange && !email_et.getText().toString().isEmpty())
                                {
                                    data.put("Email",email_et.getText().toString());
                                }

                                if(winningschange && !winnings_et.getText().toString().isEmpty())
                                {
                                    data.put("Winnings",Double.valueOf(winnings_et.getText().toString()));
                                }
                                if(xpchange && !xp_et.getText().toString().isEmpty())
                                {
                                    data.put("xp",Double.valueOf(xp_et.getText().toString()));
                                }

                                if(cashchange && !cash_et.getText().toString().isEmpty())
                                {
                                    data.put("Cash",Double.valueOf(cash_et.getText().toString()));
                                }
                                db.collection("Users").document(username).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AdminUserDetailsActivity.this,"Users details updated successfully",Toast.LENGTH_LONG).show();
                                        b.dismiss();
                                    }
                                });

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


        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AdminUserDetailsActivity.this);
                LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                title_tv.setText("Delete Account");
                detail_tv.setText("You are deleting the account +"+username+", it can't be recovered later.");

                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                Button nobtn = dialogView.findViewById(R.id.btn_no);

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        db.collection("Users").document(username).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AdminUserDetailsActivity.this,"Account deleted successfully",Toast.LENGTH_LONG).show();
                                b.dismiss();
                                AdminUserDetailsActivity.this.finish();
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


    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
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
