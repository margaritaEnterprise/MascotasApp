package com.example.mascotasapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mascotasapp.R;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Map<String, Object>> listMap;
    public PostAdapter(List<Map<String, Object>> listMap) {
        this.listMap = listMap;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_card, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Map<String, Object> map = listMap.get(position);
        holder.userName.setText((CharSequence) map.get("userName"));
    }

    @Override
    public int getItemCount() {
        return listMap.size();
    }
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView userPhoto;
        Chip userId;
        Chip userName;
        Chip category;
        ImageView cardPhoto;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.post_cardPhotoUser);
            userId = itemView.findViewById(R.id.post_cardUserId);
            userName = itemView.findViewById(R.id.post_cardUsername);
            category = itemView.findViewById(R.id.post_cardTag);
            cardPhoto = itemView.findViewById(R.id.post_cardPhoto);
        }
    }
}
