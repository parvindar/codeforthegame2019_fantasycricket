package com.test.fantasycricket;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    ListView lv ;
    String matchid,contestid;
    String participantID;

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        String team1,team2,contestname;
        Integer totalspots;

        lv = findViewById(R.id.lv_leaderboardlist);
        matchid = getIntent().getStringExtra("matchid");
        contestid = getIntent().getStringExtra("contestid");
        team1 = getIntent().getStringExtra("team1");
        team2 = getIntent().getStringExtra("team2");
        participantID = getIntent().getStringExtra("participantID");
        contestname = getIntent().getStringExtra("contestname");
        totalspots = getIntent().getIntExtra("totalspots",0);

        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv = findViewById(R.id.tv_team2);
        TextView contestname_tv = findViewById(R.id.tv_contestname);
        TextView totalspots_tv = findViewById(R.id.tv_spots);
        TextView participantid_tv = findViewById(R.id.tv_participantid);
        final TextView points_tv = findViewById(R.id.tv_points);
        final TextView rank_tv = findViewById(R.id.tv_rank);

        team1tv.setText(team1);
        team2tv.setText(team2);
        contestname_tv.setText(contestname);
        totalspots_tv.setText(totalspots+" Spots");
        participantid_tv.setText(participantID);

        db.collection("Matches").document(matchid).collection("Contests").document(contestid).collection("Participants").orderBy("Points", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Participant> participants = new ArrayList<>();
                int i =0;
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                {
                    i++;
                    String id = documentSnapshot.getId();
                    double points = (double)documentSnapshot.get("Points");
                    participants.add(new Participant(id,points));
                    if(id.equals(participantID))
                    {
                        points_tv.setText(Constants.dec.format(points));
                        rank_tv.setText(String.valueOf(i));
                    }

                }

                ParticipantListAdaptor participantListAdaptor = new ParticipantListAdaptor(LeaderboardActivity.this,R.layout.leaderboard_element,participants);
                lv.setAdapter(participantListAdaptor);

            }
        });








    }

    class Participant{
        String id;
        double points;
        int rank;

        public Participant(String id, double points) {
            this.id = id;
            this.points = points;
        }
    }





    private class ParticipantListAdaptor extends ArrayAdapter<Participant> {
        private static final String TAG = "ParticipantListAdaptor";
        private Context mContext;
        private int mResource;

        public ParticipantListAdaptor(Context context, int resource, List<Participant> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(getItem(position)!=null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView Participantid_tv = convertView.findViewById(R.id.tv_participantid);
                TextView pointstv = convertView.findViewById(R.id.tv_points);
                TextView ranktv = convertView.findViewById(R.id.tv_rank);

                Participantid_tv.setText(getItem(position).id);
                pointstv.setText(Constants.dec.format(getItem(position).points));
                ranktv.setText(String.valueOf(position+1));


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {


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
