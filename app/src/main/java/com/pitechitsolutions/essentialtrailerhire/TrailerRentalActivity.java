package com.pitechitsolutions.essentialtrailerhire;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class TrailerRentalActivity extends AppCompatActivity {
    private EditText inputName, inputSurname, inputIdNumber, inputContactNumber, inputEmail;
    private EditText inputResidentialAddress, inputInvoiceNumber;
    private EditText inputTrailerBarcode, inputEstimatedDistance, inputCurrentLocation;
    private Spinner inputDeliveryDestination, inputTrailerCondition;
    private EditText inputTrailerRemarks;
    private Button btnCaptureDriversLicensePhoto, btnCaptureVehicleDiskPhoto;
    private Button btnSelectRentalDateTime, btnSelectDeliveryDateTime, btnViewTermsConditions;
    private CheckBox inputOneWayTrip, inputTermsAndConditions;
    private Button btnRentTrailer;
    private SignaturePad signaturePad;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    // Declare ImageViews to display the captured images
    private ImageView driversLicenseImageView, vehicleDiskImageView;
    // Declare a request code for your camera intent
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // Declare two different request codes for camera intents
    private static final int REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE = 1;
    private static final int REQUEST_IMAGE_CAPTURE_VEHICLE_DISK = 2;

    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer_booking);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        Rental rental = new Rental(); // You need to define this class

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        driversLicenseImageView = findViewById(R.id.client_license_image);
        vehicleDiskImageView = findViewById(R.id.client_vehicle_image);
        // Map fields with views
        inputName = findViewById(R.id.client_name);
        inputSurname = findViewById(R.id.client_surname);
        inputIdNumber = findViewById(R.id.client_id);
        inputContactNumber = findViewById(R.id.client_contact);
        inputEmail = findViewById(R.id.client_email);
        inputResidentialAddress = findViewById(R.id.client_address);
        inputInvoiceNumber = findViewById(R.id.invoice_number);

        btnCaptureDriversLicensePhoto = findViewById(R.id.client_license_photo);
        btnCaptureVehicleDiskPhoto = findViewById(R.id.client_vehicle_photo);
// Declare a DatePickerDialog and a TimePickerDialog

        inputTrailerBarcode = findViewById(R.id.vehicle_barcode);
        inputEstimatedDistance = findViewById(R.id.estimated_distance);
        inputCurrentLocation = findViewById(R.id.current_location);
        inputOneWayTrip = findViewById(R.id.one_way);
        inputDeliveryDestination = findViewById(R.id.delivery_destination);
        inputTrailerCondition = findViewById(R.id.trailer_condition);
        inputTrailerRemarks = findViewById(R.id.trailer_remarks);

        btnSelectRentalDateTime = findViewById(R.id.booking_date);
        btnSelectDeliveryDateTime = findViewById(R.id.delivery_date);

        signaturePad = findViewById(R.id.signature_pad);

        inputTermsAndConditions = findViewById(R.id.terms_conditions);
        btnViewTermsConditions = findViewById(R.id.terms_conditions_button);

        btnRentTrailer = findViewById(R.id.book_trailer);

        btnRentTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputName.getText()) ||
                        TextUtils.isEmpty(inputSurname.getText()) ||
                        TextUtils.isEmpty(inputIdNumber.getText()) ||
                        TextUtils.isEmpty(inputContactNumber.getText()) ||
                        TextUtils.isEmpty(inputEmail.getText()) ||
                        TextUtils.isEmpty(inputResidentialAddress.getText()) ||
                        TextUtils.isEmpty(inputInvoiceNumber.getText()) ||
                        TextUtils.isEmpty(inputTrailerBarcode.getText()) ||
                        TextUtils.isEmpty(inputEstimatedDistance.getText()) ||
                        TextUtils.isEmpty(inputCurrentLocation.getText()) ||
                        TextUtils.isEmpty(inputTrailerRemarks.getText()) ||
                        signaturePad.isEmpty()) {
                    Toast.makeText(TrailerRentalActivity.this, "Please fill all required fields.", Toast.LENGTH_LONG).show();
                    return;
                }

                String branchId = auth.getCurrentUser().getUid();

                Customer customer = new Customer();
                customer.setName(inputName.getText().toString());
                customer.setSurname(inputSurname.getText().toString());
                customer.setIdNumber(inputIdNumber.getText().toString());
                customer.setContactNumber(inputContactNumber.getText().toString());
                customer.setEmail(inputEmail.getText().toString());
                customer.setResidentialAddress(inputResidentialAddress.getText().toString());
                customer.setInvoiceNumber(inputInvoiceNumber.getText().toString());

                String customerId = mDatabase.child("customers").push().getKey();
                assert customerId != null;
                mDatabase.child("customers").child(customerId).setValue(customer);

                Trailer trailer = new Trailer();
                trailer.setBarcode(inputTrailerBarcode.getText().toString());
                trailer.setEstimatedDistance(Double.parseDouble(inputEstimatedDistance.getText().toString()));
                trailer.setCurrentLocation(inputCurrentLocation.getText().toString());
                trailer.setOneWayTrip(inputOneWayTrip.isChecked());
                trailer.setDeliveryDestination(inputDeliveryDestination.getSelectedItem().toString());
                trailer.setCondition(inputTrailerCondition.getSelectedItem().toString());
                trailer.setRemarks(inputTrailerRemarks.getText().toString());
                trailer.setStatus("In Transit");

                String trailerId = mDatabase.child("trailers").push().getKey();
                assert trailerId != null;
                mDatabase.child("trailers").child(trailerId).setValue(trailer);

                Rental rental = new Rental();
                rental.setCustomerID(customerId);
                rental.setTrailerID(trailerId);
                rental.setOneWayTrip(inputOneWayTrip.isChecked());
                rental.setTermsAndConditionsAccepted(inputTermsAndConditions.isChecked());
                rental.setRentalDateTime(btnSelectRentalDateTime.getText().toString());
                rental.setDeliveryDateTime(btnSelectDeliveryDateTime.getText().toString());
                Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedSignature = Base64.encodeToString(byteArray, Base64.DEFAULT);
                rental.setSignature(encodedSignature);

                String rentalId = mDatabase.child("rentals").push().getKey();
                assert rentalId != null;
                mDatabase.child("rentals").child(rentalId).setValue(rental);
                mDatabase.child("branches").child(branchId).child("rentals").child(rentalId).setValue(true);

                Toast.makeText(TrailerRentalActivity.this, "Trailer successfully rented.", Toast.LENGTH_SHORT).show();
            }
        });



        btnViewTermsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a new Activity or Fragment to show the terms and conditions.
                Toast.makeText(TrailerRentalActivity.this, "Show Terms and Conditions", Toast.LENGTH_LONG).show();
            }
        });

        btnCaptureDriversLicensePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE);
                }
            }
        });

        btnCaptureVehicleDiskPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_VEHICLE_DISK);
                }
            }
        });

        btnSelectRentalDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(TrailerRentalActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                rental.setRentalDateTime(year + "-" + (month + 1) + "-" + dayOfMonth + " " + hourOfDay + ":" + minute);
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });
        btnSelectDeliveryDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(TrailerRentalActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                rental.setDeliveryDateTime(year + "-" + (month + 1) + "-" + dayOfMonth + " " + hourOfDay + ":" + minute);
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (requestCode == REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE) {
                // Display the image in an ImageView
                driversLicenseImageView.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE_VEHICLE_DISK) {
                // Display the image in an ImageView
                vehicleDiskImageView.setImageBitmap(imageBitmap);
            }
        }
    }
}