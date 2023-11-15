package com.example.mascotasapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionBarPolicy;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mascotasapp.R;
import com.example.mascotasapp.signup.fragments.RegisterAuthFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Map<String, Object>> listMap;
    private Context context;

    public interface PostClickListener{
        void postClick(Map<String, Object> item);
    }
    PostClickListener postClickListener;

    public PostAdapter(List<Map<String, Object>> listMap, Context context) {
        this.listMap = listMap;
        this.context = context;
        this.postClickListener = (PostClickListener) context;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_card, parent, false);
        return new PostViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Map<String, Object> map = listMap.get(position);
        holder.id = (String) map.get("id");
        holder.userName.setText((CharSequence) map.get("username"));
        //holder.userId.setText((CharSequence) map.get("userId"));
        String value = (String) map.get("category");
        String text = "";
        int color = 0;
        switch (value){
            case "adoption":
                text = context.getString(R.string.adoption);
                color = context.getColor(R.color.adoptionChipColor);
                break;
            case "lost":
                text = context.getString(R.string.lost);
                color = context.getColor(R.color.lostChipColor);
                break;
            case "found":
                text = context.getString(R.string.found);
                color = context.getColor(R.color.foundChipColor);
                break;
            case "couple":
                text = context.getString(R.string.couple);
                color = context.getColor(R.color.coupleChipColor);
                break;
        }
        holder.category.setText(text);
        holder.category.setChipBackgroundColor(ColorStateList.valueOf(color));

        Uri userPhotoUrl = Uri.parse((String) map.get("userPhotoUrl"));
        Picasso.with(context)
                .load(userPhotoUrl)
                .resize(30, 30)
                .into(holder.userPhoto);

        Uri photoUrl = Uri.parse((String) map.get("photoUrl"));
        Picasso.with(context)
                .load(photoUrl)
                .resize(250, 250)
                .centerCrop()
                .placeholder(R.drawable.ic_add_photo_foreground)
                .error(R.drawable.baseline_photo_camera_24)
                .into(holder.cardPhoto);
        holder.cardPhoto.setOnClickListener(v -> postClickListener.postClick(map));
    }

    @Override
    public int getItemCount() {
        return listMap.size();
    }
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView userPhoto;
        Chip userId;
        TextView userName;
        Chip category;
        ImageView cardPhoto;
        String id;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.post_cardPhotoUser);
            //userId = itemView.findViewById(R.id.post_cardUserId);
            userName = itemView.findViewById(R.id.post_cardUsername);
            category = itemView.findViewById(R.id.post_cardTag);
            cardPhoto = itemView.findViewById(R.id.post_cardPhoto);

        }
    }
}
