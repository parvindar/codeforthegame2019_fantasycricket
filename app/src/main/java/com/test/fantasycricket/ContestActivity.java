package com.test.fantasycricket;

import android.content.Context;
import android.content.Intent;
import android.os.DeadObjectException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContestActivity extends AppCompatActivity {
    ListView contestlist;
    static String matchid;
    String team1,team2;
    ArrayList<Contest> contestArrayList;
    String contestname,totalprize,totalspots,price;
    Double gain;
    FirebaseFirestore db;
    public static DecimalFormat dec = new DecimalFormat("#0.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest);
        CreateTeamActivity.matchid = matchid;
        db=FirebaseFirestore.getInstance();

        contestlist=findViewById(R.id.lv_contests);
        FloatingActionButton createcontestbtn = findViewById(R.id.fab_createcontest);
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


        createcontestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContestActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.createcontest, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                final EditText et_contestname = dialogView.findViewById(R.id.et_contestname);
                final EditText et_totalprize = dialogView.findViewById(R.id.et_totalprize);
                final EditText et_totalspots = dialogView.findViewById(R.id.et_totalspots);
                final TextView tv_price = dialogView.findViewById(R.id.tv_price);
                TextView dialogteam1 = dialogView.findViewById(R.id.tv_team1);
                TextView dialogteam2 = dialogView.findViewById(R.id.tv_team2);
                dialogteam1.setText(team1);
                dialogteam2.setText(team2);
                contestname="";
                Button btn_createcontest = dialogView.findViewById(R.id.btn_submit);

                btn_createcontest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contestname = et_contestname.getText().toString();
                        totalprize = et_totalprize.getText().toString();
                        totalspots = et_totalspots.getText().toString();
                        price = tv_price.getText().toString();

                        Map<String,Object> contest = new HashMap<>();
                        contest.put("ContestName",contestname);
                        contest.put("TotalPrize",totalprize);
                        contest.put("TotalSpots",totalspots);
                        contest.put("Price",price);
                        contest.put("SpotsFilled",0);
                        db.collection("Matches").document(matchid).collection("Contests").add(contest).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(ContestActivity.this,"Contest Created Successfully",Toast.LENGTH_LONG).show();
                                b.dismiss();

                            }
                        });




                    }
                });


                et_totalprize.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().isEmpty() && !et_totalspots.getText().toString().isEmpty())
                        {
                            TextView tv_price =(TextView)dialogView.findViewById(R.id.tv_price);

                            if(Double.parseDouble(s.toString())==0 || Double.parseDouble(et_totalspots.getText().toString())==0)
                            {

                                return;
                            }
                            String prize = s.toString();
                            Double prizeamt= Double.parseDouble(prize);
                            Double totalspots = Double.parseDouble(et_totalspots.getText().toString());
                            gain = prizeamt/5.0;
                            price =String.valueOf (dec.format((prizeamt + gain)/totalspots));
                            if(Double.parseDouble(price)<1)
                            {
                                price="1";
                                gain = totalspots-prizeamt;
                            }
                            tv_price.setText(UserInfo.INR+price);

                        }

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                et_totalspots.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        if(!et_totalprize.getText().toString().isEmpty() &&  !s.toString().isEmpty())
                        {
                            if(Double.parseDouble(s.toString())==0 || Double.parseDouble(et_totalprize.getText().toString())==0)
                            {
                                return;
                            }

                             String prize = et_totalprize.getText().toString();
                             Double prizeamt= Double.parseDouble(prize);
                             Double totalspots = Double.parseDouble(s.toString());
                             gain = prizeamt/5.0;
                            price =String.valueOf (dec.format((prizeamt + gain)/totalspots));
                            if(Double.parseDouble(price)<1)
                            {
                                price="1";
                                gain = totalspots-prizeamt;
                            }
                             TextView tv_price =(TextView)dialogView.findViewById(R.id.tv_price);
                             tv_price.setText(UserInfo.INR+price);

                        }

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

    }



    class Contest{
        Integer prize,price,spotsfilled,totalspots;
        String contestname;

        public Contest(Integer prize, Integer price, Integer spotsfilled, Integer totalspots) {
            this.prize = prize;
            this.price = price;
            this.spotsfilled = spotsfilled;
            this.totalspots = totalspots;
            this.contestname ="";
        }
    }


    public class ContestListAdaptor extends ArrayAdapter<Contest> {
        private static final String TAG = "ContestListAdaptor";
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
                pricetv.setText(UserInfo.INR+String.valueOf(price));
                prizetv.setText(UserInfo.INR+String.valueOf(prize));
                totalspotstv.setText(String.valueOf(totalspots));
                spotslefttv.setText(String.valueOf(totalspots-spotsfilled));
                spotsfilledpb.setProgress((100*spotsfilled)/totalspots);



                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ContestActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            final View dialogView = inflater.inflate(R.layout.participate_contest_dialog, null);
                            dialogBuilder.setView(dialogView);
                            final AlertDialog b = dialogBuilder.create();
                            b.show();
                            TextView prizetv= dialogView.findViewById(R.id.tv_dialog_prize);
                            TextView pricetv = dialogView.findViewById(R.id.tv_dialog_price);
                            TextView contestname = dialogView.findViewById(R.id.tv_dialog_contestname);
                            Button btn_participate = dialogView.findViewById(R.id.btn_participate);
                            Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);
                            prizetv.setText(UserInfo.INR+getItem(position).prize.toString());
                            pricetv.setText(UserInfo.INR+ getItem(position).price.toString());
                            contestname.setText(getItem(position).contestname);

                            btn_cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    b.dismiss();
                                }
                            });

                            btn_participate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(ContestActivity.this,CreateTeamActivity.class);
                                    intent.putExtra("team1",team1);
                                    intent.putExtra("team2",team2);
                                    startActivity(intent);

                                }
                            });




                            Toast.makeText(getApplicationContext(),"It will work soon!",Toast.LENGTH_LONG).show();
                        }

                    }
                });


            }
            return convertView;

        }



    }




}
