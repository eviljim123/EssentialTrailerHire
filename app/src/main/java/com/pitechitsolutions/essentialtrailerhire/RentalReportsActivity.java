package com.pitechitsolutions.essentialtrailerhire;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class RentalReportsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String loggedInBranchId = "branchID1"; // TODO: replace this with actual logged-in branch ID.
    private static final String TAG = "RentalReportsActivity";
    private RentalAdapter mAdapter;
    private List<Rental> rentals = new LinkedList<>(); // Use LinkedList for efficient add to start of list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_reports);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = findViewById(R.id.rentalReportsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RentalAdapter(rentals);
        recyclerView.setAdapter(mAdapter);

        // First fetch the logged-in branch ID
        fetchLoggedInBranchIdFromFirebase();
    }


    private void fetchLoggedInBranchIdFromFirebase() {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        loggedInBranchId = sharedPref.getString("loggedInBranchId", null);

        if(loggedInBranchId == null) {
            Log.e(TAG, "No branch ID found. Make sure to save branch ID at login.");
            return;
        }

        // Now fetch the rentals
        fetchRentalsFromFirebase();
    }

    private void fetchRentalsFromFirebase() {
        mDatabase.child("branches").child(loggedInBranchId).child("rentals")
                .orderByKey().limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        rentals.clear();
                        for (DataSnapshot rentalSnapshot : snapshot.getChildren()) {
                            try {
                                Rental rental = rentalSnapshot.getValue(Rental.class);
                                rentals.add(0, rental);
                            } catch (DatabaseException e) {
                                Log.e(TAG, "Failed to convert snapshot to Rental", e);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch rentals", error.toException());
                    }
                });
    }

    class Rental {
        private String clientName, trailerBarcode, invoiceNumber, bookingTime, returnTime;

        public Rental() {
            // Default constructor required for calls to DataSnapshot.getValue(Rental.class)
        }

        public Rental(String clientName, String trailerBarcode, String invoiceNumber, String bookingTime, String returnTime) {
            this.clientName = clientName;
            this.trailerBarcode = trailerBarcode;
            this.invoiceNumber = invoiceNumber;
            this.bookingTime = bookingTime;
            this.returnTime = returnTime;
        }

        // Getters and Setters
        // ... add getters and setters for all fields here ...

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public String getTrailerBarcode() {
            return trailerBarcode;
        }

        public void setTrailerBarcode(String trailerBarcode) {
            this.trailerBarcode = trailerBarcode;
        }

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }

        public String getBookingTime() {
            return bookingTime;
        }

        public void setBookingTime(String bookingTime) {
            this.bookingTime = bookingTime;
        }

        public String getReturnTime() {
            return returnTime;
        }

        public void setReturnTime(String returnTime) {
            this.returnTime = returnTime;
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
            holder.clientNameView.setText(rental.getClientName());
            holder.trailerBarcodeView.setText(rental.getTrailerBarcode());
            holder.invoiceNumberView.setText(rental.getInvoiceNumber());
            holder.bookingTimeView.setText(rental.getBookingTime());
            holder.returnTimeView.setText(rental.getReturnTime());
        }

        @Override
        public int getItemCount() {
            return rentalList.size();
        }
    }
}
