package com.test.fantasycricket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

public class UserInfo {

    static String INR = "\u20B9 ";
    static Boolean logined=false;
    static String usertype;
    static String name;
    static String username;
    static String email;
    static ArrayList<String> contests;
    static Double cash;
    static Double winnings;
    static Double xp;
    private static ProgressDialog progressDialog;

    private static ListenerRegistration listener;


    public static void  login(String _usertype,String _username,String _name,String _email,Double _cash,Double _winnings,Double _xp)
    {
        logined=true;
        usertype=_usertype;
        username=_username;
        name= _name;
        email=_email;
        cash=_cash;
        winnings=_winnings;
        xp = _xp;


        // real-time update cash.
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        if(UserInfo.logined)
        {
            listener = db.collection("Users").document(UserInfo.username).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w("ERROR", "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {

                        try{
                            UserInfo.cash = documentSnapshot.getDouble("Cash");
                        }
                        catch (Exception io)
                        {
                            UserInfo.cash = Double.valueOf(String.valueOf(documentSnapshot.get("Cash")));
                        }
                        UserInfo.name = documentSnapshot.getString("Name");

                        try
                        {
                            UserInfo.xp = documentSnapshot.getDouble("xp");
                        }
                        catch (Exception ioo)
                        {
                            UserInfo.xp = Double.valueOf(String.valueOf(documentSnapshot.get("xp")));
                        }
                        try
                        {
                            UserInfo.winnings = documentSnapshot.getDouble("Winnings");
                        }
                        catch (Exception nhi)
                        {
                            UserInfo.winnings = Double.valueOf(String.valueOf(documentSnapshot.get("Winnings")));
                        }
                        try {
                            MyAccountActivity.cashtv.setText(Constants.INR+String.valueOf(UserInfo.cash));
                        }
                        catch (NullPointerException ne)
                        {
                            ne.printStackTrace();
                        }

                    } else {
                        Log.d("ERROR", "Current data: null");
                    }

                }
            });
        }
    }

    public static void logout()
    {
        logined=false;
        username=null;
        usertype=null;
        name=null;
        email=null;
        contests=null;
        cash=null;
        winnings=null;

        listener.remove();

    }


    public static void instantLogin(final Context context)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.activity_login, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog b = dialogBuilder.create();
        b.show();


        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextView text_reg = (TextView) dialogView.findViewById(R.id.tv_registerText);
        text_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RegisterActivity.class);
                context.startActivity(intent);
            }
        });

        final EditText usernameText = dialogView.findViewById(R.id.et_username);
        final EditText passwordText = dialogView.findViewById(R.id.et_oldpassword);
        Button loginBtn = dialogView.findViewById(R.id.btn_login);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username,password;
                username = usernameText.getText().toString();
                password = passwordText.getText().toString();

                if(username.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(context,"Enter username and password to login.",Toast.LENGTH_LONG).show();
                    return;
                }
                progressDialog = new ProgressDialog(context);
                progressDialog.show();


                db.collection("Users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> user = new HashMap<>();
                                user = document.getData();
                                if(user.get("Password").equals(password)){
                                    Toast.makeText(context, "Welcome "+ user.get("Name"), Toast.LENGTH_LONG).show();
                                    UserInfo.login(user.get("UserType").toString(),user.get("Username").toString(),user.get("Name").toString(),user.get("Email").toString(),Double.parseDouble(user.get("Cash").toString()),Double.parseDouble(user.get("Winnings").toString()),Double.parseDouble(user.get("xp").toString()));

                                    SharedPreferences sharedPref = context.getSharedPreferences("app",Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("username",username);
                                    editor.putString("password",password);
                                    editor.putBoolean("logined",true);
                                    editor.commit();


//                                    Intent intent = new Intent(context, HomeActivity.class);
//                                    startActivity(intent);

                                    b.dismiss();
                                    progressDialog.dismiss();
                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Incorrect username and password !", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Username doesn't exist !", Toast.LENGTH_LONG).show();
                            }
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });




    }


    public static void addCash(final Context context)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.add_cash_paytm, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog b = dialogBuilder.create();
        b.show();
        final EditText cash_et = dialogView.findViewById(R.id.et_addcash);
        Button submit_btn = dialogView.findViewById(R.id.btn_submit);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random r = new Random();

                String orderid = UserInfo.username+String.valueOf(r.nextInt());
                String custid = UserInfo.username;

                Intent intent = new Intent(context, checksum.class);
                intent.putExtra("orderid", orderid);
                intent.putExtra("custid",custid);
                intent.putExtra("amount",cash_et.getText().toString());
                context.startActivity(intent);
                b.dismiss();

            }
        });

    }


    public static void showConfirmWindow(final Context context, final Intent intent, String title, String detail)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.confirm_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        TextView title_tv = dialogView.findViewById(R.id.tv_confirmbox_title);
        TextView detail_tv = dialogView.findViewById(R.id.tv_dialogbox_detail);
        title_tv.setText(title);
        detail_tv.setText(detail);

        Button yesbtn = dialogView.findViewById(R.id.btn_yes);
        Button nobtn = dialogView.findViewById(R.id.btn_no);

        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                context.startActivity(intent);
                b.dismiss();
            }
        });

        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.dismiss();
            }
        });

    }


    private static final UserInfo ourInstance = new UserInfo();

    public static UserInfo getInstance() {
        return ourInstance;
    }

    private UserInfo() {
    }
}
