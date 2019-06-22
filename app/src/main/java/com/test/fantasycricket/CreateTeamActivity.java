package com.test.fantasycricket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CreateTeamActivity extends AppCompatActivity {

    ListView wicketkeeperlv ;
    ListView batsmanlv;
    ListView allrounderlv ;
    ListView bowlerlv ;
    String team1,team2;
    Integer credits = 90;
    String apiurl ="https://cricapi.com/api/fantasySummary?apikey=VdUTmLVaoVNmU4V8wnQR6LBnezo2&unique_id=";
    static String matchid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        apiurl = apiurl+matchid;

        TextView team1tv= findViewById(R.id.tv_team1);
        TextView team2tv= findViewById(R.id.tv_team2);
        team1=getIntent().getStringExtra("team1");
        team2=getIntent().getStringExtra("team2");
        team1tv.setText(team1);
        team2tv.setText(team2);

        TextView totalplayerselectedtv = findViewById(R.id.tv_total_player_selected);
        TextView wicketkeepertv = findViewById(R.id.tv_wicketkeeper);
        TextView batsmantv= findViewById(R.id.tv_batsman);
        TextView allroundertv = findViewById(R.id.tv_allrounder);
        TextView bowlertv = findViewById(R.id.tv_bowler);

        wicketkeeperlv = findViewById(R.id.lv_wicketkeeperlist);
        batsmanlv = findViewById(R.id.lv_batsmanlist);
        allrounderlv = findViewById(R.id.lv_allrounderlist);
        bowlerlv = findViewById(R.id.lv_bowlerlist);

    }
}
