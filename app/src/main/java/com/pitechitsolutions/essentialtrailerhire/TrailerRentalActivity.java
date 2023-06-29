package com.pitechitsolutions.essentialtrailerhire;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.gcacace.signaturepad.views.SignaturePad;
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

    // Declare currentBranchId as a global variable
    private String currentBranchId;
    private StorageReference signatureCaptureRef;
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
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // Declare two different request codes for camera intents
    private static final int REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE = 1;
    private static final int REQUEST_IMAGE_CAPTURE_VEHICLE_DISK = 2;
    private Button scanQrCodeButton;

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

    private Bitmap driversLicenseBitmap;
    private Bitmap vehicleDiskBitmap;
    private Bitmap signatureBitmap;


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
        licenseCaptureRef = storage.getReference().child("Disk Captures");
        vehicleCaptureRef = storage.getReference().child("Licence Captures");
        signatureCaptureRef = storage.getReference().child("Signature Captures");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentBranchId = currentUser.getUid();

            DatabaseReference branchDbReference = FirebaseDatabase.getInstance().getReference("branches");
            branchDbReference.child(currentBranchId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String branchName = dataSnapshot.child("branchName").getValue(String.class);
                    Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    calculateAndSetDistance();
                    if (branchName != null && latitude != null && longitude != null) {
                        EditText currentLocation = findViewById(R.id.current_location);
                        currentLocation.setText(branchName);
                        currentBranchLatitude.value = latitude;
                        currentBranchLongitude.value = longitude;
                    } else {
                        // Log a message or handle the case where branch name or latitude or longitude is null
                        Log.e("FirebaseData", "Branch with id " + currentBranchId + " has null name or coordinates.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Log the error message
                    Log.e("FirebaseData", "Error fetching data", databaseError.toException());
                }
            });
        }
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
        String[] conditions = new String[]{"Great", "Good", "Average", "Bad", "Inoperable"};
        ArrayAdapter<String> trailerConditionAdapter = new ArrayAdapter<>(TrailerRentalActivity.this,
                android.R.layout.simple_spinner_dropdown_item, conditions);
        inputTrailerCondition.setAdapter(trailerConditionAdapter);
        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(TrailerRentalActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scan a barcode");
                integrator.setCameraId(0);  // Use a specific camera of the device
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
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

                // Instead of using the Uid from auth, use inputIdNumber as the customerId
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

                // Create a query to find the trailer by its barcode
                Query trailerQuery = mDatabase.child("trailers").orderByChild("barcode").equalTo(inputTrailerBarcodeStr);

                trailerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // dataSnapshot is the "snapshot" of all trailers with the matching barcode
                            for (DataSnapshot trailer : dataSnapshot.getChildren()) {
                                Trailer trailerInfo = trailer.getValue(Trailer.class);
                                if (trailerInfo != null) {
                                    // Check if the trailer is "In Transit" before proceeding
                                    if (!trailerInfo.getStatus().equals("Idle")) {
                                        Toast.makeText(TrailerRentalActivity.this, "The trailer is not available for rental", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // Set the trailer status to "In Transit"
                                    trailerInfo.setStatus("In Transit");
                                    trailer.getRef().setValue(trailerInfo);
                                    // Now you can continue with your operation
                                    // Upload the license photo
                                    uploadPictureToFirebase("Licence Captures", driversLicenseImageView, customerId, rentalId, new OnUploadCompleteListener() {
                                        @Override
                                        public void onComplete(String licenseUrl) {
                                            // Upload the vehicle disk photo
                                            uploadPictureToFirebase("Disk Captures", vehicleDiskImageView, customerId, rentalId, new OnUploadCompleteListener() {
                                                @Override
                                                public void onComplete(String vehicleDiskUrl) {
                                                    // Upload the signature
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
                                                            rental.setDeliveryDestination(inputDeliveryDestination.getSelectedItem().toString().trim());
                                                            rental.setTrailerRemarks(inputTrailerRemarks.getText().toString().trim());


                                                            // Set rentalDateTime and selectedDeliveryDateTime directly from TextView
                                                            TextView tvRentalDateTime = findViewById(R.id.tvRentalDateTime);
                                                            rental.setRentalDateTime(tvRentalDateTime.getText().toString());

                                                            TextView tvDeliveryDateTime = findViewById(R.id.tvDeliveryDateTime);
                                                            rental.setSelectedDeliveryDateTime(tvDeliveryDateTime.getText().toString());

                                                            // Inside the ValueEventListener for the trailerQuery
                                                            // Create an IncomingTrailer instance
                                                            IncomingTrailer incomingTrailer = new IncomingTrailer();

                                                            incomingTrailer.setBranchID(inputCurrentLocation.getText().toString().trim()); // Assuming the current location EditText holds the branch ID
                                                            incomingTrailer.setDeliveryBranch(inputDeliveryDestination.getSelectedItem().toString().trim()); // Assuming the delivery destination Spinner holds the delivery branch ID
                                                            incomingTrailer.setEstimatedArrivalDateTime(tvDeliveryDateTime.getText().toString()); // Assuming this TextView holds the delivery date and time
                                                            incomingTrailer.setOriginBranch(inputCurrentLocation.getText().toString().trim()); // Assuming the current location EditText holds the origin branch ID
                                                            incomingTrailer.setStatus("In Transit");
                                                            incomingTrailer.setTrailerId(inputTrailerBarcodeStr); // Assuming this is the trailer ID

                                                            // Add the incoming trailer to Firebase
                                                            mDatabase.child("incomingTrailers").child(inputTrailerBarcodeStr).setValue(incomingTrailer);


                                                            // Check state of the checkboxes and set the corresponding values
                                                            boolean oneWayTrip = oneWayCheckBox.isChecked();
                                                            rental.setOneWayTrip(oneWayTrip);

                                                            boolean termsAndConditionsAccepted = inputTermsAndConditions.isChecked();
                                                            rental.setTermsAndConditionsAccepted(termsAndConditionsAccepted);


                                                            // Add logic to update condition and estimatedDistance in Trailers and oneWayTrip before saving it to firebase.
                                                            trailerInfo.setCondition(getTrailerCondition());
                                                            double newEstimatedDistance = getEstimatedDistance();
                                                            double currentEstimatedDistance = trailerInfo.getEstimatedDistance();
                                                            trailerInfo.setEstimatedDistance(currentEstimatedDistance + newEstimatedDistance);
                                                            trailerInfo.setOneWayTrip(getOneWayTrip());
                                                            trailer.getRef().setValue(trailerInfo);

                                                            // Add the rental to Firebase
                                                            mDatabase.child("rentals").child(rentalId).setValue(rental);

                                                            // Add notification after rental successfully added
                                                            Toast.makeText(TrailerRentalActivity.this, "Trailer successfully rented.", Toast.LENGTH_SHORT).show();

                                                            // Reset the form
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
                            Toast.makeText(TrailerRentalActivity.this, "The trailer does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Use a switch statement to handle various error codes
                        switch (databaseError.getCode()) {
                            case DatabaseError.DISCONNECTED:
                                Toast.makeText(TrailerRentalActivity.this, "Network disconnected!", Toast.LENGTH_SHORT).show();
                                break;
                            case DatabaseError.NETWORK_ERROR:
                                Toast.makeText(TrailerRentalActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
                                break;
                            case DatabaseError.PERMISSION_DENIED:
                                Toast.makeText(TrailerRentalActivity.this, "You don't have permission to read/write this data", Toast.LENGTH_SHORT).show();
                                break;
                            case DatabaseError.OPERATION_FAILED:
                                Toast.makeText(TrailerRentalActivity.this, "The server failed to process the operation", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                // Default case for other errors
                                Toast.makeText(TrailerRentalActivity.this, "Unknown error occurred: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                break;
                        }

                        Log.e(TAG, "Error occurred: " + databaseError.getMessage());
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
                Log.e("Scan", "Scanned Barcode: " + scannedBarcode);
                Log.d("Barcode", "Scanned barcode: " + scannedBarcode);

                inputTrailerBarcode.setText(scannedBarcode);
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("trailers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("Firebase", "Data: " + dataSnapshot.toString());
                        Trailer scannedTrailer = null;
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
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_DRIVERS_LICENSE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            driversLicenseBitmap = (Bitmap) extras.get("data");
            driversLicenseImageView.setImageBitmap(driversLicenseBitmap);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_VEHICLE_DISK && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            vehicleDiskBitmap = (Bitmap) extras.get("data");
            vehicleDiskImageView.setImageBitmap(vehicleDiskBitmap);
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

        return roundedDistance;
    }

    private void showTermsAndConditions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Terms and Conditions");
        builder.setMessage("1)\tRIGHT OF ADMISSION RESERVED\n" + "2)\tA deposit as stated in the application for trailer rental is payable on the day of collection [A full refund will be paid, if the trailer is returned undamaged]\n" + "3)\tSTRICTLY NO REFUNDS ON BOOKINGS OR PAYMENTS MADE\n" + "4)\tFull amount due to be paid on or before the day of rental [REGRET! NO CHEQUES].\n" + "5)\tIf vehicle is returned later than the agreed upon date and time, the client could be held liable for extra days rent. Is subject to the discretion of the owners of ESSENTIAL TRAILER HIRE].\n" + "6)\tAll vehicles will be inspected with the client BEFORE rental and AFTER return.\n" + "7)\tClient will be responsible for all damages to trailer whilst in his/her possession, including\n" + "tyre damage and burst tyres.\n" + "8)\tIf spare wheel is taken, client will be held responsible for damages or loss (1500 OR replace rim and tyre)\n" + "9)\tESSENTIAL TRAILER HIRE will not be held responsible for any injuries sustained during the use of the vehicle. 0] Tyre pressures to be checked by client.\n" + "10)\tTrailers not to be sublet by client under any circumstances.\n" + "11)\tAfter hours call out fee of R200.00 applies.\n" + "12)\tEssential Trailer Hire, the owners, the employees and affiliates do not accept any responsibility for any damage caused to any person or institution arising from this trailer rental.\n" + "13)\tESSENTIAL TRAILER HIRE are hereby exempt and indemnified from any possible actions taken against them.\n" + "14)\tNO INSURANCE ON TRAILERS OR CONTENTS OR THIRD PARTY INSURANCE.\n" + "15)\tBOOKINGS PAYABLE STRICTLY IN ADVANCE.\n" + "16)\tLivestock trailers to be cleaned before returning (to be returned in the same condition as it was taken), otherwise R100.00 cleaning fee applies.\n" + "17)\tWheel spanners, ropes and canvas covers - not included in hire agreement.\n" + "18)\tCustomer to ensure trailer handbrake in off position before traveling.\n" + "19)\tNo VAT applicable.\n" + "20)\tAny traffic transgression, during the hire period will not be for ESSENTIAL TRAILER HIRE\n");
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
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

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

}