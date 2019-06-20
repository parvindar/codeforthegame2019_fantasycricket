package com.test.fantasycricket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

public class ContestActivity extends AppCompatActivity {
    ListView contestlist;
    static String matchid;
    String team1,team2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest);
    }



    class Contest{
        public Integer prize,price,spotsfilled,totalspots;

        public Contest(Integer prize, Integer price, Integer spotsfilled, Integer totalspots) {
            this.prize = prize;
            this.price = price;
            this.spotsfilled = spotsfilled;
            this.totalspots = totalspots;
        }
    }
}
