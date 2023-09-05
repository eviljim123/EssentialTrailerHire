package com.pitechitsolutions.essentialtrailerhire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class rentalLeaderBoard extends AppCompatActivity {

    private Set<String> excludedBranches = new HashSet<>(Arrays.asList(
            "essentialtestbranch2@gmail.com",
            "newtestbranchemail@essentialtrailerhire.com",
            "essentialtestbranch1@gmail.com",
            "essentialjbay@essentialtrailerhire.co.za",
            "essentialtestbranch3@gmail.com"
    ));

    private RecyclerView leaderBoardRecyclerView;
    private HashMap<String, Integer> branchCountMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_leader_board);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference outstandingRentalsRef = database.getReference("outstandingRentals");
        DatabaseReference rentalsRef = database.getReference("rentals");


        // Initialize RecyclerView
        leaderBoardRecyclerView = findViewById(R.id.leaderBoardRecyclerView);  // Replace with actual RecyclerView ID
        leaderBoardRecyclerView.setLayoutManager(new LinearLayoutManager(this));  // Important!

        // For outstanding rentals
        outstandingRentalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    OutstandingRental rental = postSnapshot.getValue(OutstandingRental.class);
                    if (rental != null) { // Null check
                        String branch = rental.getDeliveryDestination();
                        if (branch != null && !excludedBranches.contains(branch)) { // Null check and exclusion check
                            branchCountMap.put(branch, branchCountMap.getOrDefault(branch, 0) + 1);
                        }
                    }
                    Log.d("Debug", "Outstanding Rentals Updated"); // Added Debug
                }
                updateLeaderBoard();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(rentalLeaderBoard.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });


        rentalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Rental rental = postSnapshot.getValue(Rental.class);
                    if (rental != null) {  // Null check
                        String branch = rental.getDeliveryDestination();

                        // Check if this is a valid rental based on presence of required fields
                        if (branch != null && !excludedBranches.contains(branch) && rental.getRentalDateTime() != null && rental.getSelectedDeliveryDateTime() != null) {
                            // Calculate the number of days the trailer has been rented for
                            int numberOfDays = calculateNumberOfDays(rental.getRentalDateTime(), rental.getSelectedDeliveryDateTime());

                            // Update the count
                            branchCountMap.put(branch, branchCountMap.getOrDefault(branch, 0) + numberOfDays);
                            Log.d("Debug", "Branch: " + branch + ", Count: " + branchCountMap.getOrDefault(branch, 0));

                        }
                    }
                }
                updateLeaderBoard();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(rentalLeaderBoard.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


        public void updateLeaderBoard() {
        // Sort the branches by count
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(branchCountMap.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Collections.reverseOrder()));

        // Now 'sortedEntries' is sorted by rental counts, update your UI here
        // For example, you can populate a RecyclerView to display the sorted leaderboard
        LeaderBoardAdapter adapter = new LeaderBoardAdapter(sortedEntries);
        leaderBoardRecyclerView.setAdapter(adapter);
    }

        public int calculateNumberOfDays(String startDateTime, String endDateTime) {
            // You can change the date format according to the one you are using
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date startDate = sdf.parse(startDateTime);
                Date endDate = sdf.parse(endDateTime);

                long diff = endDate.getTime() - startDate.getTime();
                return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            } catch (ParseException e) {
                e.printStackTrace();
                return 0; // return 0 if there is an error
            }
        }

    }