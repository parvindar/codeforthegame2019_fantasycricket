package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ThrowOnExtraProperties;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MatchListAdaptor extends ArrayAdapter<Match> {
    private static final String TAG = "MessageListAdaptor";
    private Context mContext;
    private int mResource;
    private List<ViewHolder> lstHolders;
    private Handler mHandler = new Handler();
    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (lstHolders) {
                long currentTime = Calendar.getInstance().getTimeInMillis();
                for (ViewHolder holder : lstHolders) {
                    holder.updateTimeRemaining(currentTime);
                }
            }
        }
    };

    public MatchListAdaptor(Context context, int resource, List<Match> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        lstHolders = new ArrayList<>();
        startUpdateTimer();
    }

    private void startUpdateTimer() {
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);
    }





    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(getItem(position)!=null) {

            final String team1 = getItem(position).team1;
            final String team2 = getItem(position).team2;
            String timeleft = getItem(position).timeleft;
            Boolean started = getItem(position).started;
            String type = getItem(position).matchtype;
            String winner_team = getItem(position).winner_team;
            String toss_winner_team = getItem(position).toss_winner;

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            TextView team1tv = convertView.findViewById(R.id.tv_team1);
            TextView team2tv = convertView.findViewById(R.id.tv_team2);
            TextView timelefttv = convertView.findViewById(R.id.tv_timeremaining_top);
            TextView matchstatus = convertView.findViewById(R.id.tv_matchstatus);
            TextView matchtype = convertView.findViewById(R.id.tv_matchtype);
            TextView team1fulltv = convertView.findViewById(R.id.tv_team1_full);
            TextView team2fulltv = convertView.findViewById(R.id.tv_team2_full);
            LinearLayout linearLayout = convertView.findViewById(R.id.ll_matchelement_layout);
            CircularImageView team1flag = convertView.findViewById(R.id.img_team1);
            CircularImageView team2flag = convertView.findViewById(R.id.img_team2);


            try
            {
                String uri1 = "@drawable/"+Constants.getTeamShortName(team1).toLowerCase();
                int imageResource1 = mContext.getResources().getIdentifier(uri1, null, mContext.getPackageName());
                Drawable res = mContext.getResources().getDrawable(imageResource1);
                team1flag.setImageDrawable(res);

            }catch (Exception e)
            {
                e.printStackTrace();

            }
            try {
                String uri2 = "@drawable/"+Constants.getTeamShortName(team2).toLowerCase();
                int imageResource2 = mContext.getResources().getIdentifier(uri2, null, mContext.getPackageName());
                Drawable res2 = mContext.getResources().getDrawable(imageResource2);
                team2flag.setImageDrawable(res2);
            }catch (Exception e)
            {
                e.printStackTrace();

            }

            team1tv.setText(Constants.getTeamShortName(team1));
            team2tv.setText(Constants.getTeamShortName(team2));
            team1fulltv.setText(team1);
            team2fulltv.setText(team2);
            timelefttv.setText(timeleft);
            matchtype.setText(type);
            if(started)
            {
                matchstatus.setText("Started");
                if(!toss_winner_team.isEmpty())
                {
                    timelefttv.setText(Constants.getTeamShortName(toss_winner_team)+" won the toss.");
                    timelefttv.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
                }
                if(!winner_team.isEmpty() && (winner_team.equals(team1)|| winner_team.equals(team2)))
                {
                    timelefttv.setText(Constants.getTeamShortName(winner_team)+" won the match");
            //        timelefttv.setTextColor(0xFFCA4300);
                    timelefttv.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                    matchstatus.setText("Finished");
                    matchstatus.setTextColor(0xFF00A70B);
                }
            }
            else
            {
                matchstatus.setText("");
                ViewHolder holder = new ViewHolder();
                if(getItem(position).open)
                {
                    holder.tvTimeRemaining = timelefttv;
                    synchronized (lstHolders) {
                        lstHolders.add(holder);
                    }

                    holder.setData(getItem(position));
                }

            }
            if(!getItem(position).open)
            {
                linearLayout.setBackgroundColor(Color.parseColor("#E0E0E0"));
            }
            else
            {
//                linearLayout.setBackgroundColor(Color.parseColor("#05a2cffe"));
            }



            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getItem(position)!=null)
                    {

                        if(getItem(position).open)
                        {
                            Intent intent;
                            if(MyMatchesActivity.active)
                            {
                                intent = new Intent(getContext(),MyContestsActivity.class);
                            }
                            else
                            {
                                intent = new Intent(getContext(),ContestActivity.class);
                            }
                            intent.putExtra("team1",team1);
                            intent.putExtra("team2",team2);
                            intent.putExtra("matchid",getItem(position).uniqueid);
                            intent.putExtra("started",getItem(position).started);
                            ContestActivity.matchid = getItem(position).uniqueid;
                            mContext.startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(mContext,"Contests will open soon for this match!",Toast.LENGTH_LONG).show();
                        }
                    }

                }
            });


        }
        return convertView;

    }





    private class ViewHolder {
        TextView tvTimeRemaining;
        Match mProduct;

        public void setData(Match item) {
            mProduct = item;
            updateTimeRemaining(Calendar.getInstance().getTimeInMillis());
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff = mProduct.realdate.getTime() - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);
                int days = (int) ((timeDiff / (1000 * 60 * 60*24)) );
                if(days > 2)
                {
                    tvTimeRemaining.setText(days+"days "+hours + "hrs.");
                }
                else if(days > 0 )
                {
                    tvTimeRemaining.setText(days+"d "+hours + "h " + minutes + "m");
                }
                else if(hours > 0)
                {
                    tvTimeRemaining.setText(hours + "h " + minutes + "m " + seconds + "s");
                }
                else {
                    tvTimeRemaining.setText(minutes + "m " + seconds + "s");

                }
            } else {
                tvTimeRemaining.setText("Match Started");
            }
        }
    }



}
