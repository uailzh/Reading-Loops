package com.example.readingloops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {


    private ArrayList<FeedApp>  arrayList;

    public RecyclerAdapter(ArrayList<FeedApp> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_view, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedApp feedApp = arrayList.get(position);

        holder.title.setText(feedApp.getTitle());
        holder.message.setText(feedApp.getMessage());

        // Load profile picture using Glide
        Glide.with(holder.itemView.getContext())
                .load(feedApp.getProfileIcon())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.profileImg);

        // Load post image using Glide
        Glide.with(holder.itemView.getContext())
                .load(feedApp.getPostImage())
                .into(holder.postImg);
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        ImageView profileImg;
        ImageView postImg;
        TextView title, message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg = itemView.findViewById(R.id.ivProfile);
            postImg = itemView.findViewById(R.id.ivPost);
            title = itemView.findViewById(R.id.feedTitle);
            message = itemView.findViewById(R.id.message);
        }
    }
}
