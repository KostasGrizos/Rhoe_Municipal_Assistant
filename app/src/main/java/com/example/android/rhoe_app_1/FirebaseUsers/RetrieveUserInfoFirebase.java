package com.example.android.rhoe_app_1.FirebaseUsers;

/**
 * Created by User on 8/03/2018.
 */

public class RetrieveUserInfoFirebase {
    private String Type, Fname, Lname, SignatureNum, Municipality, MID, MACAddress, PrinterFriendlyName, CatPreference;

    public RetrieveUserInfoFirebase() {

    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getFname() {
        return Fname;
    }

    public void setFname(String fname) {
        Fname = fname;
    }

    public String getLname() {
        return Lname;
    }

    public void setLname(String lname) {
        Lname = lname;
    }

    public String getSignatureNum() {
        return SignatureNum;
    }

    public void setSignatureNum(String signatureNum) {
        SignatureNum = signatureNum;
    }

    public String getMunicipality() {
        return Municipality;
    }

    public void setMunicipality(String municipality) {
        Municipality = municipality;
    }

    public String getMID() {
        return MID;
    }

    public void setMID(String MID) {
        this.MID = MID;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public String getPrinterFriendlyName() {
        return PrinterFriendlyName;
    }

    public void setPrinterFriendlyName(String printerFriendlyName) {
        PrinterFriendlyName = printerFriendlyName;
    }

    public String getCatPreference() {
        return CatPreference;
    }

    public void setCatPreference(String catPreference) {
        CatPreference = catPreference;
    }
}

