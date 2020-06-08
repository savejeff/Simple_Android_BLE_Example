package com.example.test_ble;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.example.test_ble.global.DisplayToast;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity {
    String TAG = "MAIN";

    public BLEManager BLEM;

    TextView TV_Out;
    TextView TV_Status;
    Button B_Disconnect;

    boolean UpdateUART = false;

    //Timer that recularly calles itself
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Update_UI();
                }
            });

            timerHandler.postDelayed(this, 100);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG,"onCreate()");

        checkPermission();

        TV_Out = findViewById(R.id.TV_OUT);
        TV_Status = findViewById(R.id.TV_Status);
        B_Disconnect = findViewById(R.id.B_Disconnect);

        global.getInstance().init(this);

        String YOUR_DEVICE_NAME = "BLE_TEST_SERVER";

        BLEM = global.getInstance().BLEMan;

        if(!BLEM.isBluetoothEnabled())
        {
            DisplayToast("Bluetooth disabled");
        }
        else
            BLEM.Connect(this, YOUR_DEVICE_NAME);


        //Start UI Update Timer
        timerHandler.postDelayed(timerRunnable, 0);

    }

    public void checkPermission()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
            /*
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }*/
        } else {
            // Permission has already been granted
            Log.e(TAG, "Permission granted");
        }
    }




    String output = "";
    public void Log(final String TAG, final String MSG) {
        Log(TAG + ": " + MSG + "\n");
    }

    public void Log(final String MSG) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                output += MSG;
                TV_Out.setText(output);
            }
        });
    }
	
	
    public void Update_UI()
    {
        TV_Status.setText(BLEM.isConnected()? format("CONNECTED to %s", BLEM.SERVER_NAME) : "DISCONNECTED");
        TV_Status.setBackgroundColor(ContextCompat.getColor(this, (BLEM.isConnected()? R.color.color_green : R.color.color_red)));

        B_Disconnect.setText(BLEM.isConnected()? "DISCONNECT" : "CONNECT");
        B_Disconnect.setBackgroundColor(ContextCompat.getColor(this, (BLEM.isConnected()? R.color.color_red : R.color.color_green)));

        if(UpdateUART)
            Update_UART_RX();
    }



    public void Update_UART_RX()
    {
        if(!BLEM.isConnected())
            return;
        if(BLEM.UART_Available()) {

            String Answer = "MSG received!";

            Log("UART", format("UART RX: '%s'", BLEM.UART_Read().replace("\n", "\\n")));
            Log("UART", format("UART TX: '%s'", Answer));

            BLEM.UART_Write(Answer);
        }
    }

    
    public void B_Disconnect_onClick(View v)
    {
        if(BLEM.isConnected())
            BLEM.Disconnect();
        else
            BLEM.Connect(this);
    }

    public void B_Scripting_onClick(View v)
    {
        //Set a Breakpoint here and use Android Studio to Test some stuff with the "Evaluate Expression" Function
        Log.e(TAG, "Scripting");
        /*
        if(BLEM.UART_Available()) {
            BLEM.UART_Write("MSG recieved!");
            Log("UART", BLEM.UART_Read());
        }
         */
    }
    public void B_StartUARTCheck_onClick(View v)
    {
        Log.e(TAG, "Scripting");

        UpdateUART = true;
    }

    public void B_ReadValue_onClick(View v)
    {
        Log.e(TAG, "Read Value");
        String value_string = BLEM.ReadValue_String(BLEM.CHARACTERISTIC_TEST);

        ((EditText) findViewById(R.id.ET_Value_Out)).setText("Result: '" + value_string + "'");

    }



    public void B_RQST_ReadValue_onClick(View v)
    {
        Log.e(TAG, "RQST Read Value");

        global.getInstance().BLEMan.RQST_ReadValue(global.getInstance().BLEMan.CHARACTERISTIC_TEST);
    }

    public void B_WriteValue_onClick(View v)
    {
        Log.e(TAG, "Read Value");

        String value_string = ((EditText) findViewById(R.id.ET_Value)).getText().toString();

        global.getInstance().BLEMan.WriteValue(global.getInstance().BLEMan.CHARACTERISTIC_TEST, value_string);
    }


    public void B_WriteUART_onClick(View v)
    {
        Log.e(TAG, "RQST Read Value");
        EditText ET_WriteUART =  findViewById(R.id.ET_WriteUART);
        BLEM.UART_Write(ET_WriteUART.getText().toString());
        ET_WriteUART.setText("");
    }


}