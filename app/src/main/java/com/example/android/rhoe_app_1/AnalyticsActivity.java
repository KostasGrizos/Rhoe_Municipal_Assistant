package com.example.android.rhoe_app_1;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import com.example.android.app_v12.R;
import com.example.android.rhoe_app_1.FirebaseFine.RetrieveFineInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseUsers.RetrieveUserInfoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class AnalyticsActivity extends AppCompatActivity {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef;
    DatabaseReference mURef;
    DatabaseReference mRefClick;
    FirebaseAuth mAuth;
    String userID;
    String MunicipalityIndex;
    String selectedKey;
    String DateSelected, TimeSelected, PlateSelected, AddressSelected;
    TextView Test;
    TextView Test1;
    TextView Test0;

    private Integer[] yAll = new Integer[7];

    ArrayList<String> listDate = new ArrayList<>();

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String IterDate;

    @SuppressLint({"NewApi", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        listDate.clear();
        yAll = new Integer[]{0, 0, 0, 0, 0, 0, 0};

        Test = (TextView) findViewById(R.id.tvAnalyticsTest);
        Test1 = (TextView) findViewById(R.id.tvAnalyticsTest1);
        Test0 = (TextView) findViewById(R.id.tvBefore);

        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        IterDate = simpleDateFormat.format(calendar.getTime());
        listDate.add(0, IterDate);
        for (int i=1; i<7; i++) {
            calendar.add(Calendar.DATE, -1);
            IterDate = simpleDateFormat.format(calendar.getTime());
            listDate.add(i, IterDate);
        }

        //Test0.setText(list.get(6) + " " + list.get(5) + " " + list.get(4) + " " + list.get(3) + " " + list.get(2) + " " + list.get(1) + " " + list.get(0));

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mURef = FirebaseDatabase.getInstance().getReference("Users");

        mURef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot UdataSnapshot) {
                showUData(UdataSnapshot);
                for (int i=0; i<7; i++) {
                    final int finalI = i;
                    mRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int y = 0;
                            Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();
                            while (items.hasNext()) {
                                DataSnapshot item = items.next();
                                String RealDate = item.child("Date").getValue().toString();
                                if (Objects.equals(RealDate, listDate.get(finalI))) {
                                    y = y +1;
                                }
                            }
                            yAll[finalI] = y;
                            Test.setText(Test.getText() + " " + yAll[finalI]);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, yAll[6]),
                new DataPoint(1, yAll[5]),
                new DataPoint(2, yAll[4]),
                new DataPoint(3, yAll[3]),
                new DataPoint(4, yAll[2]),
                new DataPoint(5, yAll[1]),
                new DataPoint(6, yAll[0])

        });

        series.setColor(R.color.colorAccent1);
        graph.setTitle("Ημερήσιες Βεβαιώσεις Παραβάσεων Κ.Ο.Κ.");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Βεβαιώσεις");
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {listDate.get(6), listDate.get(5), listDate.get(4), listDate.get(3), listDate.get(2), listDate.get(1), listDate.get(0)});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(45);
        graph.addSeries(series);*/

        //Test1.setText(Integer.toString(yAll[6]) + " " + Integer.toString(yAll[5]) + " " + Integer.toString(yAll[4]) + " " + Integer.toString(yAll[3]) + " " + Integer.toString(yAll[2]) + " " + Integer.toString(yAll[1]) + " " + Integer.toString(yAll[0]));


    }

    private void showUData(DataSnapshot UdataSnapshot) {
        for (DataSnapshot ds : UdataSnapshot.getChildren()) {
            RetrieveUserInfoFirebase RUInfo = new RetrieveUserInfoFirebase();
            RUInfo.setMunicipality(UdataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getMunicipality());

            MunicipalityIndex = RUInfo.getMunicipality();
            mRef = FirebaseDatabase.getInstance().getReference("Fines").child(MunicipalityIndex);

        }
    }

    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            RetrieveFineInfoFirebase RInfo = new RetrieveFineInfoFirebase();
            RInfo.setDate(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getDate());
            RInfo.setTime(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getTime());
            RInfo.setCarPlate(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarPlate());
            RInfo.setAddress(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getAddress());

            DateSelected = RInfo.getDate();
            TimeSelected = RInfo.getTime();
            PlateSelected = RInfo.getCarPlate();
            AddressSelected =RInfo.getAddress();


        }
    }
}


