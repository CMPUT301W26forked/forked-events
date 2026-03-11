package com.example.lottery.Entrant.Activity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lottery.Common.Utils.DeviceManager;
import com.example.lottery.Entrant.Service.EntrantService;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrantEventDetailsFragment extends Fragment {

    private String eventId;
    private FirebaseFirestore db;
    private EntrantService entrantService;
    private boolean isOnWaitlist = false;
    private String entrantId;

    public EntrantEventDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        // Views
        ImageButton btnBack         = view.findViewById(R.id.btnBack);
        MaterialButton btnJoin      = view.findViewById(R.id.btnJoin);
        TextView tvEventName        = view.findViewById(R.id.tvEventName);
        TextView tvDescription      = view.findViewById(R.id.tvDescription);
        TextView tvStatusTag        = view.findViewById(R.id.tvStatusTag);
        TextView tvTotalSpots       = view.findViewById(R.id.tvTotalSpots);
        TextView tvWaitlist         = view.findViewById(R.id.tvWaitlist);
        TextView tvConfirmed        = view.findViewById(R.id.tvConfirmed);
        TextView tvEventDates       = view.findViewById(R.id.tvEventDates);
        TextView tvLocation         = view.findViewById(R.id.tvLocation);
        TextView tvOrganizer        = view.findViewById(R.id.tvOrganizer);
        TextView tvAgeRange         = view.findViewById(R.id.tvAgeRange);
        ImageView ivEventPoster     = view.findViewById(R.id.ivEventPoster);

        db = FirebaseFirestore.getInstance();
        entrantService = new EntrantService();
        entrantId = DeviceManager.getDeviceId(requireContext());

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        loadEventDetails(view, tvEventName, tvDescription, tvStatusTag,
                tvTotalSpots, tvWaitlist, tvConfirmed,
                tvEventDates, tvLocation, tvOrganizer, tvAgeRange,
                ivEventPoster, btnJoin);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnJoin.setOnClickListener(v -> {
            if (isOnWaitlist) {
                leaveWaitlist(btnJoin);
            } else {
                joinWaitlist(btnJoin);
            }
        });

        return view;
    }

    private void loadEventDetails(View view,
                                  TextView tvEventName, TextView tvDescription,
                                  TextView tvStatusTag, TextView tvTotalSpots,
                                  TextView tvWaitlist, TextView tvConfirmed,
                                  TextView tvEventDates, TextView tvLocation,
                                  TextView tvOrganizer, TextView tvAgeRange,
                                  ImageView ivEventPoster, MaterialButton btnJoin) {
        if (eventId == null) {
            Toast.makeText(getContext(), "Event ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    // Basic info
                    tvEventName.setText(doc.getString("name"));
                    tvDescription.setText(doc.getString("description"));
                    tvStatusTag.setText(doc.getString("status"));
                    tvLocation.setText(doc.getString("location"));
                    tvOrganizer.setText(doc.getString("organizer"));
                    tvAgeRange.setText(doc.getString("ageRange"));

                    // Stats
                    Long totalSpots = doc.getLong("totalSpots");
                    Long waitlistCount = doc.getLong("waitlistCount");
                    Long confirmedCount = doc.getLong("confirmedCount");
                    tvTotalSpots.setText(totalSpots != null ? String.valueOf(totalSpots) : "0");
                    tvWaitlist.setText(waitlistCount != null ? String.valueOf(waitlistCount) : "0");
                    tvConfirmed.setText(confirmedCount != null ? String.valueOf(confirmedCount) : "0");

                    // Dates
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    Date start = doc.getDate("registrationStart");
                    Date end   = doc.getDate("registrationEnd");
                    if (start != null && end != null) {
                        tvEventDates.setText(sdf.format(start) + " - " + sdf.format(end));
                    }

                    // Poster image
                    String posterUri = doc.getString("posterUri");
                    if (posterUri != null && !posterUri.isEmpty()) {
                        Glide.with(requireContext())
                                .load(posterUri)
                                .centerCrop()
                                .into(ivEventPoster);
                    }

                    // Check if already on waitlist
                    List<String> registeredIds = (List<String>) doc.get("registeredEntrantIds");
                    if (registeredIds != null && registeredIds.contains(entrantId)) {
                        isOnWaitlist = true;
                        setLeaveWaitlistStyle(btnJoin);
                    } else {
                        isOnWaitlist = false;
                        setJoinWaitlistStyle(btnJoin);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT).show()
                );
    }

    private void joinWaitlist(MaterialButton btnJoin) {
        btnJoin.setEnabled(false);

        entrantService.signUpForEvent(entrantId, eventId);

        db.collection("events")
                .document(eventId)
                .update("registeredEntrantIds", FieldValue.arrayUnion(entrantId),
                        "waitlistCount", FieldValue.increment(1))
                .addOnSuccessListener(unused -> {
                    isOnWaitlist = true;
                    setLeaveWaitlistStyle(btnJoin);
                    btnJoin.setEnabled(true);
                    Toast.makeText(getContext(), "Joined waitlist successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnJoin.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to join waitlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void leaveWaitlist(MaterialButton btnJoin) {
        btnJoin.setEnabled(false);

        db.collection("events")
                .document(eventId)
                .update("registeredEntrantIds", FieldValue.arrayRemove(entrantId),
                        "waitlistCount", FieldValue.increment(-1))
                .addOnSuccessListener(unused -> {
                    isOnWaitlist = false;
                    setJoinWaitlistStyle(btnJoin);
                    btnJoin.setEnabled(true);
                    Toast.makeText(getContext(), "Left waitlist successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnJoin.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to leave waitlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void setJoinWaitlistStyle(MaterialButton btn) {
        btn.setText("Join Waitlist");
        btn.setBackgroundTintList(ColorStateList.valueOf(0xFF9575CD)); // purple
    }

    private void setLeaveWaitlistStyle(MaterialButton btn) {
        btn.setText("Leave Waitlist");
        btn.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        ));
    }
}