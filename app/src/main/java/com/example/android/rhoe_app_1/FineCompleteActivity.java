package com.example.android.rhoe_app_1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.app_v12.R;
import com.example.android.rhoe_app_1.FirebaseFine.FineAInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseFine.FineBInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseFine.FineBasicInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseFine.FineCInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseFine.FineDInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseFine.RetrieveFineInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseMunicipality.RetrieveMunicipalityInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseUsers.RetrieveUserInfoFirebase;
import com.example.android.rhoe_app_1.Zebra.DemoSleeper;
import com.example.android.rhoe_app_1.Zebra.SettingsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import org.apache.commons.net.ntp.TimeStamp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.Condition;

public class FineCompleteActivity extends AppCompatActivity implements LocationListener {

    //Database
    DatabaseReference databaseReference;
    DatabaseReference userDatabaseReference;
    DatabaseReference municipalitiesDatabaseReference;
    DatabaseReference tempDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private String userID, P1, P2, LocalMAC, MunicipalityIndex, OfficerName, MunicipalityShort, MID, MIDBlanks;
    private String munAddress, munBank, munBankIBAN, munDepartment, munEmail, munName, munPayAddress1, munPayAddress2, munPayAddress3, munPayName, munPostNum, munRegion, munTel1, munTel2;

    //Zebra Printer
    private ZebraPrinter printer;
    private Connection printerConnection;
    private TextView ConnectivityStatusFineTextView;

    //Basic Information
    private EditText PlateEditText, PlateCountryEditText, ColorEditText, DateEditText, DayEditText, TimeEditText, AddressEditText, FineAmountEditText, FinePointsEditText;
    private Spinner TypeEditText;
    private AutoCompleteTextView FineTypeAutoCompl, BrandAutoCompl;
    double latF = 0, lonF = 0;
    private String[] FineType, FineAmount, FinePoints, CarBrand;

    //Date-Time
    android.icu.util.Calendar calendar;
    SimpleDateFormat simpleDateFormat, simpleDateFirebaseFormat, simpleTimeFormat, simpleDayFormat;
    String Date, DateFirebase, Time, Day;

    //Location
    LocationManager locationManager;
    LocationListener listener;
    String provider;
    EditText LocationEditText;
    final int MY_PERMISSION_REQUEST_CODE = 7171;
    double lat, lng;


    //Buttons
    ImageButton FineInfoButton, FineClearButton, OCRButton, TimeStampButton;
    Button FineSaveButton, FineConfirmButton;

    //Extra Information
    private Switch switchA, switchB, switchC, switchD;
    private TableLayout tableA, tableB, tableC, tableD;
    private EditText A1, A2, A3, A4, A5, A6, B1, B2, B3, B4, B5, B6, C2, C3, C4, C5, C6, C7, C8, D1, D2, D3, D4, D5;
    private Spinner C1;

    //Tables
    private String[] FineBasic = new String[10];
    private String[] A = new String[6], B = new String[6], C = new String[6], D = new String[6];

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(provider, 20000, 1, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getLocation();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fine_complete);

        //OCR-Extra Bundles
        Bundle conOCR = this.getIntent().getExtras();
        final boolean ConditionOCR = conOCR.getBoolean("ConditionOCR");
        final Bundle OCRResultB = this.getIntent().getExtras();
        assert OCRResultB != null;
        final String OCRResult = OCRResultB.getString("OCR");
        final Bundle OCRResultCountryB = this.getIntent().getExtras();
        assert OCRResultCountryB != null;
        final String OCRResultCountry = OCRResultCountryB.getString("OCRC");

        A = new String[]{"", "", "", "", "", ""};
        B = new String[]{"", "", "", "", "", ""};
        C = new String[]{"", "", "", "", "", "", "", ""};
        D = new String[]{"", "", "", "", ""};

        //Initial Printer Connection
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                doConnection();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

        //Firebase Connection
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            //profile activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        userID = user.getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
                municipalitiesDatabaseReference = FirebaseDatabase.getInstance().getReference("Municipalities").child(MID);
                municipalitiesDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        showDataFromMunicipality(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        tempDatabaseReference = FirebaseDatabase.getInstance().getReference("TempFine").child("Temp " + userID);
        if (!ConditionOCR) {
            tempDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    showDataFromOCR(dataSnapshot);
                    if (OCRResult != null) {
                        PlateEditText.setText(OCRResult);
                    }
                    if (OCRResultCountry != null) {
                        PlateCountryEditText.setText(OCRResultCountry);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Basic Information
            //A-Printer
        ConnectivityStatusFineTextView =(TextView) findViewById(R.id.tvConnectivityStatusFine);

            //B-Fine Selection
        FineInfoButton = (ImageButton) findViewById(R.id.btnFineInfo);
        FineClearButton = (ImageButton) findViewById(R.id.btnFineClear);
        FineTypeAutoCompl = (AutoCompleteTextView) findViewById(R.id.acViolation);
        FineType = getResources().getStringArray(R.array.autoComplViolations);
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, FineType);
        FineTypeAutoCompl.setAdapter(adapterType);

        FineAmountEditText = (EditText) findViewById(R.id.etFineAmmount);
        FineAmount = getResources().getStringArray(R.array.autoComplViolationPrice);
        ArrayAdapter<String> adapterAmount = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, FineAmount);

        FinePointsEditText = (EditText) findViewById(R.id.etPoints);
        FinePoints = getResources().getStringArray(R.array.autoComplViolationPoints);
        ArrayAdapter<String> adapterPoints = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, FinePoints);

        FineTypeAutoCompl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String FineA;
                String FineP;
                if (Arrays.asList(FineType).indexOf(FineTypeAutoCompl.getText().toString()) >= 0) {
                    FineA = FineAmount[Arrays.asList(FineType).indexOf(FineTypeAutoCompl.getText().toString())];
                    FineP = FinePoints[Arrays.asList(FineType).indexOf(FineTypeAutoCompl.getText().toString())];

                    for (int i = 0; i < FineA.length(); i++) {
                        if ((Character.isDigit(FineA.charAt(i))) || (FineA.subSequence(i, i + 1).equals("."))) {
                            FineAmountEditText.setText(FineAmount[Arrays.asList(FineType).indexOf(FineTypeAutoCompl.getText().toString())]);
                        } else {
                            FineAmountEditText.setText("");
                            FineAmountEditText.setHint("βλ.(i)");
                            break;
                        }
                    }
                    if (FineP.length() > 1) {
                        FinePointsEditText.setHint("βλ.(i)");
                    } else {
                        FinePointsEditText.setText(FinePoints[Arrays.asList(FineType).indexOf(FineTypeAutoCompl.getText().toString())]);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        FineInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FineTypeAutoCompl.length()>0) {
                    showFineDetails(FineCompleteActivity.this);
                }
            }
        });

        FineClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FineTypeAutoCompl.setText("");
                FineAmountEditText.setText("");
                FinePointsEditText.setText("");

            }
        });

            //C-Car Selection
        OCRButton = (ImageButton) findViewById(R.id.btnOCR);
        PlateEditText = (EditText)findViewById(R.id.etLiscencePlate);
        PlateCountryEditText = (EditText)findViewById(R.id.etCarCountry);
        TypeEditText = (Spinner)findViewById(R.id.spCarType);
        ColorEditText = (EditText)findViewById(R.id.etColor);
        BrandAutoCompl = (AutoCompleteTextView) findViewById(R.id.acBrand);
        CarBrand = getResources().getStringArray(R.array.autoComplBrands);
        ArrayAdapter<String> adapterBrand = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, CarBrand);
        BrandAutoCompl.setAdapter(adapterBrand);

        OCRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndPrint("0", "0");
                Intent intent = new Intent(FineCompleteActivity.this, OCRActivity.class);
                startActivity(intent);
            }
        });

            //D-Location/Date Selection
        TimeStampButton = (ImageButton) findViewById(R.id.btnTimestamp);
        LocationEditText = (EditText) findViewById(R.id.etAddress);
        DateEditText = (EditText)findViewById(R.id.etDate);
        DayEditText = (EditText)findViewById(R.id.etDay);
        TimeEditText = (EditText)findViewById(R.id.etTime);
        AddressEditText = (EditText)findViewById(R.id.etAddress);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            getLocation();
        }
        TimeStampButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {
                getTimestampFull();
                getLocationButton();
            }
        });

        //Extra Information
        tableA = (TableLayout) this.findViewById(R.id.tlA);
        switchA = (Switch) this.findViewById(R.id.swA);
        tableB = (TableLayout) this.findViewById(R.id.tlB);
        switchB = (Switch) this.findViewById(R.id.swB);
        tableC = (TableLayout) this.findViewById(R.id.tlC);
        switchC = (Switch) this.findViewById(R.id.swC);
        tableD = (TableLayout) this.findViewById(R.id.tlD);
        switchD = (Switch) this.findViewById(R.id.swD);

        switchA.setChecked(false);
        tableA.setVisibility(TableLayout.GONE);
        switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkSection(switchA.isChecked(),tableA);
            }
        });

        switchB.setChecked(false);
        tableB.setVisibility(TableLayout.GONE);
        switchB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkSection(switchB.isChecked(),tableB);
            }
        });

        switchC.setChecked(false);
        tableC.setVisibility(TableLayout.GONE);
        switchC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkSection(switchC.isChecked(),tableC);
            }
        });

        switchD.setChecked(false);
        tableD.setVisibility(TableLayout.GONE);
        switchD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkSection(switchD.isChecked(),tableD);
            }
        });

        A1 = (EditText) findViewById(R.id.etA1);
        A2 = (EditText) findViewById(R.id.etA2);
        A3 = (EditText) findViewById(R.id.etA3);
        A4 = (EditText) findViewById(R.id.etA4);
        A5 = (EditText) findViewById(R.id.etA5);
        A6 = (EditText) findViewById(R.id.etA6);

        B1 = (EditText) findViewById(R.id.etB1);
        B2 = (EditText) findViewById(R.id.etB2);
        B3 = (EditText) findViewById(R.id.etB3);
        B4 = (EditText) findViewById(R.id.etB4);
        B5 = (EditText) findViewById(R.id.etB5);
        B6 = (EditText) findViewById(R.id.etB6);

        C1 = (Spinner) findViewById(R.id.spC1);
        C2 = (EditText) findViewById(R.id.etC2);
        C3 = (EditText) findViewById(R.id.etC3);
        C4 = (EditText) findViewById(R.id.etC4);
        C5 = (EditText) findViewById(R.id.etC5);
        C6 = (EditText) findViewById(R.id.etC6);
        C7 = (EditText) findViewById(R.id.etC7);
        C8 = (EditText) findViewById(R.id.etC8);

        D1 = (EditText) findViewById(R.id.etD1);
        D2 = (EditText) findViewById(R.id.etD2);
        D3 = (EditText) findViewById(R.id.etD3);
        D4 = (EditText) findViewById(R.id.etD4);
        D5 = (EditText) findViewById(R.id.etD5);

        //Final Buttons
        FineSaveButton =(Button)findViewById(R.id.btnFineSave);
        FineConfirmButton =(Button)findViewById(R.id.btnFineConfirm);

        FineSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndPrint("0", "1");
            }
        });

        FineConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndPrint("1", "1");
            }
        });
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    private void showData (DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            RetrieveUserInfoFirebase RUInfo = new RetrieveUserInfoFirebase();
            RUInfo.setFname(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getFname());
            RUInfo.setLname(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getLname());
            RUInfo.setMID(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getMID());
            RUInfo.setMunicipality(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getMunicipality());
            RUInfo.setSignatureNum(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getSignatureNum());
            RUInfo.setType(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getType());
            RUInfo.setMACAddress(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getMACAddress());
            RUInfo.setPrinterFriendlyName(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getPrinterFriendlyName());

            MunicipalityIndex = RUInfo.getMunicipality();
            MID = RUInfo.getMID();
            databaseReference = FirebaseDatabase.getInstance().getReference("Fines").child(MID);
            P1 = RUInfo.getMACAddress();
            P2 = RUInfo.getPrinterFriendlyName();
            OfficerName = RUInfo.getLname() + " " + RUInfo.getFname();
            MunicipalityShort = MunicipalityIndex.subSequence(6, MunicipalityIndex.length()).toString();

            MIDBlanks = RUInfo.getMID().subSequence(0, 1).toString() + "  "  +
                    RUInfo.getMID().subSequence(1, 2).toString() + "  "  +
                    RUInfo.getMID().subSequence(2, 3).toString() + "  "  +
                    RUInfo.getMID().subSequence(3, 4).toString();

        }
    }

    private void showDataFromMunicipality (DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            RetrieveMunicipalityInfoFirebase RMInfo = new RetrieveMunicipalityInfoFirebase();
            RMInfo.setMunAddress(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunAddress());
            RMInfo.setMunBank(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunBank());
            RMInfo.setMunBankIBAN(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunBankIBAN());
            RMInfo.setMunDepartment(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunDepartment());
            RMInfo.setMunEmail(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunEmail());
            RMInfo.setMunName(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunName());
            RMInfo.setMunPayAddress1(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunPayAddress1());
            RMInfo.setMunPayAddress2(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunPayAddress2());
            RMInfo.setMunPayAddress3(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunPayAddress3());
            RMInfo.setMunPayName(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunPayName());
            RMInfo.setMunPostNum(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunPostNum());
            RMInfo.setMunRegion(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunRegion());
            RMInfo.setMunTel1(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunTel1());
            RMInfo.setMunTel2(dataSnapshot.getValue(RetrieveMunicipalityInfoFirebase.class).getMunTel2());

            munAddress = RMInfo.getMunAddress();
            munBank = RMInfo.getMunBank();
            munBankIBAN = RMInfo.getMunBankIBAN();
            munDepartment = RMInfo.getMunDepartment();
            munEmail = RMInfo.getMunEmail();
            munName = RMInfo.getMunName();
            munPayAddress1 = RMInfo.getMunPayAddress1();
            munPayAddress2 = RMInfo.getMunPayAddress2();
            munPayAddress3 = RMInfo.getMunPayAddress3();
            munPayName = RMInfo.getMunPayName();
            munPostNum = RMInfo.getMunPostNum();
            munRegion = RMInfo.getMunRegion();
            munTel1 = RMInfo.getMunTel1();
            munTel2 = RMInfo.getMunTel2();
        }
    }

    public void showFineDetails(FineCompleteActivity view) {
        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage(Html.fromHtml("<b>Περιγραφή Παράβασης</b> <br>" + FineTypeAutoCompl.getText().toString() + "<br>" +
                "<b>Χρηματικό πρόστιμο (€)</b> <br>" + FineAmount[Arrays.asList(FineType).indexOf(FineTypeAutoCompl.getText().toString())] + "<br>" +
                "<b>Βαθμοί Σ.Ε.Σ.Ο.</b> <br>" + FinePoints[Arrays.asList(FineType).indexOf(FineTypeAutoCompl.getText().toString())] + "<br>" +
                "<b>Αφαίρεση ΣΚ</b> <br>" + "TBA")).create();
        myAlert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTimestampFull() {
        calendar = android.icu.util.Calendar.getInstance(TimeZone.getTimeZone("EET"));
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        Date = simpleDateFormat.format(calendar.getTime());

        simpleDateFirebaseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");
        //simpleDateFirebaseFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        DateFirebase = simpleDateFirebaseFormat.format(calendar.getTime());

        simpleTimeFormat = new SimpleDateFormat("HH:mm");
        //simpleTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        Time = simpleTimeFormat.format(calendar.getTime());

        Locale locale = new Locale("el-GR");
        simpleDayFormat = new SimpleDateFormat("EEEE", locale);
        //simpleDayFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        Day = simpleDayFormat.format(calendar.getTime());

        DateEditText.setText(Date);
        TimeEditText.setText(Time);
        DayEditText.setText(Day);
    }

    private void getLocationButton() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location myLocation = locationManager.getLastKnownLocation(provider);
        lat = myLocation.getLatitude();
        lng = myLocation.getLongitude();
        new GetAddress().execute(String.format("%.4f,%.4f",lat,lng));
    }

    private void checkSection(boolean condition, TableLayout table){
        if(condition){
            table.setVisibility(TableLayout.VISIBLE);
        }
        else {
            table.setVisibility(TableLayout.GONE);
        }
    }

    private boolean extraInfoChecker(int length, String[] table) {
        boolean b = true;
        for (int i = 0; i < length; i++) {
            if (!table[i].equals("")) {
                b = false;
                break;
            }
        }
        return b;
    }

    public void saveAndPrint(String Print, String allComplete) {
        FineBasic = new String[]{PlateEditText.getText().toString(),
                TypeEditText.getSelectedItem().toString(),
                BrandAutoCompl.getText().toString(),
                ColorEditText.getText().toString(),
                DateEditText.getText().toString(),
                FineAmountEditText.getText().toString(),
                DayEditText.getText().toString(),
                TimeEditText.getText().toString(),
                AddressEditText.getText().toString(),
                FineTypeAutoCompl.getText().toString(),
                PlateCountryEditText.getText().toString(),
                FinePointsEditText.getText().toString()
        };
        A = new String[]{A1.getText().toString(),
                A2.getText().toString(),
                A3.getText().toString(),
                A4.getText().toString(),
                A5.getText().toString(),
                A6.getText().toString()};
        boolean conA = extraInfoChecker(6, A);
        B = new String[]{B1.getText().toString(),
                B2.getText().toString(),
                B3.getText().toString(),
                B4.getText().toString(),
                B5.getText().toString(),
                B6.getText().toString()};
        boolean conB = extraInfoChecker(6, B);
        C = new String[]{C1.getSelectedItem().toString(),
                C2.getText().toString(),
                C3.getText().toString(),
                C4.getText().toString(),
                C5.getText().toString(),
                C6.getText().toString(),
                C7.getText().toString(),
                C8.getText().toString()};
        boolean conC = extraInfoChecker(8, C);
        D = new String[]{D1.getText().toString(),
                D2.getText().toString(),
                D3.getText().toString(),
                D4.getText().toString(),
                D5.getText().toString()};
        boolean conD = extraInfoChecker(5, D);


        if (allComplete.equals("1")) {
            if ((FineBasic[0].length() != 0) &&
                    (FineBasic[1].length() != 0) &&
                    (FineBasic[2].length() != 0) &&
                    (FineBasic[3].length() != 0) &&
                    (FineBasic[4].length() != 0) &&
                    (FineBasic[5].length() != 0) &&
                    (FineBasic[6].length() != 0) &&
                    (FineBasic[7].length() != 0) &&
                    (FineBasic[8].length() != 0) &&
                    (FineBasic[9].length() != 0) &&
                    (FineBasic[10].length() != 0) &&
                    (FineBasic[11].length() != 0) &&
                    (latF != 0) &&
                    (lonF != 0) &&
                    DateFirebase != null)
            {

                FirebaseUser userFirebase =firebaseAuth.getCurrentUser();

                FineBasicInfoFirebase fineBasicInfoFirebase = new FineBasicInfoFirebase(FineBasic[0],
                        FineBasic[1],
                        FineBasic[2],
                        FineBasic[3],
                        FineBasic[4],
                        FineBasic[5],
                        FineBasic[6],
                        FineBasic[7],
                        FineBasic[8],
                        FineBasic[9],
                        userFirebase.getUid(),
                        FineBasic[10],
                        FineBasic[11],
                        "No",
                        latF,
                        lonF);

                databaseReference.child(DateFirebase).setValue(fineBasicInfoFirebase);

                if (!conA) {
                    FineAInfoFirebase fineAInfoFirebase = new FineAInfoFirebase(A[0], A[1], A[2], A[3], A[4], A[5]);
                    databaseReference.child(DateFirebase).child("Fine A").setValue(fineAInfoFirebase);
                }
                if (!conB) {
                    FineBInfoFirebase fineBInfoFirebase = new FineBInfoFirebase(B[0], B[1], B[2], B[3], B[4], B[5]);
                    databaseReference.child(DateFirebase).child("Fine A").setValue(fineBInfoFirebase);
                }
                if (!conC) {
                    FineCInfoFirebase fineCInfoFirebase = new FineCInfoFirebase(C[0], C[1], C[2], C[3], C[4], C[5], C[6], C[7]);
                    databaseReference.child(DateFirebase).child("Fine C").setValue(fineCInfoFirebase);
                }
                if (!conD) {
                    FineDInfoFirebase fineDInfoFirebase = new FineDInfoFirebase(D[0], D[1], D[2], D[3], D[4]);
                    databaseReference.child(DateFirebase).child("Fine D").setValue(fineDInfoFirebase);
                }

                //Printer
                if (Print.equals("1")){
                    new Thread(new Runnable() {
                        public void run() {
                            Looper.prepare();
                            doConnectionTest();
                            Looper.loop();
                            Looper.myLooper().quit();
                        }
                    }).start();

                }

                tempDatabaseReference.setValue(null);
                Intent intent = new Intent(FineCompleteActivity.this, DashboardActivity.class);
                startActivity(intent);

            } else {
                toastMessage("You must complete all the fields!");
            }
        } else {FirebaseUser userFirebase =firebaseAuth.getCurrentUser();

            FineBasicInfoFirebase fineBasicInfoFirebase = new FineBasicInfoFirebase(BlankField(FineBasic[0]),
                    BlankField(FineBasic[1]),
                    BlankField(FineBasic[2]),
                    BlankField(FineBasic[3]),
                    BlankField(FineBasic[4]),
                    BlankField(FineBasic[5]),
                    BlankField(FineBasic[6]),
                    BlankField(FineBasic[7]),
                    BlankField(FineBasic[8]),
                    BlankField(FineBasic[9]),
                    BlankField(userFirebase.getUid()),
                    BlankField(FineBasic[10]),
                    BlankField(FineBasic[11]),
                    "No",
                    BlankFieldL(latF),
                    BlankFieldL(lonF));

            tempDatabaseReference.setValue(fineBasicInfoFirebase);

            if (!conA) {
                FineAInfoFirebase fineAInfoFirebase = new FineAInfoFirebase(A[0], A[1], A[2], A[3], A[4], A[5]);
                tempDatabaseReference.child("Fine A").setValue(fineAInfoFirebase);
            }
            if (!conB) {
                FineBInfoFirebase fineBInfoFirebase = new FineBInfoFirebase(B[0], B[1], B[2], B[3], B[4], B[5]);
                tempDatabaseReference.child("Fine A").setValue(fineBInfoFirebase);
            }
            if (!conC) {
                FineCInfoFirebase fineCInfoFirebase = new FineCInfoFirebase(C[0], C[1], C[2], C[3], C[4], C[5], C[6], C[7]);
                tempDatabaseReference.child("Fine C").setValue(fineCInfoFirebase);
            }
            if (!conD) {
                FineDInfoFirebase fineDInfoFirebase = new FineDInfoFirebase(D[0], D[1], D[2], D[3], D[4]);
                tempDatabaseReference.child("Fine D").setValue(fineDInfoFirebase);
            }

            //Printer
            if (Print.equals("1")){
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        doConnectionTest();
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();

            }

            Intent intent = new Intent(FineCompleteActivity.this, DashboardActivity.class);
            startActivity(intent);
        }
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final Location location = locationManager.getLastKnownLocation(provider);
        if(location == null)
            Log.e("ERROR","Location is null");
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        new GetAddress().execute(String.format("%.4f,%.4f",lat,lng));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

    private class GetAddress extends AsyncTask<String,Void,String> {
        ProgressDialog dialog = new ProgressDialog(FineCompleteActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                double lat = Double.parseDouble(strings[0].split(",")[0]);
                double lng = Double.parseDouble(strings[0].split(",")[1]);
                String response;
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%.4f,%.4f&sensor=false&language=en",lat,lng);
                response = http.GetHTTPData(url);
                return response;
            }
            catch (Exception ex) {}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                JSONObject jsonObject = new JSONObject(s);

                String address = ((JSONArray)jsonObject.get("results")).getJSONObject(0).get("formatted_address").toString();
                LocationEditText.setText(address);
                latF = lat;
                lonF = lng;

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(dialog.isShowing())
                dialog.dismiss();
        }
    }

    //Printer Proccessing
    private void doConnection() {
        printer = connect();
    }

    public ZebraPrinter connect() {
        setStatus("Σύνδεση...", Color.YELLOW);
        printerConnection = null;
        printerConnection = new BluetoothConnection(getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());

        try {
            printerConnection.open();
            setStatus("Εκτυπωτής: Συνδεδεμένος", Color.GREEN);
        } catch (ConnectionException e) {
            setStatus("Σφάλμα Επικοινωνίας! Αποσύνδεση", Color.RED);
            DemoSleeper.sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus("Εντοπισμός Γλώσσας...", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus("Εκτυπωτής: Συνδεδεμένος [" + pl + "]", Color.GREEN);
            } catch (ConnectionException e) {
                setStatus("Εκτυπωτής: Άγνωστη Γλώσσα", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus("Εκτυπωτής: Άγνωστη Γλώσσα", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

    public void disconnect() {
        try {
            setStatus("Αποσύνδεση...", Color.RED);
            if (printerConnection != null) {
                printerConnection.close();
            }
            setStatus("Εκτυπωτής: Μη Συνδεδεμένος", Color.RED);
        } catch (ConnectionException e) {
            setStatus("Σφάλμα Επικοινωνίας! Αποσύνδεση", Color.RED);
        }
    }

    private String getMacAddressFieldText() {
        return P1;
    }

    private void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                ConnectivityStatusFineTextView.setBackgroundColor(color);
                ConnectivityStatusFineTextView.setText(statusMessage);
            }
        });
        DemoSleeper.sleep(1000);
    }

    private void doConnectionTest() {
        if (printer != null) {
            //sendFineLabel(createZplFine());
            //sendFineLabel(createZplFineBack());
            //sendFineLabel(createZplQR());
            sendFineLabel(MunFine());

        }
    }

    private void sendFineLabel(String Doc) {
        try {
            byte[] configLabel = getConfigLabel(Doc);
            printerConnection.write(configLabel);
            setStatus("Sending Data", Color.BLUE);
            DemoSleeper.sleep(1500);
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                setStatus(friendlyName, Color.MAGENTA);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            setStatus(e.getMessage(), Color.RED);
        }
    }

    private byte[] getConfigLabel(String Doc) {
        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;
        if (printerLanguage == PrinterLanguage.ZPL) {
            configLabel = Doc.getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }

    private String MunFine() {
        String tmpFine1 =
                "^XA" +
                        "^MMT^PW561^LL0440^LS0" +
                        "^FO96,32^GFA,03072,03072,00048,:Z64:eJztkzFu3DAQRUdgwXIuIHCu4WJBHSetShVKTGALdd7aQLA5R5pEW/kSLgSkSMpxN8EKpIeUtJsDpEmg3wggPh4+P78Adv1jwmB+2eAbQZMEJ8AhMLD3ZvIBYyPsmwhNdNAtfgs421ATY5WYBKzN/rquxjTSTMw1zUCzg3bjN9EER6J+UVThi/PmmKbHSCJu5cvGP5Dy7YhQce9WvnP2eOFej9lBDVD/we9wePImIJhJ6JbfDqOIHrMHD+Ddwy1/Z4dTnf0Kwy0/6bdr9ZjrhV/f+GKPp3P2Y2AMacmPeApd5otb+F42f2tfvpT8VLG1acmPaKF/nO/5P618GsUO379lvocJT4WfIj7pxXJz7Kn0E7vN39rh+TX7a+VjyZ9m+1kvRnPOv/T/YeU3k+b/GrOfNP8ZF745K/9Q8iufovu55v84aT9vc86f+3Fr//ZV+znc+6cXXvwda/8ct/792r+JdpSHe//4Y+W3rO/Lffbn96W1/0rMhQ/3/umy8llHY6ZI1/Q77wfTkHL/6k9To/tJseyHjgtfmbrPsac39TOx9p9y/kovXfaZYulH36b4dea6/8k313TN+zeFr6EFlv1v/CB/42/btWvXrl27/kO9A2TRKnc=:B6BA" +
                        "^FO64,32^GFA,03584,03584,00056,:Z64:eJztUzFu3EAM5JoFO28eIGifkPYCCNKXUiqA4ltHhTvfD+4d7rLKFfcJF0z8AG+6LYRluJIcJA9IE2iuOQqkRjNDAuzYsWPHjh07/jlQUmy73OIPeQktNIlOyCgv3B6zdK850RcZxR8Z1jbGkZPOGYmxclKZSaZQQdPTyQQjP7k5zkd3nXt6kItMEkHbcmUCTuHjMpdSXV70oBwHOBzs6V75vnMjWb6+zlor3ygMa5vHMRQ+MDHWUNf0ME3cwzCQNQxG52DAd8BaU6BrUL4aoCZPl4UPkGMLd52cxhdJcOjEIgOVuWRb4E5uMeAlFH0AnXi8LPqAQqwgOSFTRDROrAlg77kx0TmITpzOLXwVgBNQfX2Zsz7VGK3ysSSbXLYjgzPcILfWc6mLpqJPm8XLpk/fWWOwliTE6Oa6LvoanaPQELAql0CLn2XOGtn0tSDZBvuIwomP821b9GXUh/6OPN+2qDl8Uz8ld9k+kmz6KpBZHTsbCZ+4b2xV9A1GH/qefLSV0fTCMWqbm+lM45ofOC9Zt+CsPn3gdLBP6gm0Ood8QM/2CcdR7rnwuYxnO2761M/aRHpWnz6HOFBPqk81qsSBAmutXzNt+dEz/ZVfwln5Oo4HSkWfnUp+xU+tR82P1/ww27f8dF8qqMysfC70DcWiD8vcUKnXFEnnJl7zM/Obvm0/MamfnZeEycq6n13WHcilVn3bfq7V2z3ULpuofjo4zmUFeLsH0h9Gkut6D0ub5qD/f98fJBC2/r26uPAt96dvV6tv/rg/SDcb344d/yF+AelMtWs=:2DA4" +
                        "^FO384,96^GFA,03840,03840,00020,:Z64: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:041F" +
                        "^FT382,143^A0I,20,19^FH^FD" + GreekConverter("Απ. Σαμανίδη 21 ΤΚ 552 35 ΠΑΝΟΡΑΜΑ") + "^FS" +
                        "^BY2,3,80^FT515,320^BCI,,Y,N" +
                        "^FD>:]>5100760>68[>5150417>64[16]^FS" +
                        "^FT382,122^A0I,20,19^FH^FD" + GreekConverter("ΤΗΛ: " + munTel1 + " - " + munTel2) + "^FS" +
                        "^FT382,101^A0I,20,19^FH^FD" + GreekConverter("email: " + munEmail) + "^FS" +
                        "^FT382,213^A0I,23,24^FH^FD" + GreekConverter(munRegion) + "^FS" +
                        "^FT339,15^A0I,23,24^FH^FDNo 000000^FS" +
                        "^FT382,187^A0I,23,24^FH^FD" + GreekConverter(munName) + "^FS" +
                        "^FT382,165^A0I,20,19^FH^FD" + GreekConverter(munDepartment) + "^FS" +
                        "^FT382,239^A0I,23,24^FH^FD" + GreekConverter("ΕΛΛΗΝΙΚΗ ΔΗΜΟΚΡΑΤΙΑ") + "^FS" +
                        "^PQ1,0,1,Y^XZ";
        String tmpFine2 =
                "^XA" +
                        "^MMT^PW561^LL0112^LS0" +
                        "^FO256,64^GFA,02560,02560,00040,:Z64:eJztkjGOGzEMRSmoYGddQB5dw8VAupJKFQNYxhYpc4MgJ1lr+yAn2ILAXoDbqRiMQskZB7lAmsyHG2Eevj/5CXDoX8i1/ArmhSYPVL3X5Z6Bzb22D/KBzHdFoT041QAVTRZitBOWa4GIjtsnz4EC71xoL40MkBc/9s7IC+op1EZ1CdSqcNvjj/GNjGJrgdkap98Y0qSz4pQQeFEF7OC8ubHJbfUXrt58M7lC27SEnKt88YpgHdxigBGuq00cJ3zHnOSBBdhxjy0cD64KZ6BVXyWfXo1wofuRo74GTXpwih1EAynZRfKpipBgnns+V1AVp4oundPkxDJfqt+4bar2fP6ic5PQ3U+RycNPuIgwx/OPeF273wJT0uXK2P2Ew7Lnk5+n8JNkzpHvlMTvdz5FmgaXxhxncl8kH659Xhd7vvNj3qL4z/7AUfja9/fa85n61/7qsw8EVxxytDj6QO77W+Log2DumJF+P1o1FLTs7zT6lQdLv6n3G9p92+/lJpxsSvJZvLUCWNznfi+hubbfnxYue/28P03heX+736FDhw4d+j/0C3dUEYQ=:CE9A" +
                        "^FO128,0^GFA,01024,01024,00016,:Z64:eJzdkD1ug0AQhWc8a0DyBq+7KSI5uQElJUdIbsARSOcisrFBhALBFXKUPUAukI4yZUoqCAq74D5Forzu0+j97AL8d6GAHN0XraLgm6mACppGcxybO0pMc+AkmgwrqvBcA4fpxDSIeyRQOxNIvby7UMobW7CSTzmeO9dyLd9rbDrfIObicdygzJ3SWpY1lLyZ8hHebtY5lrwzffCxbQoqObD8sHbJFSo2++DgU9ZvO7MfIBj3H7DTwc8+6reE2YCf+/75pGfO+n3fn1rDmhxHCQWxdTg+eT4vLD2UgiGyzEyVx9BaDhQKTy332KOCec5PE0ZH8dyfhh5dwtmP+lYiBVd+3yd6veon18Fo6adxP7ZHmw84QAJ6ft+f0hdwAkuM:F7AD" +
                        "^FO384,0^GFA,01536,01536,00024,:Z64:eJztk7FKA0EQhmd2lrsrjnWFIFOIYmM9MSkOEU3A3lc40NIiDyDkwolaWafMI1imjGhhIZrS8ipLsbwiGBc1ObKVjYXoFAf78TH8NzsL8F8/U9qqVMgq0YvcJIEIuU/g+aytIGvfh8iwUGREeTycaKFwIqceXyoDoaVSzvxATJ2u5vqtxwOmq+kZN549PbBYvwns1pGXX4lq5Eq2Nj2fRNcJpVnz+tMgbuQ02FnzOKa2nmPaXPE4FCJTKBLj+6NUujhK/Pn89UJkFI5jDTbMJtjHL075BSUcGQPJXlHiBWUzX9Oq1WEAVjriDvNORhmOyO1NmgheVvumdcgxuj1rseD525wrQ8MIXgBG0SLX+Krh2Pla1lV1XxRDaWAX4PFOunk8yw8YwVMNmgAH99LuRTM7oz71t7/69/J+5VuyG3D7kecabTWKYcCH+Wf+Xjas+Djmdu5yuP+9zsYVL5izB/cO3XymWVHxVJZh3/lh62Qd0u9cwm+td3XJUw4=:6AC8" +
                        "^FT143,39^A0I,23,24^FH^FD" + GreekConverter(FineBasic[1]) + "^FS" +
                        "^FT143,15^A0I,23,24^FH^FD" + GreekConverter(FineBasic[3]) + "^FS" +
                        "^FT409,15^A0I,23,24^FH^FD" + GreekConverter(FineBasic[2]) + "^FS" +
                        "^FT409,39^A0I,23,24^FH^FD" + GreekConverter(FineBasic[0]) + "[" + GreekConverter(FineBasic[10]) + "]" + "^FS" +
                        "^PQ1,0,1,Y^XZ";
        String tmpFine3 =
                "^XA" +
                        "^MMT^PW561^LL0264^LS0" +
                        "^FO384,96^GFA,01920,01920,00020,:Z64:eJztkj9OwzAUxu0kJA/JSs2WIQL3Bq5YKgaaXIAzpBdAZWNAihuqqkOFGBmQegUOwNAAqhg4REYGhnbrEKnkj2Oatiuw9FNkWT99+t7ze0For1+RhoS8ccUMNJU3qhgo3w8jO3wNIURic3anPy0bScmsRXy77KStlbVIO/OqnIhIc8xegduMVTHxzOgOjuIHagY9yRwxNJtD1n/gpvI58cDoGq3MdxNw5RsUvhG93shrxda98VP3NQIG7Fa/t1V/83CypE5rZRylVObpoj1JwGHPhH6qPPVwinhvgwFHnNcIR6SHTuY1xlBWtKor1UPWFG1os+IfCItsydlO81MxhNyCuTUG1bfGnPy/yc9CF5qGo8N3G32dH74HrGDh0MK4kYZWGjZSr5xfEOl9nbS7Q9DHbVbOz58d9LHr+G8GfnS8knkvpB8BTMcEj0DmeTMSY+JOP8gJcWWeF4GvA4gRHAPIvfkz2sUkyyPZGZb78CPazPLyuiMIk7I/k16tMoe1DIkj8y41xk8BbJScqbyaPL7NZH/5PLZ82hpjO3yVzB1sr73+Sd/itm90:3CFB" +
                        "^FO256,160^GFA,00256,00256,00004,:Z64:eJxjYBhg0ADELkwCDHnMDxhqGA8w2ADpOvYPzP/7fzEIMBxgkACqEKCd9QA12Qn8:DEAC" +
                        "^FO32,160^GFA,00256,00256,00004,:Z64:eJxjYKAvqALi+gNA/ICBgfEDkFMBxDUMDPz/GBjk/0FoBhkIlv/PwGAP5NsDaQYLiFqQnnoQbgBroxgAANcAEJE=:A190" +
                        "^FO512,160^GFA,00512,00512,00008,:Z64:eJxjYBgFaICRA0p/gAo8QOUz/4DQ7H+h8glQ2oHWLqMBAABr4QVr:9FD6" +
                        "^FO0,0^GFA,09216,09216,00072,:Z64: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:EA58" +
                        "^FO128,160^GFA,01920,01920,00020,:Z64:eJztkjFugzAUhm0h4QXZOcCrewVGR0LhLL0BTM0QtakYvJELVPQqRQwe0wNkqMTA2qEDQ+XUwSXBgR6gFf/ip0+///f0bIRmTWtBEAJzQnhhobdF3JxcXFhiWOcbsHbM4NEjwcEHfAjTUvkdo3EmmY410yKt99T6cCUBloqBSEtpGcf1Lod1XYBo651lCDckgFQBhElD/J8utaQ8qQsukubsO+Wl6hlEck+GecumiESrWW773lUyAEGo8T2Q83xHuoooa+OPFdPWd4NNTwD8CgkQPVpbNLFKGGywL+hgg33BBxvsC7Yd+/6DNmiN3wQ47Jhp1go+RFhlkkLkMK+SMueCOuxJEfPETp6X7TPzFdy7pSIAkeubyMPVqe/1fC+j+dDm9hO/u3lWbp5V/OVaOub+ocVvvuv4CTZr1h/TN6H3ZY4=:A393" +
                        "^FO416,160^GFA,01536,01536,00016,:Z64:eJztkbFKxDAYx/MlH5cMHzRjhxt6+ALtVnBoBB/k+gY+gHDVgDqIs48T0NGHCJzg6uhwXE1z2ksHQXERud/Q8OfPr//QMvYPgd2Bn1nsDvqy78Ye2JmEChHcHJ6LJRN2m617UvK1sZuiYCAwby0i5ifXWJrgzuqFvVWk/B2FnvGZrgCRyJxi8BnnZWVJKeWeYi8w9orMY/Q5//Bd9CH4behRt1fzYf+SZ2tLfR/23xo/7EsAXPXhfq5x3/wg6oeZ2F9HsnNwzow589vMd37Mun2g0izHXB/Zm7rb5/ATcp349fHFfZ76FWht9n25mPr58P50/2W6L+UGXOJHEj+S+Hx49H6aV26aEwQ7cOD3vAPVAkg2:61BB" +
                        "^FT359,127^A0I,28,28^FH^FD" + GreekConverter(FineBasic[4]) + "^FS" +
                        "^FT425,186^A0I,31,31^FH^FD" + GreekConverter(FineBasic[5] + "€") + "^FS" +
                        "^FO24,172^GB0,51,2^FS" +
                        "^FO259,172^GB0,51,1^FS" +
                        "^FO170,114^GB0,50,1^FS" +
                        "^FO301,172^GB0,51,1^FS" +
                        "^FO535,114^GB0,51,1^FS" +
                        "^FO535,172^GB0,51,1^FS" +
                        "^FO170,113^GB366,0,1^FS" +
                        "^FO24,172^GB236,0,1^FS" +
                        "^FO170,164^GB366,0,1^FS" +
                        "^FO24,222^GB236,0,1^FS" +
                        "^FO301,172^GB235,0,1^FS" +
                        "^FO301,222^GB235,0,1^FS" +
                        "^PQ1,0,1,Y^XZ";

        String tmpFine4 =
                "^XA" +
                        "^MMT^PW561^LL0230^LS0" +
                        "^FO25,50^A0I,23,24^FH" +
                        "^FB500,7,,J," +
                        "^FD" + GreekConverter("1. O/H " + OfficerName + " κατέβαλα τον/ην ανωτέρο οδηγό την " + FineBasic[4] + " ημέρα " + FineBasic[6] + " και ώρα " + FineBasic[7] + " στη(ν) " + FineBasic[8] + " να διαπράττει την/τις σημειούμενες με Χ παραβάση/σεις, για τις οποίες σας επιβάλλεται διοικητικό πρόστιμο" ) +
                        "^FS" +
                        "^XZ";

        String tmpFine5 =
                "^XA" +
                        "^MMT^PW561^LL0060^LS0" +
                        "^FO224,0^GFA,02816,02816,00044,:Z64:eJztkrGK3DAQhkeoUHfzAsZ6DRc++5U2pFm4JdbiwqUf4e4xUo7ZYsu8wiwuUp6OKyK4ZScjr+9ImRSBBPY3CGN+ffr1ewBuuummf1Fefk/pD71/S/jYnxifygYDJEiGQZLMMspz2EqQmRqoEo49q9e5/cRtLEsHRvQh6M7dQY5yDBvZy4FqqLfO6Xf1DoeZJZUeg5VB+sztZn37HpKuM1dQbXG0C7efJoq7AgfdNgx73b/1YMkdaFPombSFuna4cMHObLgBHAMOY79nMPEObMBeYoP9ScuqWrnmBfNChktwR43u7MRgWb3kjXDhzCQRdv7izOKFN7bqtc+Zaw8MSJnbWGHlskRM7QXt1duRIa8lKHewEwFCzrszJyqcUIz+XBZrXmgXLkjAccnbwp2VkMwbZ3bSknw+YPHeK1e9P3Le3IPPXtrACxUg9Ik3NRZu5T6859XO5PECGDL3C7xq5pkfOFb49T3vtQebe9iLP4OjnNfD51Bo/HuKtavWvOZ16dd90x5C8pdrZwGDKLfnllKFac2r94W4U+jHOG1yv446yFxPm52L7uO/6QgA6pxdtc4Dt0H6vHbJJpSF6/TqbYTyg9ulZc6oBZ0z8qE924iycHMUfILmF+4yv3oHnV/2AZPllXvTTTf99/oJ5S91+w==:BA6E" +
                        "^PQ1,0,1,Y^XZ";


        String tmpFine6 =
                "^XA" +
                        "^MMT^PW561^LL0120^LS0" +
                        "^FO25,50^A0I,23,24^FH" +
                        "^FB500,3,,J," +
                        "^FD" + GreekConverter("X " + FineBasic[9]) +
                        "^FS" +
                        "^XZ";

        String tmpFine7 =
                "^XA" +
                        "^MMT^PW561^LL0480^LS0" +
                        "^FO512,320^GFA,00512,00512,00008,:Z64:eJxjYKA1kOF+AKHlP4BpG3sILVMjAFFgAVVYA6XtGyC0PJTmb0CVh6m3geq3fwBVDzGXgXkBFVw9UgAAIdQKuA==:BFA3" +
                        "^FO512,224^GFA,00512,00512,00008,:Z64:eJxjYBjqQEYGlbaB8et/QBhw+g9UwgJKF0DpBCh9AEo3QGh5GM2ASstRw9WDBQAAle8JHQ==:B2DF" +
                        "^FO512,416^GFA,00512,00512,00008,:Z64:eJxjYBhuQKb+AZT+AKZt7CG0DCNEnIG5AULzQzXwwTRCaQsoXYPGtxGA0PZQc+Q/oJo3JAEADz0KFA==:857F" +
                        "^FO0,96^GFA,02560,02560,00040,:Z64:eJztkr9qVEEUxs/MLDPjcrg7QYgny5gdN41FwIlYpAhmschzXEihheAtQqrFnaigRZ7CV/AFxgja5BEsLrGx3C5bXHI9975EEO7XDAzf+fidPwCDBg0a9J9Itekv4fsgVU2ooVzdQJ5tJimf1oG/pNXHDRSJfZ8bj8tKG35G7LtlXzNbl6fryjSkUK8aWGUO1LqwFCTirtUQYA8SUIwhpIDIeTJaKEsAg8YgRU16e8R58F0kiBW5MkfSpLx2HnIEQKvUJYUPJKcdn7hoE9dbW+ZAkl5ZTRYS+7wX4rc/+OnMo47PCPbl7JHznPG/UHsnsgOIVqQNPbmKcstKCBIuQKRkZZ1ClHRjpXWQCKAiAUs6OIlq65OBEuEHKFig4ryodr5N7jCKxHmB897a+TxIuhxDWcAVqJStZL4gH3xlX+jzIvNd0/m5M+76MfPBCQjmG5W5cmbnIepR2fNZq8QXmj+P0r2JKag0B6i539DxTcdWytD3q9EopBfbpN1ZzFx7xswVUT+/2cxrs+jnJ4tCIz0d8/D3Qy6h3gegwxiYD3FaWC0z1DWI1jSIy6Nd0zi/eNeu1kfQ7/f1bbd5g9rkbr+qhTXZl4eFqh2lZ3fHNcFkwx3v/Wk/dvcieY+T+77qQYPuRf8AztyLzQ==:6B0F" +
                        "^FO384,96^GFA,01536,01536,00024,:Z64:eJztkTFOxDAQRcfYSoIUst4uVAkXQEZQpItBSHsNIwpaCgq6zGovNisuMoC0dcpUmAG8LAegQGh/+fT953sGYK+9/pX6kYILptmAn+McPDwkPmFwbTW8ANd4jqTf0gNL3trSXwJZfwakdeJB/F1Bt4g1nyBlCr84SX4z0k1EOzwDmcQVcnD9xMK74RW5gsRB8gvr/VKNToYYoE+eQWRXup6fZpNbI5XJn39w0zZ8Zaxd7/wapU/VtmFT1t0K6TufJN94768Le6x2fYDFf8BtOC3qw+WP/vfSX/sQLkp7JP483+6H7uKKeu5mU6Ujb/cjX11EhQ3VasxUJHj8lTPt9Rf0Dn29YjY=:D693" +
                        "^FO64,256^GFA,08192,08192,00064,:Z64:eJztVzFv20YUfjxe7hjgIFPIEAZgo7MNBB2C4DxFgxHKNRr1R3Q42UGy0mjRZHArihJUD0HQMUOGjB37AzqcJMNaMvQHdKDjJaO8eRDsHmVKpBNScpEOAaoPAnR8x0/ffffI904ASyyxxBJL/LewAQwAYl8JCpCTb2IbLTWfzwGQ/tgfByfQ4dYC/Vq+fi3Rh8ECfqLvfBRUl/oO9Bfw96n6uTL8ybaNHmWu7W8pPJICxlSJ51SHn1A1l++tROeVwbktoI1KluBRQM+4gEc04l5bh+/TaC7f+G1vYG+TiT6e6FNH+mC6Qog4vOmK+QaC1RB2kMNnAWzx78A4cDiPw47lFHNjqB8MqGuhlM/k92C8sYX/TIddx55D1mifIBoiO12l1j8A47XWX9Phr50F+oOBSQdX9RsdMBxbiF0dfrJIP1hH0EfOFf1u6v+bBf6/Pdwb2kPi+infbRCou77Yi8PDzEweJvkP0cofswgd6RX9Qk/OvTjcotFcvkvVi9smoanLG6NdE/bNwcVDHS4bbTWX/yUAfSYfF84Y1+KTwpnrrayYT6/F/1/Ayo1eP/X5SS5O/cdgudH8VV2fnx/9BOYr+PAIwQnwdYCoBMF0YiU4K+ni88oIQmMWtPg9ayVEsI5WgkjFS6QX5rhpmhfK95R5VlG16a30dFwZuOULQ22nfFfWcaXXBW9Q6Y1rcf0qrXaZGSJHveWKMK5mt4Io8X6JD5DqZ/V7L0UUAu/De8ZjPts7xEab2Ft/+TXM/Jqa8W0it2m592ttmPLB7x368gj8bXiKfXvyiweWgXQ5fc05sThPb3UIXzNFK+QZ/yCCD5zfA74GO8QbxfriEFdMre8IibGUtZS/L3eN01ZHZvxDOdgX8hGIXXqU8PlLdlfrNywuMeMyo/9C7sDb1kue8Q83g/u6+wDfoV0y6Yxs4x27QYlTZ7F/mfWPZV37ZyLr/yt458un4NfhMXYlxI951zIRglDvPbLE1f0PkWhbIuu/BAecX+6/ZcX61KCjpndqHGFPYVwZjqd36vw/bOv8O3bWP6VY7E7yP3jhxnulpaNNL2p10APdH0srZ9M7zeCDfv74n46V9U/0M7KG4AEqBRFLveYjeQVolp/OqhvvFvCTEoAy/ExV4OQgGRX1caM9+TIvUr6ZzjrUTUZFr7oXzuGbZ+aU50I+msNPlzQb0VmmoPAc8bntb4l/Cc7nzV62lGBObZdzj3QTvjGPP18fTfULe5Oce6Sf6Rf1xoBzXjUDVOW691R1sYn0dVycz4DhSHVYyYw2w1uBblgqh24o6TfGFUXHG+O4EjV7Y32NiV0e69PoWUNX6uGo2a1EF8NcPgQRX+063GSCiAED3mb6GumzPoG7ujl0LRFUo9BZPThJ++SVBYxEg9pC1zji1zHU6lhfa32bwG3sljtY1O70uvYG3jNy9eG1Lm6CI8KZ3CEg+7oFodg/gzXMbnZ0jXaC0F5jq5mSkNV/4z+jvqBE7MtnBGpHRF/H+vvwHLOVQya3TrX+c7ZXwLcu9RGvxlVW13++jmL/VVjFlhVafDXS+usWz//zqzduV/s3scD+duLfjPUxbHXcOx3cVOWJf5H/5xcxcdx1JGKc8T6DWv9y/2P/NZ2XLvNUdRA6Dd0Tc/kEV47/jvNfnuS/1hs3j8fNo8rpGNRwdDwcy/rogsRHj8f5fFTiI/38+XxUQj7wIPKiqve+MhpB0I2C1u98920V3QqqwVouP0byBuY/4n4tGfxYyK8mW5E7yVUyuF/AJtNmYExPYlfeyI1eMtgs4CPj/HLgBTyRzE5XguByUNQyiJG0FU/JHP1sK87XT4cyRx8W8ZdYYoklllhiiSW+ePwDjkWFEA==:1890" +
                        "^FO32,160^GFA,08704,08704,00068,:Z64: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:DC04" +
                        "^FO0,352^GFA,09216,09216,00072,:Z64:eJztWM+P28YVnh8MZ+IOpFnsZdwy4WgdBD0UxWwcoEIbeGW78N589iEHygaCHrlA0fRgVCNKkFV0keboQw4G+g/k6EMP1O7CysGAe+yhQLU20AI+adFDFIAR+yjteimKsjeuAR+6D9wl9Tj88Ombx/dDCJ3buf3vRuYnPvvfQEjPLuSL+9oUn5gtpYurEHLmJ3GCExRw5BLO7B5egTPnE5/wUS/uq6CIM1tBfigfr1HEMWV8qMs5Hd1EnFeIqVwz1VodvomRRjOLdNXWubUjTq21io9QPbu4gia0/Stqv6Bfn+Jgx3HYuGU9tTVM4PAPp1KiUErjxyj0B8k77Xtj6Y8bDc8f45QdDSw4/UHKDtPqOMenK7hQgeVc7wk46rVIafR7pXU9QA/MYY/anuLcxPZnwuBIGGvRfkU1I6H3eX4LHCGEdzUWXrDtwBE2GfD5FPiEIf4bfGJ41/M8Gce/9RS+58lBjLZduYO9tcFXeZ0JB2sAn2CD6GtE1wjo0wd9tEb39SVCUZ9zqazd4ApTDhdog5ga5bJ9f2G/HEeoRgx/190AjiaVEh9kfAxW5g59p70rpJSNxg2lMFYXBxbdcc0mVmuDRRyR8Ynh77ar91zgA/p0QR+tMdcbRNhdzpWK7R7wiWZ8bgNruGr3F/kIoUAfBeK8f6wP7gGDMKQCPnmDricV6LPvSYxP9fGPBuzLoj5NK+o6EpVrwhy60mCiDOwXEbrWU3q2X9Z2uAF9sv2KiKrR6riTjx/EktY/VAux79JOIreTLDjGlPr/SdPYdUwzkTv3jtLWePv7NsQPZuPU+h3XH4A+qZP/Xvhx2uc3Ef3jlIz03qiCH/ojQip/mabWIRBFWttRmton39k/xTiiNrUVQmgb9PmryOPkLWysuFFinkQrcbQ9Ow4E80I8oxyHzfhshAL4E2F25Cw+vfStPhNOtoqNUHW0Cuc4+5yFD4uzoxznJBuehc+5vT3TJxfzKpZtoX1RaU8XwP+aPb6vTyvcqTVOLtQJDob17umCeeDC69TMzhhwTNm7ZQt87Go+J/d1GU6RT6PIxyzysSv4TOiogiypa47WI8Ir8ShzPM8S9Si21Z6APFSp0weTD2sRthNYb7XSUFwLOC2oqNiyxHjIP+x5LciJkBihsOJ7STP2h5CnM89R0jrEeJCgxI+h4sJTBRwqVNNaKnQdKR1x3RGmb/azQtbjkJOafQ1JGuqsa5qUWBdBSYEuoMOLLQ2GijFA1DEfQ7tzQzW2nZ1uCFVB4q63BjgHpumafSkBB9OGi6g0t+RaWxU1wn0eW0tc/QGS+uc8hjLW07dnhUy8a1HjMVQ1HWntQN0igYMgb3+jau1+MYaghMcDy1xzA8nwioofuc1u8NmMj6jGLL4LdSwcGgln7AR3EQ3Dx8Dn/hJOH7ocS4i5Bt/rI26jjM9Gpk+Xc8vsT6HOmwhKmq5RB0QkRnflZgkfL9OHOeY64HymGsOcPhfhe/3a3EnlUBqoZ1gYBzG52S3Th2QV1UL57EA7uMeDSNR3zZ5QsF/KWBTvmmdTBXKJrK4bgaD/gU97orhffxBey5+wZHOA/OEjr9VO/K9mhRXfGx9m8eP/m3kOeMJm6l1MMAQX88fTpfipQ3dYnZCwhuwsntGI3q+gsT9GvZG1VfBcAsrUjrW+LPkY4jmi1CzH86sNs2XfUmN9Fpxo2cdf+sAK98FCfZpNBW750rmRFf7JaZI6WTV9GQ5d4Q/xEp/0dfigZT7n9nbspXH5A6yswr6OvSk+3psAoRXarxP6BbI3UYUbPeqQ+en49lNsxyjvJ+gKHSmCxobk+9VqCpOFw1Ict6zjyfDqgM1P89tsn7IE5f0MbbGJAq90t3L5UNwRHMaJWnsUWMHrOu5E85Od3cadqFJBeX+EYBBVpFLhLkyMp+Jc9RR1xM4guBqXfG3cPhDFVCg8V1EBCTvI9fOq1gc+HKplw5bh2D4vphIuiSKcVid5PuqqUp84wuybRikf+6kqpkghqbquMEuCXIbmzT7/BCpXJFfg1DkuEBWaqKccd3lBH485YvPAK9fHht4SjmHvffve9z0vv18cRk5CCBTyZmH5Sj7wwMWP3v0ukvn4cRg7mraG2BGtEhjAuVWmz8V1hWHOz+lDXBo/3HpKuLhZjrO7tF8QPRcucEp0Xp9X2Ir4eb8qGA5NWPpMOQ5xi/WOC/HjC66LH5hCZV419oKxYfZ+LdoWSzzGEhz7hX5DvwTnX1m/vGjwvnsECmy8OJ++XuNQZm8K59zetpHcj6nB8dXr9D7uPNZnb0bj+GpFB/kKPuYFnxNmr8OHFfmw1XyqX5M6z37aTZ9NtyaoTtAI1aDEWr1un//kwa7i8fOKI1AjnoBj3f4SZrWAFJMkQv6X7l2FQn+cDqetMU4ceMGbU8cfhf7h1D9KlRxMWwcOagyyH4P9w9QfxwFbxiHczfhAMolBDtyDaoZg7EO6ri5FKptCoab3YEWnlzmyicfqEj6u53wMLVCoMEywIWaO66AmcxCMoJeZXBsIs+2ijM91ljk2sbcWB7QEp5p8AC1QAHnYBUqEOC6qQQXR38gNmLLau3oPygnos0fAAQdXpfoAzu+gBYo9hlwcM+Y4Mz4sfiw/d4FPN3jkVjOdh0z+wpWfY88r18flG8DHcoEIRZQQl2R8aNyVtUiadhREBEYwpCOSOS5lfEr1cdVl0KchFJsyhOlMH+qgHVdufgt8WGPbxcf6gGOTqkyfdBmHQM+BdAwD35SgbL8EqvUIrxEV3Fa6Q+2eyPQJBr3M0cz2qxSHyZ2/ozD2BHKpxUnrnxA/SevJM+YPHsk16NQGSWuYoNYTGEsfzcbWcn2o1sAn4AReHYvqW2kWz1vPIrreBn0eYmvHFTZB6s8TcKzb+o9suT7YmFsn8RMs3JpNpfknjsfU8vjBD/RvYGaGeHYLpWw+ldqiA5W/XzgO78Jp3BpOFyskhqkULfxwig9mb3s6KtHHt7GuQ6c8e98XcLKiuYiDJmSOU8JntdH573SNnCtcNb++zMoy3/lkem7ndm7n9n9m/wWcMMQg:CED4" +
                        "^FO36,438^GB27,27,1^FS" +
                        "^FO270,409^GB27,27,1^FS" +
                        "^FO131,326^GB27,27,1^FS" +
                        "^FO233,299^GB27,27,1^FS" +
                        "^FO400,300^GB27,27,1^FS" +
                        "^PQ1,0,1,Y^XZ";

        String tmpFine8 =
                "^XA" +
                        "^MMT^PW561^LL0583^LS0" +
                        "^FO128,160^GFA,03584,03584,00056,:Z64:eJztkzGK3DAUhp9R4KUI1gUG6xBpVAh7jzSQRgGx0uBiyjnCXiOlhilS5goKW2wZTbUqjJUnacw2gVzAf2eZn0//+58Adu3atWvXrn+I5Xs0dlX40r2HY5cXLTgoKzze/PfIBXrMQTwpdBbAMA8KY/OFIIvvAq9BQ0665yDJx2avY98zRz4byJc9yOLjy4MXjF3I193jEfJiOAfTeDqKofCuOSo85Vh5Riztor+DBKnw7ELSAEmTTzIJhaf7gTlgp5AUziHSuQM5fqk+vEbT0fn5FMwRukj5DgoPUHh6oHwg5mDov0+VB+ZT9XEXZEf3n09eaWCB8vWy7ytPHigf9Myrch9d84FsAxVdMEi+89VPEdBTPmEGDpZ45kD54Bmbz/AFC6/5xi5IdMS7OZGITvkmKXHOxFOy5NPMjTQ3J6dEXzD56jPUmYApz29eRBBgM7fK4M98y2kylK+LjUdTXso9sfkUdcbdlN2b4wl6sCv/+kOy8zzn1OuSjyiq8nTNx5tvIB76KZ/eYTjSIfG+LQZ/IfGGY8lntnyxzvPQ8nHKx8o+/IGxzpPz1yTZpcyzjyXfuM0zlv7ogh/91X2AR394J95L6U/E2h979Nd4qvXHaD9BE2+Fti89D2nkl5l4PJR8uO1LqvnGz8XWbfvpnrttP6+LyJiJh0Hktp+UP0e72EyFP/aa9nNaq6+9B85d4isr/RWfY+U90Lyzm5JdaSDp4/0VnmHb+/MUuKs8X3lePJHPEqnw2vvbtWvXrv/oL8/KZp4=:4075" +
                        "^FO64,512^GFA,04096,04096,00064,:Z64:eJztkz2O2zAQhUdgwSKAeAFHvMYWgnQlLlKEhbGi4SKlj5CbbGiocLk3CGiocJdwsUW4WUGTR/+snSME0UCQ4JGePz6+IdFcc80111xzzTXXP1e1cgnXtxSVU00x+ET4/UjKiVC1QTjFA68nUvRIRaDWUVOJJ5dSc9ZLMri6iLuqoDe0lAWTxNdZ7yX33EOPHvRdoEUlds6YxUnfSGeV23NKJZWlGIKlByU4KKLQZL488cvc+02cMn/nUnznOytX+2hsRVoXvbe0VHILfREXkoQn6QUWpdFruYg283tv4uLGPw8hMRiKe/h/UGoVwecRm+DyTriEd6vIvA7LzO99/Ms/b73pRlLwClQnFUVJ1I0ScNK4GVLoPR96X2f+1gdTXf2LzG8nMUkAEnUbhQzAT0d+Q+CLCb2XYfCNnMAP4da/6Lcc61qy/LKG/26jyUBv7dH/EjcsUdP983bnF3oEf+8v/lvGnw+OU3OnWWzW8P/29DXPBN2d+EngE83ave6HwTXtpHn7Ei7+WyZTHFwXK1spsctbfb/TeSaoNke+EexNpcDf78GvF5r9J2+ufEsHF1Npm1L8EMjf/jz5b8KR/5D5TYnec/ZvG+jf3K3/4hdFo02ti+8F/FvGXkP/MRz5OvuvNXqvO+y/xf77z+42f2KKSaW6LCYB/9Ajf0c6XPOv3/O3yN/nFV3zpw58GStVjNm/6TBr8K/9MX/pZfZ/mb9l5nd0nf8NhylnHdqJJoEBty0GAk90Bj7PP6bjPP8t8vctYVzP/A37JXVRes00nvQFr7Letwc+nz/NBbvCU+dz/lk/XvyD31CbRPjAOWuc/9Y95qdyVz7enc+/yvzmnT/XXP9t/QEJ6LCN:5EB7" +
                        "^FO64,192^GFA,00768,00768,00012,:Z64:eJxjYBgFdAcCONgKOMTt7JhbnJQZWRQcGBjk6/mPJMs3syQA2YwNHUeUDJpaQOKMjRNSBAXcWkDiDA0TVLgUmCDi8wVK5A1cXApA4n0MQHEXF4h6ARdBARcXkHpmRQ4XI4MOFYj5skeK5XtSQOp55NhYnPQ4wOIMDOxgt4DNZ2CGuBfMZjwCIusPgEj+BhBpD2bLH0HyAgdpIUQ3AACHtCDW:AB0A" +
                        "^FO224,224^GFA,00512,00512,00016,:Z64:eJzVj0GKwkAURCudYL4YtUdFDMTpPzlBu8tyIkE9huIJBNeDo6BZeIBZzllm4WrAW7jOAQS1G0VcurQ2xaOo4n/AwaOeZOEukpRXW6a/pEYEVxXHtFffaf9wUj8BRKjXsaQ+B7HQOQG9t9I07KTaazo6k8BHWcTVHL/UELwvgEm3NPejzyJoR+NhYXMG8SV/ZwHbb1Wl6bs600BIW6JcmH1eMqDkMZrJgfQPX2ozBlxOiIm5skxqlyWjzvV2z8ms0+0XZ2SMg9ur3/+WvSurRW5M39ka43V1BpbJJbQ=:A6DA" +
                        "^FO256,96^GFA,03840,03840,00040,:Z64:eJztkcGK00AYx7+ZxMwnfMQcR+iSWfcu2VsPYlMK9jXiXgQPEvEBJjsL0oMP4OOkRuzFg48wLILX6qmH0DpN02WlW/Uqzg9CMuGf3/znC4DH4/F4PB7PP02lADgAA0C3MkdzeQEgupx0q8nRXL3zQfUXvuhOnzUZ4lNUashD+6pSZ/AjyVhwfza0seHqPACJnWHREmnKXrehWOm61PO1y7F0pZe6Edk5g2TQNTKEyGbq9C2PsKitajgqZkheYGF4cmoAqdt4ERKxd+VzEYaU51+KaZQkjJFsKJ9Gcux8u5yJEANUZ5yHWFTv1QWXrh+iwcpwHO/7sYUgSil7E0TON5blC7E9x+Bhc1JPxMk86Pt1vhjVo60vn6PzoQKD+AFrw6ly/ToffNr2o77fmMpnfb/JIG+ie5cM0nY3P+r6bc8rC0Oqid2+V4gFFlexuAwgXfXz23zWNPre6m/LfBrqeZsu2WYgy0SztWCbm/nZzewJjuxw9NXmH/ljsA8svET3h2K45my97/dbAnf18/tjTt74Dj9I+jv7xXdYQO0fKrjV77gP6ttvD33ZXWU9nv+Nn0lRa6k=:FF60" +
                        "^FO416,192^GFA,01024,01024,00016,:Z64:eJxjYBgFo4AEwMbAx8j0mKNBmeOBhYICkM8i38z+sbdBfvafugQDEN+gqUmio8Ggg+MQSJ6BxcCtRcLngIAPy0EBB5AJCk5NHBoNCh4czfYHwPIubhK2DQYRhY3yYL6CixNYvqKBgwEi7wLWz3NQACTPYNChZAA2v4mjAcitn9+TDrJ/5o86WRDfvo9D6QFHgx7QfWB5BBBgFUDhc7AqgC2Ae43dAIVfx4QqXwcLgWEEACkwLuU=:7068" +
                        "^FO0,224^GFA,23040,23040,00072,:Z64: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:6969" +
                        "^FO96,0^GFA,03840,03840,00060,:Z64:eJztVr+P2zYUfhQZSgFUiYcABQu4OQIesjLoorSDqbtD/w65f0ChoUOHoqatw6FDkLlb/42Och2gS5AuHToSuaVT6yKLhoPVR9k++36huqy9D4ZpUe/Tp4+P79EA9wLZjOJ+tDXsetAfQCX2w3W33KxvPChjHrdA6rz+lNRCjwehMKYXl9qiKKLWpu635aRqomy4Spe26PmexhSC2FgbMT+VYpCfSVH30wXrnMqIjbJaTl9FMhlWmbauH3dSGy0ICGEOCIsFH1PR1y+0ttCKWCnnzr6MI3pOte7pF3JrRIFckf8zPY0FyVG3r9+xLeS4tTKrs+lL73eWZX11x2Bk3kKsT8ScS8HGXBR9/Q6hiGqC+f11uaJN+zPm1/XVfQKGIdfvqwGZtzr/NrR9/X4OLrHbnbxB3/x+DObRZeVuYIp+3CRtQ9vay+L1aHvqDtKWEs+t97j1HcF3wf5nxN24r9YDroJdb7cS53DQu3lxI2CDQMKNWzEOmboluMOuP/PrumKtK8xduruH3tCN1rqRu0u36J5XwjP6jRAiJJKxEsuyNPnjPwbk9xDKeC4pHgcipEyIAalNqU0Jg8MLMIc1wCcrGC1WMlOH5yueNs7RRi/SZlRd4CetVhTSRo1mgcywWSwapVbQ/tRA7evoIw76mKOuGHPGZWloLPJYjM+YPmYh4RRfTegjjFjE+jhWY46VLMH4+uUBqK+CTCs9nPEkyoogiRZR5F7F6h2ndIZcqbqI6ofsl0QNZ3jGaLA1ckMK6hjdCP2cMhZLw9ijeSTNa2a8Lu10C4w4mEpxwnSOC4ArVft+lc68rkThz6okiROXJOkikvWbuHgXpFWFoX9qhxEvpj/Kt6hL4Sk41EW/7AiUdyOU12W8Rtc5E+aEmSPO5qhLnCgw4oAI8SVTY+T6tuf7c/x1p7v2yyPqAh6v/aJudO79atVFeL9cDQOglQU7Qd04A71gshQi54xJiuss80jmp9JP55t19hHdOuv8DAi2zNrnF94AZi9937bnq8nfTYL5XaLuqGq+mAV4wmN+37ejCk/6v3x+Rw7zO7VQK9QlDJ4FPDxoJ6ScvHZxt69i3ETuacD18xL9ztuQrkLxnd9XI4f7Cgy2Z7/Vz67WR3H5M+m+6e7WLkoD/gNB3dMrXL2rnPAad7CLwn1V+sDV3tMAXAFbZWr9d9tsrjbv4XHYgBqhXygvp3yJYtq2yiO/kjBZrq8IfL/jXoDo1nnfRYet7sju36R7Lwg3ynnLNXAbyK2zD3jAA/6P+BcwnyC3:E56B" +
                        "^FO33,222^GB27,27,1^FS" +
                        "^FO202,222^GB27,27,1^FS" +
                        "^FO398,222^GB27,27,1^FS" +
                        "^FT523,56^A0I,20,19^FH^FD" + GreekConverter(munPayAddress3) + "^FS" +
                        "^FT524,80^A0I,20,19^FH^FD" + GreekConverter(munPayAddress2) + "^FS" +
                        "^FT525,104^A0I,20,19^FH^FD" + GreekConverter(munPayAddress1) + "^FS" +
                        "^FT555,128^A0I,20,19^FH^FD" + GreekConverter("Α) στα ταμεία του " + munPayName) + "^FS" +
                        "^PQ1,0,1,Y^XZ";

        String tmpFine9 =
                "^XA" +
                        "^MMT^PW561^LL0170^LS0" +
                        "^FO25,50^A0I,20,19^FH" +
                        "^FB500,5,,J," +
                        "^FD" + GreekConverter("Γ) Στην " + GreekConverter(munBank) + ", στον λογαριασμό " + GreekConverter(munBankIBAN) + " ή web banking στον ίδιο λογαριασμό. (Να αναγράφεται το ονοματεπώνυμο του οφειλέτη, ο αριθμός κυκλοφορίας του οχήματος και ο αριθμός κλήσης ΚΟΚ)") +
                        "^FS" +
                        "^XZ";

        String tmpFine10 =
                "^XA" +
                        "^MMT^PW561^LL0623^LS0" +
                        "^FO384,320^GFA,01536,01536,00024,:Z64:eJztkEFKAzEUht+bV+gbCHa6SyGQgB4gS0Gx48ZzKPQAA64KYpN200UPNdqF15gjVNw3RmfqQFtXInQx3/Ljz5+fB9Bx8tAvHv/iibgyNzJ1y+qKzXlovd7YmRx6/R6EfWz9XFiDy8wLu2BjkrZVZRmu7FoNvSjuCfq7BywNrswrj77yBAPXeCWt7hV3auSVLQh0WfuE2Zwl+QWz/+4fNB5F7O/nE5FNhX0Iu3zcbw2Ru2Rb72/64/6PMCMn9eZWjKufPTRP19trdDItPY+rdn/9iZMAL3B4a8cAi0OPpUJ4izrs5UsB7jn67Z7Pe+ie4BjojuqOjv/nE5uuNrI=:8594" +
                        "^FO0,96^GFA,04608,04608,00072,:Z64:eJztk7Fq3DAYxyU0aDFSumUQcR/hRkGPM3kjHQ30hsIlePBS7Dfo0pe4qfjQcC/RUhkNWQ1ZPIRTP8m+pE65OydD6aA/mA9k/f/+6dNnhKKioqKi/k+JvtCnBTPJphCug/lglG/LkYjAxsWz8fJtObMjPPz2tTxDzsGYos/YpPQTUwzdMFqLplaw2Es1u4KJ1bige4KNUj/rG/zDG3siblzeOuv4IyN7xrvMmha5Aaq1tmILOS7I6byVytk934OR0UCEG33XXmv4ECNFIqQCHnzIWTb+hRyX4Jgtt7rgAoy8z0EPOt8gW5RXkFPOZduYFgNSUGd1BWvjghqbb2QHoV8FslVJ+pyPWz3DO8quGKVsLozngRadEOTMFDSLCuGNw4095HaT5QVjjBalnBvgIWZ1Ksd4HmXvi++L7Fsx9Act73YizWkigCe5FO15HuiMmKmt5wEjd2HxPRxaYDj40J/AU5/kaWwl5Cr0B4yVCzwXMBTiGo4JPH46lttakTP90dTfq788MA48mXElW9uCPzoX5kef5XF5yTIYpj3v1vrQnxTNE5YSmqj1ry7BdYrP9QfvyBcmgHsFuwnl9Mi2P+ZnitgwP3/luOf/Yope8njrh9tJOSOClzw1PO+GnBSdFJ7CM0FHOtJ/o54UEXTkhqKioqKiov6RfgN74g1/:AEB3" +
                        "^FO0,576^GFA,04608,04608,00072,:Z64:eJztlDFu20AQRYfYYjvyAgJ5jQVCmEfJFRSkMIMQ4BIs1NkXMHIOl0uwYJkLBNAQKtwZK6jwFoQnMyRlq00dTqGVhNXX49//F2Cbbbb5jyaik/N+V7uv4KuIzvjDV7XzqT7qYdIue6NJ09C9uH3mslAP3UguUQMdI5ckyunefehY9KZAsmiARjTBFBZT9di2IQnZiYKitjlZX2ARCn5HNlYtPSiXxMqq1t7whF3REfpceMqqnHkOh8NUTNmFvKKh4U3ka+ZpRrLC88gswtOuPKB6hyFPFCLmEI1Y5iYGTHXbW8xz1TgP+mBPDjEEE2uLinkcJLzEorPygB6cn3aJZpdy0B3rVCkIz9BgleqOdRLdjN2IlS9T3aC2vBk02yRrQisQC+O74dc9+6MaNOnMo9gS3LMDiMAfxvGEBk3MG4THguLHkzWhFShT/Og7re2eeZIIy6zMhKd5a/yeSc4ecuY59y7fl9nCo4QnIlk/dO6Uw1f2x5bsT8w6SZj9sXyAuPAY/vXYO1OHWDerP+qgaF6vBlWK/Jv4U/oKdhEFPbE/Gbm6Q89fnx0ETXgZXFUH9kvyQwP1vzWJP2pYfc4V4emZefjxIWUd9cw8GdkvnCvm+eag5C2X1possD9y7tRS+5DM/qgHuPpD/vK0+pMJj/jDOvfsT6btdwcV89y3Ls/Fn1mnp+aYSX5A/YHreTk8Tczj2Z/EYhkv+bF8Xvz/jAVy2j8bZ4z59Cd6jePZn6eb/Jynm/ykS36al07y42oHckp1hFW55MeJL9GUzPnR0zXPLR9JmPPM+eF+mSU/LX9v7pQtLOi2wQK8Nzf5gWn1x1/71QuP9IsmWPt1JMnz3C+fgfSLdZZ+0dovx9eAZPlTR6ozSd9Fh/sVTP1Iv/Rje5C+o+gcGrwD6Xt9oLnvPdmg3mcdvOGRK+crLDxy/wjPev8sPB2msNw/tN4/ji+rWefa92222WabbbbZZptt/n3+AlBeFpA=:119E" +
                        "^FO96,544^GFA,03584,03584,00056,:Z64:eJztU7tu2zAU5UM1ORCOMlWDUN2kP0Bv3qqgQ7fMHWl06FZoSzffOIKSIeicoUM/oWO3UoFRLwHSP6g+oIMKBKgHAS5lSYbzQtei0AFEXV3y8Nx7SBHSo8d/CrMebfsFDy9STR4Jodim4q2REP8RXlKP1DZPt8dmfFRPbvS6ylrdv+h5erO2XXmMY0aKw8ux5AWM+Sc0xAzdxuMAzok8JFgo/oH8hH0WwEcspkWjTHNaCVFNF5US5aSKSmtIEllLK393RYLp8WkVih+nVfJG+P5qUU3LpNGb0YyjhBMlh8FeFmgE8llbpJmvT4g0iPLZYJRJ2GO+zmeSQOsAp+IJqviV54XhxO1pDf2exDEV/m5OgoOcqKfeaO7pSer7eeoRqxseY0yhjC8HUsp9BuD6uwCDzLVz7PIW5b6CcwWQulpSRbHlCSGUvYkXAxWqt0LXeqExVtR6GMY53hwpfaWSyZXTmyvROc44k3EJM+b0njMNCFQ6Ve7aqfUQyz0JZy515mucSdbxOOW+CeKXnqfCCfdH1nBV98e7/oKDdDRX2rx2idRTGz3nHLj+Wj9df0xB7WcAzs8D52eckUzq4r2bS9WQbPys/KNlvKhWq/r8rDUDL7quaBX9qs8v+r2036hYRkUSlfncE6Q9P0rGoAuYJauv9X1BhAEb7izJOCq/EPluZ1ngibtQQ4CoxExyhI53G+6+PIzGf2pbvc2f0H0+xmv9KNr57n+KXXjh3qtie/utOFiH0zLuCmte4J5tHmzx1rFchy+Krr623rvl3dML2/rw9rq7vHt6PXr06NGjxz+GPzkKwGw=:373C" +
                        "^FO0,128^GFA,16128,16128,00072,:Z64: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:9405" +
                        "^FO0,352^GFA,09216,09216,00072,:Z64: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:6D9F" +
                        "^FO64,448^GFA,08192,08192,00064,:Z64:eJztVzFv20YUfkdeqDNASCd0yAWQbRoesnQ4IQsDBBFpB8mWX9CBSgp0tTsUGYrqKKqyBsNeswTNT8hPIGUj6GAgS4eMTAKkq9AuLCqIfSQlWZHlxJGnAnqQSd5Hf/r03T2+dwRYxSpWsYpVrGIVXwiiZHYSAHQpvq6s7MQAzOX0YcJfUn/KX05/fyAd5xF5+4j8cVtf4idsJZYVpwdJGgwbfe3r+R61PKfZEc0ulTvG1/PBsCzPO2TekWk9WUIfKPp3zoRzSq3dZZagjPrxGYvPltVH/84D5jyg1lL+c/3g9xj9v19Gv4b+Xf+t2xXyZAl9PWn8OUrbLA2SRjD6ej4ZGD8MH1P2mMS39SX4RRhsWWYRVFyLzsr29eRLg2vxTT2+Hv9a7FVcGg7w/Ezyj0nH1b4Y4rpnx2nRUXDh+Qsh705Y5bPbZTau9sUQ807Ho3HOv/D8z+orzHJSfF8xBOCz+mQBP4QisVEnu22Pu10xxK6ZHctz+iUgJXBqVCc1MviG78FDcMiAr5PXuiQDy/t5LdxfC9cplr1NQghQWoJmRL3aQfhTxq/EeiNWLUPXR5Vh4+8RtFQMiWwdpMFLwG4zqgzSyqB1kHhh5V2ggWE23qXxQdI6jvP6ITwCWNI6N/yueCAkBdd3wORRp+6HYEr3xKxG2VA4DjRzfe4eYgdy1X4+lbalg6fCdjlo26+FXYYYdctSHUoVYrXdCnp3g56tjlgcQh31y6bc7m0dsVDdaWd8LjP98JRGOt/lnILjSqBVQmUUYrVvElbNPpSifj3Xt+7QOg7Vdq4vpV5SSnXZSSD7Qj6DuC/BQH0riLHbbQc9ETChuqYXwp3cv7XNrK4ZBh+0sX5JOepUuLpE/Wfg9Dnq+x0Z4e+QLmG3InbL75hTfVk36x3TDSJt7F/FKuyKD227n/vvCijX/UOrbeM8oH/7395dP/e/FQRT//fUy79y/WL+KW8a4mHm3+2IbP4poRzn3zsxUU9GNJ//AwJGjTehSUVNeT9O1v9Nmhpye1R5bdeH0DpkeiJHmo75n3WbSvy0Eo9KSSusxGkj1odprHD9reO4Pc6/m6V0SGX1GTnjmH/fMYr5VyO6aZLB+qPhWhithbUbYSsswf7mgJym4ES/hu6B357P/0siUAvAWJGr8qNFoAtk/CgvauPW7OA9zP8AfBCfKnJUDPQFfDk7+IVc4AM0gAyvyLcv8hXcB0iuyC+q32eBVSwds3uhyUKJAneAdHJwvERmBl2Fz4q/ELIcO+8UOTQfs7voybryAkd9moPa9F8/rz9J9Kk+dGGm0y3UZ4JDSadVpe3xF/6gRsIaeYvFhol1N3zRoXzdB3zVqZGI9MT6zp4U83wBjbaWVRaJ29oU+0olwWLHRONNkh6ZMvWVpkF6/M7vicbJcJ5POQfZN7inUx6VTCz8J5SjPuWeg2UQQQKagTMhfYR2qeRz+owB7uWlpWHV75ZJTwY9hsWeYcXN6r7E7oD6iPcR6hvWRP9+cTLRv7NrcKlR7ncoEVVfZMXWxEMGVP3cPxF8hwlnh8o5fuY/fq/ZUitL/6i8IW75z0XuP4xi/8is+7n/DSE+ItQ3pvxUTf17O4W+S+kGrxJe+HfcGIEqyf1vcP49Qjvn/lsz/p9o9ti/Xkmw5Y3925l/P/eP+Lef+m+d+8+m3NNN7gcm2RycmDz37yrud2vZ/GMaIn4PocCc91/5J1X4LoPrn0j1W5I+F9mWI4RK0jhOVIBJsZngqwriNkJBMs/Xqynm3+gmYP5Fm4Max96DzQZ0vkHCiJzy2uYAPgLiHCFtb379L+TzJS8XzC6ErS+8vLBLvp/xgjifv1mMO5GVHcqvPrkVTi7Kr1j+eDaSi3w6wy/Fn9xyJhelON//QmPBy8+sfrbJXaSvK+NCH5zTlwtuOTPXF/rYKlaxilWsYhX/z/gPm1HInQ==:C0D2" +
                        "^FO503,232^GB27,27,1^FS" +
                        "^FO498,311^GB28,27,1^FS" +
                        "^FO498,439^GB28,27,1^FS" +
                        "^FO497,526^GB27,27,1^FS" +
                        "^PQ1,0,1,Y^XZ";

        String fine = tmpFine1 + tmpFine2 + tmpFine3 + tmpFine4 + tmpFine5 + tmpFine6 + tmpFine7 + tmpFine8 + tmpFine9 + tmpFine10;
        return fine;
    }

    private String createZplFine() {
        String tmpHeader =
        /*
         Some basics of ZPL. Find more information here : http://www.zebra.com/content/dam/zebra/manuals/en-us/printer/zplii-pm-vol2-en.pdf

         ^XA indicates the beginning of a label
         ^PW sets the width of the label (in dots)
         ^MNN sets the printer in continuous mode (variable length receipts only make sense with variably sized labels)
         ^LL sets the length of the label (we calculate this value at the end of the routine)
         ^LH sets the reference axis for printing.
            You will notice we change this positioning of the 'Y' axis (length) as we build up the label. Once the positioning is changed, all new fields drawn on the label are rendered as if '0' is the new home position
         ^FO sets the origin of the field relative to Label Home ^LH
         ^A sets font information
         ^FD is a field description
         ^GB is graphic boxes (or lines)
         ^B sets barcode information
         ^XZ indicates the end of a label
         */

                "^XA" +

                        "^MMT^PW561^LL2757^LS0\n"+
                        "^FO32,2624^GFA,03072,03072,00048,:Z64:eJztkjFv00AUx9/zpfZrcdIwFF3AJeeUoRtWp26cxFAGhg5QGIp0ropgdJeyVOrZDYmHVGVBYmAofIJ+BEdCmTswMGS4MsDCAFsGlHKOKmZ2/J9/+t3/3nsAVapUqVLlP037B+ai0BPfcUAentbYb9lxiMBQFMLJFKE/KYRgmhbNydTyS7vI1TAdN1wX5OM3tbltGbs88LZ5EuPxFuLrp0ap5SRYio+3yge+OSRM5jRKP+MEJMOMqNGg9RByF0FHRohonUDkrsVx5PG9X13Wdj2QHg+gKeMRtw/wgxi7NUSd/FRJwjnE3VrJH00pFAOcz6zfXZyAkGKHiBx6EELPt36VCyHsj0TPL3nvEY+vD7A+sv39+iYoqfZ5wD3+MEbLoy7GSik/gHjGO6xGnWaOeeknv2l5sUbEHeqGkJH1a+tXpT8jy3tY469a7zHYsP15LcLi0LQ556zVjy/7HC/T9JNKXoyD9kWfW96FnDqUQ74zryV3BeiX5uasfxbm1p+nuifeqfwOmSv/gO/WB+jv17UM3ATkVf8WizFtIqbFSH1R/j7EadPyzPpXacBojbRsOOLvfK6xENIz298ciVxQB0R6Vs6nOOcHc10W3GgW0mMSIqnK+bu30PrPyvkP4yDh962/5KE4p1W359CCkJKxwu7XDoJ8fwE+gD4t96s/+lE01EKflvdTJDzx+mP/dvL88xMsZvfTeraxHeAFamPvZ3P43V3eG76NtbH8PaMoYnpCDXF3ephqNpErzvxlZggubPfyPvVXh2l9YmZ8lSpV/jF/AAL5st0=:BF1C\n" +
                        "^FO96,2688^GFA,02304,02304,00036,:Z64:eJztkbFqHDEQhiUWrGatu3QbUOxXUDqZHOdXyTZJk0JgCAcJRMtCrjGXNkUewmXKEYK7xiQvcIXEQCrjW3cqTDazeyH4FQL7d4Jfn74ZMTZlypQpT2JPdKk4VBDPmfeB73qnlbUJanjL3rCfHXWMNFIWzsTYR0RsD70zZyuLEQHdoThk6lTlvCy5qyzEzificOIY67savNvRA084JsUOU2h2rTNqYfEmEQfpQJ3zR12KnlWLBDY8EEcwrZRNNzUk0nMD5/LRSOqYJboVPmAzXF1Ki98RUgpHjqoqJQTPyju9exl4WBNHWP+VfHzTsoEjjZHFZpYlOvPrIvRhQ+hZh9+Q5mzDl2EuOddlcXJWCXR6+zz0fksd0aXrmjpkl/9xlmbg3L/CFn+Qm+hwgxDR3bPBR+h5ya9VRT729kXg/vbo87kGuGLb0UdoI/lemQXNdXcaGrhjRtFc6wTxA/vLea1L6lSa9rM/DRz2x/2sa4jv2buRU0QjWZZmFWPMM2xiZmZhLIlBXLLfI4fDsx66WbbwCbIgzooN/+6bBKDYx1keORd9zEWO8dLlom2oY1RnsUmOOGbsTJnyn+QP3an1lA==:9187\n" +
                        "^FO128,1920^GFA,01536,01536,00024,:Z64:eJxjYBgFo2D4A4EDDgUoAiwJPA4ODAwcD5rXoYizKXA8SGBgkHBsZ0dVbyDwAaheQrmPuQFFvYHFDqB6AWMWRlT18h8CgOIcVjxo6q0WnACZL8ODpl5C4QzIfD4eRhT1fBwK50Dmy6Cbz+LgAnK/Hbr5PAmHQOYLY6o/jNX9bA2HFwDNd2RjQ1XP1uDogC18mBsOPACac+AAWngyMxxwYBgFQwwAAPOaJH4=:2B82\n" +
                        "^FO128,1632^GFA,01152,01152,00012,:Z64:eJxjYKAxOKDQ0ODQoMYDZgseOOB4IB3MbjAAiSdzgBUZHgSKO/aA2SD19Qc6wGyBAwccSg+cAbNB6hUaMiDq/x9wKGyogajvAIlrwNULMuTA1QszdMDMd5BmPAM3n4sZLM5acODoFT52sHvYPzT0/+Nj4qBlWNAScDD0f2ho5gC7X4KxtcDxsAwPRLxB4WATB5jNwNgg4OzIA2EDY6ApGaIeKG7gXgxRz8DfoHBECSpe2v7BxZD/D5id0GTQYcwFCWfHBoMYYRkWMPtAg8IJLqj6gw0CAbJQ8xMaDDZww8xpLShgh5nf/+ABG8eQDedRQFUAAG4DSZo=:14D4\n" +
                        "^FO128,1792^GFA,03072,03072,00012,:Z64:eJztlj1OwzAYhu3Ecj0YkqkKKFWt9gJhgI3+cIduHZKFOQPsTgJNhg6MDB16BI7gLHTlAB16hI4dOtT5wUZCYkQg+ZkevfryyZ+yvAD8MgQU1qd7cNX5kmM1BJdIf5EfdJ7PlTspUf6Ye8rDVPs0c5WLhCkvkzc9z9d6T7LT+7n46Q6DwWAwGAx/Eu5m1mgEFlXfmFxluPsANlWvCFmWUgJSWnmUv3sUlJWPd09pQEHyKv06et4ECJYf0tmkuGPY4Tvp7nR1H6OeiKV7vBgEOKjzi/LlJkBxuZdO+HoQUMYL6Wdw33VpWPrSHbC+9AjLqt7SgaLfj2/r92Agzm3hLPD3E/4JY3HoH4dNT5uV2948antaasmDm54Gke37Ia3dopAQ1niHQuq1OSZw6bXzvg9xEB1rJ/JvMVbUjijEsdpjW6ydt5Fth2HTG+HCgqLNZ5stFO17xvwA+FD3ScMJxF9RQQ==:0390\n" +
                        "^FO416,1600^GFA,02560,02560,00020,:Z64:eJztlDFvEzEUx+3ci2NR13FEBwNFMULqwIAcUaRrqYhPjTIxdITN0B1lp4OTooghQmXrwGdA5Qsgl0YMLCx8gJO6MEVlq0SEuJimSY5KwAr5T6effvf83rNkhOaZ598KJ0oTS0n92241PWMAQoOSpPbosGLGHpeWa8ljeYTsuScNCIGESNDYI4R5Uj9Bsb455RUdXG8hqSvnHicl904qRNU1pM5YaRjhPrWI2cWyH9d7E+12A1u4NOmv2AYwCMyVST3Ojl5yj7i/N3UuS0YeMY+n+pPb3YJDBXc85Yna58ihqH2IJ/1p/T2wyV7m+dvIsvPMx7Ns2RswYoZRqzyzavZnozMvx3zsWT3NecLAqsl50g+1nWWf9pLXIud93D9i0uXrNUHkWBp/YDTv6SYsziJk0/esnPPu+qRRynnZXoYE/Ue5aFi4gLHxxw4BQQHubygBVlQDGxQ4pZzcXleatvQzlyGMYVnKMjChhGwJYzIW4c5VSRe6kiotv2iXhvO7lwUtggAlxIOKs4F17lDKuporrZ/WnAo99WtSsn72AArdqDidMY46N/Y32CDmKlZP1lwcvGaytwSNkWc2K0j/rGdfrLAVnb2pdvttqEfwc/Nwk62OzjXJq8AKuONOj3vrMVPapwehvwgTt5XAkpRKOLMV5sCDyMXtHuentw6cPQnzop0SWmv3APv6V2Ra1dz6IvfrSvFvL2eeP8oPzGOBpw==:CDF1\n" +
                        "^FO128,2144^GFA,00384,00384,00004,:Z64:eJxjYKAdEGAUYDFgtuAD0jwMzBIcDIz8PxiY+XYwyLMIMLzmMWA4yGLA0MykwNjI5MDYzJzAcJDRgeE18wEGecYGGjprUAEAq2kKrw==:8473\n" +
                        "^FO352,1760^GFA,08064,08064,00028,:Z64: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:5FB4\n" +
                        "^FO384,2048^GFA,04608,04608,00024,:Z64:eJztlrGPG0UUxt94xus5sqzHUiTm4ETGnBQhgdAcd8VyseQZYkGbknKDKVBE4RKJwuPbE1CcSHtloEI6FFFaVOu4SXF/QMpB11FEpjuJkOOZ7PnkHZcQgeSv2OKnT09vR58+PYC11vofqQiRyGxEZsyICpfKf508Tbyucjs5lB+Hfmh/79Kpy1SV390BbWHoq3xbOzVx+wG/3XLaOmGq/K3NuT8NfuGzTVAWQv9N7tTUcVflvTrOh42Av92c7/9qwL+sg/wI6lUMKXXJc2gGfEAdmUEj4HMFy6/1j8oYKZiE/sXhcj6Lgqcxh7OTI58u+7lgHPqEmeWce44CmlfzbOSNcwmEQLdY4gVPxhxIDpU8my0mkB9U8+k7ieYAB1DJs7nOtMSMgqjMf6+m0P8UpFv2f1ozfO7fWH4I36cZ8s+hkmdjicH5aSXPr/w2yotfHPLlPDf+dJPJRQECIljr5YlAO2JK2BEBJgQn5HmZZ7cdxUpPcgpJyjmtPSz7ubCNuX+C/o6U+C3zXJzdP1Lp9Iw66EhOHS/zPJxsoP9DSwD2cD6Irv+b38v5kdKP+hTgfZwPusyzGYlDJXpt9O/O5+syz+18fNqdne7i/N3jDnW+zPMOEb29Qa+F/h1xnYAp/bu5+kLpx7dwvv72JgVV9nOLmJ4Sn2yiXz+5jfuU/fw69e+q9Pwazk/Pz3Inyzy/ScyeEnfqc/8A/4KVeU6o6yjtm+88AJ2OTn54WPZwTNyWEqbRnYHYHF38/mx1P6/1EjQczbrnN0J+8s14f/yVCzhhDONmAl7jUZq6LOBM1kTLqYAnksoPXBrwWFDZhuqdgyUpa3x7xRwpKbMu9HNZS35d4cd90B/uGfEocis4jRl1hQn4yf0xdX+4gA/JjMCK91zr39RdZ8lMG4pNYkecDWeX3E8b4zSr/bTvpzmP7/1ccltYxqQBORjYCb+6N8iDKY/iDFKPNc1jpS65tpibDLTBmuZMZ5d8PJW1qO+zIn3U5/EVP7aSUmtMgTXN2eLeIMdT5B5PIHl6q/lscW8QZgUlBgojeq24d+X/DufTDHyRPn6Dx4v7mRzO9zEwMFjTnC3uCnKA+ycK99dPrvH4ilP83y383yy7U+dscW+Q/MX7/LjvfZPHix4mo8mL93wNTIOzdT+v9Z/XXwdP7Dc=:A5D1\n" +
                        "^FO224,1696^GFA,02816,02816,00044,:Z64:eJztkjGK3DAYhfUj4r9R5KRzQMxsbqB0gixx9iYecgGlSxGwjCBuFm87xRwipUsNGzLNsmcQuEgTgtO5slaeHc8WaZbUfqX5+PT8JEKWLFmyZMmSZwWCZoIlSARUO5DSrX/CdXkzFpmALAhRrIku3cwqLjhHEmzV0Fa5/A9tIusVp23YCR/MmSUoxeSF/eGqQaEdHIDBdaJfccyQZZu90zCzPB8mL7VddzOupKeH0NAmfk1HRRvVOa+pObFiPUxehNureuSfHdjA4BvLexwzinJjtMb555RQRy/p3tY8VZ5ay2nDlESuqPWd8ZrPrBZSpCMi+fWuZqgdRinUIitiWaT+U/Q+sVxeHtnuvW1o7/lUtr6MBzQqTF6tP5xZJo99ye3rikHvEvjBcpuJj8CyEtzUV5z78oupb/S+sVvautTcc2pb5un2n74iKR69h5fVNol9yR2DSjIH2wxRb0zxtMMq8ad9U9vyuK/7Hb3fuYuXOO0bvTjvm6J7vDf7oupF3Nd9id4eDfQZPd3biYWR7sM9L/8Gi2ZQyud+YKHyqSGDCjvlQx3KmQ2wD3dJ2X+NlbXULnealcbHjvr4zlYYzm9nyZL/yQPUbsK6:1A07\n" +
                        "^FO192,2016^GFA,03072,03072,00048,:Z64:eJztk71qG0EUhWcYsbcZZlNOcYNeQUIBD8RoCYE8h6qkneAiLkwyy0DchK1V6CFSqlyxYDV5hIA3LNidWeNmIUKTO/oxccoUIcWe8nA4+nTmLmO9evXq1esfCeUgASGAWfnp3jHYQIk6Gc0q/iWBEtibbA2avGG7zxulEiG8F1sVHmI+q9GoUd34QqWdcHfZLRjyjnmNEkDkawC0Y0e/Zy3iYNRW/BIRRX7Fq0STx+yhXyolfN4UUHRnlDetwXRrusZfLgyKULCmiB6rD/wY+dk7FNK+dxfMzhDTzZj6vbaSh89s9U2Tx8tHfiWEe5CiMI37yOraoJKTZeP90ipeFazuDHnC7fPDjYQg3AcUC/PKMV7WiIkez6uQt9RfJbzsNHnHfLZRafAuW+z693llXs6bkHfniuCz2E+M7gn/CfHrVcwTKzybyIpT/4C9ToaljfzAft/fnaEodEUdkV+YiSL+zip2o/7g3+8f+fF5Tp6daRSjkwHtM6fV3yZ89V2T97jPbn8X90895c9j19dp7F/S6nfxL8R+/mR/vgYlU075U3pLXr+A3fsadpGww/vOjvzhRmQ/PWxV7Icu+xGqegrxfrKOTVV2qwx51/WRP1xRXvBWAt/dZx5W5amg+5RQMkx295mH6/avP4FevXr9D/oFfq/RNQ==:EA1B\n" +
                        "^FO384,2240^GFA,05376,05376,00024,:Z64:eJztlk9PG0cYxt+ZWXYndCDrUtKFrmEwlxx62EY5cMJLiNR+gJrmQKU1RE3Vk63mzwWJ8RqBD1Z7qsQhUtNvkI8wxsTuAakc0pysylU/QU/4gExncGFnt4eeklz8HB/9/PjZmVevBmCssd6WKF8agijKAWVE0MT3K59tIFFq9XyWr/gGv8IxCIgxpcGKwcODwEYCWcSzKp5n+N9qHjNEGacm/7BiI+kw9JxFvpEPBc3bFDVomn8UDJHwfeRbkW/mb/EfQDCGGixK8WXdR/l+Jn/pxWUfQjP55VPFE4v4mf5RX/FoH1MWBCYfVTdKstTt5Tfzf38dJj7nw6IoisHNAaldCBjr3YowXBRqhJYHAY+x4d+0Z2XgQHnTrayTxMfTGKTiOeP9I4OHKRtkoK6ZuaddZPg3YhBcXTMNDuMU34FQ81bu0MwHdqTygfTZUiOVzzqLsgL58nmOdS8Mnxawyvf47TssLpj8U53vRtaMlc4v6HzOWQGn82/p/kHEPnK6Jk/n4TKf3sVxKmdh9pXus+mjdsqfLv7WB/LX+QpK5Y/19oWG0sNFKUNex9AWYnDlb4SuXZJhWO7a0G61e1c/mOZ6e4aKj6Eukvn0o8CGUPEH+0DW9q7v1yuq7abz92LAUfN6zoMnoRp/xZ90wKkmPg/C1cv8xj3AvIHEv37krr1ZlOHun4ffAMklfOTV4kkpv+s3CoC95jVfybdG+adPgeRPkv+l9VH/l3cA0yTHnyKj/i9vAZlI+rMpFF/2fzEP2N6/+l606aA3s69eh2W5AI5zcHU+ZIhhtfjjMORiGjC5Ps+x3pkEp0VRh4cY6mCcP5KRVarV4L6N2rX6HwYfMRA1safn39y3MmSo1Wod20Bax07i76pHjBAXise8aeztJ9JCNfG7pfiq6XNxrviWNeLFtV9t9XZrck3xTs7kl2K6LERB817D4HOOzt/W+XMm/yFW/Uc8M/1FW/WXjzU/cWzs20ms+9/VPDa/d4H0Sj+99ic2gDj1XuIzOC/+PPCc4Xie35vU4zPgq0Rkz99CfiU6O2iZ96WFsXpFi3pqnrVs4u2UoW3s55HIkH6xDEeROT9azpf+yTasV0+QTPMW3Suoh7Q5n1rImttXfO55Jke9tdVTY8trZvle3nkEn+ez/wuND8gy3KPNTE84/QSp/hPZ/nA6iX4RcbKfr1TxUf+MON3M+ajHFfRXx/P8frRKMLUwXlxw6dD0nyHHshzycd71vjJ9EU8z5sQ2dal5vwhaU8yaaDPL9cx5ILB+o2HhmGKXpv21uT3LXvdImsew7TWZveXhdL4Da+7Jgv2957jebopf5r9O4oLmzfezyg/uz9gz2XwCR/zTpf15iv/TP9ypdvK+7Xrm/CNRl7d5PMlYhj9D0g87jrMz+yB1Pmox+LKDob84SJ3nWGON9f/6B9tFLLo=:218F\n" +
                        "^FO320,2208^GFA,02048,02048,00032,:Z64:eJztkjFqwzAYhS1+sBYjd/Sg9AwyXf7g0F5FpRdQltKhEBuDs7lrhx6iY0e76dDBlI4ZFTJkKcXQxYOJK8m5QoeC3yAQj0/v15M8b9Kkf6whonFWkJb65DEC8tBWUpAWSB8PlfNf2HxThh1l8ISQP3RaYthBfkxGn3Jf7QrOKSXM8Kn6lMrsvGBZOT8Mj7gvF4zSoTR82nYSFwxSltTa+izsxbKIKIWBWn63lSKi4AVx7XjGAkxKZBRyeDG8fteIlsd9an3uczUrhIlfky0MWdVIFR9Mvto5HxniVYnm/BI6GPL0W3bJIcyPJz6iZ2Jm88mb4UmWvsl7s6t7ceP64YBjvvdh87PqVTbIWMVw9BkIZeb3qde4+fXmuol8XgXq1vkhPOOPvb/3Fbr7b2wbqBleOp+SVtS2P+8usPyyvl5zHqlALMb3yfR8b/pftd2567/emwWT/sQPRMd1QfSqVXx8vwMQrS56wf/qy0yaNGnULwTqlpg=:7803\n" +
                        "^FO352,2432^GFA,01792,01792,00028,:Z64:eJztkTFqxDAQReUVWM0ybl1ocY6g7RQIPosgF5BJk2LBCrnEHkdGha8xIRdwuhTGylgrE0iTImX828fj648Y2/NPYziW9bGfWqYP50Vr3zKM7sZsMwkp+6n3UnRR3w/xm6HW5BmFKEFBrcKrQ755z5I823lsZitkN754FFufAvLUozPVrEowgSFCZtYm78lZkFaIYSw8yszQQNnM+t0ZKgPhR/IyKyYLopkvwa9lkTxil+x9pr46uLtDrRbu1j6dvWvqOwVnhfrRx6/qSN5xXaUUAAbmt3cKmfadBlpl0z7yquxBukvl3jxHVea7ZE80H1FKUURfTHTPbojVEm8MSu4X8lhLU9M/PBBzf//fPXv2/JIvUhh6Og==:5BF4\n" +
                        "^FO192,2464^GFA,04608,04608,00048,:Z64:eJztlsGK6zYUhqURWJS6ylYDZ8YDfQFd7kalZUzfRGmgt6uiMHDnFgLWYBjvsr7LPkIfQWag3s0TzEJuXsBZXS9M3GNnkpncTSkULgWfjbA4+fTn1/GfEDLVVFP9zysmbRRLYzLfEmlUxHTEwqy+Z0lIaie7LgQZv+oH2kcA1mZlT3DhHKKkTeuKJ02y9TD0A7zms/z+Uporludu5EuhtVrcM2LIxkltjD/lc1Yk0lpWMLfngwa7rDixtPa4s/SnfEHvr2UwXNA9X8RKGNRPAi1xR3/nTvmC/nmLmJmgZTfwBViw85Ffll2AzJ3wBfLfon5c89Yo3guphJoP+tld3gaZkKR71Z8IWt0gH8+pBv09B3uZbgv0h9MCAnzWf4165h+Dwe/xOOqPtBGD/7PA6FoGuCQn+m+H/idrLwV9Gv3h2gKM+nn5NPJP9L9ds/vw0VxxwdYjn/2hYj36z3Lky+9P/bkpWFEinxfYP/BZo0A9+/8orf7M/zovIt8GhBVizw9a69F/Uq+lVnO8X39sp2XVc9/d9hmuXdZwThucnLrqh/mpcAcvGV76iQ8tc3qxS13L2rSJkMt+n9V5P8xn3n7thvl05F8V9f/YMtVUX7hYa0qXYhBvSOiCZPpMadx7s7tOpVdpElKCDwrziY39vFuWLsMg/kSrzgIHriTv7LJP0wu/7JNtT/DBAi34/gA5rx3DD2/cY2w0Awxmoo0SQsrwbU42+cDX8YFPYL71vOC07h8B39wEDyErTCIAyD4V5LYg1mK40erAXyA/YrTs13HasFmnNFFGgRQyQ/gC+YokLTvw5c3SY55hDK9B2wyDWSLRguTSLgpMpoEPMnnhv3GC98w9rGNtMJhRf8garSOpHkb9WbOLdXr0591vHjCQUWA3JCYGP/VZY1ccluXoT7bdgd0+81n707mLMdDIX6yT8/xMon5vlNJnUrtsFjLUvxNX9dGf9xcuwcB07/BH68eKKyVpY63WHM7LiiyrQX+0LI/+//qVE+iP/zmK4zJnatCP/mghz+9yTNbBn8i88FcXo//hPYdvyoqjNeTD4L+Ai7sDn5v66M8vs/F+wzWLZ27Dgnq+31jO6AOZPwz3y8Lm6M+K+2F+mhXv+F1Pm6wd54e3kNDD/DBs6ff97QfusuGPwg+kYy4lIW15OyKlIIf5ZD4tdv/xmzHVVFNNNdUXrL8BsTCjjQ==:8716\n" +
                        "^FO416,2624^GFA,02048,02048,00016,:Z64:eJzllPtLU2EYx99zcVubVEsrLzlt5gqUOprYqGQZ6wYSS12aUKlF0AXphwKLspNiHTW6QEJR0ZVKGTaVZBjIKhOLIflT0vW4KKLETAimlqf36ta/UO8P+57Pnud5n/N833MOAP/IUiNBAvpfkazJjpEI5DLV6s0RLKQ4Js9Gxl/9DHCR9YIylSiHMX7mnvJxLfzHqpCx9IQ2Op2tjbqeuICdscU3Nu6bvN3K+Jglr/CO2aJnHFT7W9M8jal0Q0643JA0tLs8309YP/OKPTTe2GYvIxz92rbBr/9xNY1ynLW/C4BlvXclwuaXTTIHuNrzlwjvqq0BAtz7y33C63UA8wKRsN9HOOYzaX9ahT1hb/Mxwt0pMBcOF+8j/An2rXJoUzOuk/ovZZqmOjTNSFgYzLtgBpw55swHcvtfnxoUGSj1gTd4QH5sbXGxLBQXPduOB9S1n4LKA/CxFA8U3VQPMGdtwgMl1DVqmixoWkczvoE5J3kSz7wZjTgnaLRaAW+1Sp5hxLt7TW43ENxuqacHcWDESPKl/G7Egz2CKAJeFCVkBOCUERPZ37s0FbE5wPItW9A4Az5TS4vMt7R4EwvhAEJaLO0vcWZ0sW82y+ca0cVawPpzz2E+3w+MtJ7fi/geYPX8Yci6Zpn11x2BHBUrz4D1HKwXcyHrs/0G4rNkQI+ceIgeO2Vd9w0WT5hAdk+5GM91IXsn7IwXhuCvmO1i9TbsZ+50fGUGEreB8Tobkvf3GW+6i6QgwOq3VSBpD7D4vnTk3/zVjAuxn1c8BoD+MUgF+xE/FAWnE/BOp7S1GfkbE6D+SJW4PnMR83NHOYo/amX+H32E4q5YgcazcH6WzPKt6H64Dtn0+50s/H4n8Z2I22Sjosi8onh5fP5NfuZ/cj7igRDz37EE8bqQicXjEFeorP4cfqFSVYHkSw4vYtso61+NvwDzShhfxCdlOUD3b+vEbFxP+y8/hG0yFNL4rUrim7uenL9zmHDViwa86ilvXEPU4CSakUI0sY+yl+jcQaI55L0FmXGUqcbQ78XyEqK2MqLx9PxE2meWB4tuWCYctdmPRP+YIBCWqShLq6DM1WnwQTuR/JYy+K7BD0TQEWK8YijZNfT4uJ/xLM+umt50ZZrFxUm/zryPA9MrMfita8Ie5gXZew4WlYVZ98AeLJfDzJ281ncdRKwJ+85QJCf7j4O/VpUK/s/1B4uEP/o=:2A35\n" +
                        "^FO0,448^GFA,01280,01280,00020,:Z64:eJztkD1OA0EMhZ8nFpgoxaQLnUkFXaj4k6JZTrK5iWdXQkQg0VJGnGRLjhEqChrKlHhnySWivGI0sj59TzZwzGFntFuGM/0abS3j7Tdg6TPaEVGs2H847YDUg9LldTSZdRl2GZDNZxGJYnkdYirChdUv8UNl08I+JwE9NwWqac8Tgf+LL0yvzucq7+5rdwNH0OsYlVFUoRSvoA/3c+XXFrm5Dah9VrkyAuyV5PWFc9/deKzSNGame1+qXMOBE0gHTq3+6bn2KZltBs43yBOOIKuQ0rCHdLaWE5nZs5p2w76yIhJm7Ts14XF/Pwl+v8XN9xblfsccSv4AkKk4MQ==:F107\n" +
                        //length\/
                        "^FO192,160^GFA,01536,01536,00024,:Z64:eJztkTFOxDAUROdjwe+cNkW0uULKFCvlKhwh5RYRMYrEduEIXCVRCncrDpAi0hZ01CksG7PORiAkJChhx93T+Hv+GLjoT4vcF3Xf8R/LjXix9ZNOqDd1b8HPC29xtMXjMaZmLhqLaF74nrSpU+/fm1pbpCZwcYMuFu37SaBb5DJwZtftNsyRkwlpiTIKXAqnslg07KR/QuJWBJ54Xm5Js+PgX3mjppyGRrKhwWISKnDS1JXe3/Ip/8ShhxgPmDIMPtBcqE9+lJm/NHJS94d1vsQrMj//frxKaGi9f8mPCrutOPnJ73XOLzzPN6LBLHz+dV9yqmKTOsYc8pz7cSpny1aoWZi6+9BnV7ElwyjJ8ztc/+4bL/qXegOqA50A:AFA2\n" +
                        "^FO224,320^GFA,01024,01024,00016,:Z64:eJzt0TEOwiAUBuD3gNA3kPoGhw4m5QiMbvYoPYlh7OaVepQeQePCVHwtpnFzMg76b+Tjh0cA+K20+SXz+/0WLvP5RJOu7lknAIO7JOtOEaMRNxBJefKqCZpocWDH4A0zln70x5qC85t7L2US52b1vtfUprB51yEJMzrpVznGUQ91qIOui0dxw9Ut43W9fxxxsF5cFe/Fbe/KfMv9cr6aaOszKMJxmZ9Xb4IljOKuuNsfCMEw2uf79UQQ9T2r9Mmf+ecbeQDTWTQ9:69EC\n" +
                        "^FO192,448^GFA,01792,01792,00028,:Z64:eJztkkFOwzAQRb9dF0ZVhZwdSKia3iBcAKaIA3CkwUEiC6R2yy5HKTcJN+gyOxg7DRwAFiz6E0txZp7nz8jASSf9X82GlffcBu2DJlx6VnpSwpBj7rAG5CGIBNvF8qu8WSTJ8zPJjowjX3ekTP6sxCIkGCORSvIVqnfEMHK13i85EbeZg6eu3nc1eYynOys21UKIcHEyylhT5raHwvlt3237Hw7iYhViqTeDq8zgaPRDub1rdjevXLhZ27+1PTnNsY0t2RRr58B1PgRybJDBas/CJvICJK+6hH7Xw6OlWpvOPnUenuZBj1ytcmDtFn6vrSItkzZp4gzICk4QZfTtEKZ5KrP2F1DdCRI1aBoc55lbEZupU2ecVM4ER8XKsIJx1o5+doxbVuTN8IsrcdJJf6Yv/YJKMg==:84FC\n" +
                        "^FO416,448^GFA,01024,01024,00016,:Z64:eJztjzFOAzEQRb8nk2SkIOGSjpFFkRJRUYXJagtKjsBRHCvabEeOwFGiCHGQnIAyVcR4iTgCNPmV5fef/A1c8q+5PkjC5n1C+YGwAKYf9TZyQATMj0lS6LcCS4RVBlq2EF+cqXc8n5Io9kKaiOCcUdUYBtlTRE83b1942p85G8fWG8bknbA5aied0n1x/orAXpmucRurbIPfXxVVKb/+Mxqz4C9V3onOpeyaGf3w1tf7hOUYGPap6KOsslVc99+xRTYsJ+43/t/D7KijnBc0OtX/X/Kn+QZ/qSch:402A\n" +
                        "^FO96,2592^GFA,01408,01408,00044,:Z64:eJztkzFuGzEQRYcjYkEECy0RuNguYwYwUgo5QEApF1n4BL5BRhsX7Owj6CibKrdI1KVNaQSBnD+knLRuDYQSKGj/6PHr71+il7cO3XU36HA8bsrjj28bpSnMLGtm+bCWKBBklrtTnb33Wx/y8CVEevz6PTZAjHui7GMkCE7z8LvNhkRRaPah6FKCkozgykeW/SyjCZezlL4h2ubJZ9p6bBSzp+zIXhA9XeaMK7ZCYJJIhbuic9eBK+AuaU60P+DAwmk3lXD25ihHg1D+e1AE3PzFSn/C0vCTAXeFudDMrA/DEVzl60R6GAVCSqKN66MB8XO3OI/tpmGiMOWbxvU5t3w6XJwChxWVt92KlML0+ST6PiXK8uYIIV19mjeNa4bw6UjBxax9g187bmM4d4FwGpel+g0EZ/AArla/r+B3gt9A4kWlzebmF/trb0d4ffrbZhNCvMjb8+xyzpfxRg7EWvNdWw4133f9dObWZCPd2p0z9/UIpNtBMNO3FOG3Re+Uax/6UHpm9GFlXNm1PiQIYyBd/hUTVQjolfPVuhUMeewA8RDAzY1L6G7t74ib/jDC7+oXn2p/O4lXVuxeVJ/zKPxfL3z9Aej3bYA=:3A63\n" +
                        "^FO416,1472^GFA,01536,01536,00016,:Z64:eJztUbFOwlAUvX19wgsh5jZxeCQNPuqgI2xutsYoAwMf4FAXJwcHP+BSDDL5ByYm/sjDGFz9BD6hX2C8rwgIEhdGOUPT03PPuTenAFv8L0zmb+QeHuCM25XJ9foETN3vZ74NB32XdYmYyJtx2E0+3tyoMIaoKbQCUo772KIzPEUZU3saZagNESiiYbEAW9DBa5T2vTrTFetAlM394Pw2yTlesH4Pr1pVKXN2ibdJ2RvvdXFU7M9LB5+QCf/Z9BenO7RILnFDYsFLAAF4+ZwLgN2fw/4fjW4KzSUDxCC/uZqe524ogL91crpDLU8Rg6cdW78Y36UIFWWaCl5k05jo0SCE0hxjMNI6jg8lcwXmiP/MUFEauYhQpp0w6HH78blkvxLcPhICNTLBupaNATqdWJ/6M9bZHws4mbD/ylNBr6pjW/grKvI4n/eTEDxfyyPvIeh5dr88cP4lmJUiUlz+aNYNrfItNsEXufFNuA==:7F39\n" +
                        "^FO256,1568^GFA,00256,00256,00004,:Z64:eJxjYCADNACxC5MAQx7zA4YaxgMMNkC6jv0D8//+XwwCDAcYJIAqBMgxmP4AACyKCfw=:27A7\n" +
                        "^FO32,1568^GFA,00256,00256,00004,:Z64:eJxjYMAPqoC4/gAQP2BgYPwA5FQAcQ0DA/8/Bgb5fxCaQQaC5f8zMNgD+fZAmsECohakpx6EG8DaBh0AAARaEJE=:148A\n" +
                        "^FO512,288^GFA,00512,00512,00008,:Z64:eJxjYBiqgCMBQssUoPIZ/x+AMP43QOhCKF8ZqrEcSudB6RooXYGDLqDcrYMXAAAD6wkp:C8C7\n" +
                        "^FO512,1568^GFA,00512,00512,00008,:Z64:eJxjYKA7YOSA0h+gAg9Q+cw/IDT7X6h8ApR2oLXLRiAAAM5OBWs=:B666\n" +
                        "^FO512,544^GFA,00512,00512,00008,:Z64:eJxjYBjsgMMOQsvUQ/mPG8A040EIzXAAqvABlLaA0jZQOgGNhqkrhtJQ8xliKHTokAQA6+cIUA==:29F4\n" +
                        "^FO384,1152^GFA,01536,01536,00024,:Z64:eJztkDFOwzAYRm0qYobIWT2kcnuDv5sREr0IA9zAFkuHDE5P9lmVyDVadUcZO6C6Dih1ZzZQ3vj06Q0fYxMT/4o7pSLJ6oNh5Vetf4j+x4t6Hk257jgcXEAdcd2fVUk77glvaS/GkFKl/nK76G08tJ7pG1+dXpOnGJKvR18PuNAx60IAa0avT0Yagy2jZZv2lD0J0qHjvRu8zX1VLCp8iv5p8PubvrBzNNJqnvrBX/dytjfYSNI87Y/ZFzM8oinst3/P/TOHhhG95Omc59yPHAKb+8GDvfzq/Yk/xQUgOVUo:6719\n" +
                        "^FO160,928^GFA,00512,00512,00008,:Z64:eJxjYBiGoAFCMaIIMrIxsx8A0UxMHAogmp1NQAAkwcSgIAGWZzGQActzKHCA6P88BhJgeSifTaBAACwvoACm2QWg+hUg5rEVgM1nYFCgodeoDQDwpgay:9B51\n" +
                        "^FO160,1120^GFA,00512,00512,00008,:Z64:eJxjYBgowAOlWYAQBNgYOKB8CM0jIVED5gsIuIHlLQyaIHxBRoi8YnMDmO/ICJFPgMgbOLSAaOYZDRWoNjZAKEYolxGNPyIBAKUkCLM=:3D5C\n" +
                        "^FO0,1152^GFA,00512,00512,00008,:Z64:eJxjYKA2YGyAMhoYIKwDjFA+hGZsZuYHSzQxcRwA0cfZBBwgfAUFsDyLQQGYzwHmMxzngci3cQiA9RUL8KPaKAClFaC0ARp/FOAGAAO9DEw=:3CE9\n" +
                        "^FO288,1312^GFA,00256,00256,00008,:Z64:eJxjYCAIHNhZIAxGCO0ApRsYWsD0wYNHBEB0Y0MLiGZsdADTDE0KEH5bwhEovwHMbzGA8FsEmgxA/COChy9AzGuAmg+x7gCUhvEVmAi7lWQAACkZEps=:35E5\n" +
                        "^FO64,1376^GFA,00256,00256,00008,:Z64:eJxjYEAFzFCaEQjBdAMTlA+hmdnY5MF8FhbBBhDdx6PQAOE7OIDlORIegPkCYD5jnwREnkdAEWzAMwU5VBsVoLQDlE5A49MWAACikgin:D3CF\n" +
                        "^FO512,1344^GFA,00512,00512,00008,:Z64:eJxjYBgqgEMCQsvIoPIZoHwGcvkwWg6NHhEAANmXAZ0=:C8EE\n" +
                        "^FO0,1376^GFA,09216,09216,00072,:Z64:eJztWL9vG0cWfjP7wh0ba2kWti+jwwYcqUpJpTgQhwA3KzGyiyvOZQADWfUp5ObggoBmV4TD4hDkP7D+hPwJQ5uWr3DhMumY4IordYCAu8IA82Yp0iuRPJG0ixT8iuHscubbj2/evh8EWGONNdZYYw1IEJ7ShxlfyxV5NAfnP8bXYkUegyXPRI9aXY/9CHrwM8Sj2NVTZO6BYz34211mvmijxOV4oqCAjUjrUwT5EBBsl7TtFEvzALQwVMqkGCUmi5KeilnjAVuBZwe4FFaDEGQkwQQUcm+V428hk2hTUmCMxFTKltxbSQ/tiCygwNRK3BGyJQrorGIf0uNSVKWev5OeO2dsBR5e2gdK+wghhfyccbJPtCSN2cOno/NSxiiVKNkMA7JPfUkeXUBE/mOxJo1BUgSSc/KfPy7JU2f/qn3q6uHwmPz5ExeFb2VQa9ff7S7JA95CVyDKa20/iMc7AfMTY5bmGR+xGXFGZQAgD18W7QpP4GndqkHxPc8IH8azxseHf8Mvk0OTRrriehWerZKnPF+nwHswmtX1lLCjlLWanhjGepiVs/Uo07p2x26ADZwumqC/5BsWNppB12pRiH82YQD/FvCPk+/hV00BkZZt2iZKaHLYtN9epWHuSQ9QZq/P4PAVu+fMbiv5msn4WXL0IEwgUfHr6NGze6aFmUN5Lz1Dyc4QNl37Go81PvzqIoBtWwAMdCGVpUgoZE7vdyAgF7pT01xoS8u2GUjGvJ4vr/EYk4LE7ITBNqSQ/pjmjZip3RPKnYhAynJ51EKzpzLKPBndJB6Euhte5eHaaisjisQUVArItSHjWNInbkGXBHXB/qAf8lIPLeOUvTit+8svQ3uFB4/codfTZ/SZBkyaXhbnmL4gPVEN2E9xT2Z/RZPKzFFW/QPryxrrQzhdC5EOCSM99CThy53cT1gZjLvS+qisfVCWZUFU6uHXwxDK0j760j6tyAQyZjyv2geQ9Pyoq/Zh13mEMM4fBB2VP68OmUDqk8AWQtgOBWfRoKqnI7QbjM6LHsyuJxEPFb33n68YvsGEap2vGHumZC9M2Fn0KMcwVLEx3n+Ssf/MwEY4CAbDX8mf/7z5UnCuA9uEYNgFXR9AM7CWzmywATD0y2yz/s7784JgdupWVjXqojwwzaMr87l63PW5GV+x8URWlh1X1897/LSUCc+crya4Yo6ri9m8ZWv8rmDK0Y17rqoHY7WKWQx23HMt/EbN1mPHZdPKeux8PcvwJMa177vk1UX483MKf4iU/l6DSdrxc3bRri/MI0iKauycULw8JVGR19O1bpvDaUHR2i3KA2afK5We1WRM7Q5skB5m00H6MnYHyLOF9YAtfKfT4ZqSK8Jt7hWaU1JJl3wJO5u+77z6LCv1xN4+rkfJLXYtyh6L81COIlGU6Mvifdvb54RLr0cv9XeA6ZAeGhoxtaew2yE9lNMMi+0B4rL20WQf6e2jCyT7FMLnU06Xi/Okr2vUAI7PyxxE9Jv62KjFzlzg4v4DO3kHGkD+o09zUuftQ4lVCTgloy3xP0fyzfld13h1Hvaeh0NmDy+O2Xeihg3yZ3wqF9dTYmbfR6/JsjyH7uPwbF/Joh4WfIlWKbAXQn0mzxFVY8tiFs8aq+GGRrpfju6DH2Mr4//FDXpGjZa5meeGt7c7Sw/q8Sx0j2sRPE52t94+BlNn0KYCG+EigjrD+45tncs2whP3uHUH+vKb3iAEdn65idHuiQq9zwXs4/btxj44erMLahwiair8W64a7LYAztltza2wVlJXBoaLy00cgok+FefUi+Tf7SZxng+ylDFZg42AoqZJUSmWoOQhwzjvY4oy7atGRkljtCmv6vHxFmyuqZGwp94AXZ+nuM+kKAV9L/gdjsJ2BfG4rtCa+h9RbipgczDmkTEc0M9I/6NiSKnpYeyNz1MsAneIEpmi4c4L3IJ+RDzmTWRkVJNbo01QP5/wUNtCPRZkNPCyszr5HwUcr7M8C3q4EgXcohZ+h1yD9Pg27Fa5iVdKYNKzR7fME9Kzr7yejqI8RdHOTvTIferlW9GfSA+1hCM9flNaKavF6Kfa03JCaRMKRXmqap8yT5F9cGwfmNjnPRLZexZB72SXGq0++vNidLQH5B6T88KMWtceJjQ1/ag8L9k7o029K148coVtGgQ0qFlmJOrSf6D0H9DUTHP+uSD7FEJV/afi2TKpDyFB0pOImnzw33csYzY7OoZ6OLzr/fn4qZFPTMIc1t+Z/U+G5M/HrtxUbTpmwM25P/X37g3V6zyeqfZvWs9CmXrq8e+9eYxZJRFenwfjKzP3UbP0TPFMMJ9njTXW+N3iN87moBk=:CAA8\n" +
                        "^FO0,576^GFA,13824,13824,00072,:Z64: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:8EEC\n" +
                        "^FO0,224^GFA,08704,08704,00068,:Z64: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:A8F1\n" +
                        "^FO0,480^GFA,08704,08704,00068,:Z64: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:6276\n" +
                        "^FO0,736^GFA,09216,09216,00072,:Z64:eJztWE1v20YQnV2OxXHB2Mu2qG1AKNay2+Yo39xTlzYT6yAYKdBjDzLQJFf1F2TF+qCDDwJ67cHIL6ECo/Ep8D+oe+vR7dmAO0vq25I/YgFJCz7Aa3JJPT6+Hc6jBFDgQdD5ICyAdJtq5Fjt/jzQ5xnFfXgavSGdoqc+Jz10w+c8RB/+uPSh2eMxzbL/VbP8awslH0HVFOdlZdz4TW3lCpq+mMojAQnaSJb6eug1HVPlCEgCJQhtyedYNz4+5eOHZKcLQlDVACEKejxREKkXQfTbOpYQnqBCVJGKAh6rdSUEqu4NltDADM0XhwrYvj+P+aB1m2Q1SValr9nW12MU39oO9nh2UHQ2sdUJ2R/WA4jQUjturNZCKVjwLD2shkTS17PDWrTTw/6I9iGtsneQsItWu/qRegYJ6yFU8Kanp7GD6dlP2M31nMZxgNR1eqjbqCsUpcYMPewvX3nMH6q48hlMDf3h6Zl6yuyPUNCvChVB5H9N0eoWCl7KGLC0uqsiN+q4GotETa8f+MT5o6HV7k+0X0tNlYBnEexmQpKy+iGpkeeSWfUTQkMJ4/sXvdvcu/gRn1+UV95ewYkPC6IpqPzqwo167+wZPPX9dCqPBq3BTs7NwMwDAMYqA2ZsCs3UMxnVG3hSb0LNDTz3g5/mRNmohpszVupW6KmbBT4Q9GDLXjs2iIbtsWkzutPrNHlUZTibYFGwOPWTE3v30XMD+nqGPJ1retZu07NEb/QR3zZ53IVBE15KvW+1PT+CiqQll8fvvF8O/+Lw2lGH67DtWevZWuq1CbaXKJVaZ+rDRyvllwgxBoY7f4SI4uj7zoFYfYnlrgo95Ytvgx+erJVEsKdOtgCDyMd0vxnHnz+Kw/1mdBTmgjgZWzLrvdx2+KrggbZaUktW+TlMQEpFukL95iyzaCOl+ZQE2mqj9+BWa59hhMC5aLQxERK34aiz7vGkijriUC3IsKM2F/s8QkULYGJlNsNQVGPu1Ll1upaFJce5Zh7rLieN1QnYROtTjm+WZDmthqmprbU1yoJC1zhSg8yfRv0ppifQRdVomFddTyGUzNlByU02nvlIwe/qLDxZLA30HARdU6cojULRqJMpLeTsLpzcJV1bzlaeQ9u61GI9vE8uHfJI6+vh2CLS+SZVpOz582nPH6gaIUR4KdB0DjJ/OMhQqV2hVLw21CO4f7M/EZ+tnD+iv17INZIQG5RKt2gS2R9LXX6FWs3WUZDG0fViN+u8SPY4WaoriaJXPy/weV4/oflCCM31Y47+zOonbQpfKa6XOKZ3PZ6YX4diVz8mjWJ09eNn8yVXBcuw/eV3sOSsOQZZhWTj7Xl7eQOMXE7ZJK/lIj7Htuedb3C4/82LvE1k/5kWPYEAg8NdMfncj8Hwc8d/w1YwwjP+HIqbn3INWbarzJ+RjsDvWstpdDnk9PKSV2OfHoSWUSpI+f/PMMEz/qJ3a8fRfKGR3dURPdN5ZuiZPFCgwEeArSyXHjLkPOsklX3Q8GF9KFCgQIEC1zHjp7H/PEbf4B6UXFfnA55g1+Xi+w1o9JzubD48aMxceOb1MxBmXzDmATsXFgznQjM34JTvXu/Fc5efYe7CU7r9nDthTg0Ibz/lTvi/9sMCBQoUKFCgwMeIfwEcKRS1:8C46\n" +
                        "^FO0,832^GFA,04608,04608,00072,:Z64:eJztlb9qG0EQxmf3JrpxULFKTKLAgUcX94khD7ArC6xCaYNLKfgBZHAdVuKKq0LyBIE8yckI4UKF3yCGvIDTpQgmc3KMV7ZjG6QiMfrgBu2fm/3p27ldgJVWWmml/0rRR3lOswY1TcaLJDoGB2a8Vd2RsCS2RRRhybN4Hq0Xz1EK1XJ4liVU8Sg6XUIeIO0XTyM8plX8Q/sVadO2y9gvJOIlpIF+fPx8Cfu10gNQZRa7QSyFF/HvZWuD6aLzb4WDeG/NTb85zxUeeyPPXO8CPIHU6INTRRL3DOxjxrudpFGowk0OkqqbwkESf5bDLy52Jgr6LVSzG6sM0E/eFvYSSLPW8tBXAPJDJtIpax6meUosdClprchbL9S5BkXa+EYZIOfHHPpQqzg026ZnFLYybrW3Nw1u2afowGINXX0LwbQsNZXCGIP/gd2xCXhgTVshYVkYeMjNtt8EmpmWA2uEEgSI81fff8LcKajhiII0uCY8NOr2jMb1cdd1RnumWrPv0E5tq4L2uCY8uz1q1mrwaBzy2A6FPMIx45EFaMiQe5Yufi884hIIjwZDTGlphZ/jIQr9eaEd1geghCfJ2B0NNg09kYbNrHNoPwlPXULTbQG2Ah5tbT3AkcVn/mjhQs8F+ZRLf1D8Achm/lRyjg49n4Tlpc4gpyDNxX45o6p7YzPpuEaBb8wzdFXrigNXbyCouoHJyBW4HuSpdKfmhvoZCpkdykWgWfoolcph8SMt7yotzaH389+snq8fNdI736Se41+wn32J2+2ErSrUISYbPwqVJfEAJQ+rSA0AqwEPbKgi9OdW+fO7kwRaye/qXfPndLmKxbJQALFV0QqmGNQzlLUU1s9tYtmz6Awo8mXr9dyYDPmXJ1deMAHH+UuzA4j7f8YRrql/vQuA7tV1Vf7uKSs9aP0G5reHrg==:1A1A\n" +
                        "^FO0,1184^GFA,15232,15232,00068,:Z64:eJztmTFr20AUx9+djvgG45whg4ZQnkUHDx2UzUMpJ+OCx34EE/oBOnY8ySYIEkIoHVzoELp169gxhVIMDf0Mrrt3zlBKT06cSLHkSJZbu+F+huPuJP7+37vzPZ0MYDAYDAaDwfBfIgYN3l66wKmGfdwI95cucK3jNxgMBsPfQQz2rrNF8Zo71YgSxeQ6ZRSt4XoDYDAYDIaNZRXnF8Ea0F66wLWO32AwGAybS4n8IitXGvZBlLCWKnpNX5Ufg1daAtAp7wNAllZAp7wLYOUlsImlNYQsLQHNsLxGRYjyIgbDpoPrNnAFrlSN500mWINYD7Rq+BP6wfS3H730I9ev/xbUBIFYD+kIUofXXrH9Axsq1iKBO2mpsHFSbNQy3iC6LSD6FPIh4z7AQkco5b4opJHcw4k2IuBEuCV8EF/nODW0W4U05uIReYNnxXxgohm4bQIhFpsXsZfwodcHKbw+Ln4nND5uq29wGNyXHLWKcdgr0Lgv8fxf6GZeUbH6ollRMFqowfGyvnh1yIVX8/lYPJaZj7hGIokwubtb6Yr0VAPgA+hN2NPFZ6vuk7dQV1ONg+jPpVlhDZ13zZHgNz2xAsY+h5Dj+afQeh++wmCfV0/nzW6BR1yZNVRJGDAO0oNpZmHIBM7fRbnysZu14Z9qH5Rj1wHqhi4OGnQWnDiMKYLdLB/C1/v5jp43BkIIjxGR+rxeVcc4yph6MvQ5oQ6OnCq1h7Y6ajh2yq0sOhhlxYMAYVFeiXxoI1I30nzoeASZ8aC8r+Ph4Jc2UFCIB7SRGg/uZfu4dOLpqdPplusG9ewUDb0+KJ5/yPLxVa8P/82ZH5KAT876x+ponOKjt1uFrOTFWP2BXqJMyh50LGKdAdSzHG8GfPFlhNlWpLJvuvvoXLnzjhw+4C4fhhWj1m3gCvXvvzK5pv1wwulF7VDxsfp+RNPSy1xBKd+OGdfPvG2dO2BHAjwVHZrrhSEkDxNU63FrC0OHY8Cf5xuJ9oG9W30E5InOXwwe5g5IzMalD0rPhraNqvo4tw+FN00mp0GWoi5kh1Xz2kic56L/6rQPVOIUg7waVGeZ29u3Tls6HtHs5NMgv0RcI8pX3BrUQtQ++KO8PjAeD5h+f7Q+JOr1kU8jhSf0pdtX+GMyHuT2IXGut/BJQMr5PlXkrYael5aa643SZ4pyto2UVFPTw5lXLk4RHwaDwWAw3Hv+AC4MQV4=:B559\n" +
                        "^FO0,896^GFA,09216,09216,00072,:Z64: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:D3A0\n" +
                        "^FO0,992^GFA,13824,13824,00072,:Z64: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:AC38\n" +
                        "^FO128,1536^GFA,01920,01920,00020,:Z64:eJztkM9qgzAcxxOUeClRT8tYur6CsEuEgSs77DVa+gJKH6CWgr153mnPYlB69BlS+gKDXQoDnf1jiYl7gA2/JCAfPvnmZwAYMuTXWBCAGQBwJ7HAiE8MlBIjMAZh4xUSY40nGiZ5aAKRO4uycTHOXkdXZudJIL5FVRJxwBdmQr6l0YfHCzLNR+3pQ8qaxUuy36Qti5CzMF1e0Mi4ecuELVI/LsnTBt+8LVma7rqgD0anL/Z5yiZtH/LmiTPPwil6fmw9FOwrNvsSK+uI7fo63x14I2GUUfhutkwKlr699iV7WPNAGoMSY1rz/wr8NDKKUIcZR1vUKe4wi9KQKx7GjOVJl5kmuec7zfPXJVY8x+3xGFf6zvcm6ny1Nh8QK+0/zlHZy6m0Us422650T43oYUP+en4A7zpdqw==:D0E2\n" +
                        "^FO416,1536^GFA,01536,01536,00016,:Z64:eJztkbFuwjAQhs9g0aOK6I0ZUBXBC2T0aKoMfQyepHGAIQNCjDzOITH0MYJ4gY4dezGIuFFbZUOV+AdLn37/Pts/wF3/T9av+srOr70uPo0VZ5oeeKwOJPvMYmSOa8TktHhMWTYoBJsREWhd++BK5BWm6MpBOhdbkbaaaAiv3lfFLuJ1nAzde8/Yiy95BfbHPBQY13kqVt43+43h8/y4nk8zxTZ7y+V+Vf7R8T+iFusWDzqeczs9O9OvOLlyAi/yPQ3P7RIxYDtTdKnZy01Bemh8kfQQ+BPYRn/keb+Unn6fP3r67FcBnw9pPSLI+wby6jsHavNdt9EXwJM7+Q==:28EA\n" +
                        "^CI34^FT388,1771^A0I,20,19^FH^FD\n" + GreekConverter(C[7]) + "^FS\n" +
                        "^FT160,1802^A0I,20,19^FH^FD\n" + GreekConverter(C[6]) + "^FS\n" +
                        "^FT487,1803^A0I,20,19^FH^FD\n" + GreekConverter(C[5]) + "^FS\n" +
                        "^FT368,1833^A0I,20,19^FH^FD\n" + GreekConverter(C[4]) + "^FS\n" +
                        "^FT421,1864^A0I,20,19^FH^FD\n" + GreekConverter(C[3]) + "^FS\n" +
                        "^FT438,1921^A0I,20,19^FH^FD\n" + GreekConverter(C[1]) + "^FS\n" +
                        "^FT114,1949^A0I,20,19^FH^FD\n" + CarOwnerNo(C[0]) + "^FS\n" +
                        "^FT462,1893^A0I,20,19^FH^FD\n" + GreekConverter(C[2]) + "^FS\n" +
                        "^FT251,1949^A0I,20,19^FH^FD\n" + CarOwnerYes(C[0]) + "^FS\n" +
                        "^FT134,2011^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[1]) + "^FS\n" +
                        "^FT120,1981^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[3]) + "^FS\n" +
                        "^FT465,1983^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[2]) + "^FS\n" +
                        "^FT126,1639^A0I,20,19^FH^FD\n" + GreekConverter(D[4]) + "^FS\n" +
                        "^FT127,1670^A0I,20,19^FH^FD\n" + GreekConverter(D[2]) + "^FS\n" +
                        "^FT387,1516^A0I,28,28^FH^FD\n" + GreekConverter(FineBasic[4]) + "^FS\n" +
                        "^FT308,1321^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[8]) + "^FS\n" +
                        "^FT456,1320^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[7]) + "^FS\n" +
                        "^FT121,1344^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[6]) + "^FS\n" +
                        "^FT362,1346^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[4]) + "^FS\n" +
                        "^FT394,956^A0I,20,19^FH^FD\n" + /*Πόντοι*/ "^FS\n" +
                        "^FT525,1220^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(145,194, FineBasic[9])) + "^FS\n" +
                        "^FT527,1244^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(95,144, FineBasic[9])) + "^FS\n" +
                        "^FT527,1271^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(45,94, FineBasic[9])) + "^FS\n" +
                        "^FT499,1294^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(0,44, FineBasic[9])) + "^FS\n" +
                        "^FT485,1372^A0I,20,19^FH^FD\n" + GreekConverter(OfficerName) + "^FS\n" +
                        "^FT150,1576^A0I,25,24^FH^FD\n" + /*Πληρωμή*/ "^FS\n" +
                        "^FT420,1576^A0I,25,24^FH^FD\n" + GreekConverter(FineBasic[5] + "€") + "^FS\n" +
                        "^FT447,1639^A0I,20,19^FH^FD\n" + GreekConverter(D[3]) + "^FS\n" +
                        "^FT488,1670^A0I,20,19^FH^FD\n" + GreekConverter(D[1]) + "^FS\n" +
                        "^FT414,1698^A0I,20,19^FH^FD\n" + GreekConverter(D[0]) + "^FS\n" +
                        "^FT429,2010^A0I,20,19^FH^FD\n" + GreekConverter(FineBasic[0]) + "[" + GreekConverter(FineBasic[10]) + "]" + "^FS\n" +
                        "^FT471,2077^A0I,20,19^FH^FD\n" + GreekConverter(B[5]) + "^FS\n" +
                        "^FT443,2106^A0I,20,19^FH^FD\n" + GreekConverter(B[4]) + "^FS\n" +
                        "^FT385,2137^A0I,20,19^FH^FD\n" + GreekConverter(B[3]) + "^FS\n" +
                        "^FT121,2166^A0I,20,19^FH^FD\n" + GreekConverter(B[2]) + "^FS\n" +
                        "^FT485,2165^A0I,20,19^FH^FD\n" + GreekConverter(B[1]) + "^FS\n" +
                        "^FT405,2196^A0I,20,19^FH^FD\n" + GreekConverter(B[0]) + "^FS\n" +
                        "^FT422,2264^A0I,20,19^FH^FD\n" + GreekConverter(A[5]) + "^FS\n" +
                        "^FT423,2295^A0I,20,19^FH^FD\n" + GreekConverter(A[4]) + "^FS\n" +
                        "^FT403,2324^A0I,20,19^FH^FD\n" + GreekConverter(A[3]) + "^FS\n" +
                        "^FT417,2354^A0I,20,19^FH^FD\n" + GreekConverter(A[2]) + "^FS\n" +
                        "^FT456,2382^A0I,20,19^FH^FD\n" + GreekConverter(A[1]) + "^FS\n" +
                        "^FT431,2415^A0I,20,19^FH^FD\n" + GreekConverter(A[0]) + "^FS\n" +
                        "^FT325,2686^A0I,23,24^FH^FD\n" + GreekConverter(MunicipalityIndex.toUpperCase()) + "^FS\n" +
                        "^FT477,2560^A0I,23,24^FH^FD\n" + GreekConverter(MunicipalityShort) + "^FS\n" +
                        "^FT198,2562^A0I,23,24^FH^FD\n" + GreekConverter(MIDBlanks) + "^FS\n" +
                        "^FT366,615^A0I,20,19^FH^FD\n" + /*X5d*/ "^FS\n" +
                        "^FT459,690^A0I,20,19^FH^FD\n" + /*X5c*/ "^FS\n" +
                        "^FT71,715^A0I,20,19^FH^FD\n" + /*X5b*/ "^FS\n" +
                        "^FT206,715^A0I,20,19^FH^FD\n" + /*X5a*/ "^FS\n" +
                        "^FT543,717^A0I,20,19^FH^FD\n" + /*X5*/ "^FS\n" +
                        "^FT126,743^A0I,20,19^FH^FD\n" + /*X4b*/ "^FS\n" +
                        "^FT248,743^A0I,20,19^FH^FD\n" + /*X4a*/ "^FS\n" +
                        "^FT540,818^A0I,20,19^FH^FD\n" + /*X4*/ "^FS\n" +
                        "^FT541,872^A0I,20,19^FH^FD\n" + /*X3*/ "^FS\n" +
                        "^FT274,949^A0I,20,19^FH^FD\n" + /*X2b*/ "^FS\n" +
                        "^FT504,948^A0I,20,19^FH^FD\n" + /*X2a*/ "^FS\n" +
                        "^FT542,974^A0I,20,19^FH^FD\n" + /*X2*/ "^FS\n" +
                        "^FT540,1151^A0I,20,19^FH^FD\n" + /*X1*/ "^FS\n" +
                        "^FO13,1858^GB410,0,1^FS\n" +
                        "^FO14,1887^GB448,0,1^FS\n" +
                        "^FO15,2071^GB457,0,1^FS\n" +
                        "^FO14,1916^GB425,0,2^FS\n" +
                        "^FO15,2101^GB429,0,1^FS\n" +
                        "^FO13,1694^GB401,0,1^FS\n" +
                        "^FO14,1827^GB354,0,1^FS\n" +
                        "^FO13,1634^GB114,0,1^FS\n" +
                        "^FO15,2259^GB409,0,1^FS\n" +
                        "^FO13,1664^GB117,0,1^FS\n" +
                        "^FO207,1635^GB240,0,1^FS\n" +
                        "^FO208,1664^GB278,0,1^FS\n" +
                        "^FO14,1767^GB373,0,1^FS\n" +
                        "^FO15,2289^GB410,0,1^FS\n" +
                        "^FO14,1797^GB146,0,1^FS\n" +
                        "^FO209,1797^GB279,0,1^FS\n" +
                        "^FO14,2006^GB121,0,1^FS\n" +
                        "^FO15,2191^GB390,0,1^FS\n" +
                        "^FO15,2131^GB372,0,1^FS\n" +
                        "^FO14,1976^GB105,0,1^FS\n" +
                        "^FO207,2006^GB222,0,1^FS\n" +
                        "^FO208,1977^GB258,0,1^FS\n" +
                        "^FO15,2161^GB107,0,1^FS\n" +
                        "^FO161,2161^GB325,0,1^FS\n" +
                        "^FO15,2319^GB388,0,1^FS\n" +
                        "^FO15,2348^GB403,0,1^FS\n" +
                        "^FO15,2378^GB439,0,1^FS\n" +
                        "^FO15,2408^GB416,0,1^FS\n" +
                        "^FO97,2588^GB112,0,1^FS\n" +
                        "^FO97,2552^GB112,0,1^FS\n" +
                        "^FO125,2552^GB0,37,1^FS\n" +
                        "^FO152,2552^GB0,37,1^FS\n" +
                        "^FO180,2552^GB0,37,1^FS\n" +
                        "^FO97,2552^GB0,37,1^FS\n" +
                        "^FO24,1560^GB0,51,2^FS\n" +
                        "^FO259,1560^GB0,51,1^FS\n" +
                        "^FO170,1502^GB0,51,1^FS\n" +
                        "^FO301,1560^GB0,51,1^FS\n" +
                        "^FO535,1502^GB0,51,1^FS\n" +
                        "^FO535,1560^GB0,51,1^FS\n" +
                        "^FO208,2552^GB0,37,1^FS\n" +
                        "^FO220,2552^GB260,0,1^FS\n" +
                        "^FO170,1501^GB366,0,1^FS\n" +
                        "^FO24,1560^GB236,0,1^FS\n" +
                        "^FO170,1552^GB366,0,1^FS\n" +
                        "^FO24,1610^GB236,0,1^FS\n" +
                        "^FO301,1560^GB235,0,1^FS\n" +
                        "^FO301,1610^GB235,0,1^FS\n" +
                        "^FO252,943^GB27,27,1^FS\n" +
                        "^FO483,942^GB28,27,1^FS\n" +
                        "^FO106,738^GB28,27,1^FS\n" +
                        "^FO227,736^GB28,27,1^FS\n" +
                        "^FO52,710^GB27,27,1^FS\n" +
                        "^FO185,709^GB27,27,1^FS\n" +
                        "^FO437,684^GB28,27,1^FS\n" +
                        "^FO344,608^GB27,27,1^FS\n" +
                        "^FO520,711^GB28,27,1^FS\n" +
                        "^FO519,811^GB28,27,1^FS\n" +
                        "^FO519,866^GB28,27,1^FS\n" +
                        "^FO93,1944^GB27,27,1^FS\n" +
                        "^FO228,1944^GB27,27,1^FS\n" +
                        "^FO519,968^GB28,27,1^FS\n" +
                        "^FO519,1145^GB28,27,1^FS\n" +
                        "^PQ1,0,1,Y^XZ";

        String header = tmpHeader;
        return header;
    }

    private String createZplQR() {
        String tmpMiddle=
                "^XA\n" +
                        "^MMT\n" +
                        "^PW561\n" +
                        "^LL0200\n" +
                        "^LS0\n" +
                        "^FT195,202^BQN,2,8\n" +
                        "^FH^FDLA,\n" + MID + "/" + DateFirebase + "^FS\n" +
                        "^PQ1,0,1,Y^XZ";



        String middle = tmpMiddle;
        return middle;
    }

    private String createZplFineBack() {
        String tmpBottom=
                "^XA\n" +
                        "^MMT\n"+
                        "^PW561\n"+
                        "^LL0879\n"+
                        "^LS0\n"+
                        "^FO384,288^GFA,01536,01536,00024,:Z64:eJztkD1Ow0AQhWd/8E5hoYmE0BYrMYgm5dLRsTScgXK5gQ9AsQKk0HEFjuJIOUC6UPoIlBSWAGMbOXKo0kX+yjefnkYPYGKE/SfHPXznN1A8FKdi9mHe43JRd3nOb5EfWYJEnHNKvZ9T6aOgmXCabFgm3R84ESs6l1YyDny4LKlQdJ+R8vZmy0eWGJ6h94/bnEobXR5WJLr+s4EPCUlS61+3uW/6j9KdNc4Wjd/myBWzlGmOmXrhtPjsckfrEG8N+JPcDPuRX5/qKwWeKo0XX9XfP78IIADd7Klhi/SzJe7Yv3QQNO30Q+bHftAQxBrA1OPT2J6YOGy+ARhANw0=:39E5\n" +
                        "^FO160,800^GFA,03328,03328,00052,:Z64:eJztk7FuE0EQhv/ZXa0X5eRsOheRsskDRFchQxHvBR7kBBJ1HgDhtY2sKyh4BB6DjsNIVDzEKW5SpkQo8jHjw3ZNARLo/uLXnXSz3/07M0CvXr169foXNG1/T81f/LexUkPk0xyNDVRpBIUfCBpZmVH76T5pvAGS2C3usORHwBsYhJlHMp4cwYMIILhoqIVPRDN+Y0PEczIknJFSCucLh6Ry7TRzh0gByUUHjTxphfqoYcMK3+gXx3JNQRnBesWcsc0QA2ofDZRQDcqTUzZcHzhWaaz0UMM6y5xL5VByTV5msMLJEMolG3O+KrvLQ5jJAcYZ5hg+mz+IeZSgnMfBkxjneQZDqctD7ULrDdTxA3MuVI4QMGlKBycch1GaVRgx54syuzyMIX6zJmPOufVSE2IUInM8TojNc57CdHkmG4U11noIax1zwjxITd5wnnHHeZJSxc/rdv1unwcFpnxxr5VnDqKcCV8L5+w+TXecsxpF2vcHL7Cmivs0Es7FNs8oxW2exWKXp16hruw+zyt8xntjSTjhsZe7foTu3ub7e0vXiPW+P7jEAh8zqz8I5+m2P0ep689S+pPmYsz5fshzihlu+E5qyZM5mYMBujl4y3MQCyYW3J94c8gzYk6TKTTCGVqZN4tyO29zmbdxxSaccMjj+ZTSWkTJY4kLuSbKgHhuHWFQs0XOU3acsZo0DhPemw3ScbvdHxkKvHzY7k+LK9mfK0xu0YRBm/74Rvfq9X/rJzqXtkY=:A6E0\n" +
                        "^FO32,64^GFA,04096,04096,00064,:Z64:eJztk01Kw0AUgF9MyGzKpMsII16h2ci4kRaRXsPiBV6poLvGH8xGcoLqHdy5TEkx15gSqNu6i7Q0TpLWFgoOddv5mE2G9/Hm/QRAo9FoNJq95JaBEe0iEAAZ74Lhl5/Zzr4PAoAvfSP6R/4mQH1liVr5np38zfxT1ugMuTec9nIBuA5zW4/bB9rkgNRuIiSH04snu4hbMD6OB6cPmfTNxdrnabh9IH9yCO2nc2eW5SEt4o5l/tj2EiZ9QtZ+vWNvH+gmhFgY2wxZ96PMT565iINGSK98QU1fUbyYOAHF1OacpdX7zSK/+TayuncRM1R+s+0ENUxIw2WtpT+Q9d99h/Rr6LtKX3y+BOF0Yr7zbFzVbxT5ja+RhUOfKTeh2T5KLGyTRh1bSdlt85lfxg88pJep7yo3QUx4WtXPx0HhGwHDTkJk//EK1Pm7Sf2cYWwxdKv6jYXznc1e5fyxDzVl//N7ni5Qzj9d1X9NvKzHvEjgCVimQpf7h50RNpFEolXNf80ZbO6PGrvyf2cmf8S5Ujr4y4dsF5/Sylc6m9hbN8qeK3yNRrOf/ABEI6wR:25B3\n" +
                        "^FO128,480^GFA,03584,03584,00056,:Z64:eJztU7GK20AUfMqC1CzrK7cQ0R8ke6TZkHAiVX5Dwj8gc4077SFwGqNr8xkpU65Q4V+4cs3CtTFco0JIec9rXyB1ihSaYprRMJ63Y4AFCxYs+O9Q77mSRQbWZp6lqmuQbBo9zgYVlyaQ7aScoFhPBWecw5dT8M2t0NrVzrncs7fKN0hubtCHitO5q1utJ3B+qjRrBfjhksf5Z1lYRNYzTnlIXUd5nFeyqkrMi6EoRyUp72safILzO320lbVR07TaE/XGt8ygorTTvpVSgHOQD2zP8fvgi9EnS6PQx1hCeYk6QLlLSFGVlV3IKyA7UV58uYsQItfeaOdYw9gPT/QM628xKfreph77nfO0on7JxZeuxg+yhBtrEzZHp65BOsCmiUnBH5Juzvesj2NaUV5kg0+uxk8/Mc/amM1mODZIz3DbtKTIo+F5S77ZT9zl2C9ywcc5v3265DHKY+e8hz0paWfTLPRbT7zACwP7008Nf/Xz4B9GUoRz+v7aTziP/a6+BK+2LcM7XO/ZQWeeSEmsvdlc75nYHvOud8GueotrsRZ6fLqO3u/Y92YgZWW2p4/h/fzEjMd+7PvrXpQuajvTXoQ6vCDZOjIVKbhOm++y4bxPZurVCNFln3UrcBXvYA77/PVC+7xj5GtFYlbmfZsPU8jLHyd44/7N/2nBggULFix4xW/cKgDk:4F33\n" +
                        "^FO64,544^GFA,00384,00384,00012,:Z64:eJxjYEAGBQxYASN2YQbGCwWFE6cfYBRwAHIkODo6OhgaFEASPIwsPTMUHcHiDJwMLQ0KjAogCRZBlvYJgoIGYHEFhsYGJrAwSLxxgpAgA1icoaMJqp6RQ6CwdYaJINgcDqBEBwMHWH1BgcCEqUcg9kIBxCAGARBhABVvAGL7A2DmgwYkdyOzBxgAAIjZG2w=:B885\n" +
                        "^FO224,448^GFA,02560,02560,00040,:Z64:eJztUbFKA0EQndms7qSbI4IXIriRILG7QD4gC4doZ6llJB9gYSOkWGOKIH6En5JCxHTa2+QT7GxCnEvuYtRErOXewiy78+btm1mAHDly/IZT0KsTzSREgOmR1gnYRZjhaJ1ebc7L9AZ/04sGHFfKZmM6ZINgdNDFbTQaX02tAq40Hk/SqgF4GpD3LZIAyap731eK7hPn50A+fZdjGnEMrZAd8EPQQ9k3dZs7Re24w7zgAR2LD1+XIEuhtXAL1loCxZZSHoYch1fcdP6SGxDFAWLUwEA3omYZHDe5kenJCMNET7z6eY+2BzM9SVmyKY/EX5lrCDFXwSZ6UEXWcqYTxzVqZXoR0IGUg1ryZ73K/EHqrx/F4RnHJSjM+72RftEeFsT2heP9MPPnoVeX+SkZ4+f8rALq1cXt3sLf9cvbqGj0FqIxQ7M76cr+2HJ3Bp9w+tymADK9eTvrvvkbdlbccRLKv9fZ7I339MItZ/VPHsIKqC+8HDly/AN8AL7JSf0=:6EE5\n" +
                        "^FO32,128^GFA,13056,13056,00068,:Z64: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:02CD\n" +
                        "^FO224,544^GFA,00512,00512,00016,:Z64:eJzNj78VAUEQxr9dc3cbbDCLULDuKUAJy7sSFCBQwMkFd949KQWIVOICBcgFSlCCYUlknsQkM7/55i/wtfFvelhllnjZJLeLDVYSBwMNU2kc2AuqHhfOUZGa2nmZpQGPNUZS4ivRU8cLzKhPdJrv/FPPfQ4DbPz2Kux4GEoaEFWOw6t/Ko2mghFWXZ60Z1qktrV0jPtrhaCx1ngsKDN0uGmScq+Sj9s77x+iU+rFPjodA/XmAcYx/b92B/KbHFY=:5653\n" +
                        "^FO32,320^GFA,06528,06528,00068,:Z64:eJztV89vG0UUfjP7kh2qKJ2xkbqVXGW8BJHjuj2knDprb0mQECQHpByQ6lQcOG4RxwCzTqRaFYdw6yGISvwj69RI8FeAEH9AKg5copY3a8f55Y1dFSEh+UXZHc/OfPPtN2/fewMws5nN7A1MtgHMsM0spHTNzw3wRy3GpoO0APx8j5iMoeCUB4Ib5ZXhl2Lo6XmU2QLkt3JTM5/mbAfWmmmaAusdfsx8X95gUPOz3EfJ8sQHH39gfCyG0He1hlBbbTnYP4RbNQwjLsDqDoSCa2pw3VmxK/anEgzQtTVpWvppI94FuIOoEGQlzaTZ0jFsy0RCIlEmkYmSXikGvf8zC90TKYiIcEN1oYPVhWKC7kToolQjw6aETEkVk+oVRIkgKiam5U0MZp94LEsUCUEs99h8OQ/tKGjC4BJ0F8RDatTBMd93PDSIjibLyt6ljUYp4lFVhFGlV9+A4A7xqCMRi/eXCx5BEuno3Yz54zF08drXxFAPp8JAD27dg1BPocf2gtFgMFhWHUCOKfGQlXYvaMdAf8F2PtgXaWSSleohdChCpDstEgohyRucf+gMnH+QFnDiHxaejMeI0pt9rOEN56fsMIU0Yr3nD/z+e+SqeQ218XGH9V6twipm8OUVepy+6Jmvw46fMNnIS893mCkm6fM/hft9Kn+ZY563Ylk5igypEScRxDHwPDsig1cDnVFhLA87FZ2ZvbG5iFGqtoCJ0dz5Qt0O7mMtGP7/Bzzabr4ZdZ3/RiZg8EW+6Nm785pH11/C0o9WZ8fw2Hp/HQG/Z+URBfJFiiEReyKgLppyz13sactheJtMzTUS1WYHG2iY3XgkaywwkVRYYabxNoNoc2FLJsn7ATZqLdm/cCl4kBCCApTuYJHSumCL4EnhinMIr7n2HuU4EN3ydzExU5S/t2NMFgyD9U2pmDTQVrwyb5oU2zfM2rrCJFgvxwDOpWBacxfCOXQt1LlTlPGQ4iZzvYweCLFSiuGZGNVNFtVbHoVqHz78Xm6/Ih6UY6q49Fy9ot2Kf1HeelDuIHyQQ3QY8iI0dF2eHeSWkGQQ1rW6jscVGJRESQ+ZMDTS4NLPc3T1c1h7wThKe4TUH3+kXibyi3IM3aHSoiMp3HGXabtsmOCJGgwo6Y6IOMhyJ+ObrOr8o68O4oj25f6cjLYCp4e5jTeZ3GKwickm/6DxtBxj8fp3HtydF9d08dt5gXMUSqYhERTkJYvCs5z1JpdhznLikcxJGQkDUuUPmc9kVDxhE0L4qVH+tHu0S5FwH50NnXNEw0eTZw8+0xiWfuszdSz93Pv7hW3dQ5YOP+DETEuk3KJpeVxl/wKPmb220XlpVE6wk0ZRdJhR1mFt18Vs6R7aMX1jSh5+lcee4TFq8As8hhjT8OCXGtPxMBmy/nJgag2W+s+/SlmeNqCKaseP80erTLKjGrKjA3aItx5XpHwA8dFljCJeCRfVuoJ3Kb52tQ3dok+0XelQxexiCZXJEHapYqYcdDmatDME73Nh4nqGykOgw5MCjqojE7UrY6rPY4SFjMr3uFuRG+08uRxQnmVUPVChbsMOD7kLZEVwpbpcCI/Kdzon0KiM+Fh7W7hS8ZJYTBGP+M8gzlZbXqOKEpTXAA8ViwzlnnckugFzu0F1Ltuvyg1jksFunTu+0DK27Y6OnGs+UGaQd4XLPUMeewUP7jLvUI7FMzyAljGkR7ZsuK44HrzQg/LOWwzadH6jAWhFxZ0x5Z4x6wMe189su9h1opAeOnR6wEgP6VohDHi4/bG0N6BPeJzFwBbpQf7RihqobtNRUmED2ILa9WlfoljOBy10ZBsYB3cUptZc5uFWboYr0Fwh5+DO27uDAo3STuEfovAP1O6g7diI0cQTQ7y1xPr1X18cRzL1m9iel+m3OfS//kzcz3d8/5iJ2hKkB0t5v+YffgOfQDLdGaSwsuTGXwODKpGx5tXK5+iLPGzJwN+nxpAXz9GmfGopxoTnM5vZzP6n9g/tkR85:511F\n" +
                        "^FO64,384^GFA,06144,06144,00064,:Z64:eJztls9vG0UUx9/MPq+fkUneJDm4kqGTTUE5bqRKNRe0ay+58x84EDi7P1RxyGHstMiKenChUnsAqeIvsaUI+DMs/gKQOPRQUd6sm3gdr0PFDeGvo/VoJm8+896bfc8Aa6211lprrfV/VzCYfavipH777cDKs+UffuRK7DHwz2TRXgHme3srTpJ4Zq/K7C9Z5UsX6NV8XcKHGV/UBYLEzkal9qcDsK0Ncq092WgKU9uyOg4G8ZQcqH5of879nw4DO22clmww+RJNZg7jzz6ZsOIKJ9mBemY6H3AYJ3DXHB1UYCdO+X7zK77/LMXlDSKAATyBgW0T+I+2A4SIqJ67HbGTGaAxRQRDS8v2W2gUZEZxyoBIycSmqLaRkFmC8gV/C8ommPIxpiNTwtdglaWashFJBMj2I60jP5IA1Gwkh1NWgxtZcGXxg23hx8kNFe9xgNhL+7c7wcE29uAGQy3eM49BxR1Uo+NKfwVfcksEM74kyWptc75EQ/gC9fzhKj4Ln73/ifefk8mtXZSgMDKBTKqKREH8r4v/XMaPyA5C4kHcBk2etb+HWpJCW6SVhJCcZIT6QUSK7LI5Hu2YDA/j7DL/sfnBdCoc3sbAmEnAkhzJP0r+d0ryr9z0mxbRuBVtvpb71/3jlRX3NXRpDDAO/UVubGxOn4Cd1oPTv8oiAP7+/5Pwun8puVZLsquX+H13QVlcKLyUmFxzgGtqwIUCt3qN55SV/HeJ0VrvInV6eWEQgmA+P/ti/ycLiZR/fDufLGygCxdOr0q9XnkrUBf4BftFvk7E/l/yeYF/pbNAVSM3uFntPR8/6N0LkNU4uwN38Ec1GfeaVakxvPvn3U/T0T3GRlWdV/TNdFLc5DfP10RDqXFnOcfty+cnecV8hSffEygEd+bHDkjb92zxAJGUNk7N+YtdVKH2lS6T4pdN5CSUkud/XFdh8jRkJMCoFnbPTZEvFV/2ZecEI/5LdxlYUZ98L3LSrNlCTcN32vOHkQyJimHaFv9N3zweSemviv/YyGIbf9Qn7sx6DN9CE6TfB7KiyJggkfYwC908/k7KubDz+JPzfEfSa2Y92vqSP/B8X5I9f0beyJ9b3v8+o6BM7j9l0t2Phd/GJOcfo9HJyK8oYhMmWW3G37z0PxaO25XWO/df+MEraT85X3n//Tk3f3+pfbrm9nggG8cdbryIH/BDVffxj5M462PcxqPGgfC/bhidPvWRUQ0+CruPTJFv/cVt+/zn3f8i/w5tG/L800uZjNyQ9L7/+XIGF/nPT6HG5yeVz1+H9vn4UfcQzk/U5E0Vqti/ORljs/rLG6juTo/C5qh3on6t1jl5+GFlcvUSz+SuvGMlLwNZu2JFToKQLU4sNxKM/a8/VcZ3MLxiD8PSY4rKGp+DHjT9IMlB/tErgN8eacwM5XJLg6LmrthVh1prrbXWWuu/pL8B4fTENQ==:4F30\n" +
                        "^FO416,544^GFA,00512,00512,00016,:Z64:eJzNkL0NwkAMhZ2TAYNSHISIINGQCYIYAJ/EGBSMkBGca6C8ERCThJ/BSByaUCIKXvP06enpWQb4tVxU82oaFo9yA0XD7IGvJGTOLG0eoT26OGQ57oW1QRfBADl46PKljU7BJqnvCkIglYDJ/E40HwNXwc5g77rFCYlp+7DW/jBBNwjZFotEY29A94lMi6PbEw82pPcS5x+nx31E7HNzhzq/uYRaXb740r/oBYoPH3U=:95CE\n" +
                        "^FO0,544^GFA,20736,20736,00072,:Z64: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:13A9\n" +
                        "^FO33,543^GB27,27,1^FS\n" +
                        "^FO202,543^GB27,27,1^FS\n" +
                        "^FO503,189^GB27,27,1^FS\n" +
                        "^FO503,264^GB27,27,1^FS\n" +
                        "^FO504,383^GB28,27,1^FS\n" +
                        "^FO504,438^GB27,27,1^FS\n" +
                        "^FO398,543^GB27,27,1^FS\n" +
                        "^PQ1,0,1,Y^XZ";



        String bottom = tmpBottom;
        return bottom;
    }

    //ZPL Coverters
    private String GreekConverter(String Word) {
        String CodedWord = "";
        String Addition;
        for (int i=0; i<Word.length(); i++) {
            if (Word.subSequence(i, i + 1).equals("Α")) {Addition = "_c1";}
            else if (Word.subSequence(i, i + 1).equals("Α")) {Addition = "_c1";}
            else if (Word.subSequence(i, i + 1).equals("Β")) {Addition = "_c2";}
            else if (Word.subSequence(i, i + 1).equals("Γ")) {Addition = "_c3";}
            else if (Word.subSequence(i, i + 1).equals("Δ")) {Addition = "_c4";}
            else if (Word.subSequence(i, i + 1).equals("Ε")) {Addition = "_c5";}
            else if (Word.subSequence(i, i + 1).equals("Ζ")) {Addition = "_c6";}
            else if (Word.subSequence(i, i + 1).equals("Η")) {Addition = "_c7";}
            else if (Word.subSequence(i, i + 1).equals("Θ")) {Addition = "_c8";}
            else if (Word.subSequence(i, i + 1).equals("Ι")) {Addition = "_c9";}
            else if (Word.subSequence(i, i + 1).equals("Κ")) {Addition = "_ca";}
            else if (Word.subSequence(i, i + 1).equals("Λ")) {Addition = "_cb";}
            else if (Word.subSequence(i, i + 1).equals("Μ")) {Addition = "_cc";}
            else if (Word.subSequence(i, i + 1).equals("Ν")) {Addition = "_cd";}
            else if (Word.subSequence(i, i + 1).equals("Ξ")) {Addition = "_ce";}
            else if (Word.subSequence(i, i + 1).equals("Ο")) {Addition = "_cf";}
            else if (Word.subSequence(i, i + 1).equals("Π")) {Addition = "_d0";}
            else if (Word.subSequence(i, i + 1).equals("Ρ")) {Addition = "_d1";}
            else if (Word.subSequence(i, i + 1).equals("Σ")) {Addition = "_d3";}
            else if (Word.subSequence(i, i + 1).equals("Τ")) {Addition = "_d4";}
            else if (Word.subSequence(i, i + 1).equals("Υ")) {Addition = "_d5";}
            else if (Word.subSequence(i, i + 1).equals("Φ")) {Addition = "_d6";}
            else if (Word.subSequence(i, i + 1).equals("Χ")) {Addition = "_d7";}
            else if (Word.subSequence(i, i + 1).equals("Ψ")) {Addition = "_d8";}
            else if (Word.subSequence(i, i + 1).equals("Ω")) {Addition = "_d9";}

            else if (Word.subSequence(i, i + 1).equals("α")) {Addition = "_e1";}
            else if (Word.subSequence(i, i + 1).equals("β")) {Addition = "_e2";}
            else if (Word.subSequence(i, i + 1).equals("γ")) {Addition = "_e3";}
            else if (Word.subSequence(i, i + 1).equals("δ")) {Addition = "_e4";}
            else if (Word.subSequence(i, i + 1).equals("ε")) {Addition = "_e5";}
            else if (Word.subSequence(i, i + 1).equals("ζ")) {Addition = "_e6";}
            else if (Word.subSequence(i, i + 1).equals("η")) {Addition = "_e7";}
            else if (Word.subSequence(i, i + 1).equals("θ")) {Addition = "_e8";}
            else if (Word.subSequence(i, i + 1).equals("ι")) {Addition = "_e9";}
            else if (Word.subSequence(i, i + 1).equals("κ")) {Addition = "_ea";}
            else if (Word.subSequence(i, i + 1).equals("λ")) {Addition = "_eb";}
            else if (Word.subSequence(i, i + 1).equals("μ")) {Addition = "_ec";}
            else if (Word.subSequence(i, i + 1).equals("ν")) {Addition = "_ed";}
            else if (Word.subSequence(i, i + 1).equals("ξ")) {Addition = "_ee";}
            else if (Word.subSequence(i, i + 1).equals("ο")) {Addition = "_ef";}
            else if (Word.subSequence(i, i + 1).equals("π")) {Addition = "_f0";}
            else if (Word.subSequence(i, i + 1).equals("ρ")) {Addition = "_f1";}
            else if (Word.subSequence(i, i + 1).equals("ς")) {Addition = "_f2";}
            else if (Word.subSequence(i, i + 1).equals("σ")) {Addition = "_f3";}
            else if (Word.subSequence(i, i + 1).equals("τ")) {Addition = "_f4";}
            else if (Word.subSequence(i, i + 1).equals("υ")) {Addition = "_f5";}
            else if (Word.subSequence(i, i + 1).equals("φ")) {Addition = "_f6";}
            else if (Word.subSequence(i, i + 1).equals("χ")) {Addition = "_f7";}
            else if (Word.subSequence(i, i + 1).equals("ψ")) {Addition = "_f8";}
            else if (Word.subSequence(i, i + 1).equals("ω")) {Addition = "_f9";}

            else if (Word.subSequence(i, i + 1).equals("Ά")) {Addition = "_a2";}
            else if (Word.subSequence(i, i + 1).equals("Έ")) {Addition = "_b8";}
            else if (Word.subSequence(i, i + 1).equals("Ή")) {Addition = "_b9";}
            else if (Word.subSequence(i, i + 1).equals("Ί")) {Addition = "_ba";}
            else if (Word.subSequence(i, i + 1).equals("Ό")) {Addition = "_bc";}
            else if (Word.subSequence(i, i + 1).equals("Ύ")) {Addition = "_be";}
            else if (Word.subSequence(i, i + 1).equals("Ώ")) {Addition = "_bf";}
            else if (Word.subSequence(i, i + 1).equals("ά")) {Addition = "_dc";}
            else if (Word.subSequence(i, i + 1).equals("έ")) {Addition = "_dd";}
            else if (Word.subSequence(i, i + 1).equals("ή")) {Addition = "_de";}
            else if (Word.subSequence(i, i + 1).equals("ί")) {Addition = "_df";}
            else if (Word.subSequence(i, i + 1).equals("ό")) {Addition = "_fc";}
            else if (Word.subSequence(i, i + 1).equals("ύ")) {Addition = "_fd";}
            else if (Word.subSequence(i, i + 1).equals("ώ")) {Addition = "_fe";}
            else if (Word.subSequence(i, i + 1).equals("ϊ")) {Addition = "_fa";}
            else if (Word.subSequence(i, i + 1).equals("ϋ")) {Addition = "_fb";}
            else if (Word.subSequence(i, i + 1).equals("ΰ")) {Addition = "_e0";}
            else if (Word.subSequence(i, i + 1).equals("Ϊ")) {Addition = "_da";}
            else if (Word.subSequence(i, i + 1).equals("Ϋ")) {Addition = "_db";}
            else if (Word.subSequence(i, i + 1).equals("€")) {Addition = "_80";}
            else {Addition = Word.subSequence(i, i + 1).toString();}
            CodedWord = CodedWord + Addition;



        }
        return CodedWord;
    };
    private String CarOwnerYes (String Owner) {
        if (Owner.equals("ΝΑΙ")) {
            return "X";
        } else {
            return "";
        }
    }
    private String CarOwnerNo (String Owner) {
        if (Owner.equals("ΟΧΙ")) {
            return "X";
        } else {
            return "";
        }
    }
    private String FineSplitter (Integer i, Integer j, String FineTypeS) {
        if (FineTypeS.length() - 1 >= i) {
            if (FineTypeS.length() - 1 <= j) {
                return FineTypeS.subSequence(i, FineTypeS.length()).toString();
            } else {
                return FineTypeS.subSequence(i, j+1).toString();
            }
        } else {
            return "";
        }
    }

    //BlankFields
    private String BlankField(String item) {
        if (item == null){
            return "";
        } else {
            return item;
        }
    }
    private double BlankFieldL(double item) {
        if (item == 0){
            return 0;
        } else {
            return item;
        }
    }

    //Temporary Data
    private void showDataFromOCR (DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            RetrieveFineInfoFirebase RInfo = new RetrieveFineInfoFirebase();
            RInfo.setAddress(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getAddress());
            RInfo.setCarBrand(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarBrand());
            RInfo.setCarColor(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarColor());
            RInfo.setCarPlate(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarPlate());
            RInfo.setCarType(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarType());
            RInfo.setDate(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getDate());
            RInfo.setDay(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getDay());
            RInfo.setFineAmount(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getFineAmount());
            RInfo.setFineType(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getFineType());
            RInfo.setTime(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getTime());
            //RInfo.setUserID(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getUserID());
            RInfo.setCarCountry(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarCountry());
            RInfo.setFinePoints(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getFinePoints());
            //RInfo.setPaid(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getPaid());
            RInfo.setLat(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getLat());
            RInfo.setLon(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getLon());
            AddressEditText.setText(RInfo.getAddress());
            BrandAutoCompl.setText(RInfo.getCarBrand());
            ColorEditText.setText(RInfo.getCarColor());
            PlateEditText.setText(RInfo.getCarPlate());
            TypeEditText.setSelection(selection(RInfo.getCarType()));
            DateEditText.setText(RInfo.getDate());
            DayEditText.setText(RInfo.getDay());
            FineAmountEditText.setText(RInfo.getFineAmount());
            FineTypeAutoCompl.setText(RInfo.getFineType());
            TimeEditText.setText(RInfo.getTime());
            PlateCountryEditText.setText(RInfo.getCarCountry());
            FinePointsEditText.setText(RInfo.getFinePoints());
            latF = RInfo.getLat();
            lonF = RInfo.getLon();

            if (dataSnapshot.hasChild("Fine A")) {
                RInfo.setA1(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA1());
                RInfo.setA2(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA2());
                RInfo.setA3(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA3());
                RInfo.setA4(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA4());
                RInfo.setA5(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA5());
                RInfo.setA6(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA6());
                A1.setText(RInfo.getA1());
                A2.setText(RInfo.getA2());
                A3.setText(RInfo.getA3());
                A4.setText(RInfo.getA4());
                A5.setText(RInfo.getA5());
                A6.setText(RInfo.getA6());
            }
            if (dataSnapshot.hasChild("Fine B")) {
                RInfo.setB1(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB1());
                RInfo.setB2(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB2());
                RInfo.setB3(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB3());
                RInfo.setB4(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB4());
                RInfo.setB5(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB5());
                RInfo.setB6(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB6());
                B1.setText(RInfo.getB1());
                B2.setText(RInfo.getB2());
                B3.setText(RInfo.getB3());
                B4.setText(RInfo.getB4());
                B5.setText(RInfo.getB5());
                B6.setText(RInfo.getB6());
            }
            if (dataSnapshot.hasChild("Fine C")) {
                RInfo.setC1(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC1());
                RInfo.setC2(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC2());
                RInfo.setC3(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC3());
                RInfo.setC4(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC4());
                RInfo.setC5(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC5());
                RInfo.setC6(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC6());
                RInfo.setC7(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC7());
                RInfo.setC8(dataSnapshot.child("Fine C").getValue(RetrieveFineInfoFirebase.class).getC8());
                C1.setSelection(selection(RInfo.getC1()));
                C2.setText(RInfo.getC2());
                C3.setText(RInfo.getC3());
                C4.setText(RInfo.getC4());
                C5.setText(RInfo.getC5());
                C6.setText(RInfo.getC6());
                C7.setText(RInfo.getC7());
                C8.setText(RInfo.getC8());
            }
            if (dataSnapshot.hasChild("Fine C")) {
                RInfo.setD1(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD1());
                RInfo.setD2(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD2());
                RInfo.setD3(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD3());
                RInfo.setD4(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD4());
                RInfo.setD5(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD5());
                D1.setText(RInfo.getD1());
                D2.setText(RInfo.getD2());
                D3.setText(RInfo.getD3());
                D4.setText(RInfo.getD4());
                D5.setText(RInfo.getD5());
            }
        }
    }
    private Integer selection(String item) {
        Integer i = 1;
        if (item.equals("Ι.Χ.")){
            i = 0;
        }
        if (item.equals("ΟΧΙ")){
            i = 2;
        }
        return i;
    }
}
