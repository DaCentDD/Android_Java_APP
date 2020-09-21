package com.example.location;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HistoryActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView current_email, id, mah, del;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        current_email = (TextView)findViewById(R.id.current_email);
        id = (TextView)findViewById(R.id.id);
        mah = (TextView)findViewById(R.id.mah);
        del = (TextView)findViewById(R.id.del);
        if (!user.isAnonymous()){
            current_email.setText(user.getEmail());
        } else{
            current_email.setText("Anonymous");
        }
        // Read database table of user
        Cursor c = BatteryActivity.db.query(BatteryActivity.table_name, null, null, null, null, null, null);
        if(c!=null&&c. moveToFirst()){
            do{
                String _id = c.getString(c.getColumnIndexOrThrow (DBHelper.KEY_ID));
                String average  = c.getString(c.getColumnIndexOrThrow (DBHelper.KEY_SUM));
                String deleted  = c.getString(c.getColumnIndexOrThrow (DBHelper.KEY_DEL));
                id.append("\n" + _id);
                mah.append("\n" + average);
                if (deleted == null){
                    del.append("\n");
                } else {
                    del.append("\n" + deleted);
                }
            }while(c.moveToNext());
        }
    }

    // Change activity
    public void Click_back(View view) {
        Intent intent=new Intent(HistoryActivity.this, AuthActivity.class);
        startActivity(intent);
    }
    // Clean table
    public void Click_clean(View view) {
        BatteryActivity.db.execSQL("DELETE FROM " + BatteryActivity.table_name);
        BatteryActivity.db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + BatteryActivity.table_name + "'");
        Intent intent=new Intent(HistoryActivity.this, AuthActivity.class);
        startActivity(intent);
    }
}
