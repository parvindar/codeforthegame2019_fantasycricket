package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContestActivity extends AppCompatActivity {
    ListView contestlist;
    static String matchid;
    String team1,team2;
    ArrayList<Contest> contestArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest);
        contestlist=findViewById(R.id.lv_contests);

        contestArrayList = new ArrayList<>();
        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv = findViewById(R.id.tv_team2);
        team1=getIntent().getStringExtra("team1");
        team2=getIntent().getStringExtra("team2");

        team1tv.setText(team1);
        team2tv.setText(team2);
        contestArrayList.add(new Contest(10000,100,25,120));
        contestArrayList.add(new Contest(500,1,168,600));
        contestArrayList.add(new Contest(100000,100,250,1200));
        contestArrayList.add(new Contest(1000,10,50,120));
        contestArrayList.add(new Contest(50000,10,3680,6000));

        ContestListAdaptor contestListAdaptor = new ContestListAdaptor(this,R.layout.contest,contestArrayList);
        contestlist.setAdapter(contestListAdaptor);



    }



    class Contest{
        Integer prize,price,spotsfilled,totalspots;

        public Contest(Integer prize, Integer price, Integer spotsfilled, Integer totalspots) {
            this.prize = prize;
            this.price = price;
            this.spotsfilled = spotsfilled;
            this.totalspots = totalspots;
        }
    }


    public class ContestListAdaptor extends ArrayAdapter<Contest> {
        private static final String TAG = "MessageListAdaptor";
        private Context mContext;
        private int mResource;

        public ContestListAdaptor(Context context, int resource, List<Contest> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(getItem(position)!=null) {

                Integer prize = getItem(position).prize;
                Integer price = getItem(position).price;
                Integer spotsfilled = getItem(position).spotsfilled;
                Integer totalspots = getItem(position).totalspots;

                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView prizetv = convertView.findViewById(R.id.tv_prize);
                TextView pricetv = convertView.findViewById(R.id.tv_price);
                TextView spotslefttv = convertView.findViewById(R.id.tv_spotsleft);
                TextView totalspotstv = convertView.findViewById(R.id.tv_totalspots);
                ProgressBar spotsfilledpb = convertView.findViewById(R.id.pb_spotsfilled);
                pricetv.setText(String.valueOf(price));
                prizetv.setText(String.valueOf(prize));
                totalspotstv.setText(String.valueOf(totalspots));
                spotslefttv.setText(String.valueOf(totalspots-spotsfilled));
                spotsfilledpb.setProgress((100*spotsfilled)/totalspots);



                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {
                            Toast.makeText(getApplicationContext(),"It will work soon!",Toast.LENGTH_LONG).show();
                        }

                    }
                });


            }
            return convertView;

        }



    }

}
