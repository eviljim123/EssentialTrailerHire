package com.pitechitsolutions.essentialtrailerhire;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputLatitude, inputLongitude;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private DatabaseReference chatsRef;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize auth and mDatabase as before
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        inputEmail = findViewById(R.id.branchEmail);
        inputPassword = findViewById(R.id.branchPassword);
        inputLatitude = findViewById(R.id.branchLatitude);
        inputLongitude = findViewById(R.id.branchLongitude);

        Button btnRegisterBranch = findViewById(R.id.btnRegisterBranch);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Sets the desired interval for active location updates, in milliseconds.
        locationRequest.setFastestInterval(5000); // Sets the fastest rate for active location updates, in milliseconds.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Sets the priority of the request.

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Use the location object to get latitude and longitude
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        inputLatitude.setText(String.valueOf(latitude));
                        inputLongitude.setText(String.valueOf(longitude));
                    }
                }
            }
        };

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        btnRegisterBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                Double latitude = null;
                Double longitude = null;
                try {
                    latitude = Double.parseDouble(inputLatitude.getText().toString().trim());
                    longitude = Double.parseDouble(inputLongitude.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Invalid latitude or longitude!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // validation code remains the same

                // create user
                Double finalLatitude = latitude;
                Double finalLongitude = longitude;
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (!task.isSuccessful()) {
                                // error handling code remains the same
                            } else {
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                String userId = task.getResult().getUser().getUid();
                                Branch branch = new Branch(email, password, finalLatitude, finalLongitude);
                                mDatabase.child("branches").child(userId).setValue(branch);
                                initChatNode(userId);
                                startActivity(new Intent(RegisterActivity.this, MainMenu.class));
                                finish();
                            }
                        });
            }
        });
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void initChatNode(String newBranchId) {
        HashMap<String, String> firstMessage = new HashMap<>();
        firstMessage.put("1", "Welcome to the chat!");
        chatsRef.child(newBranchId).setValue(firstMessage)
                .addOnFailureListener(e -> Log.e(TAG, "Error initializing chat: ", e));
    }
}
