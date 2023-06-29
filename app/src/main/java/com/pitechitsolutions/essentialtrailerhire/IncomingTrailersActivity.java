package com.pitechitsolutions.essentialtrailerhire;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private List<IncomingTrailer> trailers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_trailers);

        lvIncomingTrailers = findViewById(R.id.lv_incoming_trailers);
        trailers = new ArrayList<>();

        final IncomingTrailerAdapter adapter = new IncomingTrailerAdapter(IncomingTrailersActivity.this, R.layout.list_item_incoming_trailer, trailers);
        lvIncomingTrailers.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String branchId = user != null ? user.getEmail() : null;


        databaseReference.child("incomingTrailers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trailers.clear();
                for (DataSnapshot trailerSnapshot : dataSnapshot.getChildren()) {
                    IncomingTrailer trailer = trailerSnapshot.getValue(IncomingTrailer.class);

                    if (trailer != null && "In Transit".equals(trailer.getStatus()) && branchId.equals(trailer.getDeliveryBranch())) {
                        trailers.add(trailer);
                    }
                }

                if (!trailers.isEmpty()) {
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(IncomingTrailersActivity.this, "No Incoming Trailers", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(IncomingTrailersActivity.this, "Error loading data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
