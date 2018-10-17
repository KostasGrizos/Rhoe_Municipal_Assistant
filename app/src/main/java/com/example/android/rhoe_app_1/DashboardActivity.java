package com.example.android.rhoe_app_1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.app_v12.R;
import com.example.android.rhoe_app_1.FirebaseUsers.RetrieveUserInfoFirebase;
import com.example.android.rhoe_app_1.Zebra.DemoSleeper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    private ImageButton LogoutButton;
    private ImageButton VehicleCatButton, CleanlinessCatButton, SmokingCatButton;
    private ImageButton FineLogButton, AnalyticsButton, ViewUsersButton, NewFineButton, SettingsButton;
    private TextView NameText, SurnameText, MunicipalityText, MunicipalIDText;
    private TextView ConnectivityStatusDashTextView;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String userID, MID, CatPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final FirebaseUser user =firebaseAuth.getCurrentUser();
        userID = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        LogoutButton = (ImageButton)findViewById(R.id.btnLogout);
        VehicleCatButton = (ImageButton)findViewById(R.id.btnVehicleCat);
        CleanlinessCatButton = (ImageButton)findViewById(R.id.btnCleanlinessCat);
        SmokingCatButton = (ImageButton)findViewById(R.id.btnSmokingCat);
        FineLogButton = (ImageButton)findViewById(R.id.btnFineLog);
        AnalyticsButton = (ImageButton)findViewById(R.id.btnAnalytics);
        ViewUsersButton = (ImageButton)findViewById(R.id.btnViewUsers);
        NewFineButton = (ImageButton)findViewById(R.id.btnNewFine);
        SettingsButton = (ImageButton)findViewById(R.id.btnSettings);

        NameText = (TextView) findViewById(R.id.txtNameView);
        SurnameText = (TextView) findViewById(R.id.txtSurnameView);
        MunicipalityText = (TextView) findViewById(R.id.txtMunicipalityView);
        MunicipalIDText = (TextView) findViewById(R.id.txtMunicipalIDView);

        //Printer Status
        /*if (UserPortableData[9].equals("0")) setStatus("Status: Τώρα μπήκες", Color.CYAN);
        else{
            setStatus("Attempt", Color.CYAN);
            BTM.MaintainConnection(UserPortableData[9], ConnectivityStatusDashTextView);
        }*/

        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseAuth.signOut();
                finish();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });

        VehicleCatButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                setButtonTint(VehicleCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorAccent));
                setButtonTint(CleanlinessCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorTransparent));
                setButtonTint(SmokingCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorTransparent));

                VehicleCatButton.setBackgroundColor(R.color.colorAccent);
                CleanlinessCatButton.setBackgroundColor(R.color.colorTransparent);
                VehicleCatButton.setBackgroundColor(R.color.colorTransparent);

                VehicleCatButton.setColorFilter(R.color.colorLight);
                CleanlinessCatButton.setColorFilter(R.color.colorPrimary);
                SmokingCatButton.setColorFilter(R.color.colorPrimary);

                CatPreference = "Vehicle";
                databaseReference.child(user.getUid()).child("CatPreference").setValue(CatPreference);
            }
        });

        CleanlinessCatButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                setButtonTint(VehicleCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorTransparent));
                setButtonTint(CleanlinessCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorAccent));
                setButtonTint(SmokingCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorTransparent));

                VehicleCatButton.setBackgroundColor(R.color.colorTransparent);
                CleanlinessCatButton.setBackgroundColor(R.color.colorAccent);
                VehicleCatButton.setBackgroundColor(R.color.colorTransparent);

                VehicleCatButton.setColorFilter(R.color.colorPrimary);
                CleanlinessCatButton.setColorFilter(R.color.colorLight);
                SmokingCatButton.setColorFilter(R.color.colorPrimary);

                CatPreference = "Cleanliness";
                databaseReference.child(user.getUid()).child("CatPreference").setValue(CatPreference);
            }
        });

        SmokingCatButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                setButtonTint(VehicleCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorTransparent));
                setButtonTint(CleanlinessCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorTransparent));
                setButtonTint(SmokingCatButton, ContextCompat.getColor(DashboardActivity.this, R.color.colorAccent));

                VehicleCatButton.setBackgroundColor(R.color.colorTransparent);
                CleanlinessCatButton.setBackgroundColor(R.color.colorTransparent);
                VehicleCatButton.setBackgroundColor(R.color.colorAccent);

                VehicleCatButton.setColorFilter(R.color.colorPrimary);
                CleanlinessCatButton.setColorFilter(R.color.colorPrimary);
                SmokingCatButton.setColorFilter(R.color.colorLight);

                CatPreference = "Smoking";
                databaseReference.child(user.getUid()).child("CatPreference").setValue(CatPreference);
            }
        });

        FineLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(CatPreference, "Vehicle")) {
                    Intent intent = new Intent(DashboardActivity.this, FineListActivity.class);
                    startActivity(intent);
                } else if (Objects.equals(CatPreference, "Cleanliness")) {
                    Intent intent = new Intent(DashboardActivity.this, CleanlinessFineListActivity.class);
                    startActivity(intent);
                } else if (Objects.equals(CatPreference, "Smoking")) {
                    Intent intent = new Intent(DashboardActivity.this, SmokingFineListActivity.class);
                    startActivity(intent);
                }
            }
        });

        AnalyticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        ViewUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, EditUserActivity.class);
                startActivity(intent);
            }
        });


        NewFineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.equals(CatPreference, "Vehicle")) {
                    Bundle bMID = new Bundle();
                    bMID.putString("ConMID", MID);
                    Intent intent = new Intent(DashboardActivity.this, CarFinePhotoActivity.class);
                    intent.putExtras(bMID);
                    startActivity(intent);
                } else if (Objects.equals(CatPreference, "Cleanliness")) {
                    Intent intent = new Intent(DashboardActivity.this, CleanlinessFineActivity.class);
                    startActivity(intent);
                } else if (Objects.equals(CatPreference, "Smoking")) {
                    showSmokingOptions(this);
                }
            }
        });

        SettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TO BE CHANGED AFTER EXTRA SETTINGS!!!
                Intent intent = new Intent(DashboardActivity.this, BluetoothConnectActivity.class);
                startActivity(intent);
            }
        });
    }


    @SuppressLint("ResourceAsColor")
    private void showData (DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            RetrieveUserInfoFirebase RUInfo = new RetrieveUserInfoFirebase();
            RUInfo.setFname(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getFname());
            RUInfo.setLname(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getLname());
            RUInfo.setMID(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getMID());
            RUInfo.setMunicipality(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getMunicipality());
            RUInfo.setSignatureNum(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getSignatureNum());
            RUInfo.setType(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getType());
            RUInfo.setCatPreference(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getCatPreference());

            NameText.setText(RUInfo.getFname());
            SurnameText.setText(RUInfo.getLname());
            MunicipalityText.setText(RUInfo.getMunicipality());
            MunicipalIDText.setText(RUInfo.getMID());
            MID = RUInfo.getMID();
            CatPreference = RUInfo.getCatPreference();
            if (Objects.equals(CatPreference, "Vehicle")) {
                setButtonTint(VehicleCatButton, ContextCompat.getColor(this, R.color.colorAccent));
                VehicleCatButton.setBackgroundColor(R.color.colorAccent);
                VehicleCatButton.setColorFilter(R.color.colorLight);
            } else if (Objects.equals(CatPreference, "Cleanliness")) {
                setButtonTint(CleanlinessCatButton, ContextCompat.getColor(this, R.color.colorAccent));
                CleanlinessCatButton.setBackgroundColor(R.color.colorAccent);
                CleanlinessCatButton.setColorFilter(R.color.colorLight);
            } else if (Objects.equals(CatPreference, "Smoking")) {
                setButtonTint(SmokingCatButton, ContextCompat.getColor(this, R.color.colorAccent));
                SmokingCatButton.setBackgroundColor(R.color.colorAccent);
                SmokingCatButton.setColorFilter(R.color.colorLight);
            }
        }
    }

    private void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                ConnectivityStatusDashTextView.setBackgroundColor(color);
                ConnectivityStatusDashTextView.setText(statusMessage);
            }
        });
        DemoSleeper.sleep(1000);
    }

    public static void setButtonTint(ImageButton button, int tint) {
        ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(tint));
    }

    public void showSmokingOptions(View.OnClickListener view) {
        final AlertDialog myAlert = new AlertDialog.Builder(DashboardActivity.this).create();
        myAlert.setTitle("Πράξη επιβολής προστίμου Αντικαπνιστικού Νόμου");
        myAlert.setMessage(Html.fromHtml("Απευθύνεστε σε άτομο ή σε επιχείρηση;"));
        myAlert.setButton3("ΑΤΟΜΟ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myAlert.dismiss();
                Bundle bCategory = new Bundle();
                bCategory.putString("ConCategory", "Person");
                Intent intent = new Intent(DashboardActivity.this, SmokingFineActivity.class);
                intent.putExtras(bCategory);
                startActivity(intent);


            }
        });
        myAlert.setButton2("ΕΠΙΧΕΙΡΗΣΗ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myAlert.dismiss();
                Bundle bCategory = new Bundle();
                bCategory.putString("ConCategory", "Company");
                Intent intent = new Intent(DashboardActivity.this, SmokingFineActivity.class);
                intent.putExtras(bCategory);
                startActivity(intent);
            }
        });

        myAlert.show();
    }


}
