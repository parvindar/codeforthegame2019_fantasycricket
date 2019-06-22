package com.test.fantasycricket;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView text_reg = (TextView) findViewById(R.id.tv_registerText);
        text_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        final EditText usernameText = findViewById(R.id.et_username);
        final EditText passwordText = findViewById(R.id.et_password);
        Button loginBtn = findViewById(R.id.btn_login);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameText.getText().toString();
                password = passwordText.getText().toString();
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
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(intent);

                                    finish();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Incorrect username and password !", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Username doesn't exist !", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });
    }

}
