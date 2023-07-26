package com.pitechitsolutions.essentialtrailerhire;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReceiveTrailerActivity extends AppCompatActivity {

    // UI Components
    private Button scanQRCode;
    private TextView qrCodeResult;
    private Spinner trailerCondition;
    private EditText remarks;
    private TextView currentDateTime;

    // Firebase References
    private DatabaseReference rentalsRef, trailersRef, outstandingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_trailer);

        // Initialize UI components
        scanQRCode = findViewById(R.id.scan_qr_code);
        qrCodeResult = findViewById(R.id.qr_code_result);
        trailerCondition = findViewById(R.id.trailer_condition);
        remarks = findViewById(R.id.remarks);
        currentDateTime = findViewById(R.id.current_date_time);

        // Initialize Firebase References
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        rentalsRef = database.getReference("rentals");
        trailersRef = database.getReference("trailers");
        outstandingRef = database.getReference("outstandingRentals");

        scanQRCode.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(ReceiveTrailerActivity.this);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        currentDateTime.setText(currentDateAndTime);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String qrCode = result.getContents();
                qrCodeResult.setText(qrCode);
                processQRCode(qrCode);
            } else {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQRCode(String qrCode) {
        rentalsRef.orderByChild("trailerBarcode").equalTo(qrCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot rentalSnapshot : dataSnapshot.getChildren()) {
                        Rental rental = rentalSnapshot.getValue(Rental.class);
                        if (rental != null) {
                            try {
                                String deliveryDateTimeStr = rental.getSelectedDeliveryDateTime();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                Date deliveryDateTime = sdf.parse(deliveryDateTimeStr);
                                Date currentDateTime = new Date();
                                long diffInMilliseconds = currentDateTime.getTime() - deliveryDateTime.getTime();

                                if (diffInMilliseconds > 0) {
                                    calculateOverdueCharge(qrCode, diffInMilliseconds, rental, rentalSnapshot);
                                } else {
                                    updateBookingStatus(rentalSnapshot);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleError(databaseError);
            }
        });
    }

    private void calculateOverdueCharge(String qrCode, long diffInMilliseconds, Rental rental, DataSnapshot rentalSnapshot) {
        trailersRef.orderByChild("barcode").equalTo(qrCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot trailerSnapshot : dataSnapshot.getChildren()) {
                    Trailer trailer = trailerSnapshot.getValue(Trailer.class);
                    if (trailer != null) {
                        long overdueTimeInHours = TimeUnit.MILLISECONDS.toHours(diffInMilliseconds);
                        long additionalCharge = calculateAdditionalCharge(trailer.getType(), overdueTimeInHours);

                        createAndShowDialog(additionalCharge, rental.getRentalId(), rental.getCustomerId(),
                                overdueTimeInHours, rental.getDeliveryDestination());

                        // Update the trailer condition, remarks and status
                        trailerSnapshot.getRef().child("condition").setValue(trailerCondition.getSelectedItem().toString());
                        trailerSnapshot.getRef().child("remarks").setValue(remarks.getText().toString());
                        trailerSnapshot.getRef().child("status").setValue("Idle");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleError(databaseError);
            }
        });
    }


    private void updateBookingStatus(DataSnapshot rentalSnapshot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveTrailerActivity.this);

        builder.setTitle("Rental Status");
        builder.setMessage("Trailer returned within timeframe, no outstanding charges.");

        builder.setPositiveButton("Complete Rental", (dialog, id) -> {
            rentalSnapshot.getRef().child("bookingStatus").setValue("Completed");

            // Return the user to the mainMenu activity
            Intent intent = new Intent(ReceiveTrailerActivity.this, MainMenu.class);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private long calculateAdditionalCharge(String trailerType, long overdueTimeInHours) {
        long chargePerDay;

        if (trailerType.equals("2.5m")) {
            chargePerDay = 300;
        } else { // Assume it's a "3m" trailer
            chargePerDay = 350;
        }

        long halfDayCharge = chargePerDay / 2;
        long totalCharge = 0;

        if (overdueTimeInHours <= 2) {
            totalCharge = 0;
        } else if (overdueTimeInHours <= 6) {
            totalCharge = halfDayCharge;
            overdueTimeInHours -= 6; // Deduct the 6 hours charged
        } else if (overdueTimeInHours <= 24) {
            totalCharge = chargePerDay;
            overdueTimeInHours -= 24; // Deduct the 24 hours charged
        }

        // Add charges for every additional 24-hour period
        long additionalDays = overdueTimeInHours / 24;
        if (overdueTimeInHours % 24 > 0) { // if there are any remaining hours, count it as an additional day
            additionalDays += 1;
        }

        totalCharge += additionalDays * chargePerDay;

        return totalCharge;
    }


    private void createAndShowDialog(long additionalCharge, String rentalId, String customerId,
                                     long overdueTimeInHours, String deliveryDestination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveTrailerActivity.this);
        builder.setTitle("Outstanding Charge");
        builder.setMessage("The outstanding charge is: " + additionalCharge);

        final EditText input = new EditText(ReceiveTrailerActivity.this);
        input.setHint("S2S Invoice Number");
        builder.setView(input);

        builder.setPositiveButton("Payment Confirmed", (dialog, id) -> {
            String s2sInvoiceNumber = input.getText().toString();
            String returnDate = currentDateTime.getText().toString(); // get the return date from the TextView

            rentalsRef.child(rentalId).child("bookingStatus").setValue("Completed");

            OutstandingRental outstandingRental = new OutstandingRental(additionalCharge, customerId,
                    overdueTimeInHours, s2sInvoiceNumber, deliveryDestination, returnDate); // pass the return date to the constructor
            outstandingRef.child(rentalId).setValue(outstandingRental);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleError(DatabaseError databaseError) {
        Toast.makeText(this, "Failed to read data. Reason: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
    }
}