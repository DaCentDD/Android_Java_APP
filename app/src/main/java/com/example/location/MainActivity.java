package com.example.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.Math;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {
    double x, y, z;
    TextView View, Weather, Temp, Provider, Country, City, Weather_icon;
    Timer timer;
    LocationManager lm;
    boolean gps_enabled = false, network_enabled = false;
    Location gps_loc_check = null, net_loc_check = null;

    @Override
    public void onCreate(Bundle savedInstanceState) { // Application start
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); //Check if GPS is available
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER); // Check if Network is available
        View = (TextView) findViewById(R.id.View);
        Weather = (TextView) findViewById(R.id.Weather);
        Temp = (TextView) findViewById(R.id.Temp);
        Country = (TextView) findViewById(R.id.Country);
        City = (TextView) findViewById(R.id.City);
        Provider = (TextView) findViewById(R.id.Provider);
        Weather_icon = (TextView) findViewById((R.id.weather_icon));
        Typeface weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        Weather_icon.setTypeface(weatherFont);

        if (!gps_enabled && !network_enabled) {
            View.setText("Nothing is enabled");
        }
        // Check permission
        int permissionStatus = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, locationListenerGps); //Start GPS; check each 2 metres or 2 minutes
        if (network_enabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 2, locationListenerNetwork); //Start Network; check each 2 metres or 2 minutes
        timer = new Timer();
        timer.schedule(new GetLastLocation(), 20000); // If GPS and Network don't start too long - activate
    }


    class GetTask extends AsyncTask<String, Void, JSONObject> { // Sending GET-request in AsyncTask

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                URL url = new URL(params[2]);
                int _timeout = Integer.parseInt(params[0]);
                String _encoding = params[1];
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                connect.setRequestMethod("GET");
                connect.setRequestProperty("Content-Type", "application/json");
                connect.setRequestProperty("Content-length", "0");
                connect.setUseCaches(false);
                connect.setAllowUserInteraction(false);
                connect.setConnectTimeout(_timeout);
                connect.setReadTimeout(_timeout);
                connect.connect();
                int status = connect.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                connect.getInputStream(), _encoding));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        return new JSONObject(sb.toString());
                }
            } catch (MalformedURLException ex) {
            } catch (IOException ex) {
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) { // JSON parsing
            super.onPostExecute(result);
            int id = 0;
            String icon = "";
            try {
                JSONArray WeatherArray = (JSONArray) result.getJSONArray("weather");
                for (int i = 0; i < WeatherArray.length(); i++) {
                    JSONObject j = WeatherArray.getJSONObject(i);
                    Weather.setText(j.get("main").toString());
                    id = j.getInt("id");
                    if (id != 800) {
                        id = id / 100;
                    }
                }
                JSONObject TempArray = (JSONObject) result.getJSONObject("main");
                JSONObject LocArray = (JSONObject) result.getJSONObject("sys");
                long Cel = Math.round(TempArray.getInt("temp") - 273.15);
                Temp.setText(Cel + " ℃");
                Country.setText(LocArray.get("country").toString());
                City.setText(result.get("name").toString());
                switch (id) {
                    case 800:
                        icon = getString(R.string.weather_sunny);
                        break;
                    case 2:
                        icon = getString(R.string.weather_thunder);
                        break;
                    case 3:
                        icon = getString(R.string.weather_drizzle);
                        break;
                    case 7:
                        icon = getString(R.string.weather_foggy);
                        break;
                    case 8:
                        icon = getString(R.string.weather_cloudy);
                        break;
                    case 6:
                        icon = getString(R.string.weather_snowy);
                        break;
                    case 5:
                        icon = getString(R.string.weather_rainy);
                        break;
                }
                Weather_icon.setText(icon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    LocationListener locationListenerGps = new LocationListener() { // GPS Listener
        public void onLocationChanged(Location location) {
            timer.cancel();
            GetTask GT = new GetTask();
            gps_loc_check = location;
            if (net_loc_check != null) // If network also has location - check accuracy
                {
                    if (gps_loc_check.getAccuracy() > net_loc_check.getAccuracy()) {
                        x = location.getLatitude();
                        y = location.getLongitude();
                        z = location.getAltitude();
                        Provider.setText("GPS & NET");
                        View.setText("x: " + Math.round(x * 100.0) / 100.0 + "°\ny: " + Math.round(y * 100.0) / 100.0 + "°\nz: " + Math.round(z * 100.0) / 100.0 + "°");
                        GT.execute("20000", "UTF-8", "http://api.openweathermap.org/data/2.5/weather?lat=" + x + "&lon=" + y + "&appid=3ebec33dd0cff1ea723e69398e97cf44");
                    }
                } else
                    {
                        x = location.getLatitude();
                        y = location.getLongitude();
                        z = location.getAltitude();
                        Provider.setText("Only GPS");
                        View.setText("x: " + Math.round(x * 100.0) / 100.0 + "°\ny: " + Math.round(y * 100.0) / 100.0 + "°\nz: " + Math.round(z * 100.0) / 100.0 + "°");
                        GT.execute("20000", "UTF-8", "http://api.openweathermap.org/data/2.5/weather?lat=" + x + "&lon=" + y + "&appid=3ebec33dd0cff1ea723e69398e97cf44");
            }
        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener()
        { // Network Listener
            public void onLocationChanged(Location location) {
                GetTask GT = new GetTask();
                timer.cancel();
                net_loc_check = location;
                if (gps_loc_check != null) // If GPS also has location - check accuracy
                {
                    if (gps_loc_check.getAccuracy() <= net_loc_check.getAccuracy()) {
                        x = location.getLatitude();
                        y = location.getLongitude();
                        z = location.getAltitude();
                        Provider.setText("NET & GPS");
                        View.setText("x: " + Math.round(x * 100.0) / 100.0 + "°\ny: " + Math.round(y * 100.0) / 100.0 + "°\nz: " + Math.round(z * 100.0) / 100.0 + "°");
                        GT.execute("20000", "UTF-8", "http://api.openweathermap.org/data/2.5/weather?lat=" + x + "&lon=" + y + "&appid=3ebec33dd0cff1ea723e69398e97cf44");
                    }
                } else
                    {
                        x = location.getLatitude();
                        y = location.getLongitude();
                        z = location.getAltitude();
                        Provider.setText("Only NET");
                        View.setText("x: " + Math.round(x * 100.0) / 100.0 + "°\ny: " + Math.round(y * 100.0) / 100.0 + "°\nz: " + Math.round(z * 100.0) / 100.0 + "°");
                        GT.execute("20000", "UTF-8", "http://api.openweathermap.org/data/2.5/weather?lat=" + x + "&lon=" + y + "&appid=3ebec33dd0cff1ea723e69398e97cf44");
                }
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };



    class GetLastLocation extends TimerTask
        { // Find last known location
            @Override
            public void run() {
                // Check permission
                int permissionStatus = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                Location net_loc = null, gps_loc = null;
                if (gps_enabled)
                    gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (network_enabled)
                    net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                //if there are both values use the latest one
                if (gps_loc != null && net_loc != null) {
                    if (gps_loc.getTime() > net_loc.getTime()) {
                        x = gps_loc.getLatitude();
                        y = gps_loc.getLongitude();
                        z = gps_loc.getAltitude();
                        View.setText("GPS last known location: \nx: " + x + "\ny: " + y + "\nz: " + z);
                    } else {
                        x = net_loc.getLatitude();
                        y = net_loc.getLongitude();
                        z = net_loc.getAltitude();
                        View.setText("Networks last known location: \nx: " + x + "\ny: " + y + "\nz: " + z);
                    }
                }
                if (gps_loc != null) {
                    x = gps_loc.getLatitude();
                    y = gps_loc.getLongitude();
                    z = gps_loc.getAltitude();
                    View.setText("Only GPS found. \nGPS last known location: \nx: " + x + "\ny: " + y + "\nz: " + z);
                }

                if (net_loc != null) {
                    x = net_loc.getLatitude();
                    y = net_loc.getLongitude();
                    View.setText("Only Networks found. Networks last known location: \nx: " + x + "\ny: " + y + "\nz: " + z);
                }
                View.setText("No Last Known Available");
            }
        }
        // Change Activity
    public void Click_con(android.view.View v) {
        Intent intent=new Intent(MainActivity.this,ContactsActivity.class);
        startActivity(intent);
    }

    public void Click_bat(android.view.View view) {
        Intent intent=new Intent(MainActivity.this,AuthActivity.class);
        startActivity(intent);
    }
}


