package com.example.android.rhoe_app_1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.android.app_v12.R;
import com.example.android.rhoe_app_1.FirebaseFine.RetrieveFineInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseUsers.RetrieveUserInfoFirebase;
import com.example.android.rhoe_app_1.Zebra.DemoSleeper;
import com.example.android.rhoe_app_1.Zebra.SettingsHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


public class FineListActivity extends AppCompatActivity {

    private static final String TAG = "FineListActivity";

    //Zebra Printer
    private ZebraPrinter printer;
    private Connection printerConnection;
    private TextView ConnectivityStatusFineTextView;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef, databaseReference;
    DatabaseReference mURef, userDatabaseReference;
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
    boolean munB = false, paidB = false, unpaidB = false;
    ListView mListView;
    ArrayList<Spanned> list = new ArrayList<>();
    ArrayList<String> listKey = new ArrayList<>();
    ArrayAdapter adapter;

    private String PlateReprint, PlateCountryReprint, ColorReprint, DateReprint, DayReprint, TimeReprint, AddressReprint, FineAmountReprint, FinePointsReprint, TypeReprint, FineTypeReprint, BrandReprint, PaidReprint;
    private String A1 = "", A2 = "", A3 = "", A4 = "", A5 = "", A6 = "", B1 = "", B2 = "", B3 = "", B4 = "", B5 = "", B6 = "", C1 = "", C2 = "", C3 = "", C4 = "", C5 = "", C6 = "", C7 = "", C8 = "", D1 = "", D2 = "", D3 = "", D4 = "", D5 = "";

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
        plate = "";
        populateListView(plate, munB, paidB, unpaidB);

        StorageReference storageRef = storage.getReferenceFromUrl("gs://testproject-328af.appspot.com/");
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
                    adapter.clear();
                    populateListView(plate, munB, paidB, unpaidB);
                } else {
                    munB = false;
                    adapter.clear();
                    populateListView(plate, munB, paidB, unpaidB);
                }
            }
        });
        PaidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (PaidCheckBox.isChecked()) {
                    paidB = true;
                    adapter.clear();
                    populateListView(plate, munB, paidB, unpaidB);
                } else {
                    paidB = false;
                    adapter.clear();
                    populateListView(plate, munB, paidB, unpaidB);
                }
            }
        });
        UnpaidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (UnpaidCheckBox.isChecked()) {
                    unpaidB = true;
                    adapter.clear();
                    populateListView(plate, munB, paidB, unpaidB);
                } else {
                    unpaidB = false;
                    adapter.clear();
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
                            showFineDetails(FineListActivity.this);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });


    }

    public void showFineDetails(FineListActivity view) {
        final AlertDialog myAlert = new AlertDialog.Builder(this).create();
        myAlert.setTitle("Βεβαίωση Παράβασης Κ.Ο.Κ.");
        myAlert.setMessage(Html.fromHtml("<b>Ημερομηνία/Ώρα</b> <br>" + DateReprint + " " +TimeReprint + "<br>" +
                "<b>Αρ. Κυκλοφορίας</b> <br>" + PlateReprint + "<br>" +
                "<b>Διεύθυνση</b> <br>" + AddressReprint + "<br>"));
        myAlert.setButton3("ΔΙΑΓΡΑΦΗ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myAlert.dismiss();
                final AlertDialog myAlert1 = new AlertDialog.Builder(FineListActivity.this).create();
                myAlert1.setTitle("Είστε σίγουροι ότι θέλετε να διαγράψετε την βεβαίωση");
                myAlert1.setButton2("ΝΑΙ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefClick.setValue(null);
                        SearchbarEditText.setText("");
                        adapter.clear();
                        populateListView(plate, munB, paidB, unpaidB);
                        myAlert1.dismiss();
                    }
                });
                myAlert1.setButton("ΑΚΥΡΩΣΗ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SearchbarEditText.setText("");
                        adapter.clear();
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
                myAlert.dismiss();
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
                adapter.clear();
                populateListView(plate, munB, paidB, unpaidB);
            }
        });
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
                        adapter.clear();
                        SearchbarEditText.setText("");
                        adapter.clear();
                        populateListView(plate, munB, paidB, unpaidB);
                        myAlert2.dismiss();
                    }
                });
                myAlert2.show();
            }
        });
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

                databaseReference.addValueEventListener(new ValueEventListener() {
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
                                            list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                    item.child("CarPlate").getValue().toString()));
                                            listKey.add(0, item.getKey());
                                            adapter.notifyDataSetChanged();
                                        }
                                    } else if(Objects.equals(RealUID, userID)) {
                                        if (Objects.equals(plt, "") || RealPlate.contains(plt)) {
                                            list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                    item.child("CarPlate").getValue().toString()));
                                            listKey.add(0, item.getKey());
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                } else if (paid) {
                                    if (mun) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "No")) {
                                            list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                    item.child("CarPlate").getValue().toString()));
                                            listKey.add(0, item.getKey());
                                            adapter.notifyDataSetChanged();
                                        }
                                    } else if(Objects.equals(RealUID, userID)) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "No")) {
                                            list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                    item.child("CarPlate").getValue().toString()));
                                            listKey.add(0, item.getKey());
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                } else {
                                    if (mun) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "Yes")) {
                                            list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                    item.child("CarPlate").getValue().toString()));
                                            listKey.add(0, item.getKey());
                                            adapter.notifyDataSetChanged();
                                        }
                                    } else if(Objects.equals(RealUID, userID)) {
                                        if ((Objects.equals(plt, "") || RealPlate.contains(plt)) && Objects.equals(RealPaid, "Yes")) {
                                            list.add(0, Html.fromHtml("<b>" + item.child("Date").getValue().toString() + " " + item.child("Time").getValue().toString() + "</b> <br>" +
                                                    item.child("CarPlate").getValue().toString()));
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
            sendFineLabel(createZplFine());
            sendFineLabel(createZplQR());
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
                        "^FO352,1760^GFA,08064,08064,00028,:Z64:eJztmU9oJMcVxqu6Sj21dm+rhhhS2ghNKQsmeI0pWTq0tSKq9gzJZQ86+lgr+WBMMH2PQTU7QhFm8No3HZfksngXodyUPdVIxviwOD7q2LZCCI5JJvgyYGHl9Yw00ta0jDGJ2YX5Drp8/ObVn67++qkQGutHCaPZkEqeNjGinDOMv9N8aNrrYSTVQYugOGGMBDu5GnourRRcB7hpIeCvvuAd3W0Dd0QselkwYpmRQ2+9cwW411OM0ALUQ3w9H3pvtxhw+2sEoVehHkoWzz3T5BuSN2aBmy/qKa6H3mxr7/Fy7/FNqHdze4nYPHFDbw7zxkLWqAI3x1/ASF/g5lvyd1J9+guop7ZeJEgyO/SqWDckf2MKOHVYh3FeOfeukfxlqQ6fh3rqH0ctK66eezNYL0i+MlFwGcwymjhf7JjYJanyyRv3kUqaDz7amTz3ImynJdeV5S5SU82T/xxXSvdrrGdA683ucq9W7j24u7e493tb6mFK4bHWpV7AQpVYU+pREfCqlaVeLIiYu8SLOBGzSJV6SRKw2Us4IQhNbTknRBB/cQkH4wSu3AtZGNpL5kciSqzTpR6sJ7Hf2lJvHXcxumQfxvrfK/gej/wQrvY3zDsUO8oDrTlfh0ii692B99K/CMs3472YBcYl7EY7YeHbu6dgg4jbG2KaRlRpLjBdEud59H4gXns/iaIoTlwiSLQkmJQDC5PveJVySivHEdQLKUSSMqcDbu0wtqnCOLwfO8XiCCJp6GEqrm4ARxmFeoLOC36WKzFpi8lNFUcBI265l0Tz28lZHlFM+QRVlBJO9AsrnM7xIRegLRYBF5MkhPmxSG0N8wijbTFV1MMcxingNXEozzIH28PB/PA9AfOLo6SXD/NIr/DbFMaHcw7zI1Rl+iyPXsqz/nru3MslzO9GWyXuLHNqLhOdDexOnIN9ELAPwn1PHtnLLXy5Ndb/W5ziu12umdUi4957MInwe7sJPGFO9JSXR8AhzhW3mmd+HjGGrFIQRQ69pbz3da2HLee1rtXolp9H8R62SXJtF7h3XvOyg8Lg4CBI4BpVL49idfMPXcUkcKvz3m9S+G5VcCKAq1e93wxkx6okyoE7+rPHBTqFerTg0qo3B2JymB9EkUNm1/OwTmF+FQuc/tzLI+K+fe/Ro5b9CLiel0fY1SrfnBB04pDOxnn0TKrfKFWc8c9RX0WjFOeZf476KholPnqOBl7RKMH5s/moVzRKht3yn8++ikbJsHdKv+s0NEo6avjnoS8JjVLOVv3z11fRKGla989fX0WjZNiaf476KholI9IPy7x+oyTy3bL5FY0SfO2ulK1L0Sj9OjfdsvUsGqWKG5+jZ1JwgNKm1fWjfVxznjfTk19Ym68e/ZE8l3se56aDbKbTADp4z5Ni9p61u7ftHYKM58GDOde0n6fo9VEuZ9f/Yu2na/ZL6Pw9T0erVexu1cs4w34pWu6zVbRKJv1xwsGbauqPCw6Pcq9cuWP219CXJeNkb06Esl46Til+FW+qgz/Z/VFO8TcmNnin07wzys3kCdkSLv9368HIelZcRkSk9TQ5qfneWE+7yLHWRsIXmp7GtWPzhBc8NEZK+EAz/2wt7jzpIaG1BE6knQ7s/pNekgOXJNGaPRDM49SAoylKI+FxZlAvXENrkfA4PeBIHaWUe5w7yeVyNyFH6ChOpOc1tFnIOAYuVB6XD+oRqBdIr152Pk7i58rp/GKYH8k97nRdptNmE3tccN+sfiVffbQH60nckxzsw29P5M9Oin3Al/x/YqynXNguUNTAwn8++7IvRugzspWYEk/XgSM8K/PcWht9ErC3yrz1zseofixuldX7yn6NDnbE4zLO2DcrHQofaSWetEuLB222WcZV0YJKo6mwjJu3X8vO1vN3ZCnXMPqD6kiOFbqG2sYdPkfyMg9TpVdmSrk4iBZdbwaVcbT/fT09kmNjPfVqYKWl1ryCNL1wf9TX30lipHFiMXdR7vUWDSK0VFpkmb54f9RXEERGJg7liYsu3B8NvJAaAwGmuaYX7o/6ImFoZOyQA27R8zAlOqMaOa7f9c87aZO8Rxz66/bBwwv3R6cc1itEowavj+QY2iJmKyzq7UcX7o8G2qbpdn+cDXrF9w7j620B81OfRFd9b2W6H5jG1OmEZ6EsWQ2lQ4v5wcPJEU/9pgi+nyP97vj+aKyfTP8FiAPvMA==:5FB4\n" +
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
                        "^FO0,1376^GFA,09216,09216,00072,:Z64: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:CAA8\n" +
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
                        "^CI34^FT388,1771^A0I,20,19^FH^FD\n" + GreekConverter(C8) + "^FS\n" +
                        "^FT160,1802^A0I,20,19^FH^FD\n" + GreekConverter(C7) + "^FS\n" +
                        "^FT487,1803^A0I,20,19^FH^FD\n" + GreekConverter(C6) + "^FS\n" +
                        "^FT368,1833^A0I,20,19^FH^FD\n" + GreekConverter(C5) + "^FS\n" +
                        "^FT421,1864^A0I,20,19^FH^FD\n" + GreekConverter(C4) + "^FS\n" +
                        "^FT438,1921^A0I,20,19^FH^FD\n" + GreekConverter(C2) + "^FS\n" +
                        "^FT114,1949^A0I,20,19^FH^FD\n" + CarOwnerNo(C1) + "^FS\n" +
                        "^FT462,1893^A0I,20,19^FH^FD\n" + GreekConverter(C3) + "^FS\n" +
                        "^FT251,1949^A0I,20,19^FH^FD\n" + CarOwnerYes(C1) + "^FS\n" +
                        "^FT134,2011^A0I,20,19^FH^FD\n" + GreekConverter(TypeReprint) + "^FS\n" +
                        "^FT120,1981^A0I,20,19^FH^FD\n" + GreekConverter(ColorReprint) + "^FS\n" +
                        "^FT465,1983^A0I,20,19^FH^FD\n" + GreekConverter(BrandReprint) + "^FS\n" +
                        "^FT126,1639^A0I,20,19^FH^FD\n" + GreekConverter(D5) + "^FS\n" +
                        "^FT127,1670^A0I,20,19^FH^FD\n" + GreekConverter(D3) + "^FS\n" +
                        "^FT387,1516^A0I,28,28^FH^FD\n" + GreekConverter(DateReprint) + "^FS\n" +
                        "^FT308,1321^A0I,20,19^FH^FD\n" + GreekConverter(AddressReprint) + "^FS\n" +
                        "^FT456,1320^A0I,20,19^FH^FD\n" + GreekConverter(TimeReprint) + "^FS\n" +
                        "^FT121,1344^A0I,20,19^FH^FD\n" + GreekConverter(DayReprint) + "^FS\n" +
                        "^FT362,1346^A0I,20,19^FH^FD\n" + GreekConverter(DateReprint) + "^FS\n" +
                        "^FT394,956^A0I,20,19^FH^FD\n" + /*Πόντοι*/ "^FS\n" +
                        "^FT525,1220^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(145,194, FineTypeReprint)) + "^FS\n" +
                        "^FT527,1244^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(95,144, FineTypeReprint)) + "^FS\n" +
                        "^FT527,1271^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(45,94, FineTypeReprint)) + "^FS\n" +
                        "^FT499,1294^A0I,20,19^FH^FD\n" + GreekConverter(FineSplitter(0,44, FineTypeReprint)) + "^FS\n" +
                        "^FT485,1372^A0I,20,19^FH^FD\n" + GreekConverter(OfficerName) + "^FS\n" +
                        "^FT150,1576^A0I,25,24^FH^FD\n" + /*Πληρωμή*/ "^FS\n" +
                        "^FT420,1576^A0I,25,24^FH^FD\n" + GreekConverter(FineAmountReprint + "€") + "^FS\n" +
                        "^FT447,1639^A0I,20,19^FH^FD\n" + GreekConverter(D4) + "^FS\n" +
                        "^FT488,1670^A0I,20,19^FH^FD\n" + GreekConverter(D2) + "^FS\n" +
                        "^FT414,1698^A0I,20,19^FH^FD\n" + GreekConverter(D1) + "^FS\n" +
                        "^FT429,2010^A0I,20,19^FH^FD\n" + GreekConverter(PlateReprint) + "[" + GreekConverter(PlateCountryReprint) + "]" + "^FS\n" +
                        "^FT471,2077^A0I,20,19^FH^FD\n" + GreekConverter(B6) + "^FS\n" +
                        "^FT443,2106^A0I,20,19^FH^FD\n" + GreekConverter(B5) + "^FS\n" +
                        "^FT385,2137^A0I,20,19^FH^FD\n" + GreekConverter(B4) + "^FS\n" +
                        "^FT121,2166^A0I,20,19^FH^FD\n" + GreekConverter(B3) + "^FS\n" +
                        "^FT485,2165^A0I,20,19^FH^FD\n" + GreekConverter(B2) + "^FS\n" +
                        "^FT405,2196^A0I,20,19^FH^FD\n" + GreekConverter(B1) + "^FS\n" +
                        "^FT422,2264^A0I,20,19^FH^FD\n" + GreekConverter(A6) + "^FS\n" +
                        "^FT423,2295^A0I,20,19^FH^FD\n" + GreekConverter(A5) + "^FS\n" +
                        "^FT403,2324^A0I,20,19^FH^FD\n" + GreekConverter(A4) + "^FS\n" +
                        "^FT417,2354^A0I,20,19^FH^FD\n" + GreekConverter(A3) + "^FS\n" +
                        "^FT456,2382^A0I,20,19^FH^FD\n" + GreekConverter(A2) + "^FS\n" +
                        "^FT431,2415^A0I,20,19^FH^FD\n" + GreekConverter(A1) + "^FS\n" +
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
                        //Signature
                        "^FO128,0^GFA,06912,06912,00036,:Z64:\n " + Base64String(SignatureImage) + "\n" +
                        "^PQ1,0,1,Y^XZ";

        String header = tmpHeader;
        return header;
    }

    private String createZplQR() {
        String tmpMiddle=
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
            databaseReference = FirebaseDatabase.getInstance().getReference("Fines").child(MID);
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
    private void showDataFromFine (DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            RetrieveFineInfoFirebase RInfo = new RetrieveFineInfoFirebase();
            RInfo.setAddress(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getAddress());
            RInfo.setCarBrand(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarBrand());
            RInfo.setCarColor(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarColor());
            RInfo.setCarPlate(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarPlate());
            RInfo.setCarType(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarType());
            RInfo.setDate(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getDate());
            RInfo.setDay(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getDate());
            RInfo.setFineAmount(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getFineAmount());
            RInfo.setFineType(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getFineType());
            RInfo.setTime(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getTime());
            RInfo.setUserID(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getUserID());
            RInfo.setCarCountry(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getCarCountry());
            RInfo.setFinePoints(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getFinePoints());
            RInfo.setPaid(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getPaid());
            RInfo.setLat(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getLat());
            RInfo.setLon(dataSnapshot.getValue(RetrieveFineInfoFirebase.class).getLon());
            AddressReprint = RInfo.getAddress();
            BrandReprint = RInfo.getCarBrand();
            ColorReprint = RInfo.getCarColor();
            PlateReprint = RInfo.getCarPlate();
            TypeReprint = RInfo.getCarType();
            DateReprint = RInfo.getDate();
            DayReprint = RInfo.getDay();
            FineAmountReprint = RInfo.getFineAmount();
            FineTypeReprint = RInfo.getFineType();
            TimeReprint = RInfo.getTime();
            PlateCountryReprint = RInfo.getCarCountry();
            FinePointsReprint = RInfo.getFinePoints();
            PaidReprint = RInfo.getPaid();

            if (dataSnapshot.hasChild("Fine A")) {
                RInfo.setA1(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA1());
                RInfo.setA2(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA2());
                RInfo.setA3(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA3());
                RInfo.setA4(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA4());
                RInfo.setA5(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA5());
                RInfo.setA6(dataSnapshot.child("Fine A").getValue(RetrieveFineInfoFirebase.class).getA6());
                A1 = RInfo.getA1();
                A2 = RInfo.getA2();
                A3 = RInfo.getA3();
                A4 = RInfo.getA4();
                A5 = RInfo.getA5();
                A6 = RInfo.getA6();
            }
            if (dataSnapshot.hasChild("Fine B")) {
                RInfo.setB1(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB1());
                RInfo.setB2(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB2());
                RInfo.setB3(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB3());
                RInfo.setB4(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB4());
                RInfo.setB5(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB5());
                RInfo.setB6(dataSnapshot.child("Fine B").getValue(RetrieveFineInfoFirebase.class).getB6());
                B1 = RInfo.getB1();
                B2 = RInfo.getB2();
                B3 = RInfo.getB3();
                B4 = RInfo.getB4();
                B5 = RInfo.getB5();
                B6 = RInfo.getB6();
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
                C1 = RInfo.getC1();
                C2 = RInfo.getC2();
                C3 = RInfo.getC3();
                C4 = RInfo.getC4();
                C5 = RInfo.getC5();
                C6 = RInfo.getC6();
                C7 = RInfo.getC7();
                C8 = RInfo.getC8();
            }
            if (dataSnapshot.hasChild("Fine C")) {
                RInfo.setD1(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD1());
                RInfo.setD2(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD2());
                RInfo.setD3(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD3());
                RInfo.setD4(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD4());
                RInfo.setD5(dataSnapshot.child("Fine D").getValue(RetrieveFineInfoFirebase.class).getD5());
                D1 = RInfo.getD1();
                D2 = RInfo.getD2();
                D3 = RInfo.getD3();
                D4 = RInfo.getD4();
                D5 = RInfo.getD5();
            }
        }
    }

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

}


