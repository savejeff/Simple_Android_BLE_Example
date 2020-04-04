package com.example.test_ble;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class global {
    private static final global ourInstance = new global();

    public static global getInstance() {
        return ourInstance;
    }

    public Context context;

    public BLEManager bleManager;


    void init(Activity activity)
    {
        this.context = activity;

        bleManager = new BLEManager(activity);
    }


    public String DEBUG_LOG = "";
    public static void  Log(final String TAG, final String MSG) {
        Log.i(TAG, MSG);
        Log(TAG + ": " + MSG + "\n");
    }

    public static void Log(final String MSG) {
        global.getInstance().DEBUG_LOG += MSG;
    }
}