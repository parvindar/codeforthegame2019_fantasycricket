package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {


    String name, email, username, password, confirmPassword;
    Boolean username_status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(getApplicationContext());
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        TextView text_reg = (TextView) findViewById(R.id.tv_loginText);
        text_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        final EditText nameText = findViewById(R.id.et_name);
        final EditText emailText = findViewById(R.id.et_email);
        final EditText usernameText = findViewById(R.id.et_username);
        final EditText passwordText = findViewById(R.id.et_password);
        final EditText confirmPasswordText = findViewById(R.id.et_confirm_password);
        Button registerButton = findViewById(R.id.btn_register);

        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password = passwordText.getText().toString();
                confirmPassword = confirmPasswordText.getText().toString();
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
                password = passwordText.getText().toString();
                confirmPassword = confirmPasswordText.getText().toString();
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

        usernameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                username = usernameText.getText().toString();
                if(username.isEmpty())
                {
                    TextView usernameStatus = findViewById(R.id.tv_usernameStatus);
                    usernameStatus.setVisibility(View.GONE);
                }
                if(!username.isEmpty()) {
                    db.collection("Users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                TextView usernameStatus = findViewById(R.id.tv_usernameStatus);
                                if (document.exists()) {
                                    // username already exists !
                                    usernameStatus.setVisibility(View.VISIBLE);
                                    usernameStatus.setText("Username already exists!");
                                    usernameStatus.setTextColor(Color.RED);
                                    username_status = false;

                                    // resolving back problem
                                    if(username.isEmpty()){
                                        usernameStatus.setVisibility(View.GONE);
                                    }
                                } else {
                                    // username available
                                    usernameStatus.setVisibility(View.VISIBLE);
                                    usernameStatus.setText("Username is available!");
                                    usernameStatus.setTextColor(Color.GREEN);
                                    username_status = true;

                                    // resolving back problem
                                    if(username.isEmpty()){
                                        usernameStatus.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameText.getText().toString();
                email = emailText.getText().toString();
                username = usernameText.getText().toString();
                password = passwordText.getText().toString();
                confirmPassword = confirmPasswordText.getText().toString();
                if(name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
                    Toast.makeText(getApplicationContext(), "please fill all the fields !", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!password.equals(confirmPassword)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match !", Toast.LENGTH_LONG).show();
                    return;
                }
                if(password.length()<6) {
                    Toast.makeText(getApplicationContext(), "Password is too short ! (At least 6 characters)", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!username_status){
                    Toast.makeText(getApplicationContext(), "Username already exists !", Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, Object> user = new HashMap<>();
                user.put("Name", name);
                user.put("Email", email);
                user.put("Username", username);
                user.put("Password", password);
                user.put("UserType","user");
                user.put("Cash",0.00);
                user.put("Winnings",0);

                db.collection("Users")
                        .document(username)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Registered Successfully !", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);

                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Check your internet connection !", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
