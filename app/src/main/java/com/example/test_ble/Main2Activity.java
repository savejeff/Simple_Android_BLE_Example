package com.example.test_ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class Main2Activity extends AppCompatActivity {


    String TAG = "BLE";
    String YOUR_DEVICE_NAME = "BlueJeff";

    //Bluetooth's variables
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothManager bluetoothManager;
    BluetoothScanCallback bluetoothScanCallback;
    BluetoothGatt gattClient = null;

    BluetoothGattService service = null;
    BluetoothGattCharacteristic characteristic = null;

    BluetoothGattCharacteristic characteristicID; // To get Value

    // UUID's (set yours)
    //final UUID SERVICE_UUID = UUID.fromString("ab0828b1-198e-4351-b779-901fa0e0371e");
    //final UUID CHARACTERISTIC_UUID_ID = UUID.fromString("1a220d0a-6b06-4767-8692-243153d94d85");
    //final UUID DESCRIPTOR_UUID_ID = UUID.fromString("ec6e1003-884b-4a1c-850f-1cfce9cf6567");
    //final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    //final UUID CHARACTERISTIC_UUID_ID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    final UUID CHARACTERISTIC_UUID_ID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    //final UUID DESCRIPTOR_UUID_ID = UUID.fromString("ec6e1003-884b-4a1c-850f-1cfce9cf6567");


    TextView TV_Out;


    private Handler messageHandler;

    void _runOnUiThread(Runnable action) {
        messageHandler.post(action);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.e(TAG,"onCreate()");

        TV_Out = findViewById(R.id.TV_OUT);
        messageHandler = new Handler(Looper.getMainLooper());

        // Bluetooth
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        startScan();

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

    public void B_Disconnect_onClick(View v)
    {
        gattClient.disconnect();
        //gattClient = null;
        service = null;
        characteristic = null;
    }

    public void B_Scripting_onClick(View v)
    {
        Log.e(TAG, "Scripting");
    }

    public void B_ReadValue_onClick(View v)
    {
        Log.e(TAG, "Read Value");

        byte[] value_bytes = characteristic.getValue();

        if(value_bytes == null)
            Log.e(TAG,"onServicesDiscovered() Value=null!");
        else {
            String output = new BigInteger(1, value_bytes).toString(16);
            Log.e(TAG, "onServicesDiscovered() Value=" + output);
        }

        String value_string = characteristic.getStringValue(0);
        if(value_bytes == null)
            Log.e(TAG,"onServicesDiscovered() Value=null!");
        else
            Log.e(TAG,"onServicesDiscovered() Value=\"" + value_string + "\"");


        ((EditText) findViewById(R.id.ET_Value_Out)).setText("Result: '" + value_string + "'");

    }

    public void B_WriteValue_onClick(View v)
    {
        Log.e(TAG, "Read Value");

        String value_string = ((EditText) findViewById(R.id.ET_Value)).getText().toString();

        if(value_string.length() > 0) {
            Log.e(TAG, "onServicesDiscovered() setting value");
            characteristic.setValue(value_string);

            Log.e(TAG, "onServicesDiscovered() sending characteristic");
            gattClient.writeCharacteristic(characteristic);
        }
    }

    // BLUETOOTH SCAN

    private void startScan()
    {
        Log.e(TAG,"startScan()");
        bluetoothScanCallback = new BluetoothScanCallback();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(bluetoothScanCallback);
    }

    // BLUETOOTH CONNECTION
    private void connectDevice(BluetoothDevice device) {
        Log.e(TAG,"connectDevice()");
        if (device == null)
            Log.e(TAG,"connectDevice(): Device is null");
        else {
            Log.e(TAG,"connectDevice(): connecting to Gatt");
            if(gattClient == null) {
                GattClientCallback gattClientCallback = new GattClientCallback();
                gattClient = device.connectGatt(this, false, gattClientCallback);
            }
            else
            {
                Log.e(TAG,"connectDevice(): Gatt Client already created -> Stopping");
            }
        }
    }

    // BLE Scan Callbacks
    private class BluetoothScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.e(TAG, "onScanResult()");
            if (result.getDevice().getName() != null){
                if (result.getDevice().getName().equals(YOUR_DEVICE_NAME)) {
                    Log.e(TAG, "onScanResult(): Found BLE Device");

                    // When find your device, connect.
                    connectDevice(result.getDevice());

                    Log.e(TAG, "onScanResult(): stopping scan");
                    bluetoothLeScanner.stopScan(bluetoothScanCallback); // stop scan

                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.e(TAG, "onBathScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "ErrorCode: " + errorCode);
        }
    }

    // Bluetooth GATT Client Callback
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(TAG,"onConnectionStateChange()");

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e(TAG, "onConnectionStateChange(): GATT FAILURE");
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onConnectionStateChange(): status != GATT_SUCCESS");
                return;
            }

            Log.e(TAG, "onConnectionStateChange(): status == GATT_SUCCESS");
            Log.e(TAG, "onConnectionStateChange(): New State: " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e(TAG, "onConnectionStateChange CONNECTED");


                //Here connection to Gatt was successful
                Log("BluetoothGattCallback", "CONNECTED");

                Log.e(TAG, "onConnectionStateChange(): start discover Services");
                gatt.discoverServices();


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "onConnectionStateChange DISCONNECTED");

                Log("BluetoothGattCallback", "DISCONNECTED");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(TAG,"onServicesDiscovered()");
            if (status != BluetoothGatt.GATT_SUCCESS)
            {
                Log.e(TAG,"onServicesDiscovered() status != BluetoothGatt.GATT_SUCCESS");
                return;
            }


            Log.e(TAG,"onServicesDiscovered() status == BluetoothGatt.GATT_SUCCESS");

            // Reference your UUIDs
            //characteristicID = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID_ID);
            //gatt.setCharacteristicNotification(characteristicID,true);

            if (service != null || characteristic != null)
            {
                Log.e(TAG,"onServicesDiscovered(): service or characteristic already init -> Stopping");
                return;
            }

            Log.e(TAG,"onServicesDiscovered() getting service");
            service = gatt.getService(SERVICE_UUID);


            Log.e(TAG,"onServicesDiscovered() getting characteristic");
            characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_ID);

            Log.e(TAG,"onServicesDiscovered() requesting read of characteristic");
            gatt.readCharacteristic(characteristic);

            Log.e(TAG,"onServicesDiscovered() enable Notification on characteristic");
            gatt.setCharacteristicNotification(characteristic, true);

            Log.e(TAG, "onServicesDiscovered(): Permissions=" + characteristic.getPermissions());

            //PROPERTY DEFINTION
            /*
             //Characteristic proprty: Characteristic is broadcastable.
            public static final int PROPERTY_BROADCAST = 0x01;

             // Characteristic property: Characteristic is readable.
            public static final int PROPERTY_READ = 0x02;

             // Characteristic property: Characteristic can be written without response.
            public static final int PROPERTY_WRITE_NO_RESPONSE = 0x04;

             // Characteristic property: Characteristic can be written.
            public static final int PROPERTY_WRITE = 0x08;

             // Characteristic property: Characteristic supports notification
            public static final int PROPERTY_NOTIFY = 0x10;

             // Characteristic property: Characteristic supports indication
            public static final int PROPERTY_INDICATE = 0x20;

             // Characteristic property: Characteristic supports write with signature
            public static final int PROPERTY_SIGNED_WRITE = 0x40;

             // Characteristic property: Characteristic has extended properties
            public static final int PROPERTY_EXTENDED_PROPS = 0x80;
            */


            int prop = characteristic.getProperties();
            boolean isNOTIFY = (BluetoothGattCharacteristic.PROPERTY_NOTIFY & prop) > 0;

            Log.e(TAG, "onServicesDiscovered(): Properties=" + characteristic.getProperties());


            for(BluetoothGattDescriptor descriptor: characteristic.getDescriptors())
            {
                Log.e(TAG,"onServicesDiscovered(): got descriptor UUID=" + descriptor.getUuid());
            }

        }

        @Override
        /*Callback reporting the result of a characteristic read operation.*/
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(TAG,"onCharacteristicRead()");

            Log.e(TAG,"onServicesDiscovered() reading Value");
            byte[] value_bytes = characteristic.getValue();

            //read as bytes
            if(value_bytes == null)
                Log.e(TAG,"onServicesDiscovered() Value=null!");
            else {
                String output = new BigInteger(1, value_bytes).toString(16);
                Log.e(TAG, "onServicesDiscovered() Value=" + output);
            }


            //read as String
            String value_string = characteristic.getStringValue(0);
            if(value_string == null)
                Log.e(TAG,"onServicesDiscovered() Value=null!");
            else
                Log.e(TAG,"onServicesDiscovered() Value=\"" + value_string + "\"");

            if(value_string != null) {
                Log.e(TAG, "onServicesDiscovered() setting value");
                characteristic.setValue("Hallo this is GalaxyJeff");

                Log.e(TAG, "onServicesDiscovered() sending characteristic");
                gatt.writeCharacteristic(characteristic);
            }

            //BluetoothGattDescriptor descriptor = characteristicID.getDescriptor(DESCRIPTOR_UUID_ID);
            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            //gatt.writeDescriptor(descriptor);
        }

        @Override
        /*Callback indicating the result of a characteristic write operation.*/
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG,"onCharacteristicWrite() with Status=" + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG,"onCharacteristicChanged()");
            // Here you can read the characteristc's value


            String value_string = characteristic.getStringValue(0);
            if(value_string == null)
                Log.e(TAG,"onCharacteristicChanged() Value=null!");
            else {
                Log("onCharacteristicChanged", value_string);
                Log.e(TAG, "onCharacteristicChanged() Value=\"" + value_string + "\"");
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.e(TAG,"onDescriptorRead()");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e(TAG,"onDescriptorWrite()");
        }
    }
}
