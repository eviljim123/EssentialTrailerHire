package com.pitechitsolutions.essentialtrailerhire;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private CheckBox spareWheelCheck;
    private Uri imageUri;
    // Firebase References
    private DatabaseReference rentalsRef, trailersRef, outstandingRef;
    private StorageReference storageRef;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean isDialogShown = false;
    private long dialogAdditionalCharge;
    private String dialogRentalId, dialogCustomerId, dialogDeliveryDestination, dialogBarcode;
    private long dialogOverdueTimeInHours;



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
        spareWheelCheck = findViewById(R.id.spare_wheel_checkbox); // Added
        Button takePhotoButton = findViewById(R.id.photo_button); // Define the button inside onCreate
        // Initialize Firebase References
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        rentalsRef = database.getReference("rentals");
        trailersRef = database.getReference("trailers");
        outstandingRef = database.getReference("outstandingRentals");
        // Initialize Firebase Storage Reference
        storageRef = FirebaseStorage.getInstance().getReference("SpareWheelChecks"); // Added
        scanQRCode.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(ReceiveTrailerActivity.this);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        });
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        currentDateTime.setText(currentDateAndTime);
   takePhotoButton.setOnClickListener(view -> {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 12345); // Request code for the camera activity
        }
    });
}

    private Uri s2sImageUri;
    private AlertDialog currentDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // For capturing the S2S Slip
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Convert bitmap to Uri
            s2sImageUri = getImageUri(this, imageBitmap);
            if (currentDialog != null && !currentDialog.isShowing()) {
                currentDialog.show();
            }
        }

        // For your existing image capture
        else if (requestCode == 12345 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageUri = getImageUri(this, imageBitmap); // Store imageUri for later use
        }

        // For QR code result
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() != null) {
                    String qrCode = result.getContents();
                    qrCodeResult.setText(qrCode);
                    processQRCode(qrCode);
                } else {
                    Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void processQRCode(String qrCode) {
        rentalsRef.orderByChild("trailerBarcode").equalTo(qrCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Rental latestRental = null;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                    for (DataSnapshot rentalSnapshot : dataSnapshot.getChildren()) {
                        Rental rental = rentalSnapshot.getValue(Rental.class);
                        try {
                            Date rentalDate = sdf.parse(rental.getRentalDateTime());
                            if (latestRental == null || rentalDate.after(sdf.parse(latestRental.getRentalDateTime()))) {
                                latestRental = rental; // Keep track of the most recent rental
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (latestRental != null) {
                        // Process the most recent rental
                        String deliveryDateTimeStr = latestRental.getSelectedDeliveryDateTime();
                        try {
                            Date deliveryDateTime = sdf.parse(deliveryDateTimeStr);
                            Date currentDateTime = new Date();
                            long diffInMilliseconds = currentDateTime.getTime() - deliveryDateTime.getTime();

                            if (diffInMilliseconds > 0) {
                                calculateOverdueCharge(qrCode, diffInMilliseconds, latestRental, dataSnapshot.child(latestRental.getRentalId()));
                            } else {
                                updateBookingStatus(dataSnapshot.child(latestRental.getRentalId()));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
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
                        createAndShowDialog(additionalCharge, rental.getRentalId(), rental.getCustomerId(), overdueTimeInHours, rental.getDeliveryDestination(), qrCode);
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
            String rentalId = rentalSnapshot.getKey(); // Assuming the rentalId is the key of rentalSnapshot
            rentalSnapshot.getRef().child("bookingStatus").setValue("Completed");

            trailersRef.orderByChild("barcode").equalTo(qrCodeResult.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot trailerSnapshot : dataSnapshot.getChildren()) {
                        trailerSnapshot.getRef().child("condition").setValue(trailerCondition.getSelectedItem().toString());
                        trailerSnapshot.getRef().child("remarks").setValue(remarks.getText().toString());
                        trailerSnapshot.getRef().child("status").setValue("Idle");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleError(databaseError);
                }
            });

            // Upload the captured image to Firebase
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            }

            // Return the user to the mainMenu activity
            Intent intent = new Intent(ReceiveTrailerActivity.this, MainMenu.class);
            startActivity(intent);
            finish();

            // Delete the trailer from incomingTrailers node
            DatabaseReference incomingTrailersRef = FirebaseDatabase.getInstance().getReference("incomingTrailers");
            incomingTrailersRef.child(qrCodeResult.getText().toString()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ReceiveTrailerActivity.this, "Trailer removed from incomingTrailers", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ReceiveTrailerActivity.this, "Failed to remove trailer from incomingTrailers", Toast.LENGTH_LONG).show();
                    });
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
                                     long overdueTimeInHours, String deliveryDestination, String barcode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveTrailerActivity.this);
        builder.setTitle("Outstanding Charge");
        builder.setMessage("The outstanding charge is: " + additionalCharge);

        final EditText input = new EditText(ReceiveTrailerActivity.this);
        input.setHint("S2S Invoice Number");
        builder.setView(input);

        builder.setNeutralButton("Take Photo of S2S Slip", (dialog, which) -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        builder.setPositiveButton("Payment Confirmed", (dialog, id) -> {
            String s2sInvoiceNumber = input.getText().toString();
            String returnDate = currentDateTime.getText().toString(); // Assuming you've declared and initialized this variable elsewhere

            // Upload the captured image to Firebase
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            }

            if (s2sImageUri != null) {
                uploadS2SSlipToFirebase(s2sImageUri, s2sInvoiceNumber);  // Assuming you've declared and implemented this method elsewhere
            }

            if (!spareWheelCheck.isChecked()) {
                trailersRef.orderByChild("barcode").equalTo(barcode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot trailerSnapshot : dataSnapshot.getChildren()) {
                            trailerSnapshot.getRef().child("remarks").setValue("URGENT! NO SPARE WHEEL WHEN LAST CHECK IN!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
            }

            rentalsRef.child(rentalId).child("bookingStatus").setValue("Completed");
            OutstandingRental outstandingRental = new OutstandingRental(additionalCharge, customerId, overdueTimeInHours, s2sInvoiceNumber, deliveryDestination, returnDate);
            outstandingRef.child(rentalId).setValue(outstandingRental);

            // Show the toast
            Toast.makeText(ReceiveTrailerActivity.this, "Trailer Successfully Retrieved", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        currentDialog = dialog;  // Store the reference

        // Handle your Firebase operations as before
        DatabaseReference incomingTrailersRef = FirebaseDatabase.getInstance().getReference("incomingTrailers");
        incomingTrailersRef.child(qrCodeResult.getText().toString()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ReceiveTrailerActivity.this, "Trailer removed from incomingTrailers", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReceiveTrailerActivity.this, "Failed to remove trailer from incomingTrailers", Toast.LENGTH_LONG).show();
                });

        generateAndSetInvoiceNumber(input);
        dialog.show();
    }
    private void handleError(DatabaseError databaseError) {
        Toast.makeText(this, "Failed to read data. Reason: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
    }
    private void uploadImageToFirebase(Uri imageUri) {
        String fileName = currentDateTime.getText().toString() + qrCodeResult.getText().toString(); // Add rentalID and customerID as needed
        StorageReference fileRef = storageRef.child(fileName);
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(ReceiveTrailerActivity.this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(ReceiveTrailerActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
        });
    }

    private void uploadS2SSlipToFirebase(Uri imageUri, String invoiceNumber) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentBranchId = user.getUid();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference s2sSlipsRef = storageRef.child("S2S Slips/" + currentBranchId + "_" + invoiceNumber + ".jpg");

            s2sSlipsRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // Handle successful upload
            }).addOnFailureListener(exception -> {
                // Handle failed upload
            });
        }
    }


    private void generateAndSetInvoiceNumber(EditText input) {
        DatabaseReference branchesRef = FirebaseDatabase.getInstance().getReference("branches");
        DatabaseReference rentalsRef = FirebaseDatabase.getInstance().getReference("rentals");

        // Get currently logged-in user's UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentBranchId = user.getUid();

            branchesRef.child(currentBranchId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String branchabbr = dataSnapshot.child("branchabbr").getValue(String.class);
                        String s2sMachineCode = dataSnapshot.child("s2sMachineCode").getValue(String.class);

                        // Get current month abbreviation
                        Calendar cal = Calendar.getInstance();
                        String monthAbbr = new SimpleDateFormat("MMM", Locale.ENGLISH).format(cal.getTime()).toUpperCase();

                        // Build the prefix of the invoice number
                        String prefix = branchabbr + "-" + s2sMachineCode + "-" + monthAbbr + "-";

                        // Query rentals to determine the next number
                        rentalsRef.orderByChild("invoiceNumber").startAt(prefix).endAt(prefix + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                long maxNumber = 0;
                                for (DataSnapshot rentalSnapshot : snapshot.getChildren()) {
                                    String invoiceNumber = rentalSnapshot.child("invoiceNumber").getValue(String.class);
                                    if (invoiceNumber != null && invoiceNumber.startsWith(prefix)) {
                                        String numPart = invoiceNumber.replace(prefix, "");
                                        try {
                                            long currentNumber = Long.parseLong(numPart);
                                            maxNumber = Math.max(maxNumber, currentNumber);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                String nextNumber = String.format("%07d", maxNumber + 1);
                                input.setText(prefix + nextNumber);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }
}