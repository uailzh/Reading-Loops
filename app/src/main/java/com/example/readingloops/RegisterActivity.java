package com.example.readingloops;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        etEmail = findViewById(R.id.email_reg);
        etUsername = findViewById(R.id.name_reg);
        etPassword = findViewById(R.id.password_reg);
        etConfirmPassword = findViewById(R.id.confirm);

        MaterialButton gobackBtn = (MaterialButton) findViewById(R.id.backbtn_reg);
        btnRegister = findViewById(R.id.registerbtn_reg);



        gobackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user_email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPass = etConfirmPassword.getText().toString().trim();
                String user = etUsername.getText().toString().trim();



                if(user_email.isEmpty()) {
                    etEmail.setError("The email can not be empty");
                }
                if(password.isEmpty()) {
                    etPassword.setError("The password can not be empty");
                }
                if(user.isEmpty())
                {
                    etUsername.setError("The user name can not be empty");
                }
                if(!confirmPass.equals(password)) {
                    etPassword.setError("The passwords do not match");
                }
                else if (!isValidEmail(user_email)) {
                    etEmail.setError("Please enter a valid email address");
                }
                else {
                    auth.createUserWithEmailAndPassword(user_email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Store the username in the Firebase Realtime Database
                                FirebaseUser currentUser = auth.getCurrentUser();
                                assert currentUser != null;
                                String userId = currentUser.getUid();
                                databaseReference.child("users").child(userId).child("usernamesignup").setValue(user);

                                Toast.makeText(RegisterActivity.this, "Sign Up successful", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(RegisterActivity.this, HomePageActivity.class);

                                startActivity(intent);
                            } else {
                                Toast.makeText(RegisterActivity.this, "Sign Up failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            // Function to validate email using a regular expression
            private boolean isValidEmail(String email) {
                return Patterns.EMAIL_ADDRESS.matcher(email).matches();
            }
        });


    }
}
