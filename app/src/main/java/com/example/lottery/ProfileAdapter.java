package com.example.lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * adapter for displaying user profiles in a list
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private final List<EntrantProfile> profileList;
    private final OnProfileClickListener listener;
    private String buttonText = "Invite"; // Default text

    /**
     * listener interface for profile click events
     */
    public interface OnProfileClickListener {
        /**
         * called when a profile is clicked
         * @param profile the clicked profile
         */
        void onProfileClick(EntrantProfile profile);
    }

    /**
     * constructs a new profile adapter
     * @param profileList list of profiles to display
     * @param listener click listener for profile items
     */
    public ProfileAdapter(List<EntrantProfile> profileList, OnProfileClickListener listener) {
        this.profileList = profileList;
        this.listener = listener;
    }

    /**
     * sets the text for the action button
     * @param text the text to display
     */
    public void setButtonText(String text) {
        this.buttonText = text;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        EntrantProfile profile = profileList.get(position);
        holder.tvName.setText(profile.getName());
        holder.tvEmail.setText(profile.getEmail());
        holder.btnView.setText(buttonText);

        holder.btnView.setOnClickListener(v -> listener.onProfileClick(profile));
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        MaterialButton btnView;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProfileName);
            tvEmail = itemView.findViewById(R.id.tvProfileEmail);
            btnView = itemView.findViewById(R.id.btnViewProfile);
        }
    }
}
