package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminUserListActivity extends AppCompatActivity {
    ListView lv;
    ArrayList<User> userArrayList;
    String searchname="";

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = FirebaseFirestore.getInstance();

        lv = findViewById(R.id.lv_admin_userlist);
        final Button searchbtn = findViewById(R.id.btn_admin_search);
        final EditText searchname_et = findViewById(R.id.et_admin_searchname);


        db.collection("Users").orderBy("Name_insensitive").startAt(searchname.toLowerCase()).limit(100).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                userArrayList= new ArrayList<>();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                {
                    String name = (String) documentSnapshot.get("Name");
                    String username = (String)documentSnapshot.get("Username");
                    Double xp;
                    try{
                        xp = (Double)documentSnapshot.get("xp");
                    }
                    catch (Exception e)
                    {
                        xp = Double.parseDouble(String.valueOf(documentSnapshot.get("xp")));
                    }

                    User user = new User(name,username,xp);
                    userArrayList.add(user);
                }

                UserListAdaptor userListAdaptor = new UserListAdaptor(AdminUserListActivity.this,R.layout.admin_userlist_element,userArrayList);

                lv.setAdapter(userListAdaptor);

            }
        });


        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchname = searchname_et.getText().toString();
                db.collection("Users").orderBy("Name_insensitive").startAt(searchname.toLowerCase()).limit(100).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        userArrayList= new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                        {
                            String name = (String) documentSnapshot.get("Name");
                            String username = (String)documentSnapshot.get("Username");
                            Double xp;
                            try{
                                xp = (Double)documentSnapshot.get("xp");
                            }
                            catch (Exception e)
                            {
                                xp = Double.parseDouble(String.valueOf(documentSnapshot.get("xp")));
                            }

                            User user = new User(name,username,xp);
                            userArrayList.add(user);
                        }

                        UserListAdaptor userListAdaptor = new UserListAdaptor(AdminUserListActivity.this,R.layout.admin_userlist_element,userArrayList);
                        lv.setAdapter(userListAdaptor);

                    }
                });


            }
        });


    }


    private class User{
        String name;
        String username;
        double xp;

        public  User(){

        }

        public User(String name, String username, double xp) {
            this.name = name;
            this.username = username;
            this.xp = xp;
        }
    }



    private class UserListAdaptor extends ArrayAdapter<User> {
        private static final String TAG = "UserListAdaptor";
        private Context mContext;
        private int mResource;

        public UserListAdaptor(Context context, int resource, List<User> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(getItem(position)!=null) {
                final LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView name_tv = convertView.findViewById(R.id.tv_userlist_name);
                TextView username_tv = convertView.findViewById(R.id.tv_userlist_username);
                TextView xp_tv = convertView.findViewById(R.id.tv_userlist_xp);

                name_tv.setText(getItem(position).name);
                username_tv.setText(getItem(position).username);
                xp_tv.setText("xp: "+String.valueOf(getItem(position).xp));


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {
                            Intent intent = new Intent(AdminUserListActivity.this,AdminUserDetailsActivity.class);
                            intent.putExtra("username",getItem(position).username);
                            startActivity(intent);
                        }

                    }
                });


            }
            return convertView;

        }



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
