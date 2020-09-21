package com.example.location;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText email, pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        email = (EditText)findViewById(R.id.email);
        pass = (EditText)findViewById(R.id.pass);
    }


    public void onStart() {
        super.onStart();
        // Check if user is signed in and go to battery screen
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent=new Intent(AuthActivity.this, BatteryActivity.class);
            startActivity(intent);
        }
    }


    public void Click_wea(android.view.View v) {
        Intent intent=new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
    }


    public void Click_con(android.view.View v) {
        Intent intent=new Intent(AuthActivity.this,ContactsActivity.class);
        startActivity(intent);
    }


    public void Click_reg(View view) { //Registration
        String EM = email.getText().toString();
        String PW = pass.getText().toString();
        mAuth.createUserWithEmailAndPassword(EM, PW)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent=new Intent(AuthActivity.this, BatteryActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(AuthActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    public void Click_log(View view) { //Authentication
        String EM = email.getText().toString();
        String PW = pass.getText().toString();
        mAuth.signInWithEmailAndPassword(EM, PW)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent=new Intent(AuthActivity.this, BatteryActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void Click_log_anon(View view) { //Authentication as an anon
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent=new Intent(AuthActivity.this, BatteryActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}