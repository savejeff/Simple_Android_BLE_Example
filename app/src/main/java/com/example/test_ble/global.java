package com.example.test_ble;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import static java.lang.String.format;

public class global {
    private static final global ourInstance = new global();

    public static global getInstance() {
        return ourInstance;
    }

    public Context context;

    public BLEManager BLEMan;


    void init(Activity activity)
    {
        this.context = activity;

        BLEMan = new BLEManager(activity);
    }


    public String DEBUG_LOG = "";
    public static void  Log(final String TAG, final String MSG) {
        Log.i(TAG, MSG);
        Log(TAG + ": " + MSG + "\n");
    }


    /**
     * return String of R.String Res
     */
    public static String getR_String(int id) {
        return global.getInstance().context.getResources().getString(id);
    }



    public static void DisplayToast(String Text) {
        try {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                Toast.makeText(global.getInstance().context, Text, Toast.LENGTH_LONG).show();
            }
            else
            {
                Log.e("global", format("Toast could not be displayed - not on UI Thread\n'%s'", Text));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void Log(final String MSG) {
        global.getInstance().DEBUG_LOG += MSG;
    }
}