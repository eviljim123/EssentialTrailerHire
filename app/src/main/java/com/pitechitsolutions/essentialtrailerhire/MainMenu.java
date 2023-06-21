package com.pitechitsolutions.essentialtrailerhire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    private Button trailerBookingButton, receiveTrailerButton, incomingTrailersButton, bookingReportsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        trailerBookingButton = findViewById(R.id.trailer_booking_button);
        receiveTrailerButton = findViewById(R.id.receive_trailer_button);
        incomingTrailersButton = findViewById(R.id.incoming_trailers_button);
        bookingReportsButton = findViewById(R.id.booking_reports_button);

        trailerBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, TrailerRentalActivity.class);
                startActivity(intent);
            }
        });

        receiveTrailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ReceiveTrailerActivity.class);
                startActivity(intent);
            }
        });

        incomingTrailersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, IncomingTrailersActivity.class);
                startActivity(intent);
            }
        });

        bookingReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, RentalReportsActivity.class);
                startActivity(intent);
            }
        });
    }
}