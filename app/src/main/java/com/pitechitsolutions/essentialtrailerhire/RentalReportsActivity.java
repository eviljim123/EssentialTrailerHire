package com.pitechitsolutions.essentialtrailerhire;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RentalReportsActivity extends AppCompatActivity {
    FirebaseUser user;
    private DatabaseReference mDatabase;
    private String loggedInBranchId;
    private String loggedInBranchName;  // Add this line
    private static final String TAG = "RentalReportsActivity";
    private RentalAdapter mAdapter;
    private List<Rental> rentals = new LinkedList<>();

    private TextView rentalCountTextView;


    private Spinner spinnerMonth;
    private Spinner spinnerYear;

    // The selected filter values
    private int selectedMonth = -1;
    private int selectedYear = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_reports);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Get the current logged in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            loggedInBranchName = currentUser.getEmail().toLowerCase();
        }

        rentalCountTextView = findViewById(R.id.rentalCountTextView);
        // Initialize and populate the month and year dropdowns
        spinnerMonth = findViewById(R.id.monthSpinner);
        spinnerYear = findViewById(R.id.yearSpinner);


        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Set the selected month and year to the current month and year
        Calendar currentCalendar = Calendar.getInstance(); // Get the current date
        selectedMonth = currentCalendar.get(Calendar.MONTH); // Get the current month
        selectedYear = currentCalendar.get(Calendar.YEAR); // Get the current year
        spinnerMonth.setSelection(selectedMonth);
        spinnerYear.setSelection(selectedYear - 2023); // Subtract the starting year (2023 in this case)

        RecyclerView recyclerView = findViewById(R.id.rentalReportsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RentalAdapter(rentals);
        recyclerView.setAdapter(mAdapter);

        // Add listeners for when the selected month or year changes
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position;
                fetchRentalsFromFirebase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = 2023 + position; // Add the starting year (2023 in this case)
                fetchRentalsFromFirebase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Fetch the rentals
        fetchRentalsFromFirebase();
    }


    private void fetchLoggedInBranchIdFromFirebase() {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        loggedInBranchId = sharedPref.getString("loggedInBranchId", null);

        if(loggedInBranchId == null) {
            Log.e(TAG, "No branch ID found. Make sure to save branch ID at login.");
            return;
        }

        Log.i(TAG, "Fetched logged-in branch ID: " + loggedInBranchId);

        // Now fetch the rentals

    }

    private void fetchRentalsFromFirebase() {
        Log.i(TAG, "Fetching rentals for branch Name: " + loggedInBranchName);

        Query rentalsQuery = mDatabase.child("rentals").orderByChild("currentLocation").equalTo(loggedInBranchName);

        rentalsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.i(TAG, "No rentals found.");
                    return;
                }
                rentals.clear();
                int selectedMonthRentals = 0; // Counter for selected month rentals

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd"); // Modify this according to your date format

                for (DataSnapshot rentalSnapshot : snapshot.getChildren()) {
                    Rental rental = null;
                    try {
                        rental = rentalSnapshot.getValue(Rental.class);
                        if (rental != null) {
                            rental.setRentalId(rentalSnapshot.getKey()); // setting the rentalId
                            String customerId = rental.getCustomerId();
                            DatabaseReference customerRef = mDatabase.child("customers").child(customerId);
                            Rental finalRental = rental;
                            customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot customerSnapshot) {
                                    if (customerSnapshot.exists()) {
                                        Customer customer = customerSnapshot.getValue(Customer.class);
                                        if (customer != null) {
                                            finalRental.setCustomerName(customer.getName());
                                            finalRental.setCustomerSurname(customer.getSurname());
                                            finalRental.setCustomerContactNumber(customer.getContactNumber());
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Failed to fetch customer", error.toException());
                                }
                            });

                            try {
                                Date rentalDate = format.parse(rental.getRentalDateTime());
                                Calendar rentalCalendar = Calendar.getInstance();
                                rentalCalendar.setTime(rentalDate);

                                // Check if the rental belongs to the selected month and year
                                if (rentalCalendar.get(Calendar.MONTH) == selectedMonth && rentalCalendar.get(Calendar.YEAR) == selectedYear) {
                                    selectedMonthRentals++;
                                    rentals.add(0, rental);  // Add the rental to the list only if it matches the selected month and year
                                }
                            } catch (ParseException e) {
                                Log.e(TAG, "Failed to parse date: " + rental.getRentalDateTime(), e);
                            }
                        }
                        Log.i(TAG, "Fetched rental with ID: " + (rental != null ? rental.getRentalId() : "null"));
                    } catch (DatabaseException e) {
                        // Log the DatabaseException
                        Log.e(TAG, "Failed to convert snapshot to Rental: " + rentalSnapshot, e);
                    }
                }

                rentalCountTextView.setText("Rental Count for the selected month: " + selectedMonthRentals); // Set the count to the TextView
                mAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
                Log.e(TAG, "Fetching rentals cancelled: ", error.toException());
            }
        });
    }




    public static class Rental {
        private String bookingStatus;
        private String currentLocation;
        private String customerId;
        private String deliveryDestination;
        private String driverLicenseUrl;
        private String invoiceNumber;
        private boolean oneWayTrip;
        private String rentalDateTime;
        private String rentalId;
        private String selectedDeliveryDateTime;
        private String signatureUrl;
        private boolean termsAndConditionsAccepted;
        private String trailerBarcode;
        private String trailerRemarks;
        private String vehicleDiskUrl;

        private String customerName;
        private String customerSurname;
        private String customerContactNumber;

        // Default constructor required for calls to DataSnapshot.getValue(Rental.class)
        public Rental() {
            // This is needed for Firebase
        }

        // Constructor with all properties
        public Rental(String bookingStatus, String currentLocation, String customerId, String deliveryDestination, String driverLicenseUrl, String invoiceNumber, boolean oneWayTrip, String rentalDateTime, String rentalId, String selectedDeliveryDateTime, String signatureUrl, boolean termsAndConditionsAccepted, String trailerBarcode, String trailerRemarks, String vehicleDiskUrl) {
            this.bookingStatus = bookingStatus;
            this.currentLocation = currentLocation;
            this.customerId = customerId;
            this.deliveryDestination = deliveryDestination;
            this.driverLicenseUrl = driverLicenseUrl;
            this.invoiceNumber = invoiceNumber;
            this.oneWayTrip = oneWayTrip;
            this.rentalDateTime = rentalDateTime;
            this.rentalId = rentalId;
            this.selectedDeliveryDateTime = selectedDeliveryDateTime;
            this.signatureUrl = signatureUrl;
            this.termsAndConditionsAccepted = termsAndConditionsAccepted;
            this.trailerBarcode = trailerBarcode;
            this.trailerRemarks = trailerRemarks;
            this.vehicleDiskUrl = vehicleDiskUrl;
        }



        // Getters and Setters
        // Add getters and setters for all the properties

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerSurname() {
            return customerSurname;
        }

        public void setCustomerSurname(String customerSurname) {
            this.customerSurname = customerSurname;
        }

        public String getCustomerContactNumber() {
            return customerContactNumber;
        }

        public void setCustomerContactNumber(String customerContactNumber) {
            this.customerContactNumber = customerContactNumber;
        }
        // Getters
        public String getBookingStatus() {
            return bookingStatus;
        }

        public String getCurrentLocation() {
            return currentLocation;
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getDeliveryDestination() {
            return deliveryDestination;
        }

        public String getDriverLicenseUrl() {
            return driverLicenseUrl;
        }

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public boolean isOneWayTrip() {
            return oneWayTrip;
        }

        public String getRentalDateTime() {
            return rentalDateTime;
        }

        public String getRentalId() {
            return rentalId;
        }

        public String getSelectedDeliveryDateTime() {
            return selectedDeliveryDateTime;
        }

        public String getSignatureUrl() {
            return signatureUrl;
        }

        public boolean isTermsAndConditionsAccepted() {
            return termsAndConditionsAccepted;
        }

        public String getTrailerBarcode() {
            return trailerBarcode;
        }

        public String getTrailerRemarks() {
            return trailerRemarks;
        }

        public String getVehicleDiskUrl() {
            return vehicleDiskUrl;
        }

        // Setters
        public void setBookingStatus(String bookingStatus) {
            this.bookingStatus = bookingStatus;
        }

        public void setCurrentLocation(String currentLocation) {
            this.currentLocation = currentLocation;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public void setDeliveryDestination(String deliveryDestination) {
            this.deliveryDestination = deliveryDestination;
        }

        public void setDriverLicenseUrl(String driverLicenseUrl) {
            this.driverLicenseUrl = driverLicenseUrl;
        }

        public void setInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }

        public void setOneWayTrip(boolean oneWayTrip) {
            this.oneWayTrip = oneWayTrip;
        }

        public void setRentalDateTime(String rentalDateTime) {
            this.rentalDateTime = rentalDateTime;
        }

        public void setRentalId(String rentalId) {
            this.rentalId = rentalId;
        }

        public void setSelectedDeliveryDateTime(String selectedDeliveryDateTime) {
            this.selectedDeliveryDateTime = selectedDeliveryDateTime;
        }

        public void setSignatureUrl(String signatureUrl) {
            this.signatureUrl = signatureUrl;
        }

        public void setTermsAndConditionsAccepted(boolean termsAndConditionsAccepted) {
            this.termsAndConditionsAccepted = termsAndConditionsAccepted;
        }

        public void setTrailerBarcode(String trailerBarcode) {
            this.trailerBarcode = trailerBarcode;
        }

        public void setTrailerRemarks(String trailerRemarks) {
            this.trailerRemarks = trailerRemarks;
        }

        public void setVehicleDiskUrl(String vehicleDiskUrl) {
            this.vehicleDiskUrl = vehicleDiskUrl;
        }

    }

    class RentalAdapter extends RecyclerView.Adapter<RentalAdapter.RentalViewHolder> {

        private List<Rental> rentalList;

        class RentalViewHolder extends RecyclerView.ViewHolder {

            TextView clientNameView, trailerBarcodeView, invoiceNumberView, bookingTimeView, returnTimeView;

            RentalViewHolder(View itemView) {
                super(itemView);
                clientNameView = itemView.findViewById(R.id.clientName);
                trailerBarcodeView = itemView.findViewById(R.id.trailerBarcode);
                invoiceNumberView = itemView.findViewById(R.id.invoiceNumber);
                bookingTimeView = itemView.findViewById(R.id.bookingTime);
                returnTimeView = itemView.findViewById(R.id.returnTime);
            }
        }

        RentalAdapter(List<Rental> rentalList) {
            this.rentalList = rentalList;
        }

        @NonNull
        @Override
        public RentalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rental_report_item, parent, false);
            return new RentalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RentalViewHolder holder, int position) {
            Rental rental = rentalList.get(position);
            if (rental != null) {
                String customerInfo = "Client Name: " + rental.getCustomerName() + " " + rental.getCustomerSurname() + "\nContact Number: " + rental.getCustomerContactNumber();
                holder.clientNameView.setText(customerInfo);
                holder.trailerBarcodeView.setText(rental.getTrailerBarcode() != null ? "Trailer QR Code: " + rental.getTrailerBarcode() : "");
                holder.invoiceNumberView.setText(rental.getInvoiceNumber() != null ? "Invoice Number: " + rental.getInvoiceNumber() : "");
                holder.bookingTimeView.setText(rental.getRentalDateTime() != null ? "Booking Time: " + rental.getRentalDateTime() : "");
                holder.returnTimeView.setText(rental.getSelectedDeliveryDateTime() != null ? "Return Time: " + rental.getSelectedDeliveryDateTime() : "");
            }
        }


        @Override
        public int getItemCount() {
            return rentalList.size();
        }
    }
}