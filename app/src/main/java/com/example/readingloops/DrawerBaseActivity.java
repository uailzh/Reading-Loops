package com.example.readingloops;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.readingloops.databinding.ActivityDrawerBaseBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final int HOME_NAV_ID = R.id.home_nav;
    private static final int FEED_ID = R.id.feed;

    private static final int ACCOUNT_ID = R.id.account;

    private static final int POST_ID = R.id.post;

    private static final int ADD_POST_ID = R.id.addPost;

    private static final int LOG_OUT_ID = R.id.log_out;

    private static final int SHARE_ID = R.id.share;

    DrawerLayout drawerLayout;


    public void setContentView(View view) {

        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        Toolbar toolbar = drawerLayout.findViewById(R.id.tool);
        setSupportActionBar(toolbar);

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_drawer_open, R.string.menu_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = (TextView) headerView.findViewById(R.id.header_name);
            TextView headerEmail = headerView.findViewById(R.id.header_email);


            headerEmail.setText(userEmail);

            // Retrieve the username from Firebase Realtime Database
            String userId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);


            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User data exists in the database
                        String username = dataSnapshot.child("usernamesignup").getValue(String.class);


                        navUsername.setText(username);
                        Log.d("DrawerBaseActivity", "User data retrieved successfully. Username: " + username);

                    } else {
                        Log.d("DrawerBaseActivity", "User data does not exist in the database");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error
                    Log.e("DrawerBaseActivity", "Error retrieving user data: " + databaseError.getMessage());
                }
            });

        }

    }


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item){
            drawerLayout.closeDrawer(GravityCompat.START);

            int itemId = item.getItemId();

            if (itemId == HOME_NAV_ID) {
                startActivity(new Intent(this, HomePageActivity.class));
                overridePendingTransition(0, 0);
            } else if (itemId == FEED_ID) {
                startActivity(new Intent(this, FeedActivity.class));
                overridePendingTransition(0, 0);
            } else if (itemId == ACCOUNT_ID) {
                startActivity(new Intent(this, AccountPageActivity.class));
                overridePendingTransition(0, 0);
            } else if (itemId == POST_ID) {
                startActivity(new Intent(this, Profile.class));
                overridePendingTransition(0, 0);
            }
            else if (itemId == ADD_POST_ID) {
                startActivity(new Intent(this, PostActivity.class));
                overridePendingTransition(0, 0);
            }
            else if (itemId == LOG_OUT_ID) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class)); // Replace LoginActivity with your login activity
                finish();
            }
            else if (itemId == SHARE_ID) {
                // Create a share intent
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome app!");
                shareIntent.setType("text/plain");

                // Show the share dialog
                startActivity(Intent.createChooser(shareIntent, "Share with"));
            }

            return false;
        }

        protected void allocateActivityTitle (String titleString){
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(titleString);
            }
        }

    }


