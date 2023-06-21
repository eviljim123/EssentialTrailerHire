package com.pitechitsolutions.essentialtrailerhire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase; // add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // initialize it here

        inputEmail = findViewById(R.id.branchEmail);
        inputPassword = findViewById(R.id.branchPassword);
        Button btnRegisterBranch = findViewById(R.id.btnRegisterBranch);

        btnRegisterBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter branch email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Registration failed!" + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                String userId = task.getResult().getUser().getUid();
                                Branch branch = new Branch(email, password, "Branch Coordinates Here");
                                mDatabase.child("branches").child(userId).setValue(branch);
                                startActivity(new Intent(RegisterActivity.this, MainMenu.class));
                                finish();
                            }
                        });
            }
        });
    }
}
