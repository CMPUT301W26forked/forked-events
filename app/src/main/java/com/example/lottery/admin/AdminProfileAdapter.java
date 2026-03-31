package com.example.lottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {

    public interface OnProfileClickListener {
        void onProfileClick(AdminProfileItem item);
    }

    private final List<AdminProfileItem> items;
    private final OnProfileClickListener listener;

    public AdminProfileAdapter(List<AdminProfileItem> items, OnProfileClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        AdminProfileItem item = items.get(position);

        holder.tvName.setText(
                item.getName() != null && !item.getName().trim().isEmpty()
                        ? item.getName()
                        : "Unnamed User"
        );

        holder.tvEmail.setText(
                item.getEmail() != null && !item.getEmail().trim().isEmpty()
                        ? item.getEmail()
                        : "No email"
        );

        holder.tvRole.setText(
                item.getRole() != null && !item.getRole().trim().isEmpty()
                        ? item.getRole()
                        : "entrant"
        );

        String imageUrl = item.getProfilePictureUri();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(holder.ivProfile.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .centerCrop()
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_nav_profile);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onProfileClick(items.get(currentPos));
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onProfileClick(items.get(currentPos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvEmail, tvRole;
        MaterialButton btnRemove;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvProfileName);
            tvEmail = itemView.findViewById(R.id.tvProfileEmail);
            tvRole = itemView.findViewById(R.id.tvProfileRole);
            btnRemove = itemView.findViewById(R.id.btnRemoveProfile);
        }
    }
}