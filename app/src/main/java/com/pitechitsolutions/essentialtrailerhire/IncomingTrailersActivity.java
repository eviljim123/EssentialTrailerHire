package com.pitechitsolutions.essentialtrailerhire;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class IncomingTrailersActivity extends AppCompatActivity {

    private ListView lvIncomingTrailers;
    private DatabaseReference databaseReference;
    private List<Trailer> trailers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_trailers);

        lvIncomingTrailers = findViewById(R.id.lv_incoming_trailers);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // List to store incoming trailers
        trailers = new ArrayList<>();

        // Getting the branch id of the currently logged in user
        String branchId = ""; // replace this with the actual branch id of the logged in user

        // Add ValueEventListener to listen for changes in the database
        databaseReference.child("branches").child(branchId).child("rentals").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the list before adding any data, so that the old data is not appended
                trailers.clear();
                for (DataSnapshot trailerSnapshot : dataSnapshot.getChildren()) {
                    Trailer trailer = trailerSnapshot.getValue(Trailer.class);

                    if (trailer != null && "In Transit".equals(trailer.getStatus()) && branchId.equals(trailer.getDeliveryDestination())) {
                        trailers.add(trailer);
                    }
                }

                // check if the trailer list is empty
                if (!trailers.isEmpty()) {
                    ArrayAdapter<Trailer> adapter = new ArrayAdapter<>(IncomingTrailersActivity.this, android.R.layout.simple_list_item_1, trailers);
                    lvIncomingTrailers.setAdapter(adapter);
                } else {
                    // Display a message if there are no incoming trailers
                    Toast.makeText(IncomingTrailersActivity.this, "No Incoming Trailers", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Display the error message to the user
                Toast.makeText(IncomingTrailersActivity.this, "Error loading data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
