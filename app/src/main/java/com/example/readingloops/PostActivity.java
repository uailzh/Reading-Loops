package com.example.readingloops;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readingloops.databinding.ActivityPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PostActivity extends DrawerBaseActivity {

    ActivityPostBinding activityPostBinding;
    EditText editTextPost;
    ImageView imageViewUpload;
    Button buttonPost;
    TextView textViewInfo;

    private Uri imageUri; // Variable to store the selected image URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPostBinding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(activityPostBinding.getRoot());
        allocateActivityTitle("Posting");

        editTextPost = findViewById(R.id.editTextPost);
        imageViewUpload = findViewById(R.id.imageViewUpload);
        buttonPost = findViewById(R.id.buttonPost);
        textViewInfo = findViewById(R.id.textViewInfo);

        imageViewUpload.setOnClickListener(view -> openGallery());

        buttonPost.setOnClickListener(view -> uploadPost());

    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageViewUpload.setImageURI(imageUri);
        }
    }

    private void uploadPost() {
        String postText = editTextPost.getText().toString().trim();

        if (TextUtils.isEmpty(postText)) {
            Toast.makeText(this, "Please enter your post text", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();

        // Retrieve username and profile picture from the "users" node
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("usernamesignup").getValue(String.class);

                    // Retrieve profile picture URI
                    StorageReference profilePicRef = getCurrentProfilePicStorageRef();
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Continue with the rest of your code using the retrieved username and profile picture URI

                        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
                        DatabaseReference userPostsRef = postsRef.child("userPosts").child(userId);
                        DatabaseReference postingsRef = postsRef.child("Postings");

                        DatabaseReference newUserPostRef = userPostsRef.push();
                        DatabaseReference newPostingsRef = postingsRef.push();
                        String postId = newUserPostRef.getKey();

                        newUserPostRef.child("userId").setValue(userId);
                        newUserPostRef.child("text").setValue(postText);

                        newPostingsRef.child("userId").setValue(userId);
                        newPostingsRef.child("username").setValue(username);
                        newPostingsRef.child("text").setValue(postText);
                        newPostingsRef.child("profilePic").setValue(uri.toString()); // Save profile picture URI




                        if (imageUri != null) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("post_images").child(postId);

                            storageReference.putFile(imageUri).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(imageUri -> {
                                        newUserPostRef.child("image").setValue(imageUri.toString());
                                        newPostingsRef.child("image").setValue(imageUri.toString());
                                        Toast.makeText(PostActivity.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                } else {
                                    Toast.makeText(PostActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(PostActivity.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }


    public static StorageReference getCurrentProfilePicStorageRef() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        return FirebaseStorage.getInstance().getReference().child("profile_pic").child(userId);
    }
}
