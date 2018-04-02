package com.example.android.rhoe_app_1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.app_v12.R;
import com.example.android.rhoe_app_1.FirebaseUsers.RetrieveUserInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseUsers.UserBluetoothInfoFirebase;
import com.example.android.rhoe_app_1.FirebaseUsers.UserInfoFirebase;
import com.example.android.rhoe_app_1.Zebra.BluetoothDiscovery;
import com.example.android.rhoe_app_1.Zebra.SettingsHelper;
import com.example.android.rhoe_app_1.Zebra.DemoSleeper;
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

public class BluetoothConnectActivity extends AppCompatActivity {

    private Button BluetoothFindButton, EstablishConnectionButton, PrintTestButton, DisconnectionButton, SaveConnectionButton;
    private EditText MACAddressEditText, PrinterNameEditText;
    private TextView ConnectivityStatusTextView;

    private ZebraPrinter printer;
    private Connection printerConnection;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);

        ConnectivityStatusTextView = (TextView) findViewById(R.id.tvConnectivityStatus);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user =firebaseAuth.getCurrentUser();
        userID = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //BluetoothFindButton = (Button) findViewById(R.id.btnBluetoothFind);
        EstablishConnectionButton = (Button) findViewById(R.id.btnEstablishConnection);
        PrintTestButton = (Button) findViewById(R.id.btnPrintTest);
        DisconnectionButton = (Button) findViewById(R.id.btnDisconnection);
        SaveConnectionButton = (Button) findViewById(R.id.btnSaveConnection);

        MACAddressEditText = (EditText) findViewById(R.id.etMACAddress);
        PrinterNameEditText = (EditText) findViewById(R.id.etPrinterName);

        /*BluetoothFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BluetoothConnectActivity.this, BluetoothDiscovery.class);
                startActivity(intent);
            }
        });*/

        EstablishConnectionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        doConnection();
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();
            }
        });

        DisconnectionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        doDisconnection();
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();
            }
        });

        PrintTestButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        doConnectionTest();
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();
            }
        });

        SaveConnectionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (printer != null) {
                    String newEntry1 = MACAddressEditText.getText().toString();
                    String newEntry2 = PrinterNameEditText.getText().toString();
                    if ((newEntry1.length() != 0) &&
                            (newEntry2.length() != 0)) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        databaseReference.child(user.getUid()).child("MACAddress").setValue(newEntry1);
                        databaseReference.child(user.getUid()).child("PrinterFriendlyName").setValue(newEntry2);

                        //Toast.makeText(this, "Η σύνδεση αποθηκεύτηκε!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(BluetoothConnectActivity.this, DashboardActivity.class);
                        startActivity(intent);
                    }

                }

            }
        });

        if (printer != null) {
            setStatus("Connected", Color.GREEN);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (printerConnection != null && printerConnection.isConnected()) {
            //disconnect();
        }
    }

    private void enableTestButton(final boolean enabled) {
        runOnUiThread(new Runnable() {
            public void run() {
                PrintTestButton.setEnabled(enabled);
            }
        });
    }

    private boolean isBluetoothSelected() {
        return true;
    }

    public ZebraPrinter connect() {
        setStatus("Connecting...", Color.YELLOW);
        printerConnection = null;
        printerConnection = new BluetoothConnection(getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());

        try {
            printerConnection.open();
            setStatus("Connected", Color.GREEN);
        } catch (ConnectionException e) {
            setStatus("Comm Error! Disconnecting", Color.RED);
            DemoSleeper.sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus("Determining Printer Language", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus("Printer Language " + pl, Color.BLUE);
            } catch (ConnectionException e) {
                setStatus("Unknown Printer Language", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus("Unknown Printer Language", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

    public void disconnect() {
        try {
            setStatus("Disconnecting", Color.RED);
            if (printerConnection != null) {
                printerConnection.close();
            }
            setStatus("Not Connected", Color.RED);
        } catch (ConnectionException e) {
            setStatus("COMM Error! Disconnected", Color.RED);
        } finally {
            enableTestButton(true);
        }
    }

    private void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                ConnectivityStatusTextView.setBackgroundColor(color);
                ConnectivityStatusTextView.setText(statusMessage);
            }
        });
        DemoSleeper.sleep(1000);
    }

    private String getMacAddressFieldText() {
        return MACAddressEditText.getText().toString();
    }

    private void doConnectionTest() {
        if (printer != null) {
            sendTestLabel();
        }
    }

    private void doConnection() {
        printer = connect();
    }

    private void doDisconnection() {
        if (printer != null) {
            disconnect();
        }
    }

    private void sendTestLabel() {
        try {
            byte[] configLabel = getConfigLabel();
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

    /*
    * Returns the command for a test label depending on the printer control language
    * The test label is a box with the word "TEST" inside of it
    *
    * _________________________
    * |                       |
    * |                       |
    * |        TEST           |
    * |                       |
    * |                       |
    * |_______________________|
    *
    *
    */
    private byte[] getConfigLabel() {
        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;
        if (printerLanguage == PrinterLanguage.ZPL) {
            //configLabel = "^XA,^MMT,^PW561,^LL0609,^LS0,^FT35,74^AAN,27,15^FH,^FDHellenic Republic^FS,^PQ1,0,1,Y^XZ".getBytes();
            //configLabel =
            configLabel = "^XA,^BY3,^FT430,80,^BCI,80,Y,N,N,^FD00001978^FS,^FT360,320^ADI,25,14^FD123457^FS,^FT140,320^ADI,25,14^FD1245^FS,^FT300,270^ADI,40,20^FD8794465^FS,^FT300,215^ADI,40,20^FD99999 / 99999^FS,^FT430,175^ADI,25,15^FD40125 - Ελληνικά - Greek ^FS,^XZ".getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }

    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            RetrieveUserInfoFirebase RUInfo = new RetrieveUserInfoFirebase();
            RUInfo.setMACAddress(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getMACAddress());
            RUInfo.setPrinterFriendlyName(dataSnapshot.child(userID).getValue(RetrieveUserInfoFirebase.class).getPrinterFriendlyName());
            MACAddressEditText.setText(RUInfo.getMACAddress());
            PrinterNameEditText.setText(RUInfo.getPrinterFriendlyName());
        }
    }
}

