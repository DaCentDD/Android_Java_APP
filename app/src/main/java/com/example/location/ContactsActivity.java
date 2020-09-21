package com.example.location;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ContactsActivity extends AppCompatActivity {
    Button But;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        But = (Button) findViewById(R.id.button);
    }

    public void but_click(View v) throws IOException, JSONException {
        int permissionStatus = ContextCompat.checkSelfPermission(ContactsActivity.this, Manifest.permission.READ_CONTACTS); // Check permission
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            getContacts(this); // Get contact after pressing
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // Get permission
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    try {
                        getContacts(this); // Get contact after pressing
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // permission denied
                }
                return;
        }
    }

    private void doPostRequest(JSONObject jo) throws JSONException, IOException { // Send POST request to server
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jo.toString());
        Request req = new Request.Builder()
                .url("http://192.168.10.31:5000/")
                .post(body)
                .build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }


    public void getContacts(Context c) throws JSONException, IOException { // Get contact data in JSON
        JSONObject jo = new JSONObject();
        Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            JSONArray ja = new JSONArray();
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)); // Get name
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); // Get number
            String email = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)); // Get email
            ja.put(phoneNumber);
            jo.put(name, ja);
        }
        doPostRequest(jo);
    }

    // Change activity
    public void Click_wea(android.view.View v) {
        Intent intent=new Intent(ContactsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void Click_bat(android.view.View view) {
        Intent intent=new Intent(ContactsActivity.this, AuthActivity.class);
        startActivity(intent);
    }
}