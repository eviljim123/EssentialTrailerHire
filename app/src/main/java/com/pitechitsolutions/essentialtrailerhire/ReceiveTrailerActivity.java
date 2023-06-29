package com.pitechitsolutions.essentialtrailerhire;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ReceiveTrailerActivity extends AppCompatActivity {
    private EditText etBarcode;
    private EditText etRemarks;
    private TextView tvReturnDate;
    private Spinner spinnerCondition;
    private DatabaseReference databaseReference;
    private DatabaseReference incomingTrailersRef;
    private DatabaseReference rentalsRef;
    private final int BARCODE_READER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_trailer);
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("trailers");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Log.d("Debugging", "databaseReference: " + databaseReference); // Debugging line
        // Get a reference to the 'incomingTrailers' node
        incomingTrailersRef = rootRef.child("incomingTrailers");
        rentalsRef = rootRef.child("rentals");


        etBarcode = findViewById(R.id.et_barcode);
        etRemarks = findViewById(R.id.et_remarks);
        tvReturnDate = findViewById(R.id.tv_return_date);
        spinnerCondition = findViewById(R.id.spinner_condition);
        Button btnScanBarcode = findViewById(R.id.btnScanBarcode);
        Button btnPickDate = findViewById(R.id.btn_pick_date);
        Button btnSubmit = findViewById(R.id.btn_submit);

        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement your barcode scanning logic here
                Intent intent = new Intent(ReceiveTrailerActivity.this, BarcodeReaderActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement your date picker logic here
                DatePickerDialog datePickerDialog = new DatePickerDialog(ReceiveTrailerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(ReceiveTrailerActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                tvReturnDate.setText(dayOfMonth + "-" + (month + 1) + "-" + year + " " + hourOfDay + ":" + minute);
                            }
                        }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
                        timePickerDialog.show();
                    }
                }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barcode = etBarcode.getText().toString().trim();
                String remarks = etRemarks.getText().toString().trim();
                String returnDate = tvReturnDate.getText().toString().trim();
                String condition = spinnerCondition.getSelectedItem().toString();
                Log.d("Debugging", "barcode: " + barcode + ", remarks: " + remarks + ", returnDate: " + returnDate + ", condition: " + condition); // Debugging line

                if (TextUtils.isEmpty(barcode)) {
                    etBarcode.setError("Barcode is required");
                    return;
                }

                if (TextUtils.isEmpty(remarks)) {
                    etRemarks.setError("Remarks are required");
                    return;
                }

                if (TextUtils.isEmpty(returnDate)) {
                    tvReturnDate.setError("Return date is required");
                    return;
                }

                if (spinnerCondition.getSelectedItemPosition() <= 0) {
                    Toast.makeText(ReceiveTrailerActivity.this, "Please select trailer condition.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(ReceiveTrailerActivity.this)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to submit this trailer?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.d("Debugging", "databaseReference: " + databaseReference);
                                // We use the trailer's barcode to find it in the 'trailers' node
                                databaseReference.orderByChild("barcode").equalTo(barcode)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Log.d("FirebaseOperation", "onDataChange triggered, dataSnapshot: " + dataSnapshot.toString());
                                                Log.d("Debugging", "DataSnapshot dataSnapshot: " + dataSnapshot); // Debugging line
                                                for (DataSnapshot trailerSnapshot: dataSnapshot.getChildren()) {
                                                    Trailer trailer = trailerSnapshot.getValue(Trailer.class);
                                                    if (trailer != null) {
                                                        trailer.setCondition(condition);
                                                        trailer.setRemarks(remarks);
                                                        trailer.setReturnDate(returnDate);
                                                        trailer.setStatus("Idle");

                                                        trailerSnapshot.getRef().setValue(trailer)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(ReceiveTrailerActivity.this, "Data successfully updated in Firebase", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(ReceiveTrailerActivity.this, "Failed to update data in Firebase", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    } else {
                                                        Toast.makeText(ReceiveTrailerActivity.this, "No trailer found with the given barcode", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(ReceiveTrailerActivity.this, "Failed to fetch data from Firebase", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                // Delete the entry from 'incomingTrailers' if the barcode matches the 'trailerId'
                                incomingTrailersRef.orderByChild("trailerId").equalTo(barcode)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(ReceiveTrailerActivity.this, "Failed to delete trailer: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });

                                // Get the current user's email
                                String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                // Update the 'bookingStatus' to "Complete" in 'rentals' if the barcode matches the 'trailerBarcode' and 'deliveryDestination' matches current user email
                                rentalsRef.orderByChild("trailerBarcode").equalTo(barcode)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                    // Get the 'deliveryDestination' from the snapshot
                                                    String deliveryDestination = snapshot.child("deliveryDestination").getValue(String.class);

                                                    // Check if the 'deliveryDestination' matches the currently logged-in branch
                                                    if (currentUserEmail.equals(deliveryDestination)) {
                                                        // If they match, update the 'bookingStatus' to "Complete"
                                                        snapshot.getRef().child("bookingStatus").setValue("Complete");
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(ReceiveTrailerActivity.this, "Failed to update booking status: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                String barcode = data.getStringExtra("BARCODE");
                Log.d("Debugging", "BARCODE_READER_REQUEST_CODE barcode: " + barcode); // Debugging line
                etBarcode.setText(barcode);
            }
        }
    }

}