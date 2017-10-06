#include "ESP8266WiFi.h"
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>

//============================== CONSTANTS ================================//
const int Pir_In_pin = 5;    //D1      // Inner side PIR pin at digital pin 1
const int Pir_Out_pin = 4;    //D2     // Outer side PIR pin at digital pin 2
const int Relay_Light = 13;    //D7     // Relay pin for Light at digital pin 7
const int Relay_Fan = 12;      //D6     // Relay pin for Fan at digital pin 6
const int Red_LED_pin = 15;      //D8    // Red LED at digital pin 8
const int Yellow_LED_pin = 14;   //D5      // Yellow LED at digital pin 5
const int Green_LED_pin = 0;    //D3     // Green LED at digital pin 3
const int Photores_Pin = A0;    //A0   // Photoresistor at analog pin A0

const char* ssid ="";

const char* password="";

//================================ VARIABLES ==============================//
int Pir_Out_Val;             // Stores value from Pir_Out_pin
int Pir_In_Val;              // Stores value from Pir_In_pin
int Person_count = 0;        // Count of Persons in Room
int Person_count_saved = 0;   // Stores previous value of Person_count
int Photores_Value;          // Stores value from Photoresistor ( 0 is dark )
int DIR = 0;                 // Stores Direcretion of Motion
unsigned long previousMillis = 0;
boolean intrusion_flag = false;    // Used for intrusion detection flag

String data, light_state = "OFF", fan_state = "OFF", mode_select = "Automatic", security_mode = "OFF";  // Actual states, fetched data will be stored in these variables

String light_state_saved = "OFF", fan_state_saved = "OFF", mode_select_saved = "Automatic", security_mode_saved = "OFF";  // Previous states, if not matched with actual states, postData() will be called

/**************** SETUP: RUNS ONCE ***************************************************************************************************/
void setup(){
  Serial.begin(115200);   // Initialize Serial
  
  //----( Initialize PIR and Photoresistor Pins as input )----//
  pinMode(Pir_In_pin, INPUT);           // Set Pir_In_pin - 3 pin as an input
  pinMode(Pir_Out_pin, INPUT);          // Set Pir_Out_pin - 4 pin as an input
  pinMode(Photores_Pin , INPUT);        // Set Photores_Pin - A0 pin as an input
  
  //----( Initialize Relay pins as outputs )----//
  pinMode(Relay_Light, OUTPUT);         // Set Relay_Light pin - 5 pin as an output
  pinMode(Relay_Fan, OUTPUT);           // Set Relay_Fan pin - 6 pin as an output

  //----( Initialize Relay pins as OFF )----//
  pinMode(Relay_Light, HIGH);         // Set Relay_Light pin - 5 pin as OFF
  pinMode(Relay_Fan, HIGH);           // Set Relay_Fan pin - 6 pin as OFF
  
  //---( Initialize LED Pins as outputs )---//
  pinMode(Red_LED_pin, OUTPUT);         // Set Red_LED_pin - 7 pin as output
  pinMode(Yellow_LED_pin, OUTPUT);      // Set Yellow_LED_pin - 8 as output
  pinMode(Green_LED_pin, OUTPUT);       // Set Green_LED_pin - 9 as output

  //========================= CONNECT WiFi =============================//
  digitalWrite(Yellow_LED_pin, HIGH);
  WiFi.begin(ssid, password);
  Serial.print("Connecting WiFi ");
  while(WiFi.status() != WL_CONNECTED){
     delay(500);
     Serial.print(".");
  }
  Serial.println("Connected");
  digitalWrite(Yellow_LED_pin, LOW);
  //====================================================================//
  
  data = "person_count=" + String(Person_count) + "&light_state=" + light_state + "&fan_state=" + fan_state + "&mode=" + mode_select + "&security=" + security_mode;
  Serial.println("Person Count =" + String(Person_count));
  Serial.println( "Light State=" + light_state);
  Serial.println("Fan State=" + fan_state);
  Serial.println("Mode=" + mode_select);
  Serial.println("Security=" + security_mode);
  postData();    // In beginning, postData() called once.
}


/********************** LOOP: RUNS CONSTANTLY (20 times a second) **********************************/
void loop(){
   unsigned long currentMillis = millis();
    if (currentMillis - previousMillis >= 3000){
      // After every 3 seconds, call fetchData()
      previousMillis = currentMillis;
      fetchData();
    }
  //============================= PIR COUNTER =============================//

  //------------( Read PIR values )--------------//
  Pir_In_Val = digitalRead(Pir_In_pin);
  Pir_Out_Val = digitalRead(Pir_Out_pin);                                       
    
  //---------( Set direction of motion )----------//
  if ( Pir_In_Val == 1 && Pir_Out_Val == 0 )         // One person moving OUT-wards
    DIR = -1;                                        // Direction is -1
  else if ( Pir_In_Val == 0 && Pir_Out_Val == 1 )    // One person moving IN-wards  
    DIR = 1;                                         // Direction is +1
  if ( Pir_In_Val == 1 && Pir_Out_Val == 1 )         // Motion DETECTED
  {
     Serial.print("People in room = ");
     Person_count = Person_count + DIR;            // Inc/Dec Counter
     Serial.println(Person_count);                 // Prints Counter Value in Serial Monitor
     DIR = 0;   // Reset Direction = 0

     if (security_mode == "ON" && Person_count > Person_count_saved){
        intrusion_flag = true;                         // Intrusion detected
        digitalWrite(Red_LED_pin, LOW);                // Red LED OFF, when intrusion is detected 
     }
      
     if (!intrusion_flag){
        digitalWrite(Yellow_LED_pin, HIGH);           // Yellow LED ON, if intrusion_flag is false
     }
      
     if (Person_count > 0 && !intrusion_flag){
       digitalWrite(Green_LED_pin, HIGH);       // Green LED ON, if intrusion_flag is false
     }
     else{
       digitalWrite(Green_LED_pin, LOW);       // Green LED OFF 
     }

     if (security_mode == "OFF"){  
       if (mode_select == "Automatic"){
         if (Person_count > 0)
         {
           if (analogRead(Photores_Pin) < 45){                                       
             digitalWrite(Relay_Light, LOW);     // Turn Relay Light ON
             light_state = "ON";
           }
           else{                                                          
             digitalWrite(Relay_Light, HIGH);     // Turn Relay Light OFF 
             light_state = "OFF";
           }
           
           digitalWrite(Relay_Fan, LOW);          // Turn Relay Fan ON
           fan_state = "ON";
     
         }
         else if ( Person_count == 0 )             // If count of Persons is equall 0
         {
           digitalWrite(Relay_Light, HIGH);        // Turn Relay Light OFF
           digitalWrite(Relay_Fan, HIGH);          // Relay Fan OFF
           light_state = "OFF";
           fan_state = "OFF";
         }          
       }
     } 
     data = "person_count=" + String(Person_count) + "&light_state=" + light_state + "&fan_state=" + fan_state + "&mode=" + mode_select + "&security=" + security_mode;  // data sent must be under this form //name1=value1&name2=value2.  
     postData();                                   // Post data to website
     delay(10000);                                 // Delay 10 seconds
     digitalWrite(Yellow_LED_pin, LOW);            // Yellow LED OFF
  }
  
 //============================= ON/OFF Appliances =============================//     
  if (Person_count > 0 && !intrusion_flag){
    digitalWrite(Green_LED_pin, HIGH);       // Green LED ON, if intrusion_flag is false
  }
  else{
    digitalWrite(Green_LED_pin, LOW);       // Green LED OFF 
  }
  if (security_mode == "OFF"){  
    if (mode_select == "Automatic"){
        if (Person_count > 0)
        {
          if (analogRead(Photores_Pin) < 45){                                       
            digitalWrite(Relay_Light, LOW);     // Turn Relay Light ON
            light_state = "ON";
          }
          else{                                                          
            digitalWrite(Relay_Light, HIGH);     // Turn Relay Light OFF 
            light_state = "OFF";
          }    
            digitalWrite(Relay_Fan, LOW);          // Turn Relay Fan ON
            fan_state = "ON";   
        }
        else if ( Person_count == 0 )             // If count of Persons is equall 0
        {
          digitalWrite(Relay_Light, HIGH);         // Turn Relay Light OFF
          digitalWrite(Relay_Fan, HIGH);           // Relay Fan OFF
          light_state = "OFF";
          fan_state = "OFF";
        }          
      }
      else if (mode_select == "Manual"){
        if (light_state == "ON"){
          digitalWrite(Relay_Light, LOW);         // Turn Relay Light ON
        }
        else if (light_state == "OFF"){
          digitalWrite(Relay_Light, HIGH);         // Turn Relay Light OFF
        }
  
        if (fan_state == "ON"){
           digitalWrite(Relay_Fan, LOW);           // Relay Fan ON
        }
        else if (fan_state == "OFF"){
           digitalWrite(Relay_Fan, HIGH);           // Relay Fan OFF
        }
      }
    }

    else if (security_mode == "ON"){
        if (light_state == "ON"){
          digitalWrite(Relay_Light, LOW);         // Turn Relay Light ON
        }
        else if (light_state == "OFF"){
          digitalWrite(Relay_Light, HIGH);         // Turn Relay Light OFF
        }
  
        if (fan_state == "ON"){
           digitalWrite(Relay_Fan, LOW);           // Relay Fan ON
        }
        else if (fan_state == "OFF"){
           digitalWrite(Relay_Fan, HIGH);           // Relay Fan OFF
        }
    }
    
  //======================== COMPARE Present and Prevoius STATES ============================//
  
  if (Person_count != Person_count_saved || light_state != light_state_saved || fan_state != fan_state_saved || mode_select != mode_select_saved || security_mode != security_mode_saved) {

    if (security_mode_saved == "ON" && security_mode == "OFF" && intrusion_flag){
      // Blink RED LED for 5 seconds if intrusion was detected in security_mode
      digitalWrite(Red_LED_pin, HIGH);
      delay(1000);
      digitalWrite(Red_LED_pin, LOW);
      delay(1000);
      digitalWrite(Red_LED_pin, HIGH);
      delay(1000);
      digitalWrite(Red_LED_pin, LOW);
      delay(1000);
      digitalWrite(Red_LED_pin, HIGH);
      delay(1000);
      digitalWrite(Red_LED_pin, LOW);
    }
    
    if (security_mode == "OFF"){
      intrusion_flag = false;
      digitalWrite(Red_LED_pin, LOW);
    }
    else if (!intrusion_flag){
      digitalWrite(Red_LED_pin, HIGH);         // Red LED ON when security_mode is ON
    }
    
    data = "person_count=" + String(Person_count) + "&light_state=" + light_state + "&fan_state=" + fan_state + "&mode=" + mode_select + "&security=" + security_mode;     // Prepare data
    Serial.println("Person Count =" + String(Person_count));
    Serial.println( "Light State=" + light_state);
    Serial.println("Fan State=" + fan_state);
    Serial.println("Mode=" + mode_select);
    Serial.println("Security=" + security_mode);
    
    light_state_saved = light_state;
    fan_state_saved = fan_state;
    mode_select_saved = mode_select;
    security_mode_saved = security_mode;
    Person_count_saved = Person_count;
    
    postData();   // Send data to server if states/ counter are changed
  }
    
  delay(50);      //Small delay of T = 50 milli seconds (f = 1000/50 = 20 times/second )
}

void fetchData(){
  // Fetch data from website. It is stores in data.json. Used to check user choice as user's changes from Android app will be changed in this file.
  HTTPClient http;
  http.begin("http://example.com/data.json");
  
  int httpCode = http.GET();           // Reads data from page.
  if (httpCode > 0){
    String fetched_data = http.getString();

    StaticJsonBuffer<400> jsonBuffer;      // Store data in json object
    JsonObject& root = jsonBuffer.parseObject(fetched_data);

    const char* ls = root["data"][0]["light_state"];
    const char* fs = root["data"][0]["fan_state"];
    const char* se = root["data"][0]["security"];
    const char* md = root["data"][0]["mode"];
    int pc = root["data"][0]["person_count"];
    
    light_state = ls;       // Save data in variables
    fan_state = fs;
    mode_select = md;
    security_mode = se;
    Person_count = pc;
  }
  
  http.end();
}

void postData() {
  // To post data to website. Called whenever current and previous STATES or counter are not matched
  HTTPClient http; 
  http.begin("http://example.com/insert.php?"+data);
  http.GET();
  http.end();
}

