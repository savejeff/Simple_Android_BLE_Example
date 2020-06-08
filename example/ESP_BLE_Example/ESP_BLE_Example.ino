/*
    
Android Test Server

Creating 2 Services
    1.) Test Service with one Characteristic for Read, Write and Notify
        if a text like 'HalloWorld#' is written over Serial it will be set to the Test Characteristic.
        Note that you need to end you Serial input with a Hash (#) Symbol
        The current value of the Test Characteristic is periodically printed to Serial

    2.) UART Service with RX and TX to read and write Text
        if a device is connected, the UART Service will send periodically its loopcount as text to client.
        if a client device writes to UART RX it will be printed to Serial
   
*/
#include <String.h>

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>



#define SerialOut Serial

#define LOOPTIME_UART 2000
#define LOOPTIME_TEST 5000


#define SERVER_NAME "BLE_TEST_SERVER"

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID_UART               "6E400001-B5A3-F393-E0A9-E50E24DCCA9E" // UART service UUID
#define CHARACTERISTIC_UUID_UART_RX     "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_UART_TX     "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"


#define SERVICE_UUID_TEST               "4fafc201-1fb5-459e-8fcc-c5c9c331914b" // TEST service UUID
#define CHARACTERISTIC_UUID_TEST_RWN    "beb5483e-36e1-4688-b7f5-ea07361b26a8" // Test for Read Write and Notify


//BLE Objects
BLEServer *pServer = NULL;

BLEService* pService_UART;
BLEService* pService_TEST;

BLECharacteristic * pCharacteristic_UART_TX;
BLECharacteristic* pCharacteristic_UART_RX;
BLECharacteristic* pCharacteristic_TEST_RWN;


bool deviceConnected = false;
bool oldDeviceConnected = false;
String TxBuffer = "";


bool isAlpha(char c)
{
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
}


bool isNumber(char c)
{
    return (c >= '0' && c <= '9');
}

bool isSymbol(char c)
{
    return (c >= '!' && c <= '/') || (c >= ':' && c <= '@') || (c >= '[' && c < 'a') || (c >= '{' && c <= '~');
}

bool isPrintable(char c)
{
    return (c >= ' ' || c <= '~');
}


//Callback for Server Connection changes
class MyServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        SerialOut.println("Device Connected");
        deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
        SerialOut.println("Device Disconnected");
        deviceConnected = false;
    }
};

//Callback for incomming RX MSGs
class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string rxValue = pCharacteristic->getValue();
      String rxValue_String = String(rxValue.c_str());

      if (rxValue.length() > 0) {
        SerialOut.println("*****RX*****");
        SerialOut.println(rxValue_String);
        SerialOut.println("************");
      }
    }
};



void setup_Serial()
{
    SerialOut.begin(115200);

    delay(2000);

    SerialOut.println("Android Test Server");
    SerialOut.println("Creating 2 Services");
    SerialOut.println("\t1.) Test Service with one Characteristic for Read, Write and Notify");
    SerialOut.println("\t\tif a text like 'HalloWorld#' is written over Serial it will be set to the Test Characteristic.");
    SerialOut.println("\t\tNote that you need to end you Serial input with a Hash (#) Symbol");
    SerialOut.println("\t\tThe current value of the Test Characteristic is periodically printed to Serial");
    SerialOut.println("");

    SerialOut.println("\t2.) UART Service with RX and TX to read and write Text");
    SerialOut.println("\t\tif a device is connected, the UART Service will send periodically its loopcount as text to client.");
    SerialOut.println("\t\tif a client device writes to UART RX it will be printed to Serial");
    SerialOut.println("");
}

void setup_BLE_Server()
{

    // Create the BLE Device
    BLEDevice::init(SERVER_NAME);

    // Create the BLE Server
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());
}

void setup_Service_UART()
{

    // Create the BLE Service
    pService_UART = pServer->createService(SERVICE_UUID_UART);

    // Create a BLE Characteristic
    pCharacteristic_UART_TX = pService_UART->createCharacteristic(
        CHARACTERISTIC_UUID_UART_TX,
        BLECharacteristic::PROPERTY_NOTIFY
        );

    pCharacteristic_UART_TX->addDescriptor(new BLE2902());

    pCharacteristic_UART_RX = pService_UART->createCharacteristic(
        CHARACTERISTIC_UUID_UART_RX,
        BLECharacteristic::PROPERTY_WRITE
        );

    pCharacteristic_UART_RX->setCallbacks(new MyCallbacks());

    // Start the service
    pService_UART->start();

}


void setup_Service_Test()
{

    // Create the BLE Service
    pService_TEST = pServer->createService(SERVICE_UUID_TEST);

    // Create a BLE Characteristic
    pCharacteristic_TEST_RWN = pService_TEST->createCharacteristic(
        CHARACTERISTIC_UUID_TEST_RWN,
        BLECharacteristic::PROPERTY_READ |
        BLECharacteristic::PROPERTY_WRITE |
        BLECharacteristic::PROPERTY_NOTIFY
        );

    pCharacteristic_TEST_RWN->setValue("Hello World says Jeff");
    pService_TEST->start();
}

void setup_Advertising()
{
    
    //Add a service uuid to exposed list of services.
    BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
    
    pAdvertising->addServiceUUID(SERVICE_UUID_UART);
    pAdvertising->addServiceUUID(SERVICE_UUID_TEST);
    
    // Start advertising
    pServer->getAdvertising()->start();
    
    SerialOut.println("Waiting a client connection to notify...");
}



void setup() {
  

  setup_Serial();

  setup_BLE_Server();

  setup_Service_UART();

  setup_Service_Test();

  setup_Advertising();
}

//Handels Service UART
void loop_UART()
{
    static unsigned long LoopCount_UART = 0;
    //Skip if not Connected
    if (!deviceConnected)
        return;

    static ulong last_time_UART = 0;
    if (millis() - last_time_UART < LOOPTIME_UART)
        return;
    last_time_UART = millis();

    LoopCount_UART++;
    
    //Send Text to Client
    TxBuffer = String("Loop: ") + String((int)LoopCount_UART) + String('\n');
    //TxBuffer = "abcdefghijklmnopqrstuvwxyz01234567890"; //Test for Long Strings
    pCharacteristic_UART_TX->setValue(std::string(TxBuffer.c_str()));
    pCharacteristic_UART_TX->notify();
    
    
    //delay(100); // bluetooth stack will go into congestion, if too many packets are sent

}

void loop_Test()
{
    static String lastvalue = "";
    static String textinput = "";

    //Check Serial for Input like: SomeTextBla#
    
    while (SerialOut.available())
    {
        char c = SerialOut.read();

        
        if (c == '#' && textinput.length() > 0)
        {
            //Set new Value to Characteristic
            pCharacteristic_TEST_RWN->setValue(std::string(textinput.c_str()));
            lastvalue = textinput;
            
            
            SerialOut.print("TEST_RWN - Changed value to '");
            SerialOut.print(textinput);
            SerialOut.println("'");


            //notify the change
            pCharacteristic_TEST_RWN->notify();

            //clear text input
            textinput = "";
        }
        else if (isPrintable(c))
        {
            textinput += c;
        }
    }

    //Check if Input has Changed    
    String value = "";
    std::string rxValue = pCharacteristic_TEST_RWN->getValue();
    if (rxValue.length() > 0) {
        //for (int i = 0; i < rxValue.length(); i++)
        //{
            //value += rxValue[i];
        //}
        value = String(rxValue.c_str());
    }

    //if value changed -> print
    if (value != lastvalue)
    {
        lastvalue = value;

        SerialOut.print("TEST_RWN - New Value: '");
        SerialOut.print(value);
        SerialOut.println("'");

    }


    //Display current value every X seconds
    static ulong last_time_TEST = 0;
    if (millis() - last_time_TEST < LOOPTIME_TEST)
        return;
    last_time_TEST = millis();

    String CurrentValue = String(pCharacteristic_TEST_RWN->getValue().c_str());
    SerialOut.printf("TEST_RWN='%s'\n", CurrentValue.c_str());
}

//Handels Connection Status and Advertising
void loop_Server()
{
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
        delay(500); // give the bluetooth stack the chance to get things ready
        pServer->startAdvertising(); // restart advertising
        SerialOut.println("start advertising");
        oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        // do stuff here on connecting
        oldDeviceConnected = deviceConnected;
    }
}

void loop() {

    loop_UART();

    loop_Test();
    
    loop_Server();

    //delay(1000);
}
