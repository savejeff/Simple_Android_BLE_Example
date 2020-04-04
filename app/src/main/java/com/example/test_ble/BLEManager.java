package com.example.test_ble;

import android.app.Activity;
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
import android.util.Log;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;


public class BLEManager {


    String TAG = "BLEManager";
    //String YOUR_DEVICE_NAME = "BlueJeff";
    String SERVER_NAME = "UART Service";



    //Test_ESP_BLE_server
    final UUID SERVICE_UUID_TEST = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    final UUID CHARACTERISTIC_UUID_TEST = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    //Test_ESP_BLE_uart
    final UUID SERVICE_UUID_UART = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    final UUID CHARACTERISTIC_UUID_UART_TX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"); //TX
    final UUID CHARACTERISTIC_UUID_UART_RX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); //RX
    final UUID DESCRIPTOR_UUID_ID_TX_UART = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");



    //Bluetooth's variables
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothManager bluetoothManager;
    BluetoothScanCallback bluetoothScanCallback;


    BluetoothGatt GattClient = null;

    BluetoothGattService Service_UART = null;
    BluetoothGattService Service_Test = null;

    BluetoothGattCharacteristic CHARACTERISTIC_UART_TX = null;
    BluetoothGattCharacteristic CHARACTERISTIC_UART_RX = null;

    BluetoothGattCharacteristic CHARACTERISTIC_TEST = null;


    String UART_INPUT_BUFFER = "";

    public BLEManager(Activity activity)
    {
        /*
        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();



        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
*/


    }

    public void Connect(Activity activity)
    {
        Log.e(TAG,"Connect() to " + SERVER_NAME);

        // Bluetooth
        bluetoothManager = (BluetoothManager) activity.getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        startScan();
    }

    public void Connect(Activity activity, String ServerName)
    {
        SERVER_NAME = ServerName;
        Connect(activity);
    }


    public void Disconnect()
    {
        GattClient.disconnect();
        //gattClient = null; -> done by GattClientCallback
        Service_UART = null;
    }

    public boolean isConnected()
    {
        return GattClient != null;
    }

    public String ReadValue_String(BluetoothGattCharacteristic characteristic)
    {
        Log.e(TAG, "Read Value");

        String value_string = characteristic.getStringValue(0);
        if(value_string == null)
            Log.e(TAG,"onServicesDiscovered() Value=null!");
        else
            Log.e(TAG,"onServicesDiscovered() Value=\"" + value_string + "\"");

        return value_string;
    }

    public byte[] ReadValue_ByteArray(BluetoothGattCharacteristic characteristic)
    {
        Log.e(TAG, "Read Value");

        byte[] value_bytes = characteristic.getValue();

        if(value_bytes == null)
            Log.e(TAG,"onServicesDiscovered() Value=null!");
        else {
            String output = new BigInteger(1, value_bytes).toString(16);
            Log.e(TAG, "onServicesDiscovered() Value=" + output);
        }
        return value_bytes;
    }



    public void RQST_ReadValue(BluetoothGattCharacteristic characteristic)
    {
        Log.e(TAG, "RQST Read Value");

        GattClient.readCharacteristic(characteristic);
    }

    public void WriteValue(BluetoothGattCharacteristic characteristic, String value_string)
    {
        Log.e(TAG, "Read Value");

        if(value_string.length() > 0) {
            Log.e(TAG, "onServicesDiscovered() setting value");
            characteristic.setValue(value_string);

            Log.e(TAG, "onServicesDiscovered() sending characteristic");
            GattClient.writeCharacteristic(characteristic);
        }
    }


    public void UART_Write(String msg)
    {
        WriteValue(CHARACTERISTIC_UART_RX, msg);
    }

    public boolean UART_Available()
    {
        return UART_INPUT_BUFFER.length() > 0;
    }

    public String UART_Read()
    {
        String tmp = UART_INPUT_BUFFER;
        UART_INPUT_BUFFER = "";
        return tmp;
    }



    private void InitCharacteristics()
    {
        InitCharacteristics_Service_UART();
        InitCharacteristics_Service_Test();
    }

    private void InitCharacteristics_Service_Test()
    {
        Log.e(TAG,"onServicesDiscovered() getting service Test");
        Service_Test = GattClient.getService(SERVICE_UUID_TEST);

        if(Service_Test == null)
        {
            Log.e(TAG,"onServicesDiscovered() Service_Test not found");
            return;
        }

        Log.e(TAG,"onServicesDiscovered() getting characteristic Test");
        CHARACTERISTIC_TEST = Service_Test.getCharacteristic(CHARACTERISTIC_UUID_TEST);

        Log.e(TAG,"onServicesDiscovered() enable Notification on characteristic Test");
        GattClient.setCharacteristicNotification(CHARACTERISTIC_TEST,true);
    }


    private void InitCharacteristics_Service_UART()
    {
        Log.e(TAG,"onServicesDiscovered() getting service UART");
        Service_UART = GattClient.getService(SERVICE_UUID_UART);

        if(Service_UART == null)
        {
            Log.e(TAG,"onServicesDiscovered() Service_UART not found");
            return;
        }

        Log.e(TAG,"onServicesDiscovered() getting characteristic UART");
        CHARACTERISTIC_UART_TX = Service_UART.getCharacteristic(CHARACTERISTIC_UUID_UART_TX);
        CHARACTERISTIC_UART_RX = Service_UART.getCharacteristic(CHARACTERISTIC_UUID_UART_RX);

        Log.e(TAG,"onServicesDiscovered() enable Notification on characteristic UART");
        GattClient.setCharacteristicNotification(CHARACTERISTIC_UART_TX,true);

        //Enable Notification for UART TX
        BluetoothGattDescriptor _descriptor = CHARACTERISTIC_UART_TX.getDescriptor(DESCRIPTOR_UUID_ID_TX_UART);
        if(_descriptor !=null)
        {
            Log.e(TAG, "onServicesDiscovered() Write to Descriptor ENABLE_NOTIFICATION_VALUE");
            _descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            GattClient.writeDescriptor(_descriptor);
        }
        else
            Log.e(TAG, "onServicesDiscovered() descriptor == null");

    }


    public void printCharacteristic(BluetoothGattCharacteristic characteristic)
    {

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

        Log.e(TAG,"onServicesDiscovered(): Properties="+characteristic.getProperties());

        for(BluetoothGattDescriptor descriptor:characteristic.getDescriptors())
        {
            Log.e(TAG, "onServicesDiscovered(): got descriptor UUID=" + descriptor.getUuid());
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
            if(GattClient == null) {
                GattClientCallback gattClientCallback = new GattClientCallback();
                GattClient = device.connectGatt(global.getInstance().context, false, gattClientCallback);
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
                if (result.getDevice().getName().equals(SERVER_NAME)) {
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
            Log.e(TAG, "onConnectionStateChange()");

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

                global.Log("BluetoothGattCallback", "CONNECTED");

                Log.e(TAG, "onConnectionStateChange(): start discover Services");
                gatt.discoverServices();


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "onConnectionStateChange DISCONNECTED");

                global.Log("BluetoothGattCallback", "DISCONNECTED");
                GattClient = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(TAG, "onServicesDiscovered()");
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onServicesDiscovered() status != BluetoothGatt.GATT_SUCCESS");
                return;
            }


            Log.e(TAG, "onServicesDiscovered() status == BluetoothGatt.GATT_SUCCESS");

            InitCharacteristics();

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

            //write to characteristic
            /*
            if(value_string != null) {
                Log.e(TAG, "onServicesDiscovered() setting value");
                characteristic.setValue("Hallo this is GalaxyJeff");

                Log.e(TAG, "onServicesDiscovered() sending characteristic");
                gatt.writeCharacteristic(characteristic);
            }
             */

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
                global.Log("onCharacteristicChanged", value_string);
                Log.e(TAG, "onCharacteristicChanged() Value=\"" + value_string + "\"");
            }


            if(characteristic.equals(CHARACTERISTIC_UART_TX))
            {
                UART_INPUT_BUFFER += characteristic.getStringValue(0);
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
