package com.example.android.rhoe_app_1.FirebaseFine;

/**
 * Created by kosti on 26/07/2018.
 */

public class SmokingFineBasicInfoFirebase {
    public String Category, Name, Address, Tfn, Doy, Id, Date, Day, Time, FineType, FineAmount, UserID, Paid;
    public double Lat, Lon;

    public SmokingFineBasicInfoFirebase(){

    }

    public SmokingFineBasicInfoFirebase(String category, String name, String address, String tfn, String doy, String id, String date, String day, String time, String fineType, String fineAmount, String userID, String paid, double lat, double lon) {
        Category = category;
        Name = name;
        Address = address;
        Tfn = tfn;
        Doy = doy;
        Id = id;
        Date = date;
        Day = day;
        Time = time;
        FineType = fineType;
        FineAmount = fineAmount;
        UserID = userID;
        Paid = paid;
        Lat = lat;
        Lon = lon;
    }
}
