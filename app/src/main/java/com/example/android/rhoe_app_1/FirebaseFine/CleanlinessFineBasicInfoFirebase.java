package com.example.android.rhoe_app_1.FirebaseFine;

/**
 * Created by kosti on 25/07/2018.
 */

public class CleanlinessFineBasicInfoFirebase {
    public String Surname, Name, FathWife, Occupation, Address, Date, Day, Time, FineType, FineAmount, UserID, Paid;
    public double Lat, Lon;

    public CleanlinessFineBasicInfoFirebase(){

    }

    public CleanlinessFineBasicInfoFirebase(String surname, String name, String fathWife, String occupation, String address, String date, String day, String time, String fineType, String fineAmount, String userID, String paid, double lat, double lon) {
        Surname = surname;
        Name = name;
        FathWife = fathWife;
        Occupation = occupation;
        Address = address;
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
