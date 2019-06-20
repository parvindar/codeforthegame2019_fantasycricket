package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MatchListAdaptor extends ArrayAdapter<Match> {
    private static final String TAG = "MessageListAdaptor";
    private Context mContext;
    private int mResource;

    public MatchListAdaptor(Context context, int resource, List<Match> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }




    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(getItem(position)!=null) {

            final String team1 = getItem(position).team1;
            final String team2 = getItem(position).team2;
            String date = getItem(position).date;
            Boolean started = getItem(position).started;
            String type = getItem(position).matchtype;

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            TextView team1tv = convertView.findViewById(R.id.tv_team1);
            TextView team2tv = convertView.findViewById(R.id.tv_team2);
            TextView timeleft = convertView.findViewById(R.id.tv_timeleft);
            TextView matchstatus = convertView.findViewById(R.id.tv_matchstatus);
            TextView matchtype = convertView.findViewById(R.id.tv_matchtype);
            team1tv.setText(team1);
            team2tv.setText(team2);
            timeleft.setText(date);
            matchtype.setText(type);
            if(started)
            {
                matchstatus.setText("Started");
            }
            else
            {
                matchstatus.setText("");
            }


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getItem(position)!=null)
                    {
                        Intent intent = new Intent(getContext(),ContestActivity.class);
                        intent.putExtra("team1",team1);
                        intent.putExtra("team2",team2);
                        mContext.startActivity(intent);
                    }

                }
            });


        }
        return convertView;

    }



}
