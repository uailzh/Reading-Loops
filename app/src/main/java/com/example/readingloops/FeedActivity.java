package com.example.readingloops;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.readingloops.databinding.ActivityFeedBinding;
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

public class FeedActivity extends DrawerBaseActivity {

    private RecyclerView recyclerView;
    private ArrayList<FeedApp> arrayList;
    ActivityFeedBinding activityFeedBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFeedBinding = ActivityFeedBinding.inflate(getLayoutInflater());

        setContentView(activityFeedBinding.getRoot());
        allocateActivityTitle("Feed");

        arrayList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);

        // Fetch post data from Realtime Database in the "Postings" section
        DatabaseReference postingsRef = FirebaseDatabase.getInstance().getReference("posts").child("Postings");

        postingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // Fetch post text, image URL, and user ID from the dataSnapshot
                    String userId = postSnapshot.child("userId").getValue(String.class);
                    String username = postSnapshot.child("username").getValue(String.class);
                    String postText = postSnapshot.child("text").getValue(String.class);
                    String postImageUrl = postSnapshot.child("image").getValue(String.class);

                   String profilePicRef = postSnapshot.child("profilePic").getValue(String.class);

                    // Add the post data to the arrayList
                    arrayList.add(new FeedApp(profilePicRef, postImageUrl, username, postText));
                }

                // Create the recycler adapter with the updated arrayList
                RecyclerAdapter recyclerAdapter = new RecyclerAdapter(arrayList);

                // Set up the RecyclerView
                recyclerView.setAdapter(recyclerAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(FeedActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors in fetching posts data
                Log.e("FeedActivity", "Error loading posts: " + databaseError.getMessage());
            }
        });
    }

    // Function to get the profile picture StorageReference using user id
    private StorageReference getProfilePicStorageRef(String userId) {
        return FirebaseStorage.getInstance().getReference().child("profile_pic").child(userId);
    }
}
