package com.example.chintan.HAUSE;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public String light_state = "";
    public String fan_state = "";
    public int person_counter;
    public String mode = "", security = "";
    public int count = 1, notification_count = 1;
    public String light_state_saved = "", fan_state_saved = "", mode_saved = "", security_saved = "";
    public int person_counter_saved = -1;
    public boolean background_flag = true;

    TextView show_person;
    private TextView tvData;
    Switch mode_secure, mode_select, light_select, fan_select;
    Button submit_person;
    EditText update_person;
    int updated_value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvData = (TextView) findViewById(R.id.tvJsonItem);  // Data fetched from data.json is shown here

        new JSONTask().execute("http://example.com/data.json");

        mode_secure = (Switch) findViewById(R.id.secure_mode);  // Secure mode switch
        mode_select = (Switch) findViewById(R.id.mode);  // Automatic/ Manual mode switch
        light_select = (Switch) findViewById(R.id.light_switch);  // Light switch
        fan_select = (Switch) findViewById(R.id.fan_switch);  // Fan switch
        update_person = (EditText) findViewById(R.id.update_person);  // Update person choice
        submit_person = (Button) findViewById(R.id.change_person_submit);  // Update person submit
        show_person = (TextView) findViewById(R.id.textView);

        Log.i("Position", "onCreate()");

		// Called when secure mode switch state is changed
        mode_secure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    new update().execute("http://example.com/insert.php?light_state="+light_state+"&fan_state="+fan_state+"&person_count="+person_counter+"&mode="+mode+"&security=ON");
                }
                else {
                    new update().execute("http://example.com/insert.php?light_state="+light_state+"&fan_state="+fan_state+"&person_count="+person_counter+"&mode="+mode+"&security=OFF");
                }
            }
        });

		// Called when mode(Manual/ Automatic) switch state is changed
        mode_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    new update().execute("http://example.com/insert.php?light_state="+light_state+"&fan_state="+fan_state+"&person_count="+person_counter+"&mode=Manual&security="+security);
                }
                else {
                    new update().execute("http://example.com/insert.php?light_state="+light_state+"&fan_state="+fan_state+"&person_count="+person_counter+"&mode=Automatic&security="+security);
                }
            }
        });

		// Called when light switch state is changed
        light_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    new update().execute("http://example.com/insert.php?light_state=ON&fan_state="+fan_state+"&person_count="+person_counter+"&mode="+mode+"&security="+security);
                }
                else {
                    new update().execute("http://example.com/insert.php?light_state=OFF&fan_state="+fan_state+"&person_count="+person_counter+"&mode="+mode+"&security="+security);
                }
            }
        });

		// Called when fan switch state is changed
        fan_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i("Fan change", "Fan changed 1");
                    fan_state = "ON";
                    new update().execute("http://example.com/insert.php?light_state=" + light_state + "&fan_state=ON&person_count=" + person_counter + "&mode=" + mode + "&security=" + security);
                } else {
                    Log.i("Fan change", "Fan changed 2");
                    fan_state = "OFF";
                    new update().execute("http://example.com/insert.php?light_state=" + light_state + "&fan_state=OFF&person_count=" + person_counter + "&mode=" + mode + "&security=" + security);
                }
            }
        });

		// Update person count button
        submit_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updated_value = Integer.parseInt(String.valueOf(update_person.getText()));
                    if (updated_value != person_counter && updated_value >= 0) {
                        update_person.setText("");
                        update_person.clearFocus();

                        Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();
                        new update().execute("http://example.com/insert.php?light_state=" + light_state + "&fan_state=" + fan_state + "&person_count=" + updated_value + "&mode=" + mode + "&security=" + security);
                    } else if (updated_value < 0) {
                        Toast.makeText(getApplicationContext(), "Value must be greater than 0", Toast.LENGTH_LONG).show();
                    } else if (updated_value == person_counter) {
                        Toast.makeText(getApplicationContext(), "Updated value is same as current value", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Enter something!!!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

	// When user makes changes, update is called
    public class update extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection connection1;
            try{
                url = new URL(params[0]);  // Prepare URL to be called. insert.php with GET parameters is called, which will make change in data.json
                connection1 = (HttpURLConnection) url.openConnection();
                connection1.setConnectTimeout(1000);
                connection1.setRequestMethod("GET");
                connection1.connect();  // URL is called from here
                int statusCode = connection1.getResponseCode(); // Will be 200 for 'OK'
                Log.i("Response", statusCode + "");
                connection1.disconnect();
            }
            catch (Exception e){
                e.printStackTrace();
            }


            Log.i("Check", "After submit");
            Log.i("Param", params[0]);
            return null;
        }
    }

	// Used to fetch data from data.json. Called after every 3 seconds
    public class JSONTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                    URL url = new URL(params[0]);  // data.json
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();  // Connect to URL

                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader((stream)));  // Data reader

                    StringBuilder buffer = new StringBuilder();  // StringBuffer to store fetched data

                    Log.i("connection", String.valueOf(connection));
                    Log.i("Params[0]", params[0]);
                    //Log.i("URL Passed", String.valueOf(buffer));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);  // Append line by line
                    }

                    String data = buffer.toString();  //Fetched data in String format

                    Log.i("data", data);

                    JSONObject parentObject = new JSONObject(data);  // Convert fetched data in json format
                    JSONArray parentArray = parentObject.getJSONArray("data");

                    JSONObject finalObject = parentArray.getJSONObject(0);  

                    light_state = finalObject.getString("light_state");  // Store fetched data in variable
                    fan_state = finalObject.getString("fan_state");
                    person_counter = finalObject.getInt("person_count");
                    mode = finalObject.getString("mode");
                    security = finalObject.getString("security");

					// Called when security mode is turned ON by user, will make notification_count = 2
                    if (count != 1 && notification_count == 1 && security.equals("ON")){
                        notification_count++;
                    }

                    if ((!fan_state_saved.equals(fan_state) || !light_state_saved.equals(light_state) || !mode_saved.equals(mode) || !security_saved.equals(security) || person_counter_saved != person_counter) && count != 1){
                        if (notification_count == 3 && person_counter > person_counter_saved){
                            notification_count++;
                            sendNotification();  // Actual time to send notification
                        }
                        if (notification_count == 2){
                            notification_count++;  // Called when security mode is turned ON by user, will make notification_count = 3
                        }

                        fan_state_saved = fan_state;
                        light_state_saved = light_state;
                        mode_saved = mode;
                        security_saved = security;
                        person_counter_saved = person_counter;
                        Log.i("Original function", "Original function");
                        count = 1;  // Make count = 1 again to change switches
                    }

                    return "Light_state: " + light_state + "\nFan_state: " + fan_state + "\nPerson: " + person_counter + "\nMode: " + mode + "\nSecurity: " + security;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            tvData.setText(result);  // Show fetched data to user
            if (background_flag) {
                refresh();  // refresh() will be called again if app is running in foreground or securiy mode is ON if running in background
            }
        }
    }

    public void refresh() {

        if (count == 1){
			// Changed switches according to modes if previous states of switches were different(count will be 1 if states are changed)
            if(security.equals("ON")){
                mode_secure.setChecked(true);
                mode_select.setVisibility(View.GONE);
                update_person.setVisibility(View.GONE);
                show_person.setVisibility(View.GONE);
                submit_person.setVisibility(View.GONE);
                light_select.setVisibility(View.VISIBLE);
                fan_select.setVisibility(View.VISIBLE);
            }
            else if (security.equals("OFF")){
                show_person.setVisibility(View.VISIBLE);
                submit_person.setVisibility(View.VISIBLE);
                update_person.setVisibility(View.VISIBLE);
                mode_secure.setChecked(false);
                mode_select.setVisibility(View.VISIBLE);
                if (mode.equals("Manual")){
                    mode_select.setChecked(true);
                    fan_select.setVisibility(View.VISIBLE);
                    light_select.setVisibility(View.VISIBLE);
                }
                else if (mode.equals("Automatic")){
                    light_select.setVisibility(View.GONE);
                    fan_select.setVisibility(View.GONE);
                    mode_select.setChecked(false);
                }
            }

            if (light_state.equals("ON")){
                light_select.setChecked(true);
            }
            else if (light_state.equals("OFF")){
                light_select.setChecked(false);
            }

            if (fan_state.equals("ON")){
                fan_select.setChecked(true);
            }
            else if (fan_state.equals("OFF")){
                fan_select.setChecked(false);
            }

            count++;
        }

        Log.i("Refresh", "In refresh");
        new JSONTask().execute("http://example.com/data.json");  // Fetch data again continuously
    }

	// Sends notification
    public void sendNotification(){
        Log.i("Notification", "In function");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification));
        mBuilder.setSmallIcon(R.drawable.notification);
        mBuilder.setContentTitle("INTRUSION DETECTED!!!");  // Notification title
        mBuilder.setContentText("Someone entered in room!!!");  //Notification message
        mBuilder.setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.notification_tone));
        mBuilder.setVibrate(new long[] {0, 2000,1000,2000,1000});
        mBuilder.setPriority(2);  // Maximum priority in notification bar
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (!security.equals("ON")){
            background_flag = false;  // When user leaves app, if security mode is ON, run app in background
        }
    }

}
