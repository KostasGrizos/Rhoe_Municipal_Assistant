package com.example.android.rhoe_app_1.FirebaseFine;

/**
 * Created by User on 7/03/2018.
 */

public class FineBasicInfoFirebase {

    public String CarPlate, CarType, CarBrand, CarColor, Date, FineAmount, Day, Time, Address, FineType, UserID, CarCountry, FinePoints, Paid;
    public double Lat, Lon;

    public FineBasicInfoFirebase(){

    }

    public FineBasicInfoFirebase(String carPlate, String carType, String carBrand, String carColor, String date, String fineAmount, String day, String time, String address, String fineType, String userID, String carCountry, String finePoints, String paid, double lat, double lon) {
        CarPlate = carPlate;
        CarType = carType;
        CarBrand = carBrand;
        CarColor = carColor;
        Date = date;
        FineAmount = fineAmount;
        Day = day;
        Time = time;
        Address = address;
        FineType = fineType;
        UserID = userID;
        CarCountry = carCountry;
        FinePoints = finePoints;
        Paid = paid;
        Lat = lat;
        Lon = lon;
    }
}
