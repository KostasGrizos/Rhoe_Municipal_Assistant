package com.example.android.rhoe_app_1.FirebaseUsers;

/**
 * Created by User on 6/03/2018.
 */

public class UserBluetoothInfoFirebase {

    public String MACAddress, PrinterFriendlyName;

    public UserBluetoothInfoFirebase(){

    }

    public UserBluetoothInfoFirebase(String MACAddress, String printerFriendlyName) {
        this.MACAddress = MACAddress;
        this.PrinterFriendlyName = printerFriendlyName;
    }
}
