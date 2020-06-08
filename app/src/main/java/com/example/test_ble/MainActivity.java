package com.example.test_ble;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity {
    String TAG = "MAIN";

    public BLEManager BLEM;

    TextView TV_Out;

    //Timer that recularly calles itself
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            Update_UART_RX();
            timerHandler.postDelayed(this, 100);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG,"onCreate()");

        TV_Out = findViewById(R.id.TV_OUT);

        global.getInstance().init(this);

        String YOUR_DEVICE_NAME = "BLE_TEST_SERVER";

        BLEM = global.getInstance().bleManager;

        BLEM.Connect(this, YOUR_DEVICE_NAME);

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
        BLEM.Disconnect();
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
        timerHandler.postDelayed(timerRunnable, 0);
        /*
        if(BLEM.UART_Available()) {
            BLEM.UART_Write("MSG recieved!");
            Log("UART", BLEM.UART_Read());
        }
         */
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

        global.getInstance().bleManager.RQST_ReadValue(global.getInstance().bleManager.CHARACTERISTIC_TEST);
    }

    public void B_WriteValue_onClick(View v)
    {
        Log.e(TAG, "Read Value");

        String value_string = ((EditText) findViewById(R.id.ET_Value)).getText().toString();

        global.getInstance().bleManager.WriteValue(global.getInstance().bleManager.CHARACTERISTIC_TEST, value_string);
    }

}