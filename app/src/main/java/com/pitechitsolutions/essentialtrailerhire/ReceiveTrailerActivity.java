package com.pitechitsolutions.essentialtrailerhire;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private final int BARCODE_READER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_trailer);
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String branchId = sharedPreferences.getString("branchId", "");

        databaseReference = FirebaseDatabase.getInstance().getReference("branches");

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
                        tvReturnDate.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                    }
                }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get entered data and submit to Firebase
                String barcode = etBarcode.getText().toString().trim();
                String remarks = etRemarks.getText().toString().trim();
                String returnDate = tvReturnDate.getText().toString().trim();
                String condition = spinnerCondition.getSelectedItem().toString();

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

                // Here implement your logic to create a unique id for the branch
                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                String branchId = sharedPreferences.getString("branchId", "");

                // Here implement your logic to create a unique id for the rental
                String rentalId = databaseReference.child("branches").child(branchId).child("rentals").push().getKey();

                databaseReference.child("branches").child(branchId).child("rentals").child(rentalId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Trailer existingTrailer = dataSnapshot.getValue(Trailer.class);
                                if (existingTrailer != null) {
                                    existingTrailer.setCondition(condition);
                                    existingTrailer.setRemarks(remarks);
                                    existingTrailer.setReturnDate(returnDate);

                                    dataSnapshot.getRef().setValue(existingTrailer)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(ReceiveTrailerActivity.this, "Data successfully added to Firebase", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ReceiveTrailerActivity.this, "Failed to add data to Firebase", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Trailer newTrailer = new Trailer(barcode, condition, remarks, returnDate);
                                    dataSnapshot.getRef().setValue(newTrailer)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(ReceiveTrailerActivity.this, "Data successfully added to Firebase", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ReceiveTrailerActivity.this, "Failed to add data to Firebase", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(ReceiveTrailerActivity.this, "Failed to fetch data from Firebase", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                String barcode = data.getStringExtra("BARCODE");
                etBarcode.setText(barcode);
            }
        }
    }
}