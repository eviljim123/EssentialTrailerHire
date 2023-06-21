package com.pitechitsolutions.essentialtrailerhire;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    EditText inputAdminPassword;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputAdminPassword = findViewById(R.id.adminPassword);
        btnRegister = findViewById(R.id.register);

        auth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.username);
        inputPassword = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.login);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminPassword = inputAdminPassword.getText().toString();

                if (adminPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter admin password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!"As15011994".equals(adminPassword)) {  // replace "YourAdminPassword" with the actual admin password
                    Toast.makeText(getApplicationContext(), "Invalid admin password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proceed to the registration screen
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, task -> {
                            if (!task.isSuccessful()) {
                                // There was an error
                                try {
                                    throw task.getException();
                                }
                                // if user enters wrong email.
                                catch (FirebaseAuthInvalidUserException invalidEmail) {
                                    inputEmail.setError("Invalid Email");
                                    inputEmail.requestFocus();
                                }
                                // if user enters wrong password.
                                catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                    inputPassword.setError("Incorrect Password");
                                    inputPassword.requestFocus();
                                }
                                catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                FirebaseUser user = auth.getCurrentUser();
                                // You can perform other operations with the authenticated user here.
                                Toast.makeText(MainActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();

                                // Add this line to start MainMenuActivity after a successful login
                                startActivity(new Intent(MainActivity.this, MainMenu.class));
                            }
                        });

            }
        });
    }
}
