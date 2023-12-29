package com.example.readingloops;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForogtPasswordActivity extends AppCompatActivity {


    Button btnBack;
    Button resetBtn;
    EditText editEmail;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    String strEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forogt_password);


        btnBack = findViewById(R.id.backBtn_forgetPass);
        resetBtn = findViewById(R.id.resetPass);
        editEmail = findViewById(R.id.email_reset_edittext);
        progressBar = findViewById(R.id.progressBarForgotPass);

        mAuth = FirebaseAuth.getInstance();


        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strEmail = editEmail.getText().toString().trim();

                if (!TextUtils.isEmpty(strEmail) && android.util.Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
                    ResetPassword();
                } else {
                    editEmail.setError("Please enter a valid email address");
                }

            }
        });




        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForogtPasswordActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });





    }



    private void ResetPassword() {
        progressBar.setVisibility(View.VISIBLE);
        resetBtn.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(strEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ForogtPasswordActivity.this, "Reset link for password has been sent", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForogtPasswordActivity.this, MainActivity.class);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForogtPasswordActivity.this, "Error", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                resetBtn.setVisibility(View.VISIBLE);
            }
        });
    }



}