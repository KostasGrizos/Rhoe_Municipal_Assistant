package com.example.android.rhoe_app_1;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by kosti on 9/05/2018.
 */

public class Rhoe_Municipal_Assistant extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
