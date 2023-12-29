package com.example.readingloops;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText user_email, user_pass;

    private TextView forgetPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user_email = findViewById(R.id.username);
        user_pass = findViewById(R.id.password);
        forgetPass = findViewById(R.id.forgotPass);


        MaterialButton loginBtn = (MaterialButton) findViewById(R.id.loginbtn);
        MaterialButton registerBtn = (MaterialButton) findViewById(R.id.registerbtn);


        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForogtPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the new activity you want to open
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);

                // Start the new activity
                startActivity(intent);
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = user_email.getText().toString();
                String password = user_pass.getText().toString();

                if (email.isEmpty()) {
                    user_email.setError("Email cannot be empty");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    user_email.setError("Invalid email format");
                } else if (password.isEmpty()) {
                    user_pass.setError("Password cannot be empty");
                } else {
                    // Attempt Firebase Authentication here
                    auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                // Handle case where the user does not exist.
                                Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Handle case where the password is incorrect.
                                Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle other exceptions.
                                Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}