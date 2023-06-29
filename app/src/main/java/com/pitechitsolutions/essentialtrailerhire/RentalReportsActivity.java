package com.pitechitsolutions.essentialtrailerhire;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


        RecyclerView recyclerView = findViewById(R.id.rentalReportsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RentalAdapter(rentals);
        recyclerView.setAdapter(mAdapter);

        // First fetch the logged-in branch ID
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
        // Log the value of loggedInBranchId


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
                int counter = 0; // Counter to manually limit the results
                for (DataSnapshot rentalSnapshot : snapshot.getChildren()) {
                    if (counter >= 10) break; // Stop adding after 10 rentals
                    try {
                        Rental rental = rentalSnapshot.getValue(Rental.class);
                        if (rental != null) {
                            rental.setRentalId(rentalSnapshot.getKey()); // setting the rentalId

                            // Fetch customer details
                            String customerId = rental.getCustomerId();
                            DatabaseReference customerRef = mDatabase.child("customers").child(customerId);
                            customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot customerSnapshot) {
                                    if (customerSnapshot.exists()) {
                                        Customer customer = customerSnapshot.getValue(Customer.class);
                                        if (customer != null) {
                                            rental.setCustomerName(customer.getName());
                                            rental.setCustomerSurname(customer.getSurname());
                                            rental.setCustomerContactNumber(customer.getContactNumber());
                                            mAdapter.notifyDataSetChanged();  // Notify the adapter of the data change here
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Failed to fetch customer", error.toException());
                                }
                            });

                        }
                        // Log the value of rentalId
                        Log.i(TAG, "Fetched rental with ID: " + (rental != null ? rental.getRentalId() : "null"));
                        if (rental != null) {
                            rentals.add(0, rental);
                        }
                        counter++;
                    } catch (DatabaseException e) {
                        // Log the DatabaseException
                        Log.e(TAG, "Failed to convert snapshot to Rental: " + rentalSnapshot, e);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                holder.trailerBarcodeView.setText(rental.getTrailerBarcode() != null ? "Trailer Barcode: " + rental.getTrailerBarcode() : "");
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