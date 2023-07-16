package com.pitechitsolutions.essentialtrailerhire;

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class MainMenu extends AppCompatActivity {
    private Button trailerBookingButton, receiveTrailerButton, incomingTrailersButton, bookingReportsButton, chatSupportBtn;
    private DatabaseReference mDatabase;
    private ValueEventListener postListener;
    private final String TAG = "MainMenuActivity";
    private final String NOTIFICATION_CHANNEL_ID = "incoming_trailers_channel";
    private final int NOTIFICATION_ID = 101;

    private static final String APP_VERSION_URL = "https://pitechsolutions.co.za/output-metadata.json";
    private static final String APP_APK_URL = "https://pitechsolutions.co.za/app-debug.apk";
    private ProgressBar progressBar;

    private Button logoutButton; // Add this line
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        // Initialize the buttons
        trailerBookingButton = findViewById(R.id.trailer_booking_button);
        receiveTrailerButton = findViewById(R.id.receive_trailer_button);
        incomingTrailersButton = findViewById(R.id.incoming_trailers_button);
        bookingReportsButton = findViewById(R.id.booking_reports_button);
        chatSupportBtn = findViewById(R.id.chat_support_button);
        // Get current user email
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        progressBar = findViewById(R.id.progressBar);
        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        checkForUpdates();

        // Initialize logout button
        logoutButton = findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainMenu.this, MainActivity.class));
                finish();
            }
        });

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot trailerSnapshot : dataSnapshot.getChildren()) {
                    IncomingTrailer trailer = trailerSnapshot.getValue(IncomingTrailer.class);

                    if (trailer != null && trailer.getDeliveryBranch().equals(currentUserEmail) && trailer.getStatus().equals("In Transit")) {
                        showNotification("Incoming Trailer", "Trailer with ID: " + trailer.getTrailerId() + " is on its way.");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting data failed, log a message
                Log.w(TAG, "loadData:onCancelled", databaseError.toException());
            }
        };

        mDatabase.child("incomingTrailers").addValueEventListener(postListener);


        trailerBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, TrailerRentalActivity.class);
                startActivity(intent);
            }
        });

        receiveTrailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ReceiveTrailerActivity.class);
                startActivity(intent);
            }
        });

        chatSupportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        incomingTrailersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, IncomingTrailersActivity.class);
                startActivity(intent);
            }
        });

        bookingReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null && postListener != null) {
            mDatabase.child("incomingTrailers").removeEventListener(postListener);
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // set icon here
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        Intent notificationIntent = new Intent(this, MainMenu.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, flags);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For android Oreo and later, notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel description");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }


    private void checkForUpdates() {
        Log.i(TAG, "Checking for updates...");

        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

        new Thread(() -> {
            try {
                // fetch the version from your server
                URL url = new URL(APP_VERSION_URL);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line);
                }
                in.close();

                JSONObject outputMetadata = new JSONObject(responseBuilder.toString());
                JSONArray elements = outputMetadata.getJSONArray("elements");
                JSONObject firstElement = elements.getJSONObject(0);
                int latestVersionCode = firstElement.getInt("versionCode");

                Log.i(TAG, "Latest version code from server: " + latestVersionCode);

                // compare it with the current version
                int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

                Log.i(TAG, "Current version code: " + currentVersionCode);

                if (currentVersionCode < latestVersionCode) {
                    // notify user
                    Log.i(TAG, "Update available. Notifying user...");
                    runOnUiThread(() -> showUpdateDialog());
                } else {
                    Log.i(TAG, "No new updates available.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking for updates", e);
            } finally {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        }).start();
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage("A new version of this app is available. Please update.")
                .setPositiveButton("Update", (dialog, which) -> {
                    // this intent will open the browser to download the APK
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_APK_URL));
                    startActivity(browserIntent);
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void showPasswordDialog() {
        final EditText input = new EditText(MainMenu.this);
        AlertDialog dialog = new AlertDialog.Builder(MainMenu.this)
                .setTitle("Security Check")
                .setMessage("Please enter your branch manager password to access Rental Reports")
                .setView(input)
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String enteredPassword = input.getText().toString();
                        String storedPassword = "EtHbMpW2023"; // replace 'password' with your actual password

                        if (enteredPassword.equals(storedPassword)) {
                            Intent intent = new Intent(MainMenu.this, RentalReportsActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainMenu.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }
}
