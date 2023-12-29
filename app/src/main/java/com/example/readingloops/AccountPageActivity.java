package com.example.readingloops;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.readingloops.databinding.AccountPageBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class AccountPageActivity extends DrawerBaseActivity {

    AccountPageBinding accountPageBinding;
    ImageView accImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountPageBinding = AccountPageBinding.inflate(getLayoutInflater());

        setContentView(accountPageBinding.getRoot());
        allocateActivityTitle("Account");

        accImg = findViewById(R.id.accImg);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);


        String userEmail = currentUser.getEmail();
        TextView headerEmail = findViewById(R.id.userEmailAcc);


        headerEmail.setText(userEmail);

        // Retrieve the profile picture URL from Firebase Storage
        StorageReference profilePicRef = getCurrentProfilePicStorageRef();
        profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image into the ImageView using Glide
            Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(accImg);
        }).addOnFailureListener(e -> {
            // Handle any errors that may occur while fetching the image
            Log.e("AccountPageActivity", "Error loading profile picture: " + e.getMessage());
        });

        // Other data retrieval from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User data exists in the database
                    String username = dataSnapshot.child("usernamesignup").getValue(String.class);
                    accountPageBinding.userNameAcc.setText(username);
                } else {
                    Log.d("AccountPageActivity", "User data does not exist in the database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.e("AccountPageActivity", "Error retrieving user data: " + databaseError.getMessage());
            }
        });



        // Set click listener for changing password
        accountPageBinding.changeP.setOnClickListener(view -> {
            // Display an input window (AlertDialog) to get the new password
            showPasswordChangeDialog();
        });

        // Set click listener for changing username
        accountPageBinding.changeU.setOnClickListener(view -> {
            // Display an input window (AlertDialog) to get the new username
            showUsernameChangeDialog();
        });

        // Set click listener for changing email
        accountPageBinding.changeE.setOnClickListener(view -> {
            // Display an input window (AlertDialog) to get the new email
            showEmailChangeDialog();
        });




    }


    private void showPasswordChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_ReadingLoops);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);

        final EditText newPasswordInput = view.findViewById(R.id.newPasswordInput);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        builder.setView(view);
        builder.setCancelable(true);

        confirmButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                showReauthenticationDialog(newPassword, "password");
            } else {
                Toast.makeText(AccountPageActivity.this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void showUsernameChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_ReadingLoops);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_username, null);

        final EditText newUsernameInput = view.findViewById(R.id.newUsernameInput);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        builder.setView(view);
        builder.setCancelable(true);

        confirmButton.setOnClickListener(v -> {
            String newUsername = newUsernameInput.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                showReauthenticationDialog(newUsername, "username");
            } else {
                Toast.makeText(AccountPageActivity.this, "Please enter a new username", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void showEmailChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_ReadingLoops);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_email, null);

        final EditText newEmailInput = view.findViewById(R.id.newEmailInput);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        builder.setView(view);
        builder.setCancelable(true);

        confirmButton.setOnClickListener(v -> {
            String newEmail = newEmailInput.getText().toString().trim();
            if (!newEmail.isEmpty()) {
                showReauthenticationDialog(newEmail, "email");
            } else {
                Toast.makeText(AccountPageActivity.this, "Please enter a new email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }



    private void showReauthenticationDialog(final String newData, final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reauthenticate");

        // Set up the input
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Enter your password");
        builder.setView(passwordInput);

        // Set up the buttons
        builder.setPositiveButton("Reauthenticate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = passwordInput.getText().toString();
                if (!enteredPassword.isEmpty()) {
                    // Perform reauthentication based on the type (password, username, or email)
                    if (type.equals("password")) {
                        reauthenticateForPasswordChange(enteredPassword, newData);
                    } else if (type.equals("username")) {
                        reauthenticateForUsernameChange(enteredPassword, newData);
                    } else if (type.equals("email")) {
                        reauthenticateForEmailChange(enteredPassword, newData);
                    }
                } else {
                    Toast.makeText(AccountPageActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void reauthenticateForPasswordChange(String enteredPassword, String newPassword) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            // Reauthentication successful, now update the password
                            updatePassword(newPassword);
                        } else {
                            // Reauthentication failed
                            Toast.makeText(AccountPageActivity.this, "Reauthentication failed", Toast.LENGTH_SHORT).show();
                            Log.e("AccountPageActivity", "Reauthentication failed: " + reauthTask.getException().getMessage());
                        }
                    });
        }
    }

    private void reauthenticateForUsernameChange(String enteredPassword, String newUsername) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            // Reauthentication successful, now update the username
                            updateUsername(newUsername);
                        } else {
                            // Reauthentication failed
                            Toast.makeText(AccountPageActivity.this, "Reauthentication failed", Toast.LENGTH_SHORT).show();
                            Log.e("AccountPageActivity", "Reauthentication failed: " + reauthTask.getException().getMessage());
                        }
                    });
        }
    }

    private void reauthenticateForEmailChange(String enteredPassword, final String newEmail) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
            currentUser.reauthenticateAndRetrieveData(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            // Reauthentication successful, now update the email
                            updateEmail(newEmail);
                        } else {
                            // Reauthentication failed
                            Toast.makeText(AccountPageActivity.this, "Reauthentication failed", Toast.LENGTH_SHORT).show();
                            Log.e("AccountPageActivity", "Reauthentication failed: " + reauthTask.getException().getMessage());
                        }
                    });
        }
    }


    private void updatePassword(String newPassword) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountPageActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountPageActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                            Log.e("AccountPageActivity", "Error updating password: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void updateUsername(String newUsername) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            databaseReference.child("usernamesignup").setValue(newUsername)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountPageActivity.this, "Username changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountPageActivity.this, "Failed to change username", Toast.LENGTH_SHORT).show();
                            Log.e("AccountPageActivity", "Error updating username: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void updateEmail(String newEmail) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountPageActivity.this, "Email changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountPageActivity.this, "Failed to change email", Toast.LENGTH_SHORT).show();
                            Log.e("AccountPageActivity", "Error updating email: " + task.getException().getMessage());
                        }
                    });
        }
    }



    public static StorageReference getCurrentProfilePicStorageRef() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        return FirebaseStorage.getInstance().getReference().child("profile_pic").child(userId);
    }
}
