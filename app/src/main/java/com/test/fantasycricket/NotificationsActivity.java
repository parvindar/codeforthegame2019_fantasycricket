package com.test.fantasycricket;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    ListView lv;
    FirebaseFirestore db;
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout =findViewById(R.id.swipe_reflesh_notification);

        db = FirebaseFirestore.getInstance();

        lv = findViewById(R.id.lv_notificationlist);

        db.collection("Users").document(UserInfo.username).collection("Notifications").orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Notification> notifications = new ArrayList<>();

                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                {
                    Map<String,Object> data = documentSnapshot.getData();
                    Notification notification = new Notification(data.get("type").toString(),data.get("message").toString(),(Double) data.get("points"),((Long) data.get("rank")).intValue(), (Long) data.get("date"),(Boolean) data.get("winner"), Double.valueOf(String.valueOf(data.get("award"))));
                    notification.contestname = (String)data.get("contestname");
                    notification.team1 = (String)data.get("team1");
                    notification.team2 = (String)data.get("team2");
                    notifications.add(notification);
                }

                NotificationListAdaptor notificationListAdaptor = new NotificationListAdaptor(NotificationsActivity.this,R.layout.notifications_element,notifications);
                lv.setAdapter(notificationListAdaptor);


            }
        });


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                db.collection("Users").document(UserInfo.username).collection("Notifications").orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Notification> notifications = new ArrayList<>();

                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                        {
                            Map<String,Object> data = documentSnapshot.getData();
                            Notification notification = new Notification(data.get("type").toString(),data.get("message").toString(),(Double) data.get("points"),((Long) data.get("rank")).intValue(), (Long) data.get("date"),(Boolean) data.get("winner"), Double.valueOf(String.valueOf(data.get("award"))));
                            notification.contestname = (String)data.get("contestname");
                            notification.team1 = (String)data.get("team1");
                            notification.team2 = (String)data.get("team2");
                            notifications.add(notification);
                        }

                        NotificationListAdaptor notificationListAdaptor = new NotificationListAdaptor(NotificationsActivity.this,R.layout.notifications_element,notifications);
                        lv.setAdapter(notificationListAdaptor);
                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                });

            }
        });

    }


    class Notification{
        String type;
        String message;
        double points;
        int rank;
        String team1,team2;
        String contestname;
        long time;
        boolean winner;
        double award;

        public Notification(String type, String message, double points, int rank, long time, boolean winner, double award) {
            this.type = type;
            this.message = message;
            this.points = points;
            this.rank = rank;
            this.time = time;
            this.winner = winner;
            this.award = award;
        }
    }




    private class NotificationListAdaptor extends ArrayAdapter<Notification> {
        private static final String TAG = "NotificationListAdaptor";
        private Context mContext;
        private int mResource;

        public NotificationListAdaptor(Context context, int resource, List<Notification> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(getItem(position)!=null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                String type = getItem(position).type;
                long date = getItem(position).time;



                TextView team1vsteam2 = convertView.findViewById(R.id.tv_team1vsteam2);
                TextView datetv = convertView.findViewById(R.id.tv_date);
                TextView message = convertView.findViewById(R.id.tv_message);
                TextView points = convertView.findViewById(R.id.tv_points);
                TextView rank = convertView.findViewById(R.id.tv_rank);
                TextView contestname = convertView.findViewById(R.id.tv_contestname);
                TextView xptext = convertView.findViewById(R.id.tv_xptext);
                xptext.setVisibility(View.GONE);

                team1vsteam2.setText(Constants.getTeamShortName(getItem(position).team1) +" vs "+Constants.getTeamShortName(getItem(position).team2));

                Date date1 = new Date((date));
                String[] datearr = date1.toString().split(" ");
                String datestr = datearr[3].substring(0,5)+" "+datearr[2]+" "+datearr[1]+" "+datearr[5].substring(2,2);

                datetv.setText(datestr);
                message.setText(getItem(position).message);
                points.setText(String.valueOf(getItem(position).points));
                rank.setText(String.valueOf(getItem(position).rank));
                contestname.setText(getItem(position).contestname);
                xptext.setText("xp = "+Constants.dec.format(UserInfo.xp-getItem(position).points) +" + "+Constants.dec.format(getItem(position).points));

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
