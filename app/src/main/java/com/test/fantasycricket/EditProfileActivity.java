package com.test.fantasycricket;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    String username, name;
    boolean username_status=false;

    FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        final EditText name_et = findViewById(R.id.et_editprofile_name);
        TextView usernameText =findViewById(R.id.tv_username);
        Button submitbtn = findViewById(R.id.btn_update_name);
        Button edit_email_btn = findViewById(R.id.btn_update_email);
        Button edit_pasword_btn = findViewById(R.id.btn_update_password);
        Button delete_account_btn = findViewById(R.id.btn_delete_acount);

        final EditText email_et = findViewById(R.id.et_email);
        final EditText old_password_et = findViewById(R.id.et_oldpassword);
        final EditText passwordText =findViewById(R.id.et_newpassword);
        final EditText confirmPasswordText = findViewById(R.id.et_newpassword2);


        usernameText.setText(UserInfo.username);
        name_et.setText(UserInfo.name);
        email_et.setText(UserInfo.email);

        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(name_et.getText().toString().isEmpty())
                {
                    Toast.makeText(EditProfileActivity.this,"Enter a name to update.",Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditProfileActivity.this);
                LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                title_tv.setText("Edit profile");
                detail_tv.setText("You are changing your display name.");

                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                Button nobtn = dialogView.findViewById(R.id.btn_no);

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name = name_et.getText().toString();
                        db.collection("Users").document(UserInfo.username).update("Name",name).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                UserInfo.name = name;
                                db.collection("Users").document(UserInfo.username).update("Name_insensitive",name.toLowerCase());
                                Toast.makeText(EditProfileActivity.this,"Name changed successfully",Toast.LENGTH_LONG).show();
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

        edit_email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(email_et.getText().toString().isEmpty())
                {
                    Toast.makeText(EditProfileActivity.this,"Enter your new email.",Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditProfileActivity.this);
                LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                title_tv.setText("Edit profile");
                detail_tv.setText("You are changing your email.");

                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                Button nobtn = dialogView.findViewById(R.id.btn_no);

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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



        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = passwordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();
                TextView passwordStatus = findViewById(R.id.tv_passwordStatus);
                if(password.isEmpty())
                {
                    passwordStatus.setVisibility(View.GONE);
                }

                if(password.length() < 6){
                    passwordStatus.setVisibility(View.VISIBLE);
                    passwordStatus.setText("Password must be at least 6 characters long!");
                    passwordStatus.setTextColor(Color.RED);
                }
                /*else if(!password.equals(confirmPassword)){
                    password_status = false;
                    passwordStatus.setVisibility(View.VISIBLE);
                    passwordStatus.setText("Passwords do not match!");
                    passwordStatus.setTextColor(Color.RED);
                }*/
                else{
                    passwordStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        confirmPasswordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = passwordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();
                TextView passwordStatus = findViewById(R.id.tv_passwordStatus);
                if(confirmPassword.isEmpty())
                {
                    passwordStatus.setVisibility(View.GONE);
                }
                if(!password.equals(confirmPassword)){

                    passwordStatus.setVisibility(View.VISIBLE);
                    passwordStatus.setText("Passwords do not match!");
                    passwordStatus.setTextColor(Color.RED);
                }
                else{
                    passwordStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        edit_pasword_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String oldpassword,newpassword,newpassword2;
                oldpassword = old_password_et.getText().toString();
                newpassword = passwordText.getText().toString();
                newpassword2 = confirmPasswordText.getText().toString();

                if( oldpassword.isEmpty() || newpassword.isEmpty() || newpassword2.isEmpty())
                {
                    Toast.makeText(EditProfileActivity.this,"enter the details to change password",Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditProfileActivity.this);
                LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                title_tv.setText("Edit profile");
                detail_tv.setText("You are changing your Password.");

                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                Button nobtn = dialogView.findViewById(R.id.btn_no);

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("Users").document(UserInfo.username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                Map<String,Object> data = documentSnapshot.getData();
                                if(!oldpassword.equals(data.get("Password")))
                                {
                                    Toast.makeText(EditProfileActivity.this,"The password you have entered is not correct",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(newpassword.equals(newpassword2))
                                {
                                    db.collection("Users").document(UserInfo.username).update("Password",newpassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(EditProfileActivity.this,"Password updated successfully",Toast.LENGTH_LONG).show();
                                            b.dismiss();
                                        }
                                    });
                                }

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




        delete_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditProfileActivity.this);
                LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
                TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
                title_tv.setText("Delete Account");
                detail_tv.setText("You are deleting your account. You can't recover it after it is deleted.");

                Button yesbtn = dialogView.findViewById(R.id.btn_yes);
                Button nobtn = dialogView.findViewById(R.id.btn_no);

                LinearLayout ll = dialogView.findViewById(R.id.ll_input_password);
                ll.setVisibility(View.VISIBLE);
                final EditText password_et = dialogView.findViewById(R.id.et_password);

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(password_et.getText().toString().isEmpty())
                        {
                            Toast.makeText(EditProfileActivity.this,"Enter your password to continue.",Toast.LENGTH_LONG).show();
                            return;
                        }

                        db.collection("Users").document(UserInfo.username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                if(!password_et.getText().toString().equals(documentSnapshot.get("Password")))
                                {
                                    Toast.makeText(EditProfileActivity.this,"Incorrect password.",Toast.LENGTH_LONG).show();
                                    return;
                                }

                                db.collection("Users").document(UserInfo.username).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EditProfileActivity.this,"Account deleted successfully",Toast.LENGTH_LONG).show();
                                        b.dismiss();
                                        UserInfo.logout();
                                        Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
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
