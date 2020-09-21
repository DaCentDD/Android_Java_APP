package com.example.location;


import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BatteryActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Map<String, Integer> list = new HashMap<String, Integer>(); // Map of applications
    HashSet <String> keylist = new HashSet<>(); // List of keys of map
    ArrayList<String> savekeylist = new ArrayList<String>(); //List of deleted keys of map
    ArrayList<String> arrangekeylist = new ArrayList<String>(); //Arranged list of keys of maps
    MyTask Applications; // AsyncTask for simulation
    String pre_max = ""; // Previous maximum for battery consumption
    boolean complete = false; // Check if the whole table was drawn
    public static String table_name = "anonymous"; // Table name for database
    public static SQLiteDatabase db; // Database
    TextView current_email, key, values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        DBHelper dbHelper = new DBHelper(this, "appDB", 1); // Connect database
        db = dbHelper.getWritableDatabase();
        if (!user.isAnonymous()){ // Check if user not anonymous to personalize table name
            table_name = user.getUid();
        }
        String SQL_CREATE_GUESTS_TABLE = "CREATE TABLE IF NOT EXISTS " + table_name + " ("
                + DBHelper.KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + DBHelper.KEY_SUM + " INTEGER , "
                + DBHelper.KEY_DEL + " TEXT )";
        db.execSQL(SQL_CREATE_GUESTS_TABLE); // Create personalize table
        //db.execSQL("DROP TABLE anonymous");
        list.put("Telegram", 0); keylist.add("Telegram");
        list.put("VK", 0); keylist.add("VK");
        list.put("YouTube", 0); keylist.add("YouTube");
        list.put("Instagram", 0); keylist.add("Instagram");
        list.put("Apple Music", 0); keylist.add("Apple Music"); // Add application
        current_email = (TextView)findViewById(R.id.current_email);
        key = (TextView)findViewById(R.id.keys);
        values = (TextView)findViewById(R.id.values);
        if (!user.isAnonymous()){
            current_email.setText(user.getEmail());
        } else{
            current_email.setText("Anonymous");
        }
        Applications = new MyTask(); // Start simulation in AsyncTask
        Applications.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Applications.cancel(false); // Stop AsyncTask if activity closed
    }


    class MyTask extends AsyncTask<Void, Integer, Void> { // Simulation
        String app_name = "";
        @Override
        protected Void doInBackground(Void... app) {
            boolean first = true;
            while (true) {
                if (isCancelled()){
                    break;
                }
                for (String keys :keylist) {
                    if (savekeylist.contains(keys))
                    {
                        continue;
                    } else {
                        app_name = keys;
                        Random rnd = new Random(System.currentTimeMillis());
                        publishProgress((10 + rnd.nextInt(500 - 10 + 1)));
                        if (first) {
                            try {
                                TimeUnit.SECONDS.sleep((1));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                TimeUnit.SECONDS.sleep((2 + rnd.nextInt(4 - 2 + 1)));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } first = false; complete = true;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { // Connect with main thread
            super.onProgressUpdate(values);
            list.put(app_name, values[0]); // Put new value
            check_top(); // Start refresh table
        }
    }


    public void check_top() { // Refresh list of applications
        list = sortByValue(list);
        int sum = 0;
        boolean first = true;
        for (String key : list.keySet()) {
            arrangekeylist.add(key);
        }
        for (int value : list.values()) {
            sum = sum + value;
        };
        db.execSQL("INSERT INTO " + table_name + "( " + DBHelper.KEY_SUM + " ) VALUES ( " + sum + ")"); // Put sum of consumption into database
        key.setText(""); values.setText("");
        if (complete) {
            for (String keys : arrangekeylist) {
                if (savekeylist.contains(keys)) {
                    continue;
                } else {
                    if (first & (pre_max == keys)) { // If the application now has maximum consumption and it had maximum on previous iteration
                        stop_app(keys); // Stop application
                        continue;
                    } else if (first) {
                        pre_max = (keys);
                    }
                    key.append(keys + "\n\n\n");
                    values.append(list.get(keys) + " mAh\n\n\n");
                    first = false;
                }
            }
        } else {
            for (Map.Entry entry : list.entrySet()) {
                key.append(entry.getKey() + "\n\n\n");
                values.append(entry.getValue() + " mAh\n\n\n");
            }
        }
        arrangekeylist.clear();
    }


    public void stop_app(String key) { // Stop application
        list.remove(key);
        db.execSQL("UPDATE " + table_name + " SET " + DBHelper.KEY_DEL + " = \' " + key + "\' WHERE " + DBHelper.KEY_ID + " = ( SELECT MAX ( " + DBHelper.KEY_ID + " ) FROM "  +  table_name + ") ");
        savekeylist.add(key);
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);
        ses.schedule(new Remove(), 7, TimeUnit.SECONDS); // Return application after x seconds
    }


    class Remove implements Runnable { // Return application
        @Override
        public void run() {
            list.put(savekeylist.get(0), 0);
            savekeylist.remove(0);
        }
    }


    public static <K, V extends Comparable <?super V>> Map<K, V> // List sorting
    sortByValue(Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort( list,  new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );
        Collections.reverse(list);
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // Change activity
    public void Click_con(android.view.View v) {
        Intent intent=new Intent(BatteryActivity.this, ContactsActivity.class);
        startActivity(intent);
    }

    public void Click_wea(android.view.View view) {
        Intent intent=new Intent(BatteryActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void Click_logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent=new Intent(BatteryActivity.this, AuthActivity.class);
        startActivity(intent);
    }
    public void Click_his(View view) {
        Intent intent=new Intent(BatteryActivity.this, HistoryActivity.class);
        startActivity(intent);
    }
}