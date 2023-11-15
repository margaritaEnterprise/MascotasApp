package com.example.mascotasapp.utils;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mascotasapp.R;
import com.google.android.material.chip.Chip;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.PostViewHolder> {
    private List<Map<String, Object>> listMap;
    private Context context;

    public interface MyPostClickListener{
        void viewDetailMyPost(Map<String, Object> item);
    }
    MyPostClickListener postClickListener;
    public MyPostAdapter(List<Map<String, Object>> listMap, Context context) {
        this.listMap = listMap;
        this.context = context;
        this.postClickListener = (MyPostClickListener) context;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.my_post_card, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Map<String, Object> map = listMap.get(position);

        Uri photoUrl = Uri.parse((String) map.get("photoUrl"));
        Picasso.with(context)
                .load(photoUrl)
                .resize(250, 250)
                .into(holder.cardPhoto);
        holder.cardPhoto.setOnClickListener(v -> postClickListener.viewDetailMyPost(map));
    }

    @Override
    public int getItemCount() {
        return listMap.size();
    }
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView cardPhoto;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPhoto = itemView.findViewById(R.id.my_post_cardPhoto);
        }
    }
}
