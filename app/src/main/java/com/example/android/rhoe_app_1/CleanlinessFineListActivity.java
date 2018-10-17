package com.example.android.rhoe_app_1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.android.app_v12.R;
import com.example.android.rhoe_app_1.FirebaseFine.RetrieveCleanlinessFineInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseFine.RetrieveFineInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseUsers.RetrieveUserInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseMunicipality.RetrieveMunicipalityInfoFirebase;
import com.example.android.rhoe_app_1.Zebra.DemoSleeper;
import com.example.android.rhoe_app_1.Zebra.SettingsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class CleanlinessFineListActivity extends AppCompatActivity {

    private static final String TAG = "FineListActivity";

    //Zebra Printer
    private ZebraPrinter printer;
    private Connection printerConnection;
    private TextView ConnectivityStatusFineTextView;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef, databaseReference;
    DatabaseReference mURef, userDatabaseReference;
    DatabaseReference mMRef, municipalityDatabaseReference;
    DatabaseReference mRefClick;
    FirebaseAuth mAuth;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String userID;
    String MunicipalityIndex, MID, P1, P2, OfficerName, SignatureImage;
    String selectedKey, plate;
    String DateSelected, TimeSelected, PlateSelected, AddressSelected, MunicipalityShort, MIDBlanks;
    Bitmap image;
    byte[] data;

    EditText SearchbarEditText;
    Switch AdvancedSwitch;
    TableLayout SettingsTable;
    CheckBox MunicipalityCheckBox, PaidCheckBox, UnpaidCheckBox;
    boolean munB = false, paidB = true, unpaidB = false;
    ListView mListView;
    ArrayList<Spanned> list = new ArrayList<>();
    ArrayList<String> listPlate = new ArrayList<>();
    ArrayList<String> listDate = new ArrayList<>();
    ArrayList<String> listKey = new ArrayList<>();
    ArrayAdapter adapter;
    private String munAddress, munBank, munBankIBAN, munDepartment, munEmail, munName, munPayAddress1, munPayAddress2, munPayAddress3, munPayName, munPostNum, munRegion, munTel1, munTel2;

    private String SurnameReprint, NameReprint, DateReprint, DayReprint, TimeReprint, AddressReprint, FineAmountReprint, OccupationReprint, FineTypeReprint, FathWifeReprint, PaidReprint;
    private String B1 = "", B2 = "", B3 = "", B4 = "", B5 = "", B6 = "", B7 ="", C1 = "", C2 = "", C3 = "", C4 = "";
    private boolean conA = true, conB = true, conC = true, conD = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fine_list);

        ConnectivityStatusFineTextView = (TextView) findViewById(R.id.tvConnectivityStatusList);

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                doConnection();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

        mListView = (ListView) findViewById((R.id.lvFineList));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, list);
        //final ListAdapterActivity adapter = new ListAdapterActivity(listPlate, listDate, listKey, this);

        SearchbarEditText = (EditText)findViewById(R.id.etSearchBar);
        AdvancedSwitch = (Switch)findViewById(R.id.switchSettings);
        SettingsTable = (TableLayout)findViewById(R.id.TableSettings);
        MunicipalityCheckBox = (CheckBox)findViewById(R.id.cbMunicipality);
        PaidCheckBox = (CheckBox)findViewById(R.id.cbPaid);
        UnpaidCheckBox = (CheckBox) findViewById(R.id.cbUnpaid);
        mListView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userDatabaseReference.keepSynced(true);

        plate = "";
        populateListView(plate, munB, paidB, unpaidB);

        /*StorageReference storageRef = storage.getReferenceFromUrl("gs://testproject-328af.appspot.com/");
        final StorageReference SignatureImagesRef = storageRef.child("Signatures/" + userID + ".jpg");

        StorageReference mobileRef = storageRef.child("images/" + userID + ".jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        SignatureImagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new
        OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                data = bytes;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
        */
        SearchbarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                plate = SearchbarEditText.getText().toString();
                adapter.clear();
                populateListView(plate, munB, paidB, unpaidB);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        MunicipalityCheckBox.setChecked(false);
        PaidCheckBox.setChecked(true);
        UnpaidCheckBox.setChecked(false);
        AdvancedSwitch.setChecked(false);
        SettingsTable.setVisibility(TableLayout.GONE);
        AdvancedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkSection(AdvancedSwitch.isChecked(),SettingsTable);
            }
        });

        MunicipalityCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (MunicipalityCheckBox.isChecked()) {
                    munB = true;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    populateListView(plate, munB, paidB, unpaidB);
                } else {
                    munB = false;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    populateListView(plate, munB, paidB, unpaidB);
                }
            }
        });
        PaidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (PaidCheckBox.isChecked()) {
                    paidB = true;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    populateListView(plate, munB, paidB, unpaidB);
                } else {
                    paidB = false;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    populateListView(plate, munB, paidB, unpaidB);
                }
            }
        });
        UnpaidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (UnpaidCheckBox.isChecked()) {
                    unpaidB = true;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    populateListView(plate, munB, paidB, unpaidB);
                } else {
                    unpaidB = false;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    populateListView(plate, munB, paidB, unpaidB);
                }
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedKey = listKey.get(position);
                mRefClick = databaseReference.child(selectedKey);
                mRefClick.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        showDataFromFine(dataSnapshot);
                        if (Objects.equals(PaidReprint, "No")){
                            showFineDetails(CleanlinessFineListActivity.this);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

    }


    public void showFineDetails(CleanlinessFineListActivity view) {
        final AlertDialog myAlert = new AlertDialog.Builder(this).create();
        myAlert.setTitle("Βεβαίωση Παράβασης Κανονισμού Καθαριότητας");
        myAlert.setMessage(Html.fromHtml("<b>Ημερομηνία/Ώρα</b> <br>" + DateReprint + " " +TimeReprint + "<br>" +
                "<b>Παραβάτης</b> <br>" + SurnameReprint + " " + NameReprint + "<br>" +
                "<b>Διεύθυνση</b> <br>" + AddressReprint + "<br>"));
        myAlert.setButton3("ΔΙΑΓΡΑΦΗ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myAlert.dismiss();
                final AlertDialog myAlert1 = new AlertDialog.Builder(CleanlinessFineListActivity.this).create();
                myAlert1.setTitle("Είστε σίγουροι ότι θέλετε να διαγράψετε την βεβαίωση");
                myAlert1.setButton2("ΝΑΙ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefClick.setValue(null);
                        SearchbarEditText.setText("");
                        list.clear();
                        adapter.notifyDataSetChanged();
                        populateListView(plate, munB, paidB, unpaidB);
                        myAlert1.dismiss();
                    }
                });
                myAlert1.setButton("ΑΚΥΡΩΣΗ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SearchbarEditText.setText("");
                        list.clear();
                        adapter.notifyDataSetChanged();
                        populateListView(plate, munB, paidB, unpaidB);
                        myAlert1.dismiss();
                    }
                });
                myAlert1.show();

            }
        });
        myAlert.setButton2("ΕΠΑΝΕΚΤΥΠΩΣΗ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    public void run() {Looper.prepare();
                        doConnectionTest();
                        Looper.loop();
                        Looper.myLooper().quit();
                        new Thread(new Runnable() {
                            public void run() {
                                Looper.prepare();
                                doConnection();
                                Looper.loop();
                                Looper.myLooper().quit();
                            }
                        }).start();
                    }
                }).start();
                SearchbarEditText.setText("");
                list.clear();
                adapter.notifyDataSetChanged();
                populateListView(plate, munB, paidB, unpaidB);
                myAlert.dismiss();
            }
        });
        /*
        myAlert.setButton("ΠΛΗΡΩΜΗ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myAlert.dismiss();
                final AlertDialog myAlert2 = new AlertDialog.Builder(FineListActivity.this).create();
                myAlert2.setTitle("Επιβεβαίωση Πληρωμής");
                myAlert2.setButton3("ΕΠΙΒΕΒΑΙΩΣΗ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefClick.child("Paid").setValue("Yes");
                        SearchbarEditText.setText("");
                        adapter.clear();
                        populateListView(plate, munB, paidB, unpaidB);
                        myAlert2.dismiss();
                    }
                });
                myAlert2.setButton2("ΕΠΙΒΕΒΑΙΩΣΗ/ΕΠΑΝΕΚΤΥΠΩΣΗ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefClick.child("Paid").setValue("Yes");
                        SearchbarEditText.setText("");
                        adapter.clear();
                        populateListView(plate, munB, paidB, unpaidB);
                        myAlert2.dismiss();
                    }
                });
                myAlert2.setButton("ΑΚΥΡΩΣΗ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SearchbarEditText.setText("");
                        adapter.clear();
                        populateListView(plate, munB, paidB, unpaidB);
                        myAlert2.dismiss();
                    }
                });
                myAlert2.show();
            }
        });
        */
        myAlert.show();
    }


    private void checkSection(boolean condition, TableLayout table){
        if(condition){
            table.setVisibility(TableLayout.VISIBLE);
        }
        else {
            table.setVisibility(TableLayout.GONE);
        }
    }


    private void populateListView(final String plt, final boolean mun, final boolean paid, final boolean unpaid) {
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot UdataSnapshot) {
                showDataFromUser(UdataSnapshot);
                municipalityDatabaseReference = FirebaseDatabase.getInstance().getReference("Municipalities").child(MID);

                municipalityDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot MdataSnapshot) {
                        municipalityDatabaseReference.keepSynced(true);
                        showDataFromMunicipality(MdataSnapshot);

                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();
                                while (items.hasNext()) {
                                    DataSnapshot item = items.next();
                                    String RealUID = item.child("UserID").getValue().toString();
                                    String RealNameSur = item.child("Surname").getValue().toString() + " " + item.child("Name").getValue().toString();
                                    String RealPaid = item.child("Paid").getValue().toString();
                                    if (!paid || !unpaid) {
                                        if (!paid && !unpaid) {
                                            if (mun) {
                                                if (Objects.equals(plt, "") || RealNameSur.contains(plt)) {
                                                    list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                            item.child("Surname").getValue().toString() + " " + item.child("Name").getValue().toString()));
                                                    listKey.add(0, item.getKey());
                                                    adapter.notifyDataSetChanged();
                                                }
                                            } else if (Objects.equals(RealUID, userID)) {
                                                if (Objects.equals(plt, "") || RealNameSur.contains(plt)) {
                                                    list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                            item.child("Surname").getValue().toString() + " " + item.child("Name").getValue().toString()));
                                                    listKey.add(0, item.getKey());
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        } else if (paid) {
                                            if (mun) {
                                                if ((Objects.equals(plt, "") || RealNameSur.contains(plt)) && Objects.equals(RealPaid, "No")) {
                                                    list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                            item.child("Surname").getValue().toString() + " " + item.child("Name").getValue().toString()));
                                                    listKey.add(0, item.getKey());
                                                    adapter.notifyDataSetChanged();
                                                }
                                            } else if (Objects.equals(RealUID, userID)) {
                                                if ((Objects.equals(plt, "") || RealNameSur.contains(plt)) && Objects.equals(RealPaid, "No")) {
                                                    list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                            item.child("Surname").getValue().toString() + " " + item.child("Name").getValue().toString()));
                                                    listKey.add(0, item.getKey());
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        } else {
                                            if (mun) {
                                                if ((Objects.equals(plt, "") || RealNameSur.contains(plt)) && Objects.equals(RealPaid, "Yes")) {
                                                    list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                            item.child("Surname").getValue().toString() + " " + item.child("Name").getValue().toString()));
                                                    listKey.add(0, item.getKey());
                                                    adapter.notifyDataSetChanged();
                                                }
                                            } else if (Objects.equals(RealUID, userID)) {
                                                if ((Objects.equals(plt, "") || RealNameSur.contains(plt)) && Objects.equals(RealPaid, "Yes")) {
                                                    list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                            item.child("Surname").getValue().toString() + " " + item.child("Name").getValue().toString()));
                                                    listKey.add(0, item.getKey());
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError){

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /*
    public void populateListView(final String plt, final boolean mun, final boolean paid, final boolean unpaid) {
        fb_DataRef_User.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot UdataSnapshot) {
                showDataFromUser(UdataSnapshot);

                fb_DataRef_Fine.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();
                        while (items.hasNext()) {
                            DataSnapshot item = items.next();
                            String RealUID = item.child("UserID").getValue().toString();
                            String RealPlate = item.child("CarPlate").getValue().toString();
                            String RealPaid = item.child("Paid").getValue().toString();
                            if (!paid || !unpaid){
                                if (!paid && !unpaid) {
                                    if (mun) {
                                        if (Objects.equals(plt, "") || RealPlate.contains(plt)) {
                                            listPlate.add(0, item.child("CarPlate").getValue().toString());
                                            listDate.add(0, item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString());
                                            listKey.add(0, item.getKey());
                                            //adapter.notifyDataSetChanged();
                                        }
                                    } else if(Objects.equals(RealUID, userID)) {
                                        if (Objects.equals(plt, "") || RealPlate.contains(plt)) {
                                            listPlate.add(0, item.child("CarPlate").getValue().toString());
                                            listDate.add(0, item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString());
                                            listKey.add(0, item.getKey());
                                            //adapter.notifyDataSetChanged();
                                        }
                                    }
                                } else if (paid) {
                                    if (mun) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "No")) {
                                            listPlate.add(0, item.child("CarPlate").getValue().toString());
                                            listDate.add(0, item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString());
                                            listKey.add(0, item.getKey());
                                            //adapter.notifyDataSetChanged();
                                        }
                                    } else if(Objects.equals(RealUID, userID)) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "No")) {
                                            listPlate.add(0, item.child("CarPlate").getValue().toString());
                                            listDate.add(0, item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString());
                                            listKey.add(0, item.getKey());
                                            //adapter.notifyDataSetChanged();
                                        }
                                    }
                                } else {
                                    if (mun) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "Yes")) {
                                            listPlate.add(0, item.child("CarPlate").getValue().toString());
                                            listDate.add(0, item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString());
                                            listKey.add(0, item.getKey());
                                            //adapter.notifyDataSetChanged();
                                        }
                                    } else if(Objects.equals(RealUID, userID)) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "Yes")) {
                                            listPlate.add(0, item.child("CarPlate").getValue().toString());
                                            listDate.add(0, item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString());
                                            listKey.add(0, item.getKey());
                                            //adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        }
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
    }
    */

    //Printer

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
                        "^MMT^PW561^LL0480^LS0" +
                        "^FO64,32^GFA,03584,03584,00056,:Z64:eJztkz9u2zAYxT+VA7fwAgJ5hKJbiqjklZgpCqBEdDV49HUyBQw86BIdGGjIyiJDCMQQ+2jL7hUy6A0GBfjxx/f9IVq1atWqVatWfWH1c+IuChY0iWASpSh+z8zrpIkCzZTYmKdZMv/dab6jHNLiO7TFV/maVFSRLM4ZX21NVayys9WY97OqfO9rLuhvsCdfnq8FfJsgKScFXroSeQjXXeExXM+24KnB5wSee40Lj7qOU+QVfLHjnqyVgr+EVoMXcLDMc28Ve4mt4fhbXHiUTKZjvipo5ij1s2YTLi88sYlp8sIlM+xDZ7Zb8BdeFVXxIRGQ8Nn+IKvXHFXJJyjeTl6RVS9736gtz75beCyoGb4BpuJTyRxAzkk8Fx6lz8lpSmrae222Q0ahTz7upSy8ICsvue9j09QPMUZ+KE8he/vuO/Cm0de/Rrb3KPlRwl1p+DLCFR6CXevPkAKbiaUr8D49iizfR/DehsmJM8+JuviQzyvmokXn7uN9YH/gA+/uh2/J1rfg1W/V6MQln3g68o75NiHJhHw/A38mfkC+fOMa8G5Ql+Zj8+bkuZ6Bt0u+IEu/hMXpweMVvEE9H5WXZKUpvjv6oObSP57+9w88EZHUBIV51ejfbEr/tEH/mt5l6i7zwuOpf5gXtomWR8G9QhpSCvxOlXmpFealVtRTu/DMjPh5t8GIlPlE44LIzrhGkxRDDgf8YB8E5rMxDvtx2QeGidlVU44G+9BHHngmRdiHBpuB55R9gK8PjaLH6rwP/Yw0eVdGsuyfSTx8m6lEIi3K/WI88bB/wj2y8/6tWvVl9Q8Ka4fu:F327" +
                        "^FO96,32^GFA,01536,01536,00048,:Z64:eJztkzGO2zAQRYdgMZ1ZbhNQ13AhiMfJEdZAhJiGCpU+Qq6RkoILXWMEF+4SLhLAKgRNhqRgbxtgy2Vn4evh+fML4PN87FFBtSoiLjocF/UX1BRbqPEHxJegGZcaewZcDsgYUl4HvQJhn/IrrqCI9pI/Azky3Oe8T/mz9oWPPJDwUfgVgxoo8c8qpp8PfoumL3wyXSD0JK/vrfF6yvze01w38rhGjx5ob7DzxafS059RRR3U3GBQQ/bvT1NsLKqY8qCiYxw3f6voOgo/ANXyUvHvTgPZd3y36uvmb9XbPfORWvnTxb8fJP+OX63qvvk3QNdb8ke/18TFv7t0tDP84FsLnPkYG+HfE7+CVl/4Tfwdd7cLVS+c+NKP8L/AMWx5oF9fk78R/mVI/o79Tfg686V/oF0N3zLfzFZN66tcccDQ6nFI/o5PN0WVwQff1FD8dwfpZ3XJX0s/Y1f8/W9FdvfsB3+q0k/VSv+Ly/3HBsdT6f8k6k317Adnfcv8upH7nV3u/2BNN5T+/QpzbZ58PetL5rdW9rNUuBzXPBgeeEn573BcjOzHcd4Pxu1+D5VmmE0veSf71DzxXPIupn2mvOxTR1P2eajS/hOf0/7VcSr8Fsr+N76WSPjob+/z/O/5B+m1StY=:4C1A" +
                        "^FO96,288^GFA,02304,02304,00036,:Z64:eJztkrGq2zAUhiU0aJPWDOdKryGokV9JIcM11ASZ7s0L3AfJdJHwkJdI4QQPGSvoIoixe0zXbh26+Bdo+X9+vqMjxnbt+rs6aVMAEU0PPAEf83MtIj/zbQ7QmE6SX5xusfMiKt/o6sUUJ8rEaXrWzjvlNPkV9NYjGQDYGXge8lr4kPOt9nAA0OTXdtHR6TUq1XhvxBinhCIi3WdwytNhzi6SBb0yI6E3cBzzI6PIeNwyjQE6ZC+adXoRi/zslH9NVLH1fEU8E89WRDaxBKnkLOag4ZjX9Eh8TW3CdwJUoMAW3RCP/E5Nc6fNa1xximJFyrw+2kU1qrFVuq3nwxh+D/rtB2VOW+Yt4Yky5Dpb5JV4xKdXgnjaC/FQJuIXxNPslLzKKz1zoB5+B5DgjL3kIb0zPiSa63g/ALmBBVGJR1SjNL2PvYwR263nV8KOekQVlTlR7G0dil0MjWHlmLFlIj8etK++MaKQXwS2P9dvVS/e69pK4vHsz77CmXqQ/MqRemLhs6O9G0E8hnjG4VZDfwCO5Jf//Td37dq161/1G8uG5IA=:4938" +
                        "^FO64,64^GFA,03584,03584,00056,:Z64:eJztUzFu3EAM5JoFO28eIGifkPYCCNKXUiqA4ltHhTvfD+4d7rLKFfcJF0z8AG+6LYRluJIcJC8IAmiuOQqkRjNDAuzYsWPHjh3/MVBSbLvc4g95CS00iU7IKC/cHrN0rznRFxnFHxnWNsaRk84ZibFyUplJplBB09PJBCM/uTnOR3ede3qQi0wSQdtyZQJO4eMyl1JdXvSgHAc4HOzpXvm+cyNZvr7OWivfKAxrm8cxFD4wMdZQ1/QwTdzDMJA1DEbnYMB3wFpToGtQvhqgJk+XhQ+QYwt3nZzGF0lw6MQiA5W5ZFvgTm4x4CUUfQCdeLws+oBCrCA5IVNENE6sCWDvuTHROYhOnM4tfBWAE1B9fZmzPtUYrfKxJJtctiODM9wgt9ZzqYumok+bxcumT99ZY7CWJMTo5rou+hqdo9AQsCqXQIufZc4a2fS1INkG+4jCiY/zbVv0ZdSH/o4837aoOXxTPyV32T6SbPoqkFkdOxsJn7hvbFX0DUYf+p58tJXR9MIxapub6Uzjmh84L1m34Kw+feB0sE/qCbQ6h3xAz/YJx1HuufC5jGc7bvrUz9pEelafPoc4UE+qTzWqxIECa61fM2350TP9lV/CWfk6jgdKRZ+dSn7FT61HzY/X/DDbt/x0XyqozKx8LvQNxaIPy9xQqdcUSecmXvMz85u+bT8xqZ+dl4TJyrqfXdYdyKVWfdt+rtXbPdQum6h+OjjOZQV4uwfSH0aS63oPS5vmoP9/3x8kELb+vbq48C33p29Xq2/+uD9INxvfjh07/hl+Af3VtWs=:2065" +
                        "^FO352,160^GFA,05376,05376,00028,:Z64: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:21CA" +
                        "^FT470,136^A0I,20,19^FH^FD" + GreekConverter("ΑΦΜ: - ΔΟΥ: - ") + "^FS" +
                        "^FT382,162^A0I,20,19^FH^FD" + GreekConverter("email: " + munEmail) + "^FS" +
                        "^BY2,3,80^FT515,360^BCI,,Y,N" +
                        "^FD>:]>5100760>68[>5150417>64[16]^FS" +
                        "^FT382,183^A0I,20,19^FH^FD" + GreekConverter("ΤΗΛ: " + munTel1 + " - " + munTel2) + "^FS" +
                        "^FT382,253^A0I,23,24^FH^FD" + GreekConverter(munName) + "^FS" +
                        "^FT339,13^A0I,23,24^FH^FDNo 000000^FS" +
                        "^FT382,227^A0I,23,24^FH^FD" + GreekConverter(munDepartment) + "^FS" +
                        "^FT382,205^A0I,20,19^FH^FD" + GreekConverter("Απ. Σαμανίδη 21 ΤΚ 552 35 ΠΑΝΟΡΑΜΑ") + "^FS" +
                        "^FT382,279^A0I,23,24^FH^FD" + GreekConverter(munRegion) + "^FS" +
                        "^PQ1,0,1,Y^XZ";

        String tmpFine2 =
                "^XA" +
                        "^MMT^PW561^LL0208^LS0" +
                        "^FO256,160^GFA,02560,02560,00040,:Z64:eJztkjFu3DAQRYeYgkUA8QK2eA0XgnglliwWEA0VKfcKucly4SK3CGhs4S4YV5lC0HhoyzJygTTRByFIwMfX458BOPRPNLjMei5MLrvR3AoDuQvLrY6huh+mBvnwWYh6JtKn69UX9duTvNIQaqDdN9qcXH4W5g66Dm81AXeBpfIpVGH1rVteTvbxmWLqwXtzLQlSj9lQShboZArcffEpDcsKnZMn5ZMVM9DA7pFGU2H54pNridMCzqovwrTYAuTJmtqrj3Y+bHlhxdXKXBlCy6u+urn2WJF2Pny6Cg2DFft9Vr5haHy+WFO8KVjefUEy4y0Ljw9e5vOsfOMDZml1zu2/Lm8+iOYlT9Sn3uFPVL4+YZnItjz12T0vwUsm7tLY4W/U/rqkeRufqVh3PvMHKPo4ePMLlc/Hxnf/cd9iaO8PRPMcD51ZUfkc/9Uf7/3BpHmWemeWxmep9XeK7/OoMGz9naWuIOzaJFcUzdP563xTm2+Qy+d8z1JO0O7nBZbm07fXz30J4re9cpo3QmCs3wS4+bCGff/2vEOHDh069F/oDbD/FhI=:C05B" +
                        "^FO320,0^GFA,05120,05120,00032,:Z64:eJztV0FvG0UUfrMz2d3K2/UYqmpCTOyoiF43okIbQGRWRO2FAz9hjaVy9T/w2yZKI1SFHnMLUv9AEJccx00UbpB/wJpIHFEqLpYwu7xN4ri11yqHqpf6XTLOp2++972ZNzMLMI95zOPdjSg/Zo1/UXb3GNyHDQbr5w0w+kMILvF+vssbA1zLdzh+C79yWB+spxjneIVrtsVB6VUmOERwzAGCODbamCscuM0hSO+wXQvauE14nAbYwnSE20SMu0fs8ZByobnAoDQRxCPc9zmm+VnyOLvIBVmCIZ5hc4QrwRk2I2YJx/RoLs5A6giCER46OSVg8k1vPT2iXGwgfhtGfC2dnBjYTVypvyK+wy70R/xU+TR/iMnmXhD/4dP8WPCv9bUgjq4xxmRTP6QxM+aV/C/8VzDBw2b8MY0hfdX/Rf1ukqvTQN8TRf06sYl6o/ox8rw/qEKO5+tpWM2xqH+e5j/keIUf88bQhi7EjlFODo1zB7pmmUbzeKvRwhrU4G5Gu4FGD7JJvImr+Al+l4Fx6e8vU3hHr2INtQBN7QRGTAmkKxhg6oNRNDL+FN7tC6kLvuz2aV+W4LtBijbpd/to7Ck8iHZlhIV+EJXpB73vgxa1oVFBryT/WPZqsuUMSV/24MFwyn+Y/HxwBwr9cLNEv7OY/PT0fSj0F5My/Ury48FnBV9VkrL8F5j+5nbBlwuszL/P0o7vZN3fBj7Lp/MHm5mO5wwJtwmfyv9dD/MaXL8Gx9L/OiBlYyjoEN4fOsiwR71jHE+w00vcglA1Mr/A6eBjaHANjeXb1cElbhMfhKDFAxV0aN2pd7RtC88bKQQhuDbQyR3EIfGL3oGKra73D/GlAGVBrBXh3R45vSHkNR4qCHwILUiNW+DUO3Bjm36P+bTZYsGodxugmxFVQpxI/pI+NWtqc6TGQxMcEd89Caxr/18wrNIUlLsXoZG098HdGPHJ/18cl/C80HZp368lBf/PkT75v29BDaVL1lWvuIoK/Y/G+QfPbQjQcwv/xK1c8D8d+68lHlkXsqhfD81NVujfqtev4MY/yX6WtrOwUVw8+xn1DuHLo/pD42/4fajvZrSKjm7QVVTgQrB0YqGbM7bFPN5k2IpJBp2S+l+G5XLFeTbun4lwBFfMsaviev0nwrcU93jlkZq+fy4nyGTksYWX+mcSV2cu39kOrHKcC9VXzNsa988E/kQ922M7J2E5bjNRq0kmNmbwLb6z9DnpP5yh77C9xUXF3Huz8ofDDyoed7+c5R9Oby3Q8XN7fP5MRLrs8Mz1Z9SfrtG6w85db3T+zePthWKnitHxF/VfQNn9r6oDRR8DafvsRcn7B6DueZL4qV6hM7hs/yibmg8P4nbZ+4dC2ipi8FRHvbL3D11FljpLcK+P/bL3H/G5bDH4OoJW2fuHrgJLPeP4pI0rpfkryRdr3GxFsFLuP7SWlqz0UcEv069L+72aiJ/P1Fc2NU9zs0Xfb6X+6/X6gpKJ7s3wXx3Uq4cqSfOk7P1GH0Np3Qk8rZfn77f/F/8B6SuKPw==:8BDB" +
                        "^FT329,31^A0I,23,24^FH^FD" + GreekConverter(AddressReprint) + "^FS" +
                        "^FT400,57^A0I,23,24^FH^FD" + GreekConverter(OccupationReprint) + "^FS" +
                        "^FT329,83^A0I,23,24^FH^FD" + GreekConverter(FathWifeReprint) + "^FS" +
                        "^FT425,109^A0I,23,24^FH^FD" + GreekConverter(NameReprint) + "^FS" +
                        "^FT425,135^A0I,23,24^FH^FD" + GreekConverter(SurnameReprint) + "^FS" +
                "^PQ1,0,1,Y^XZ";

        String tmpFine3 = "";
        if (!conB) {
            tmpFine3 =
                    "^XA" +
                            "^MMT^PW561^LL0208^LS0" +
                            "^FO64,160^GFA,04096,04096,00064,:Z64:eJztlD1u3DAQhYeYYjrzAgvxGipo8UpOFQERIC5UbOc9gnMSm4aLLX2BIGHgwl1CI4UZWNjJ0NQ6KQMkpZ4ASSD0+HH+BLBq1T9I896/8BVfq6g9TuxljXPSNy+xdVF/VNHi9ynKg4+WfAvOY3RH98KvnwLxAd6x4UZFArzjIGvjfEHmKVkXXSr+wzbIY5wtwQBjVMHM5on5jf/Aji/xT34+czkLn7OKHe7uCn88doXP+ZWf+VgDoN02tmCMCsIPWPjQ9w36QXBpUGFD023hw+DI9yr1KsAGfWqq32gWf6c5CF8uWVKyOfqu1dvUqWh4eih8yG7vW4wDRujQ5676O+Ige8udgAIVPsZxptBYUrEpfvVU+Co5iR+DLXwKF5vqn7HwM7HXnoVPWbZxwjdZT7GRtDC8FL6EvRN+6Cq/t9Wf8C60KtHOk2KJn2bQYC16PZNkWgWtYSx8CucHP9CXjfg3b35M+FCiwr3Xe5b8C99J7OJ/5UtbXIKLimftzx/9Ea+uKRk5Xptf/SqpR29VxAMQLfU30PTlIMIvbdHAeRA/gX2Eo/DFP0saLur5MzxKVTN+A71f6q/9WS/8tvLpGj4UPkX7vGWJX+fC7yofBvUMLfTqM5Be6o/BXEj+av4DfoIl//b99l7Api/xb9JSP2Dhd+oo/KnWH5POv+uPs/pR62/H6X6KQ2NL/pvFTzCCBaNmiX+pPyRKFJb+i2pWX2v/WUeH29RvbKm/jkv/S055L3fNOz71v46n/nc8ZXyo/W/dbpIl20n/p8UvfTMomT+YiZFP80fBpDp/jlWm0/yZPYYxDEbmL9WTFn6HMv8+y9sbX3rtZ51/x9vSB3X+JUPRyZc3Mn86/OUfZtWqVatWrVq1atV/1C9Vgqkh:D430" +
                            "^FO128,0^GFA,08960,08960,00056,:Z64:eJztmLGO20YQhv/RrEkWNG8DpNhCiGSnSSkgQSCkuVV0gNs8Av0GNuDiigO8Cg/KFYHcuswjuEy5pwvOTQo/Ag03KVNegMMxQwOW2h0FToyAfyUI/PRzuNS/swMM+o9kDFM8o6NOyVXVCKHjF1qu91utVoaVHDAChyiuWrGQ3mQH+I3QZnqOkcGz0XMhQ8s/KymDe70flUquQhlGqPlC7ec8i99LJQe4VtYPv6o5W9Mq4I2am87QBbRqzlucAbWaGzTo44sYi2DJkZLjBnVwfKHN3d5P9gd9XrOESxf0ec2F589ifqvmLtrMtfmdmht7Y/1Indf8bZ3Na9bmLtE3vR8VWrvmYS31sTZ3iReebSSr5MDvWlk/fV7zIvb5qc5rDi0c8Keag4cd8nrQp6bMsMtwgjB+/hcepefSKOOywjVC8fwmvk7ncsNmjGuSBs36qMizitcOWw6Yzl/FKp0zzBZLOQDMrPUKv4z5F1xJYbOpi4r+2hDFyaVsKHZ6X1OfnG3i8eVaOtC6UjzPLO8oPlmcC+fNo/T9aNTXV8cNULSFoj5ZP575+DtQRqdcv3kb/5APYaZ6nmxc7X+AnCBq3foV5XE8lTsOmvdT/JyZxJlENoXX6v190KB/QUSIT7u3B/TXIT7o3l1IjGr9/GMsDpiHICK0B8xDhIte31+/54K+v5YDNXzQ99e93+Og76/fc1DPQ2TdIr4M+v66r+9r6Pvr/j6/wwHzEOE+P6C/xuQOXxzQX2Nyi3Lorwd9klp08YnBUj2/fts1dxm2pJ1fe2q49zvX5jVveCN+G21eZ4bO7ZLU8+uqwnq+pbWWcwa/zZfUaPN6XmL91VXXKOfX3hqcnCw7bV63hdR3vaVGOb/2Rp7n+ZJW2pznijc/bilo85ryPKclqfNa3s+cxa9V2nXhNKcrCrXSb9CgfyJC6V3KhZLYp5OOPvwfOJRtEtcn9lH34mbnZ+qknsvTT3KyLvf5Uk7nKRw4483I7fOzPE7rnfrEzqyeqyRBq/l+fziz0yROEvt7Y3f7Ed+6OomTxG7K2Qc/ykvnU7A+sS+LnR9vyiKmcK0Tv5e7+sgYk+Ynib2y++eZlVVI4fr1C6/265eX99I4anK8GY93P3ObJ3Ek7yfao5vdF2dpLQJ18RlqapMuHvS/1N9ySOJw:8E1C" +
                            "^FT457,109^A0I,23,24^FH^FD" + GreekConverter(B3) + "^FS" +
                            "^FT457,7^A0I,23,24^FH^FD" + GreekConverter(B7) + "^FS" +
                            "^FT457,32^A0I,23,24^FH^FD" + GreekConverter(B6) + "^FS" +
                            "^FT422,58^A0I,23,24^FH^FD" + GreekConverter(B5) + "^FS" +
                            "^FT457,84^A0I,23,24^FH^FD" + GreekConverter(B4) + "^FS" +
                            "^FT152,135^A0I,23,24^FH^FD" + GreekConverter(B2) + "^FS" +
                            "^FT457,135^A0I,23,24^FH^FD" + GreekConverter(B1) + "^FS" +
                            "^PQ1,0,1,Y^XZ";
        }

        String tmpFine4 = "";
        if (!conC) {
            tmpFine4 =
                    "^XA" +
                            "^MMT^PW561^LL0112^LS0" +
                            "^FO256,64^GFA,02560,02560,00040,:Z64:eJztkjGOGzEMRSmoYGddQB5dw8VAupJKFQNYxhYpc4MgJ1lr+yAn2ILAXoDbqRiMQskZB7lAmsyHG2Eevj/5CXDoX8i1/ArmhSYPVL3X5Z6Bzb22D/KBzHdFoT041QAVTRZitBOWa4GIjtsnz4EC71xoL40MkBc/9s7IC+op1EZ1CdSqcNvjj/GNjGJrgdkap98Y0qSz4pQQeFEF7OC8ubHJbfUXrt58M7lC27SEnKt88YpgHdxigBGuq00cJ3zHnOSBBdhxjy0cD64KZ6BVXyWfXo1wofuRo74GTXpwih1EAynZRfKpipBgnns+V1AVp4oundPkxDJfqt+4bar2fP6ic5PQ3U+RycNPuIgwx/OPeF273wJT0uXK2P2Ew7Lnk5+n8JNkzpHvlMTvdz5FmgaXxhxncl8kH659Xhd7vvNj3qL4z/7AUfja9/fa85n61/7qsw8EVxxytDj6QO77W+Log2DumJF+P1o1FLTs7zT6lQdLv6n3G9p92+/lJpxsSvJZvLUCWNznfi+hubbfnxYue/28P03heX+736FDhw4d+j/0C3dUEYQ=:CE9A" +
                            "^FO128,0^GFA,01024,01024,00016,:Z64:eJzdkD1ug0AQhWc8a0DyBq+7KSI5uQElJUdIbsARSOcisrFBhALBFXKUPUAukI4yZUoqCAq74D5Forzu0+j97AL8d6GAHN0XraLgm6mACppGcxybO0pMc+AkmgwrqvBcA4fpxDSIeyRQOxNIvby7UMobW7CSTzmeO9dyLd9rbDrfIObicdygzJ3SWpY1lLyZ8hHebtY5lrwzffCxbQoqObD8sHbJFSo2++DgU9ZvO7MfIBj3H7DTwc8+6reE2YCf+/75pGfO+n3fn1rDmhxHCQWxdTg+eT4vLD2UgiGyzEyVx9BaDhQKTy332KOCec5PE0ZH8dyfhh5dwtmP+lYiBVd+3yd6veon18Fo6adxP7ZHmw84QAJ6ft+f0hdwAkuM:F7AD" +
                            "^FO384,0^GFA,01536,01536,00024,:Z64:eJztk7FKA0EQhmd2lrsrjnWFIFOIYmM9MSkOEU3A3lc40NIiDyDkwolaWafMI1imjGhhIZrS8ipLsbwiGBc1ObKVjYXoFAf78TH8NzsL8F8/U9qqVMgq0YvcJIEIuU/g+aytIGvfh8iwUGREeTycaKFwIqceXyoDoaVSzvxATJ2u5vqtxwOmq+kZN549PbBYvwns1pGXX4lq5Eq2Nj2fRNcJpVnz+tMgbuQ02FnzOKa2nmPaXPE4FCJTKBLj+6NUujhK/Pn89UJkFI5jDTbMJtjHL075BSUcGQPJXlHiBWUzX9Oq1WEAVjriDvNORhmOyO1NmgheVvumdcgxuj1rseD525wrQ8MIXgBG0SLX+Krh2Pla1lV1XxRDaWAX4PFOunk8yw8YwVMNmgAH99LuRTM7oz71t7/69/J+5VuyG3D7kecabTWKYcCH+Wf+Xjas+Djmdu5yuP+9zsYVL5izB/cO3XymWVHxVJZh3/lh62Qd0u9cwm+td3XJUw4=:6AC8" +
                            "^FT143,39^A0I,23,24^FH^FD" + GreekConverter(C1) + "^FS" +
                            "^FT143,15^A0I,23,24^FH^FD" + GreekConverter(C2) + "^FS" +
                            "^FT409,15^A0I,23,24^FH^FD" + GreekConverter(C3) + "^FS" +
                            "^FT409,39^A0I,23,24^FH^FD" + GreekConverter(C4) + "^FS" +
                            "^PQ1,0,1,Y^XZ";
        }

        String tmpFine5 =
                "^XA" +
                        "^MMT^PW560^LL0300^LS0" +
                        "^FO25,0^A0I,23,24^FH" +
                        "^FB500,13,,J," +
                        "^FD" + GreekConverter("Στη(ν) " + AddressReprint + " σήμερα την " + DateReprint + " ημέρα " + DayReprint + " και ώρα " + TimeReprint + " ο/η υπογεγραμμένος/η " + OfficerName + " Δημοτικός Αστυνομικός, κατέλαβα τον πιο πάνω παραβάτη να υποπίπτει στην παράβαση ή τις παραβάσεις που σημειώνονται πιο κάτω με το γράμμα Χ και για τις οποίες επιβάλλονται τα παραπλεύρως αυτών αναγραφόμενα πρόστιμα, κατ' εφαρμογή των διατάξεων του Δημοτικού και Κοινοτικού Κώδικα (Ν.3464/06) και του ισχύοντα Κανονισμού Καθαριότητας του Δήμου (αρ. αποφ.: 499/2011)" ) +
                        "^FS" +
                        "^XZ";

        String tmpFine6 =
                "^XA" +
                        "^MMT^PW561^LL0120^LS0" +
                        "^FO25,50^A0I,23,24^FH" +
                        "^FB500,3,,J," +
                        "^FD" + GreekConverter("X " + FineTypeReprint + " - " + FineAmountReprint + "€") +
                        "^FS" +
                        "^XZ";

        String tmpFine7 =
                "^XA" +
                        "^MMT^PW561^LL0120^LS0" +
                        "^FO25,50^A0I,23,24^FH" +
                        "^FB500,3,,J," +
                        "^FD" + GreekConverter("Ο ΠΑΡΑΒΑΤΗΣ         Ο ΒΕΒΑΙΩΣΑΣ ΤΗΝ ΠΑΡΑΒΑΣΗ") +
                        "^FS" +
                        "^XZ";

        String tmpFine8 =
                "^XA" +
                        "^MMT^PW561^LL0480^LS0" +
                        "^FO512,320^GFA,00512,00512,00008,:Z64:eJxjYKA1kOF+AKHlP4BpG3sILVMjAFFgAVVYA6XtGyC0PJTmb0CVh6m3geq3fwBVDzGXgXkBFVw9UgAAIdQKuA==:BFA3" +
                        "^FO512,224^GFA,00512,00512,00008,:Z64:eJxjYBjqQEYGlbaB8et/QBhw+g9UwgJKF0DpBCh9AEo3QGh5GM2ASstRw9WDBQAAle8JHQ==:B2DF" +
                        "^FO512,416^GFA,00512,00512,00008,:Z64:eJxjYBhuQKb+AZT+AKZt7CG0DCNEnIG5AULzQzXwwTRCaQsoXYPGtxGA0PZQc+Q/oJo3JAEADz0KFA==:857F" +
                        "^FO0,96^GFA,02560,02560,00040,:Z64:eJztkr9qVEEUxs/MLDPjcrg7QYgny5gdN41FwIlYpAhmschzXEihheAtQqrFnaigRZ7CV/AFxgja5BEsLrGx3C5bXHI9975EEO7XDAzf+fidPwCDBg0a9J9Itekv4fsgVU2ooVzdQJ5tJimf1oG/pNXHDRSJfZ8bj8tKG35G7LtlXzNbl6fryjSkUK8aWGUO1LqwFCTirtUQYA8SUIwhpIDIeTJaKEsAg8YgRU16e8R58F0kiBW5MkfSpLx2HnIEQKvUJYUPJKcdn7hoE9dbW+ZAkl5ZTRYS+7wX4rc/+OnMo47PCPbl7JHznPG/UHsnsgOIVqQNPbmKcstKCBIuQKRkZZ1ClHRjpXWQCKAiAUs6OIlq65OBEuEHKFig4ryodr5N7jCKxHmB897a+TxIuhxDWcAVqJStZL4gH3xlX+jzIvNd0/m5M+76MfPBCQjmG5W5cmbnIepR2fNZq8QXmj+P0r2JKag0B6i539DxTcdWytD3q9EopBfbpN1ZzFx7xswVUT+/2cxrs+jnJ4tCIz0d8/D3Qy6h3gegwxiYD3FaWC0z1DWI1jSIy6Nd0zi/eNeu1kfQ7/f1bbd5g9rkbr+qhTXZl4eFqh2lZ3fHNcFkwx3v/Wk/dvcieY+T+77qQYPuRf8AztyLzQ==:6B0F" +
                        "^FO384,96^GFA,01536,01536,00024,:Z64:eJztkTFOxDAQRcfYSoIUst4uVAkXQEZQpItBSHsNIwpaCgq6zGovNisuMoC0dcpUmAG8LAegQGh/+fT953sGYK+9/pX6kYILptmAn+McPDwkPmFwbTW8ANd4jqTf0gNL3trSXwJZfwakdeJB/F1Bt4g1nyBlCr84SX4z0k1EOzwDmcQVcnD9xMK74RW5gsRB8gvr/VKNToYYoE+eQWRXup6fZpNbI5XJn39w0zZ8Zaxd7/wapU/VtmFT1t0K6TufJN94768Le6x2fYDFf8BtOC3qw+WP/vfSX/sQLkp7JP483+6H7uKKeu5mU6Ujb/cjX11EhQ3VasxUJHj8lTPt9Rf0Dn29YjY=:D693" +
                        "^FO64,256^GFA,08192,08192,00064,:Z64:eJztVzFv20YUfjxe7hjgIFPIEAZgo7MNBB2C4DxFgxHKNRr1R3Q42UGy0mjRZHArihJUD0HQMUOGjB37AzqcJMNaMvQHdKDjJaO8eRDsHmVKpBNScpEOAaoPAnR8x0/ffffI904ASyyxxBJL/LewAQwAYl8JCpCTb2IbLTWfzwGQ/tgfByfQ4dYC/Vq+fi3Rh8ECfqLvfBRUl/oO9Bfw96n6uTL8ybaNHmWu7W8pPJICxlSJ51SHn1A1l++tROeVwbktoI1KluBRQM+4gEc04l5bh+/TaC7f+G1vYG+TiT6e6FNH+mC6Qog4vOmK+QaC1RB2kMNnAWzx78A4cDiPw47lFHNjqB8MqGuhlM/k92C8sYX/TIddx55D1mifIBoiO12l1j8A47XWX9Phr50F+oOBSQdX9RsdMBxbiF0dfrJIP1hH0EfOFf1u6v+bBf6/Pdwb2kPi+infbRCou77Yi8PDzEweJvkP0cofswgd6RX9Qk/OvTjcotFcvkvVi9smoanLG6NdE/bNwcVDHS4bbTWX/yUAfSYfF84Y1+KTwpnrrayYT6/F/1/Ayo1eP/X5SS5O/cdgudH8VV2fnx/9BOYr+PAIwQnwdYCoBMF0YiU4K+ni88oIQmMWtPg9ayVEsI5WgkjFS6QX5rhpmhfK95R5VlG16a30dFwZuOULQ22nfFfWcaXXBW9Q6Y1rcf0qrXaZGSJHveWKMK5mt4Io8X6JD5DqZ/V7L0UUAu/De8ZjPts7xEab2Ft/+TXM/Jqa8W0it2m592ttmPLB7x368gj8bXiKfXvyiweWgXQ5fc05sThPb3UIXzNFK+QZ/yCCD5zfA74GO8QbxfriEFdMre8IibGUtZS/L3eN01ZHZvxDOdgX8hGIXXqU8PlLdlfrNywuMeMyo/9C7sDb1kue8Q83g/u6+wDfoV0y6Yxs4x27QYlTZ7F/mfWPZV37ZyLr/yt458un4NfhMXYlxI951zIRglDvPbLE1f0PkWhbIuu/BAecX+6/ZcX61KCjpndqHGFPYVwZjqd36vw/bOv8O3bWP6VY7E7yP3jhxnulpaNNL2p10APdH0srZ9M7zeCDfv74n46V9U/0M7KG4AEqBRFLveYjeQVolp/OqhvvFvCTEoAy/ExV4OQgGRX1caM9+TIvUr6ZzjrUTUZFr7oXzuGbZ+aU50I+msNPlzQb0VmmoPAc8bntb4l/Cc7nzV62lGBObZdzj3QTvjGPP18fTfULe5Oce6Sf6Rf1xoBzXjUDVOW691R1sYn0dVycz4DhSHVYyYw2w1uBblgqh24o6TfGFUXHG+O4EjV7Y32NiV0e69PoWUNX6uGo2a1EF8NcPgQRX+063GSCiAED3mb6GumzPoG7ujl0LRFUo9BZPThJ++SVBYxEg9pC1zji1zHU6lhfa32bwG3sljtY1O70uvYG3jNy9eG1Lm6CI8KZ3CEg+7oFodg/gzXMbnZ0jXaC0F5jq5mSkNV/4z+jvqBE7MtnBGpHRF/H+vvwHLOVQya3TrX+c7ZXwLcu9RGvxlVW13++jmL/VVjFlhVafDXS+usWz//zqzduV/s3scD+duLfjPUxbHXcOx3cVOWJf5H/5xcxcdx1JGKc8T6DWv9y/2P/NZ2XLvNUdRA6Dd0Tc/kEV47/jvNfnuS/1hs3j8fNo8rpGNRwdDwcy/rogsRHj8f5fFTiI/38+XxUQj7wIPKiqve+MhpB0I2C1u98920V3QqqwVouP0byBuY/4n4tGfxYyK8mW5E7yVUyuF/AJtNmYExPYlfeyI1eMtgs4CPj/HLgBTyRzE5XguByUNQyiJG0FU/JHP1sK87XT4cyRx8W8ZdYYoklllhiiSW+ePwDjkWFEA==:1890" +
                        "^FO32,160^GFA,08704,08704,00068,:Z64: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:DC04" +
                        "^FO0,352^GFA,09216,09216,00072,:Z64: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:CED4" +
                        "^FO36,438^GB27,27,1^FS" +
                        "^FO270,409^GB27,27,1^FS" +
                        "^FO131,326^GB27,27,1^FS" +
                        "^FO233,299^GB27,27,1^FS" +
                        "^FO400,300^GB27,27,1^FS" +
                        "^PQ1,0,1,Y^XZ";

        String tmpFine9 =
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

        String tmpFine10 =
                "^XA" +
                        "^MMT^PW561^LL0170^LS0" +
                        "^FO25,50^A0I,20,19^FH" +
                        "^FB500,5,,J," +
                        "^FD" + GreekConverter("Γ) Στην " + GreekConverter(munBank) + ", στον λογαριασμό " + GreekConverter(munBankIBAN) + " ή web banking στον ίδιο λογαριασμό. (Να αναγράφεται το ονοματεπώνυμο του οφειλέτη, ο αριθμός κυκλοφορίας του οχήματος και ο αριθμός κλήσης ΚΟΚ)") +
                        "^FS" +
                        "^XZ";

        String tmpFine11 =
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

        String fine = tmpFine1 + tmpFine2 + tmpFine3 + tmpFine4 + tmpFine5 + tmpFine6 + tmpFine7/* + tmpFine8 + tmpFine9 + tmpFine10*/;
        return fine;
    }

    private String createZplQR() {
        String tmpMiddle=
                "^XA\n" +
                        "^MMT\n" +
                        "^PW561\n" +
                        "^LL0200\n" +
                        "^LS0\n" +
                        "^FT195,202^BQN,2,8\n" +
                        "^FH^FDLA,\n" + MID + "/" + mRefClick.getKey() + "^FS\n" +
                        "^PQ1,0,1,Y^XZ";



        String middle = tmpMiddle;
        return middle;
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

    //Temporary Data
    private void showDataFromUser (DataSnapshot dataSnapshot) {
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
            databaseReference = FirebaseDatabase.getInstance().getReference("Fines").child(MID).child("CleanlinessFines");
            databaseReference.keepSynced(true);

            P1 = RUInfo.getMACAddress();
            P2 = RUInfo.getPrinterFriendlyName();
            OfficerName = RUInfo.getLname() + " " + RUInfo.getFname();
            MunicipalityShort = MunicipalityIndex.subSequence(6, MunicipalityIndex.length()).toString();
            SignatureImage = RUInfo.getSignatureNum();

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

    private void showDataFromFine (DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            RetrieveCleanlinessFineInfoFirebase RInfo = new RetrieveCleanlinessFineInfoFirebase();
            RInfo.setAddress(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getAddress());
            RInfo.setFathWife(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getFathWife());
            RInfo.setName(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getName());
            RInfo.setSurname(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getSurname());
            RInfo.setOccupation(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getOccupation());
            RInfo.setDate(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getDate());
            RInfo.setDay(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getDay());
            RInfo.setFineAmount(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getFineAmount());
            RInfo.setFineType(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getFineType());
            RInfo.setTime(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getTime());
            RInfo.setUserID(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getUserID());
            RInfo.setPaid(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getPaid());
            RInfo.setLat(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getLat());
            RInfo.setLon(dataSnapshot.getValue(RetrieveCleanlinessFineInfoFirebase.class).getLon());
            AddressReprint = RInfo.getAddress();
            FathWifeReprint = RInfo.getFathWife();
            NameReprint = RInfo.getName();
            SurnameReprint = RInfo.getSurname();
            OccupationReprint = RInfo.getOccupation();
            DateReprint = RInfo.getDate();
            DayReprint = RInfo.getDay();
            FineAmountReprint = RInfo.getFineAmount();
            FineTypeReprint = RInfo.getFineType();
            TimeReprint = RInfo.getTime();
            PaidReprint = RInfo.getPaid();

            if (dataSnapshot.hasChild("Fine B")) {
                RInfo.setB1(dataSnapshot.child("Fine B").getValue(RetrieveCleanlinessFineInfoFirebase.class).getB1());
                RInfo.setB2(dataSnapshot.child("Fine B").getValue(RetrieveCleanlinessFineInfoFirebase.class).getB2());
                RInfo.setB3(dataSnapshot.child("Fine B").getValue(RetrieveCleanlinessFineInfoFirebase.class).getB3());
                RInfo.setB4(dataSnapshot.child("Fine B").getValue(RetrieveCleanlinessFineInfoFirebase.class).getB4());
                RInfo.setB5(dataSnapshot.child("Fine B").getValue(RetrieveCleanlinessFineInfoFirebase.class).getB5());
                RInfo.setB6(dataSnapshot.child("Fine B").getValue(RetrieveCleanlinessFineInfoFirebase.class).getB6());
                RInfo.setB7(dataSnapshot.child("Fine B").getValue(RetrieveCleanlinessFineInfoFirebase.class).getB7());
                B1 = RInfo.getB1();
                B2 = RInfo.getB2();
                B3 = RInfo.getB3();
                B4 = RInfo.getB4();
                B5 = RInfo.getB5();
                B6 = RInfo.getB6();
                B7 = RInfo.getB7();
                conB = false;
            }
            if (dataSnapshot.hasChild("Fine C")) {
                RInfo.setC1(dataSnapshot.child("Fine C").getValue(RetrieveCleanlinessFineInfoFirebase.class).getC1());
                RInfo.setC2(dataSnapshot.child("Fine C").getValue(RetrieveCleanlinessFineInfoFirebase.class).getC2());
                RInfo.setC3(dataSnapshot.child("Fine C").getValue(RetrieveCleanlinessFineInfoFirebase.class).getC3());
                RInfo.setC4(dataSnapshot.child("Fine C").getValue(RetrieveCleanlinessFineInfoFirebase.class).getC4());
                C1 = RInfo.getC1();
                C2 = RInfo.getC2();
                C3 = RInfo.getC3();
                C4 = RInfo.getC4();
                conC = false;
            }
        }
    }

    //Digital Signature
    /*
    public static Bitmap Base64Decode(String base64EncodedData) {
        byte[] decodedString = Base64.decode(base64EncodedData, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
    public String Base64String(String base64) {
        String s = "";
        Integer lastI = 0;
        for (int i=0; i < base64.length()-2; i++) {
            if (base64.subSequence(i,i+2).equals("\n")) {
                lastI = i + 2;
                s = s + base64.subSequence(lastI, i);
            }
        }
        s = s + base64.subSequence(lastI, base64.length());
        return s;
    }
    */
}


