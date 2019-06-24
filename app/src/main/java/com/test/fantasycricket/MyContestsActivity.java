package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyContestsActivity extends AppCompatActivity {
    ArrayList<Contest> contestArrayList = new ArrayList<>();
    Map<String,Object> contestidobject;
    ArrayList<String> mycontestsarraylist;
    ListView lv;
    FirebaseFirestore db ;
    String team1,team2,matchid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_contests);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db= FirebaseFirestore.getInstance();

        lv = findViewById(R.id.lv_mycontests);
        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv = findViewById(R.id.tv_team2);
        team1=getIntent().getStringExtra("team1");
        team2=getIntent().getStringExtra("team2");
        matchid=getIntent().getStringExtra("matchid");

        team1tv.setText(team1);
        team2tv.setText(team2);

        db.collection("Users").document(UserInfo.username).collection("Matches").document(matchid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists())
                {
                    contestidobject = new HashMap<>();
                    contestidobject = documentSnapshot.getData();

                    mycontestsarraylist = new ArrayList<>();
                    mycontestsarraylist=(ArrayList<String>) contestidobject.get("contests");




                    db.collection("Matches").document(matchid).collection("Contests").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            contestArrayList.clear();
                            for( DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments())
                            {
                                String prize,price,spotsfilled,totalspots;
                                String contestname,id;
                                boolean contestfinished;
                                id= documentSnapshot.getId();
                                if(!mycontestsarraylist.contains(id))
                                {
                                    continue;
                                }
                                prize = documentSnapshot.getData().get("TotalPrize").toString();
                                price = documentSnapshot.getData().get("Price").toString();
                                spotsfilled = (documentSnapshot.getData().get("SpotsFilled").toString());
                                totalspots = (documentSnapshot.getData().get("TotalSpots").toString());
                                contestfinished=(boolean)documentSnapshot.getData().get("Finished");

                                contestname = documentSnapshot.getString("ContestName");
                                Contest currcontest = new Contest(id,Double.valueOf(prize),Double.valueOf(price),Integer.valueOf(spotsfilled),Integer.valueOf(totalspots));
                                currcontest.contestname = contestname;
                                currcontest.finished=contestfinished;
                                contestArrayList.add(currcontest);

                            }

                            MyContestListAdaptor contestListAdaptor = new MyContestListAdaptor(MyContestsActivity.this,R.layout.contest,contestArrayList);
                            lv.setAdapter(contestListAdaptor);

                        }
                    });




                }
                else
                {
                    Toast.makeText(MyContestsActivity.this,"Nothing to show",Toast.LENGTH_LONG).show();
                }
            }
        });







    }

    private class Contest{
        Integer spotsfilled,totalspots;
        Double prize,price;
        String contestname;
        public String id;
        public boolean finished=false;

        Contest(String id,Double prize, Double price, Integer spotsfilled, Integer totalspots) {
            this.prize = prize;
            this.price = price;
            this.spotsfilled = spotsfilled;
            this.totalspots = totalspots;
            this.contestname ="";
            this.id = id;
        }
    }




    public class MyContestListAdaptor extends ArrayAdapter<Contest> {
        private static final String TAG = "ContestListAdaptor";
        private Context mContext;
        private int mResource;

        public MyContestListAdaptor(Context context, int resource, List<Contest> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(getItem(position)!=null) {

                Double prize = getItem(position).prize;
                final Double price = getItem(position).price;
                Integer spotsfilled = getItem(position).spotsfilled;
                Integer totalspots = getItem(position).totalspots;

                final LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView prizetv = convertView.findViewById(R.id.tv_prize);
                TextView pricetv = convertView.findViewById(R.id.tv_price);
                TextView spotslefttv = convertView.findViewById(R.id.tv_spotsleft);
                TextView totalspotstv = convertView.findViewById(R.id.tv_totalspots);
                ProgressBar spotsfilledpb = convertView.findViewById(R.id.pb_spotsfilled);
                TextView contestnametv = convertView.findViewById(R.id.tv_contestname);
                TextView entrylabeltv = convertView.findViewById(R.id.tv_entry_label);

                entrylabeltv.setVisibility(View.INVISIBLE);
                pricetv.setVisibility(View.INVISIBLE);
                pricetv.setText(UserInfo.INR+String.valueOf(price));
                prizetv.setText(UserInfo.INR+String.valueOf(prize));
                totalspotstv.setText(String.valueOf(totalspots));
                spotslefttv.setText(String.valueOf(totalspots-spotsfilled));
                spotsfilledpb.setProgress((100*spotsfilled)/totalspots);

                contestnametv.setText(getItem(position).contestname);
                if(prize==0 && !getItem(position).contestname.isEmpty())
                {
                    if(getItem(position).contestname.length()>13)
                    {
                        prizetv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f);
                    }
                    prizetv.setText(getItem(position).contestname);


                    contestnametv.setText("");

                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {
                            if(getItem(position).finished)
                            {

                            }
                            else
                            {

                                Intent intent = new Intent(MyContestsActivity.this,CurrentPointsActivity.class);
                                intent.putExtra("team1",team1);
                                intent.putExtra("team2",team2);
                                intent.putExtra("matchid",matchid);
                                Map<String,Object> contestdetail = (Map<String,Object>)contestidobject.get(getItem(position).id);
                                intent.putExtra("ParticipantID",contestdetail.get("ParticipantID").toString());
                                intent.putExtra("contestid",getItem(position).id);

                                startActivity(intent);
                            }

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
