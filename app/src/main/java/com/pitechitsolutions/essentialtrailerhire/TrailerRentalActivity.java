package com.pitechitsolutions.essentialtrailerhire;

import static android.content.ContentValues.TAG;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import android.Manifest;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrailerRentalActivity extends AppCompatActivity {
    private Date startDate;
    private Uri photoUri;
    private Uri driverLicensePhotoUri;
    private Uri vehicleDiskPhotoUri;

    private Date endDate;
    private Trailer trailer;
    // Declare currentBranchId as a global variable
    private String currentBranchId;
    private StorageReference signatureCaptureRef;
    private String rentalId;
    private Rental rental;
    TextView trailerStatus, trailerVin, trailerLicensePlate, trailerConditionAfterScan;
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
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    // Declare two different request codes for camera intents
    private static final int REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE = 1;
    private static final int REQUEST_IMAGE_CAPTURE_VEHICLE_DISK = 2;
    private Button scanQrCodeButton;
    private Dialog progressdialog; // Declare here

    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    private Button termsConditions;
    private FirebaseStorage storage;
    private StorageReference licenseCaptureRef;
    private StorageReference vehicleCaptureRef;


    private final DoubleWrapper currentBranchLatitude = new DoubleWrapper(0);
    private final DoubleWrapper currentBranchLongitude = new DoubleWrapper(0);
    private final DoubleWrapper selectedBranchLatitude = new DoubleWrapper(0);
    private final DoubleWrapper selectedBranchLongitude = new DoubleWrapper(0);
    private String branchabbr;
    private String s2sMachineCode;

    private Bitmap driversLicenseBitmap;
    private Bitmap vehicleDiskBitmap;

    private Bitmap signatureBitmap;

    private Trailer scannedTrailer;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private ImageButton infobutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer_booking);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        AutoCompleteTextView searchBox = findViewById(R.id.search_client);
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        driversLicenseImageView = findViewById(R.id.client_license_image);
        vehicleDiskImageView = findViewById(R.id.client_vehicle_image);
        inputName = findViewById(R.id.client_name);
        inputSurname = findViewById(R.id.client_surname);
        inputIdNumber = findViewById(R.id.client_id);
        inputContactNumber = findViewById(R.id.client_contact);
        inputEmail = findViewById(R.id.client_email);
        inputResidentialAddress = findViewById(R.id.client_address);
        inputInvoiceNumber = findViewById(R.id.invoice_number);
        scanQrCodeButton = findViewById(R.id.scan_qr_code);
        btnCaptureDriversLicensePhoto = findViewById(R.id.client_license_photo);
        btnCaptureVehicleDiskPhoto = findViewById(R.id.client_vehicle_photo);
        termsConditions = findViewById(R.id.terms_conditions_button);
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
        trailerStatus = findViewById(R.id.trailer_status);
        trailerVin = findViewById(R.id.trailer_vin);
        trailerLicensePlate = findViewById(R.id.trailer_license_plate);
        trailerConditionAfterScan = findViewById(R.id.trailer_condition_after_scan);
        btnRentTrailer = findViewById(R.id.book_trailer);
        CheckBox oneWayCheckBox = findViewById(R.id.one_way);
        TextView deliveryTextView = findViewById(R.id.delivery_tv);
        deliveryTextView.setVisibility(View.GONE);
        inputDeliveryDestination.setVisibility(View.GONE);
        storage = FirebaseStorage.getInstance();
        infobutton = findViewById(R.id.infoButtons);
        licenseCaptureRef = storage.getReference().child("Disk Captures");
        vehicleCaptureRef = storage.getReference().child("Licence Captures");
        signatureCaptureRef = storage.getReference().child("Signature Captures");
        Button btnCalculateFee = findViewById(R.id.btnCalculateFee);
        TextView tvAmtToPay = findViewById(R.id.tvAmtToPay);

        String monthAbbreviation = new SimpleDateFormat("MMM", Locale.ENGLISH).format(calendar.getTime()).toUpperCase();

        DatabaseReference rentalsRef = FirebaseDatabase.getInstance().getReference("rentals");
        String invoicePrefix = branchabbr + monthAbbreviation + s2sMachineCode;

        rentalsRef.orderByChild("invoiceNumber").startAt(invoicePrefix).endAt(invoicePrefix + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long maxNumber = 0;
                for (DataSnapshot rentalSnapshot : dataSnapshot.getChildren()) {
                    String invoiceNumber = rentalSnapshot.child("invoiceNumber").getValue(String.class);
                    if (invoiceNumber != null && invoiceNumber.startsWith(invoicePrefix)) {
                        long numberPart = Long.parseLong(invoiceNumber.substring(invoicePrefix.length()));
                        maxNumber = Math.max(maxNumber, numberPart);
                    }
                }
                String nextInvoiceNumber = invoicePrefix + String.format("%07d", maxNumber + 1);
                inputInvoiceNumber.setText(nextInvoiceNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseData", "Error fetching data", databaseError.toException());
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentBranchId = currentUser.getUid();
            DatabaseReference branchDbReference = FirebaseDatabase.getInstance().getReference("branches");
            branchDbReference.child(currentBranchId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Populate the Branch object with the data from Firebase
                    Branch currentBranch = dataSnapshot.getValue(Branch.class);

                    if (currentBranch != null) {
                        // Use the properties from currentBranch for the branch abbreviation and machine code
                        String branchabbr = currentBranch.getBranchAbbreviation();
                        String s2sMachineCode = currentBranch.getS2SMachineCode();

                        // Set the branch details in the UI components
                        EditText currentLocation = findViewById(R.id.current_location);
                        currentLocation.setText(currentBranch.getBranchName());

                        currentBranchLatitude.value = currentBranch.getLatitude();
                        currentBranchLongitude.value = currentBranch.getLongitude();

                        // Calculate and set distance based on the newly populated branch details
                        calculateAndSetDistance();

                        // Now that you have branchabbr and s2sMachineCode, generate the invoice number
                        DatabaseReference rentalsRef = FirebaseDatabase.getInstance().getReference("rentals");
                        String invoicePrefix = branchabbr + "-" + s2sMachineCode + "-" + monthAbbreviation + "-";

                        rentalsRef.orderByChild("invoiceNumber").startAt(invoicePrefix).endAt(invoicePrefix + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long maxNumber = 0;
                                for (DataSnapshot rentalSnapshot : dataSnapshot.getChildren()) {
                                    String invoiceNumber = rentalSnapshot.child("invoiceNumber").getValue(String.class);
                                    if (invoiceNumber != null && invoiceNumber.startsWith(invoicePrefix)) {
                                        long numberPart = Long.parseLong(invoiceNumber.substring(invoicePrefix.length()));
                                        maxNumber = Math.max(maxNumber, numberPart);
                                    }
                                }
                                String nextInvoiceNumber = invoicePrefix + String.format("%07d", maxNumber + 1);
                                inputInvoiceNumber.setText(nextInvoiceNumber);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("FirebaseData", "Error fetching data", databaseError.toException());
                            }
                        });

                    } else {
                        Log.e("FirebaseData", "Branch with id " + currentBranchId + " is null.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("FirebaseData", "Error fetching data", databaseError.toException());
                }
            });
        }

        infobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TrailerRentalActivity.this);
                View view = LayoutInflater.from(TrailerRentalActivity.this).inflate(R.layout.dialog_instructions, null);

                TextView instructionsText = view.findViewById(R.id.instructionsText);
                instructionsText.setText("Client Database Search:\n" +
                        "\n" +
                        "To locate pre-existing clients, search using their ID numbers, phone numbers, names, or surnames.\n" +
                        "Select the desired client from the search results to auto-fill essential client details.\n" +
                        "For New Clients:\n" +
                        "\n" +
                        "Client Details:\n" +
                        "\n" +
                        "Full Name: Provide the complete name as on the identification document.\n" +
                        "Surname: Enter the client's surname.\n" +
                        "ID Number: Input the number from the client's official identification.\n" +
                        "Contact Number: Record the client's primary phone number.\n" +
                        "Email Address: Specify the client's email for communication.\n" +
                        "Residential Address: Detail the client's current living address.\n" +
                        "Documentation Photos:\n" +
                        "\n" +
                        "Driver's License: Capture a clear photograph of the client's driver's license.\n" +
                        "Vehicle Disk: Snap a photo of the disk from the client's vehicle used to tow trailers.\n" +
                        "Trailer Information:\n" +
                        "\n" +
                        "QR Code Scan: For swift input, scan the trailer's QR code. This action populates the \"Trailer Status\" section.\n" +
                        "Estimated Distance: Inquire about the client's intended travel distance and record the value.\n" +
                        "Current Location: This is auto-filled based on branch credentials.\n" +
                        "One Way Option: If the trailer is destined for a different branch, check the 'One Way' box. This auto-fills the estimated distance.\n" +
                        "Delivery Destination: For 'One Way' rentals, choose the receiving branch from the dropdown.\n" +
                        "Trailer Condition: Examine the trailer and select the most accurate condition description from the options provided.\n" +
                        "Remarks: Any additional notes about the trailer are mandatory. If none, please input: \"No remarks.\"\n" +
                        "Rental & Delivery Schedule:\n" +
                        "\n" +
                        "Rental Date & Time: Set the timestamp when the trailer is rented out.\n" +
                        "Delivery Date & Time: Record the client's anticipated return timestamp.\n" +
                        "Fee Assessment:\n" +
                        "\n" +
                        "After registering the trailer and setting the rental schedule, compute the rental fee. Upon client agreement, proceed to payment. The Shop 2 Shop invoice number is auto-generated for your convenience.\n" +
                        "Confirmation and Finalization:\n" +
                        "\n" +
                        "Signature: Have the client provide their signature for validation.\n" +
                        "Terms & Conditions: Clients must acknowledge and accept by checking the respective box. Full terms are accessible via the \"Terms and Conditions\" tab.\n" +
                        "Rent Trailer: Once activated, you'll be prompted to photograph the payment receipt from the Shop 2 Shop device. This action concludes the rental process, allowing a return to the main menu.\n" +
                        "Thank you for ensuring a smooth trailer rental experience!\n");

                builder.setView(view)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                builder.create().show();
            }
        });

        btnViewTermsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsAndConditions();
            }
        });
        ArrayList<String> dropdownItems = new ArrayList<>();
        List<Customer> customerList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("customers");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot customerSnapshot : dataSnapshot.getChildren()) {
                        try {
                            Customer customer = customerSnapshot.getValue(Customer.class);
                            customerList.add(customer);
                            dropdownItems.add(customer.getName() + " " + customer.getSurname() + ", ID: " + customer.getIdNumber());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database Error", databaseError.getMessage());
                Log.e("Database Error", "onCancelled called: " + databaseError.getMessage());
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, dropdownItems) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        ArrayList<String> filteredList = new ArrayList<>();
                        if (constraint != null && constraint.length() > 0) {
                            String searchString = constraint.toString().toLowerCase();
                            for (Customer customer : customerList) {
                                if (customer.getName().toLowerCase().contains(searchString) ||
                                        customer.getSurname().toLowerCase().contains(searchString) ||
                                        customer.getIdNumber().toLowerCase().contains(searchString) ||
                                        customer.getContactNumber().toLowerCase().contains(searchString)) {
                                    // Add the matching customer to the dropdown
                                    filteredList.add(customer.getName() + " " + customer.getSurname() + ", ID: " + customer.getIdNumber());
                                }
                            }
                        }
                        results.values = filteredList;
                        results.count = filteredList.size();
                        return results;
                    }


                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        addAll((List<String>) results.values);
                        notifyDataSetChanged();
                    }
                };
            }
        };
        searchBox.setAdapter(adapter);
        searchBox.setThreshold(1);
        searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                for (Customer customer : customerList) {
                    String customerData = customer.getName() + " " + customer.getSurname() + ", ID: " + customer.getIdNumber();
                    if (customerData.equals(selectedItem)) {
                        // Populate other fields with the customer information
                        inputName.setText(customer.getName());
                        inputSurname.setText(customer.getSurname());
                        inputIdNumber.setText(customer.getIdNumber());
                        inputContactNumber.setText(customer.getContactNumber());
                        inputEmail.setText(customer.getEmail());
                        inputResidentialAddress.setText(customer.getResidentialAddress());
                        // Stop the loop once you find the customer
                        break;
                    }
                }
            }
        });

        DatabaseReference trailerRef = database.getReference("trailers"); // This is a path to where your trailer data is stored

        trailerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Trailer trailer = dataSnapshot.getValue(Trailer.class);
                if (trailer != null) {
                    // Use your trailer object here
                    Log.i("Trailer Data", "Trailer data: " + trailer.getBarcode());
                } else {
                    Log.e("Trailer Data", "No trailer data found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("Trailer Data", "Failed to read trailer data.", error.toException());
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("branches");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> branchNames = new ArrayList<>();
                calculateAndSetDistance();
                for (DataSnapshot branchSnapshot : dataSnapshot.getChildren()) {
                    String branchName = branchSnapshot.child("branchName").getValue(String.class);
                    if (branchName != null) {
                        branchNames.add(branchName);
                    } else {
                        // Log a message or handle the case where branch name is null
                        Log.e("FirebaseData", "Found a branch with null name.");
                    }
                }
                if (!branchNames.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, branchNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Spinner inputDeliveryDestination = findViewById(R.id.delivery_destination);
                    inputDeliveryDestination.setAdapter(adapter);
                    inputDeliveryDestination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedBranchName = parent.getItemAtPosition(position).toString();
                            databaseReference.orderByChild("branchName").equalTo(selectedBranchName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // dataSnapshot is the "branch" node whose value is selectedBranchName
                                        for (DataSnapshot branchSnapshot : dataSnapshot.getChildren()) {
                                            Double branchLatitude = branchSnapshot.child("latitude").getValue(Double.class);
                                            Double branchLongitude = branchSnapshot.child("longitude").getValue(Double.class);
                                            if (branchLatitude != null && branchLongitude != null) {
                                                selectedBranchLatitude.value = branchLatitude;
                                                selectedBranchLongitude.value = branchLongitude;
                                                calculateAndSetDistance();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("FirebaseData", "Error occurred: " + databaseError.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Optional: Handle the case where no item is selected
                        }
                    });
                } else {
                    // Log a message or handle the case where branchNames is empty (all branches had null names or no branches were found)
                    Log.e("FirebaseData", "No valid branches found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log the error message
                Log.e("FirebaseData", "Error fetching data", databaseError.toException());
            }
        });
        String[] conditions = new String[]{"Great", "Average", "Bad"};
        ArrayAdapter<String> trailerConditionAdapter = new ArrayAdapter<>(TrailerRentalActivity.this,
                android.R.layout.simple_spinner_dropdown_item, conditions);
        inputTrailerCondition.setAdapter(trailerConditionAdapter);
        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(TrailerRentalActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scan a QR Code");
                integrator.setCameraId(0);  // Use a specific camera of the device
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        });


        btnCalculateFee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Get rental and delivery date time from TextViews
                    TextView tvRentalDateTime = findViewById(R.id.tvRentalDateTime);
                    TextView tvDeliveryDateTime = findViewById(R.id.tvDeliveryDateTime);
                    String rentalDateTime = tvRentalDateTime.getText().toString();
                    String deliveryDateTime = tvDeliveryDateTime.getText().toString();

                    // Parse your dates
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date rentalDate = format.parse(rentalDateTime);

                    long duration = 0;
                    if (!oneWayCheckBox.isChecked()) {
                        Date deliveryDate = format.parse(deliveryDateTime);
                        duration = (deliveryDate.getTime() - rentalDate.getTime()) / (1000 * 60 * 60 * 24);
                        if (duration < 1) {
                            duration = 1;
                        }
                    }

                    // Check if scannedTrailer is null
                    if (scannedTrailer == null) {
                        return;
                    }

                    // Get the trailer size
                    String trailerSize = scannedTrailer.getType();

                    // Set the trailer price based on size
                    int price;
                    switch (trailerSize) {
                        case "2.5m":
                            price = 320;
                            break;
                        case "3m":
                            price = 350;
                            break;
                        case "3m Livestock":
                            price = 850;
                            break;
                        default:
                            throw new IllegalArgumentException("Unexpected trailer size: " + trailerSize);
                    }

                    // Calculate final fee
                    int fee;
                    if (oneWayCheckBox.isChecked()) {
                        // Get distance from EditText
                        EditText estimatedDistanceEditText = findViewById(R.id.estimated_distance);
                        double estimatedDistance = Double.parseDouble(estimatedDistanceEditText.getText().toString());

                        // For one-way rentals, fee is based on the distance
                        double pricePerKilometer = 1.9;
                        if (estimatedDistance <= 400) {
                            // If the distance is less than or equal to 400 km, charge a full day's price and for the distance
                            fee = price + (int) Math.round(pricePerKilometer * estimatedDistance);
                        } else {
                            // If the distance is more than 400 km, charge based on the distance only
                            fee = (int) Math.round(pricePerKilometer * estimatedDistance);
                        }
                    } else {
                        // For non one-way rentals, fee is based on duration and trailer type
                        fee = (int) duration * price;
                    }

                    tvAmtToPay.setText("Amount To Pay: " + fee);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e("TrailerRentalActivity", "Error parsing date: " + e.getMessage());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e("TrailerRentalActivity", "Error parsing estimated distance: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Log.e("TrailerRentalActivity", "Error with trailer size: " + e.getMessage());
                }
            }
        });


        btnCaptureDriversLicensePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(),
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    ActivityCompat.requestPermissions(TrailerRentalActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile("DriverLicense");  // Use a specific type for creating a file
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (photoFile != null) {
                            driverLicensePhotoUri = FileProvider.getUriForFile(TrailerRentalActivity.this,
                                    "com.pitechitsolutions.essentialtrailerhire.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, driverLicensePhotoUri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE);
                        }
                    }
                }
            }
        });

        btnCaptureVehicleDiskPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(),
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    ActivityCompat.requestPermissions(TrailerRentalActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    // Permission has already been granted
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile("VehicleDisk");  // Use a specific type for creating a file
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (photoFile != null) {
                            vehicleDiskPhotoUri = FileProvider.getUriForFile(TrailerRentalActivity.this,
                                    "com.pitechitsolutions.essentialtrailerhire.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, vehicleDiskPhotoUri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_VEHICLE_DISK);
                        }
                    }
                }
            }
        });

        oneWayCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // If the CheckBox is checked, make the TextView and Spinner visible
                    deliveryTextView.setVisibility(View.VISIBLE);
                    inputDeliveryDestination.setVisibility(View.VISIBLE);
                } else {
                    // If the CheckBox is not checked, hide the TextView and Spinner
                    deliveryTextView.setVisibility(View.GONE);
                    inputDeliveryDestination.setVisibility(View.GONE);
                }
            }
        });

        final Rental rental = new Rental();

        btnSelectRentalDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(TrailerRentalActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(TrailerRentalActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                String dateTime = year + "-" + (month + 1) + "-" + dayOfMonth + " " + hourOfDay + ":" + minute;
                                                rental.setRentalDateTime(dateTime);
                                                // Assume you have a TextView with id tvRentalDateTime
                                                TextView tvRentalDateTime = findViewById(R.id.tvRentalDateTime);
                                                tvRentalDateTime.setText(dateTime);
                                            }
                                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                                timePickerDialog.show();
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btnSelectDeliveryDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First get the date
                DatePickerDialog datePickerDialog = new DatePickerDialog(TrailerRentalActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(TrailerRentalActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                String dateTime = year + "-" + (month + 1) + "-" + dayOfMonth + " " + hourOfDay + ":" + minute;
                                                rental.setSelectedDeliveryDateTime(dateTime);
                                                // Assume you have a TextView with id tvDeliveryDateTime
                                                TextView tvDeliveryDateTime = findViewById(R.id.tvDeliveryDateTime);
                                                tvDeliveryDateTime.setText(dateTime);
                                            }
                                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                                timePickerDialog.show();
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btnRentTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rentalId = mDatabase.child("rentals").push().getKey();
                // Validate fields before processing to Firebase
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
                        signaturePad.isEmpty() ||
                        inputTrailerCondition.getSelectedItem() == null ||
                        inputDeliveryDestination.getSelectedItem() == null) {
                    Toast.makeText(TrailerRentalActivity.this, "Please fill all required fields.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Empty field(s) detected. Aborting operation.");
                    return;
                }
                // Show toast message to the user before starting the camera
                Toast.makeText(TrailerRentalActivity.this, "Take A Picture of the S2S Slip", Toast.LENGTH_LONG).show();

// Start the camera activity to take the picture
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

                Dialog progressdialog = new Dialog(TrailerRentalActivity.this);
                progressdialog.setContentView(R.layout.dialog_progress);
                progressdialog.setCancelable(false);
                progressdialog.show();
                String customerId = inputIdNumber.getText().toString().trim();
                Customer customer = new Customer(
                        inputName.getText().toString().trim(),
                        inputSurname.getText().toString().trim(),
                        customerId,
                        inputContactNumber.getText().toString().trim(),
                        inputEmail.getText().toString().trim(),
                        inputResidentialAddress.getText().toString().trim()
                );
                System.out.println("customer = " + customer);
                mDatabase.child("customers").child(customerId).setValue(customer);
                String rentalId = mDatabase.child("rentals").push().getKey();
                String inputTrailerBarcodeStr = inputTrailerBarcode.getText().toString().trim();
                Query trailerQuery = mDatabase.child("trailers").orderByChild("barcode").equalTo(inputTrailerBarcodeStr);
                trailerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot trailer : dataSnapshot.getChildren()) {
                                Trailer trailerInfo = trailer.getValue(Trailer.class);
                                if (trailerInfo != null) {
                                    if (!trailerInfo.getStatus().equals("Idle")) {
                                        Toast.makeText(TrailerRentalActivity.this, "The trailer is not available for rental", Toast.LENGTH_SHORT).show();
                                        progressdialog.dismiss(); // Dismiss dialog if trailer not available
                                        return;
                                    }
                                    trailerInfo.setStatus("In Transit");
                                    trailer.getRef().setValue(trailerInfo);
                                    uploadPictureToFirebase("Licence Captures", driversLicenseImageView, customerId, rentalId, new OnUploadCompleteListener() {
                                        @Override
                                        public void onComplete(String licenseUrl) {
                                            // Upload the vehicle disk photo
                                            uploadPictureToFirebase("Disk Captures", vehicleDiskImageView, customerId, rentalId, new OnUploadCompleteListener() {
                                                @Override
                                                public void onComplete(String vehicleDiskUrl) {
                                                    uploadPictureToFirebase("Signature Captures", signaturePad, customerId, rentalId, new OnUploadCompleteListener() {
                                                        @Override
                                                        public void onComplete(String signatureUrl) {
                                                            // Create a new Rental instance
                                                            Rental rental = new Rental();
                                                            rental.setRentalId(rentalId);
                                                            rental.setInvoiceNumber(inputInvoiceNumber.getText().toString().trim());
                                                            rental.setDriverLicenseUrl(licenseUrl);
                                                            rental.setVehicleDiskUrl(vehicleDiskUrl);
                                                            rental.setSignatureUrl(signatureUrl);
                                                            rental.setBookingStatus("In Transit");
                                                            rental.setCustomerId(customerId);
                                                            rental.setTrailerBarcode(inputTrailerBarcodeStr);
                                                            rental.setCurrentLocation(inputCurrentLocation.getText().toString().trim());

                                                            // Determine the delivery destination based on checkbox
                                                            CheckBox oneWayCheckBox = findViewById(R.id.one_way);
                                                            String deliveryDestination;
                                                            if (oneWayCheckBox.isChecked()) {
                                                                deliveryDestination = inputDeliveryDestination.getSelectedItem().toString().trim();
                                                            } else {
                                                                deliveryDestination = inputCurrentLocation.getText().toString().trim(); // Set to current location if checkbox is not checked
                                                            }
                                                            rental.setDeliveryDestination(deliveryDestination);
                                                            rental.setTrailerRemarks(inputTrailerRemarks.getText().toString().trim());
                                                            rental.setCharge(tvAmtToPay.getText().toString().trim());
                                                            TextView tvRentalDateTime = findViewById(R.id.tvRentalDateTime);
                                                            rental.setRentalDateTime(tvRentalDateTime.getText().toString());
                                                            TextView tvDeliveryDateTime = findViewById(R.id.tvDeliveryDateTime);
                                                            rental.setSelectedDeliveryDateTime(tvDeliveryDateTime.getText().toString());

                                                            IncomingTrailer incomingTrailer = new IncomingTrailer();
                                                            incomingTrailer.setBranchID(inputCurrentLocation.getText().toString().trim());
                                                            incomingTrailer.setDeliveryBranch(deliveryDestination); // Set the same delivery destination as rental
                                                            incomingTrailer.setEstimatedArrivalDateTime(tvDeliveryDateTime.getText().toString());
                                                            incomingTrailer.setOriginBranch(inputCurrentLocation.getText().toString().trim());
                                                            incomingTrailer.setStatus("In Transit");
                                                            incomingTrailer.setTrailerId(inputTrailerBarcodeStr);
                                                            mDatabase.child("incomingTrailers").child(inputTrailerBarcodeStr).setValue(incomingTrailer);

                                                            mDatabase.child("rentals").child(rentalId).setValue(rental);
                                                            Toast.makeText(TrailerRentalActivity.this, "Trailer successfully rented.", Toast.LENGTH_SHORT).show();
                                                            progressdialog.dismiss();
                                                            resetForm();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        } else {
                            Toast.makeText(TrailerRentalActivity.this, "No trailer found with the entered barcode", Toast.LENGTH_SHORT).show();
                            progressdialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(TrailerRentalActivity.this, "Failed to rent the trailer: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failed to read value.", databaseError.toException());
                        // Dismiss progress dialog here too
                        progressdialog.dismiss();
                    }
                });
            }
        });
    }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Log.e("Scan*******", "Cancelled scan");
                } else {
                    Log.e("Scan", "Scanned");
                    String scannedBarcode = result.getContents();
                    Log.e("Scan", "Scanned QR Code: " + scannedBarcode);
                    Log.d("Barcode", "Scanned QR Code: " + scannedBarcode);

                    inputTrailerBarcode.setText(scannedBarcode);
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("trailers").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("Firebase", "Data: " + dataSnapshot.toString());
                            scannedTrailer = null;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Trailer trailer = snapshot.getValue(Trailer.class);
                                if (trailer != null) {
                                    Log.d("Barcode", "Comparing with: " + trailer.getBarcode());
                                    if (scannedBarcode.equals(trailer.getBarcode())) {
                                        scannedTrailer = trailer;
                                        break;
                                    }
                                }
                            }
                            if (scannedTrailer == null) {
                                Toast.makeText(TrailerRentalActivity.this, "The trailer does not exist", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (scannedTrailer.getStatus().equals("In Transit")) {
                                Toast.makeText(TrailerRentalActivity.this, "The trailer is not available for rental", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // If trailer exists and is not in transit, then update the fields.
                            trailerStatus.setText(scannedTrailer.getStatus());
                            trailerVin.setText(scannedTrailer.getVinNo());
                            trailerLicensePlate.setText(scannedTrailer.getLicensePlateNumber());
                            trailerConditionAfterScan.setText(scannedTrailer.getCondition());
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });
                }
            }
            else if (requestCode == REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE && resultCode == RESULT_OK) {
                try {
                    driversLicenseBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), driverLicensePhotoUri);
                    driversLicenseImageView.setImageBitmap(driversLicenseBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE_VEHICLE_DISK && resultCode == RESULT_OK) {
                try {
                    vehicleDiskBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), vehicleDiskPhotoUri);
                    vehicleDiskImageView.setImageBitmap(vehicleDiskBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }


        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                String fileName = inputCurrentLocation.getText().toString() +
                        inputInvoiceNumber.getText().toString() +
                        ".jpg";
                // Upload to Firebase
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference s2sSlipsRef = storageRef.child("S2S Slips/" + fileName);

                s2sSlipsRef.putBytes(imageData)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get the download URL
                                s2sSlipsRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // uri contains the download URL
                                        String downloadUrl = uri.toString();

                                        // Assuming rentalId is already defined earlier in your code
                                        mDatabase.child("rentals").child(rentalId).child("slipUrl").setValue(downloadUrl)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(TrailerRentalActivity.this, "Image URL saved successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(TrailerRentalActivity.this, "Failed to save image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TrailerRentalActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(TrailerRentalActivity.this, "Failed to upload image: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                                // You might want to dismiss your progress dialog here as well
                                progressdialog.dismiss();
                            }
                        });

            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }



    private void calculateAndSetDistance() {
        EditText estimatedDistance = findViewById(R.id.estimated_distance);

        if (currentBranchLatitude.value != 0 && currentBranchLongitude.value != 0 &&
                selectedBranchLatitude.value != 0 && selectedBranchLongitude.value != 0) {
            double distance = calculateDistance(currentBranchLatitude.value, currentBranchLongitude.value, selectedBranchLatitude.value, selectedBranchLongitude.value);
            estimatedDistance.setText(String.valueOf(distance));
        } else {
            estimatedDistance.setText("N/A"); // or some other default value
        }
    }

    public String toFirebaseKey(String string) {
        String base64 = android.util.Base64.encodeToString(string.getBytes(), android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);
        return base64;
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to kilometers
        long roundedDistance = Math.round(distance); // round to the nearest whole number

        double finalDistance = roundedDistance + (roundedDistance / 3.0); // add a third of the distance to the total

        long roundedFinalDistance = Math.round(finalDistance); // round the final distance to the nearest whole number

        return roundedFinalDistance;
    }



    private void showTermsAndConditions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Terms and Conditions");
        builder.setMessage("The LESSOR AND LESSEE/FRANCHISEE HEREBY AGREE AS FOLLOWS:\n" +
                "1. DEFINITIONS\n" +
                "1.1 LESSOR – means registered owner of the trailer hire depot known as ESSENTIAL TRAILER HIRE\n" +
                "1.2 LESSEE/FRANCHISEE – means the party who hires the trailers from the LESSOR;\n" +
                "1.3 TRAILER – refers to the vehicle rented by the LESSOR and all equipment and accessories attached there to.\n" +
                "1.4 CUSTOMER – is the party contracting with the LESSEE/FRANCHISEE to rent a Trailer identified in the Rental Agreement.\n" +
                "1.5 AUTHORISED DRIVER – is a driver authorised to drive a vehicle towing the Trailer, this includes the customer as well as any other driver whose name and driving license number are listed in the Rental Agreement.\n" +
                "1.6 HIRE PERIOD – is the period from the date on which the Trailer is collected from the LESSEE/FRANCHISEE by the customer or any authorised driver up until the Trailer is returned to the LESSEE/FRANCHISEE.\n" +
                "2. TERMS AND CONDITIONS\n" +
                "2.1 The LESSOR will deliver TWO (2) trailers to each LESSOR/FRANCHISEE’s business premises \n" +
                        "at the address indicated in “Annexure A”. \n" +
                        "2.2 The LESSOR/FRANCHISEE may apply for an increased amount of trailers for their business \n" +
                        "premises depending on the rental volume. \n" +
                        "2.3 The LESSEE/FRANCHISEE hereby agree that if the monthly minimum trailer target hire are \n" +
                        "not met, the LESSOR will have the right to relocate the trailers without written notice to the \n" +
                        "LESSEE/FRANCHISEE. \n" +
                        "2.4 All rentals are limited to within the borders of the republic of South Africa, unless written consent \n" +
                        "has been given by the LESSOR. Failure to adhere to the above mentioned will resort in the \n" +
                        "LESSOR/FRANCHISEE being charged with a criminal offence should the description of the \n" +
                        "load and or the destination to which the Trailer prove to be incorrect, the deposit will be forfeited. \n" +
                        "2.5 The LESSEE/FRANCHISEE acknowledges that they have received the Trailers in good \n" +
                        "roadworthy condition and undertakes to return it in the same condition to the LESSOR when this \n" +
                        "agreement is terminated. \n" +
                        "2.6 Subject to the condition of section 151 of the Road Traffic Act, the GVM of the Trailers as \n" +
                        "indicated is 750 kgs. \n" +
                        "3. RESPONSIBILITIES OF THE LESSOR \n" +
                        "3.1 A “Point of Sale” Device will be provided by the LESSOR at their expense to every Trailer Depo \n" +
                        "of the LESSEE/FRANCHISEE; \n" +
                        "3.2 The LESSOR will train the appointed staff for each Trailer Depo at their expense; \n" +
                        "3.3 The LESSOR acknowledges that at the time of delivering of the trailers, the trailers as a whole \n" +
                        "is in a good working and roadworthy condition and the LESEE/FRANCHISEE undertakes to \n" +
                        "return it in the same condition, fair wear and tear excluded. \n" +
                        "3.4 The LESSOR undertakes to maintain and keep the Trailers in a roadworthy state and supply all \n" +
                        "Roadworthy certificates to the LESSEE/FRANCHISEE. \n" +
                        "3.5 The LESSOR undertakes to attend to transfer of Sales on monthly payments; \n" +
                        "3.6 The LESSOR shall not be held liable for: loss, theft or damage to the trailer whatsoever; or loss \n" +
                        "or damage to the towing vehicle of any nature whatsoever; or any injury or death of whatsoever \n" +
                        "nature caused to the LESSEE/FRANCHISEE or CUSTOMER or third party by the rental trailer; \n" +
                        "or loss or damage of the cargo stored on the rental trailer or any third party belongings. This \n" +
                        "statement is true for occurrences both in and outside the borders of South Africa \n" +
                        "4. RESPONSIBILITIES OF THE LESSEE/FRANCHISEE \n" +
                        "4.1 The LESSEE/FRANCHISEE will acknowledge receipt of the trailers in the writing upon delivery \n" +
                        "thereof by the LESSOR to the LESSEE/FRANCHISEE’s business premises; \n" +
                        "4.2 The LESSEE/FRANCHISEE undertakes to employ a minimum of TWO (2) staff members to \n" +
                        "attend to the Trailer Depo; \n" +
                        "4.3 The LESSEE/FRANCHISEE undertakes to attend to the correct capturing of the “Rental \n" +
                        "Agreement” which include but is not limited to the Customer’s personal information, Trailer \n" +
                        "Identification and Information, Customer’s Vehicle Information and purpose of the “Rental \n" +
                        "Agreement”; \n" +
                        "4.4 The LESSEE/FRANCHISEE undertakes to complete a “Checklist / Inspection:” for every trailer \n" +
                        "hire transaction upon collection and return of the trailers and such checklists / Inspection Lists \n" +
                        "will be available for inspection by the LESSOR at the LESSOR’s demand; \n" +
                        "4.5 It is the responsibility of the LESSEE/FRANCHISEE to endure that the tyre pressures are correct \n" +
                        "according to the load being carried and the LESSEE/FRANCHISEE acknowledges that they will \n" +
                        "be responsible for all damages incurred if par 4.5 are not adhered to, including but not limited to \n" +
                        "any cuts and damage to the tyres as well as the rims of the trailers; \n" +
                        "4.6 If upon return of the trailer to the LESSOR there are any damage to the wiring or plug of the \n" +
                        "trailer, the LESSEE/FRANCHISEE will incur a fee of R100 per damaged trailer \n" +
                        "and the fee will be charged to the LESSEE/FRANCHISEE’s account; \n" +
                        "4.7 It is the responsibility of the LESSEE/FRANCHISEE to assure the correctness of the Customer’s \n" +
                        "information. The vehicle to which the trailer shall be attached shall not be used : \n" +
                        "4.7.1 In contrary to any Road Traffic Act; \n" +
                        "4.7.2 By any person who provides mistaken, false or fraudulent information to the \n" +
                        "LESSEE/FRANCHISEE; \n" +
                        "4.7.3 By anyone other than the properly licensed driver; \n" +
                        "4.7.4 By anyone other than the properly licensed driver with the consent of the \n" +
                        "LESSEE/FRANCHISEE. \n" +
                        "4.8 The LESSEE/FRANCHISEE shall not incur any expenses, nor have the trailer repaired on behalf \n" +
                        "of the LESSOR without the express written authorization of the LESSOR. This includes, but is \n" +
                        "not limited to the removal, tow away, transport or storage of the trailer. Authorized expenditure \n" +
                        "shall be paid by the LESSOR to the LESSEE/FRANCHISEE on demand. \n" +
                        "4.9 Should the trailer be involved in an accident or be stolen while in the possession of the \n" +
                        "LESSEE/FRANCHISEE, it is the responsibility of the LESSEE/FRANCHISEE to: \n" +
                        "4.9.1 report it to the nearest Police station and the LESSOR within 24 hours; \n" +
                        "4.9.2 supply the LESSOR with an enlarged copy of the Customer’s driver license; \n" +
                        "4.9.3 complete the insurance claim form; \n" +
                        "4.9.4 and pay the required insurance excess of the vehicle. \n" +
                        "4.10 The LESSEE/FRANCHISEE shall be liable for all fines, penalties and the like, including all \n" +
                        "legal costs incurred by the LESSOR or to its attorneys in accordance with the usual charges at \n" +
                        "the time for parking, traffic and other criminal offences arising out of or concerning the use of \n" +
                        "the trailer during the rental period and the LESSEE/FRANCHISEE accordingly indemnifies \n" +
                        "the LESSOR against all liability. All charges payable by the LESSEE/FRANCHISEE shall be \n" +
                        "payable on the termination of the rental period unless the LESSOR requires all or any charges \n" +
                        "to be prepaid in advance \n" +
                        "4.11 The trailer may not be used to transport goods in violation of any customs laws or any other \n" +
                        "illegal manner or beyond the borders of the territory or in any area in the territory where there \n" +
                        "is or may be a risk of incidence of civil unrest, political disturbance or riot or any activity \n" +
                        "associated with any of the aforegoing. The LESSEE/FRANCHISEE shall make adequate \n" +
                        "provision for the safety and security of the trailer and in particular without limiting the \n" +
                        "generality of the aforegoing he shall keep the trailers properly secured and locked when not in \n" +
                        "use. \n" +
                        "5. DEFAULT, LIABILITY, INDEMNITIES AND WARRANTIES \n" +
                        "5.1 A certificate under the hand of any manager or member of the LESSOR, in respect of any amount \n" +
                        "owing to the LESSOR under and in terms of this contract, the fact that such amount is due and \n" +
                        "payable thereon and the date from which such interest is reckoned shall constitute Prima Facie \n" +
                        "evidence of the LESSEE/FRANCHISEE’s indebtedness to the LESSOR and shall be sufficient \n" +
                        "to enable the Lessor to obtain judgement in any court having jurisdiction in terms hereof. \n" +
                        "Notwithstanding the amount involved, any legal action resulting from or in connection with this \n" +
                        "contract may be instituted in the Magistrates Court, or be referred for arbitration in the sole \n" +
                        "discretion of the lessor to The Arbitration Foundation of South Africa and for the purpose of such \n" +
                        "legal action, the LESSEE/FRANCHISEE chooses domicillium citandi et executandi as the \n" +
                        "address indicated below. \n" +
                        "5.2 The LESSEE/FRANCHISEE hereby indemnifies the LESSOR in respect of all claims by any \n" +
                        "person whatsoever in respect of any injury to persons, and or damage or loss to property caused \n" +
                        "by, or in connection with or arising out of renting the trailer. \n" +
                        "5.3 Further the LESSEE/FRANCHISEE hereby indemnifies the LESSOR in respect of any costs and \n" +
                        "charges connected with the claims of whatsoever nature arising out of the renting of the trailers. \n" +
                        "5.4 The LESSEE/FRANCHISEE consents in terms of Section 45 of Act 32 of 1944, to the LESSOR \n" +
                        "instituting any action or proceeding or enforcing any of the rights under this agreement in the \n" +
                        "Magistrate’s Court or any district having jurisdiction by virtue of section 28 or the said act. The \n" +
                        "LESSEE/FRANCHISEE agrees however, that the LESSOR in its sole and absolute discretion \n" +
                        "may institute any such action or proceedings in any division of the High Court which may have \n" +
                        "jurisdiction. \n" +
                        "5.5 The LESSEE/FRANCHISEE shall not be entitled to cede any of his rights under this agreement \n" +
                        "or to sub-rent or part with possession of the trailer. \n" +
                        "5.6 If the LESSOR institutes any legal proceedings against the LESSEE/FRANCHISEE to enforce \n" +
                        "any of its rights under this agreement it shall be entitled to recover from the Hirer all debt \n" +
                        "collection or legal cost it incurs to its own debt collectors or attorneys in accordance with their \n" +
                        "usual charges and assessed as between attorney/debt collector and own client. \n" +
                        "5.7 The LESSEE/FRANCHISEE chooses the address specified below as his domicilium citandi et \n" +
                        "executandi (i.e. address for service of all legal process) and any notice posted to him there shall \n" +
                        "be deemed to have been received 3 days after it is posted unless he proves the contrary.\n" +
                "5.8 LESSOR domicilium citandi et executandi");
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    interface OnUploadCompleteListener {
        void onComplete(String url);
    }

    private void uploadPictureToFirebase(String directory, View view, String customerId, String rentalId, OnUploadCompleteListener listener) {
        Bitmap bitmap = getBitmapFromView(view);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(directory + "/" + customerId + "_" + rentalId + ".jpg");

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();
            Log.d("Upload", "Upload Success, download URL: " + downloadUrl);
            listener.onComplete(downloadUrl);
        })).addOnFailureListener(e -> {
            Log.d("Upload", "Upload failed");
            e.printStackTrace();
        });
    }

    private void writeDataToFirebase(Rental rental) {
        // Get a reference to the database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get a reference to the "rentals" node.
        DatabaseReference rentalsRef = database.getReference("rentals");

        // Generate a unique id for the rental
        String rentalId = rentalsRef.push().getKey();
        if (rentalId != null) {
            rental.setRentalId(rentalId); // set the unique id to your rental object
            // Write the rental object to the database under the unique id.
            rentalsRef.child(rentalId).setValue(rental);
        } else {
            // Handle error when getting unique key
            Log.e("TAG", "Error getting unique key for rental");
        }
    }


    public String getRentalDateTime() {
        String dateTimeStr = rental.getSelectedDeliveryDateTime();
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = format.parse(dateTimeStr);
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getDeliveryDateTime() {
        // Assuming that you have a method called getDateFromButton that returns a Date object
        // from a Button that contains a date string
        Date date = getDateFromButton(btnSelectDeliveryDateTime);
        // Convert Date to String
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return format.format(date);
    }

    public String getTrailerCondition() {
        // Assuming inputTrailerCondition is a Spinner and the selected item is a String
        return inputTrailerCondition.getSelectedItem().toString();
    }

    public double getEstimatedDistance() {
        // Assuming inputEstimatedDistance is an EditText
        // Convert the string to double
        return Double.parseDouble(inputEstimatedDistance.getText().toString());
    }

    public boolean getOneWayTrip() {
        // Assuming inputOneWayTrip is a CheckBox
        return inputOneWayTrip.isChecked();
    }

    public void resetForm() {
        // Assuming all your inputs are either EditText or Spinner
        // Reset all EditTexts
        inputName.setText("");
        inputSurname.setText("");
        inputIdNumber.setText("");
        inputContactNumber.setText("");
        inputEmail.setText("");
        inputResidentialAddress.setText("");
        inputInvoiceNumber.setText("");
        inputTrailerBarcode.setText("");
        inputEstimatedDistance.setText("");
        inputCurrentLocation.setText("");
        inputTrailerRemarks.setText("");
        // Reset Spinner to default selection
        inputTrailerCondition.setSelection(0);
        inputDeliveryDestination.setSelection(0);
        // Uncheck the CheckBox
        inputOneWayTrip.setChecked(false);
        // Clear Signature Pad
        signaturePad.clear();
        // Implement other resets as necessary
    }

    // Helper function to parse date from Button text
    public Date getDateFromButton(Button button) {
        String dateString = button.getText().toString();
        // Your date string format might be different
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! You can perform your camera related task.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    public static void saveImageToGallery(Context context, Bitmap bitmap, String title) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream outStream = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private File createImageFile(String type) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = type + "_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Return the file path for use with file intents
        return image;
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

}