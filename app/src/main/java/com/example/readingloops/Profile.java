package com.example.readingloops;

import static java.security.AccessController.getContext;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.readingloops.databinding.ActivityProfileBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Profile extends DrawerBaseActivity {

    ActivityProfileBinding activityProfileBinding;
    private RecyclerView recyclerView;
    private ArrayList<FeedApp> arrayList;

    Button update;
    ImageView IVPreviewImage;

    RecyclerAdapter recyclerAdapter;

    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;

    private DatabaseReference userPostsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data!=null && data.getData()!=null){
                            selectedImageUri = data.getData();
                            setProfilePic(this, selectedImageUri, IVPreviewImage);
                        }
                    }
                }
        );

        activityProfileBinding = ActivityProfileBinding.inflate(getLayoutInflater());

        setContentView(activityProfileBinding.getRoot());
        allocateActivityTitle("Profile Page");

        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView2);

        IVPreviewImage = findViewById(R.id.profileImage);

        IVPreviewImage.setOnClickListener(v -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent(new Function1<Intent, Unit>() {
                @Override
                public Unit invoke(Intent intent) {
                    imagePickLauncher.launch(intent);
                    return null;
                }
            });

        });

        update = findViewById(R.id.updateButton);

        // Initialize the userPostsRef to the posts of the current user in "userPosts" section
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        userPostsRef = FirebaseDatabase.getInstance().getReference("posts").child("userPosts").child(userId).getRef();

        fetchUserDataAndPosts();

        recyclerAdapter = new RecyclerAdapter(arrayList);

        recyclerView.setAdapter(recyclerAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedImageUri!=null) {
                    getCurrentProfilePicStorageRef().putFile(selectedImageUri).addOnCompleteListener(task -> {
                        Toast.makeText(Profile.this, "Updated", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchUserDataAndPosts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("usernamesignup").getValue(String.class);
                    activityProfileBinding.profileName.setText(username);

                    // Load profile picture from Firebase Storage
                    StorageReference profilePicRef = FirebaseStorage.getInstance().getReference().child("profile_pic").child(userId);
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Load the image into the ImageView using Glide
                        Glide.with(Profile.this)
                                .load(uri)
                                .apply(RequestOptions.circleCropTransform())
                                .into(IVPreviewImage);

                        // Fetch and display posts
                        fetchAndDisplayPosts(uri.toString());
                    }).addOnFailureListener(e -> {
                        // Handle any errors that may occur while fetching the image
                        Log.e("Profile", "Error loading profile picture: " + e.getMessage());
                    });

                } else {
                    Log.d("ProfileActivity", "User data does not exist in the database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AccountPageActivity", "Error retrieving user data: " + databaseError.getMessage());
            }
        });
    }

    private void fetchAndDisplayPosts(String profileImageUrl) {
        userPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList.clear(); // Clear existing posts
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // Fetch post data
                    String postText = postSnapshot.child("text").getValue(String.class);
                    String postImageUrl = postSnapshot.child("image").getValue(String.class);
                    String username = dataSnapshot.child("usernamesignup").getValue(String.class);

                    // Add the post data to the arrayList
                    arrayList.add(new FeedApp(profileImageUrl, postImageUrl, username, postText));
                }

                // Notify the adapter that the data set has changed
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors in fetching posts data
                Log.e("ProfileActivity", "Error loading posts: " + databaseError.getMessage());
            }
        });
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

    public static StorageReference getCurrentProfilePicStorageRef() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        return FirebaseStorage.getInstance().getReference().child("profile_pic").child(userId);
    }
}
