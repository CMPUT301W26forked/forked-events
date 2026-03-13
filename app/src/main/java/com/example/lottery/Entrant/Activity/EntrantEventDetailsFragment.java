package com.example.lottery.Entrant.Activity;

import android.app.AlertDialog;
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
import com.example.lottery.Entrant.Repo.WaitlistCallback;
import com.example.lottery.Entrant.Service.EntrantService;
import com.example.lottery.Entrant.Service.WaitlistService;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    public String entrantId;
    public WaitlistService waitlistService;
    private String eventStatus = "Open";

    public EntrantEventDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnShowQr = view.findViewById(R.id.btnShowQr);
        MaterialButton btnJoin = view.findViewById(R.id.btnJoin);
        TextView tvEventName = view.findViewById(R.id.tvEventName);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvStatusTag = view.findViewById(R.id.tvStatusTag);
        TextView tvTotalSpots = view.findViewById(R.id.tvTotalSpots);
        TextView tvWaitlist = view.findViewById(R.id.tvWaitlist);
        TextView tvConfirmed = view.findViewById(R.id.tvConfirmed);
        TextView tvEventDates = view.findViewById(R.id.tvEventDates);
        TextView tvLocation = view.findViewById(R.id.tvLocation);
        TextView tvOrganizer = view.findViewById(R.id.tvOrganizer);
        ImageView ivEventPoster = view.findViewById(R.id.ivEventPoster);

        db = FirebaseFirestore.getInstance();
        entrantService = new EntrantService();
        waitlistService = new WaitlistService();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            entrantId = currentUser.getUid();
        } else {
            entrantId = DeviceManager.getDeviceId(requireContext());
        }

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        loadEventDetails(tvEventName, tvDescription, tvStatusTag,
                tvTotalSpots, tvWaitlist, tvConfirmed,
                tvEventDates, tvLocation, tvOrganizer,
                ivEventPoster, btnJoin);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnShowQr.setOnClickListener(v -> {
            if (eventId == null) return;
            QrDisplayFragment fragment = new QrDisplayFragment();
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnJoin.setOnClickListener(v -> {
            if (isOnWaitlist) {
                leaveWaitlist(btnJoin);
            } else {
                joinWaitlist(btnJoin);
            }
        });

        return view;
    }

    private void loadEventDetails(TextView tvEventName, TextView tvDescription,
                                  TextView tvStatusTag, TextView tvTotalSpots,
                                  TextView tvWaitlist, TextView tvConfirmed,
                                  TextView tvEventDates, TextView tvLocation,
                                  TextView tvOrganizer,
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

                    tvEventName.setText(doc.getString("name"));
                    tvDescription.setText(doc.getString("description"));
                    tvStatusTag.setText(doc.getString("status"));
                    if (doc.getString("status") != null) {
                        eventStatus = doc.getString("status");
                    } else {
                        eventStatus = "Closed";
                    }
                    tvLocation.setText(doc.getString("location"));
                    tvOrganizer.setText(doc.getString("organizer"));

                    Long totalSpots = doc.getLong("totalSpots");
                    Long waitlistCount = doc.getLong("waitlistCount");
                    Long confirmedCount = doc.getLong("confirmedCount");

                    tvTotalSpots.setText(totalSpots != null ? String.valueOf(totalSpots) : "0");
                    tvWaitlist.setText(waitlistCount != null ? String.valueOf(waitlistCount) : "0");
                    tvConfirmed.setText(confirmedCount != null ? String.valueOf(confirmedCount) : "0");

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    Date start = doc.getDate("registrationStart");
                    Date end = doc.getDate("registrationEnd");
                    if (start != null && end != null) {
                        tvEventDates.setText(sdf.format(start) + " - " + sdf.format(end));
                    }

                    String posterUri = doc.getString("posterUri");
                    if (posterUri != null && !posterUri.isEmpty()) {
                        Glide.with(requireContext())
                                .load(posterUri)
                                .centerCrop()
                                .into(ivEventPoster);
                    }

                    List<String> waitlistedIds = (List<String>) doc.get("waitlistedEntrantIds");
                    if (waitlistedIds != null && waitlistedIds.contains(entrantId)) {
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

    /***
     * Allows entrants to join waitlist.
     * Also checks the waitlist capacity set by the event organizer.
     * @param btnJoin Button that allows entrant to join waitlist.
     */

    private void joinWaitlist(MaterialButton btnJoin) {
        if (!"Open".equalsIgnoreCase(eventStatus)) {
            Toast.makeText(getContext(), "Registration is closed.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            Long waitListLimit = doc.getLong("waitListLimit");
            Long waitlistCount = doc.getLong("waitlistCount");

            if (waitListLimit != null && waitlistCount != null && waitlistCount >= waitListLimit) {
                Toast.makeText(getContext(), "Waitlist is full.", Toast.LENGTH_SHORT).show();
                return;
            }


            btnJoin.setEnabled(false);

            entrantService.signUpForEvent(entrantId, eventId);

            db.collection("events")
                    .document(eventId)
                    .update(
                            "waitlistedEntrantIds", FieldValue.arrayUnion(entrantId),
                            "waitlistCount", FieldValue.increment(1)
                    )
                    .addOnSuccessListener(unused ->
                            db.collection("users")
                                    .document(entrantId)
                                    .update("registeredEventIds", FieldValue.arrayUnion(eventId))
                                    .addOnSuccessListener(unused2 -> {
                                        isOnWaitlist = true;
                                        setLeaveWaitlistStyle(btnJoin);
                                        btnJoin.setEnabled(true);
                                        Toast.makeText(getContext(), "Joined waitlist successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        isOnWaitlist = true;
                                        setLeaveWaitlistStyle(btnJoin);
                                        btnJoin.setEnabled(true);
                                        Toast.makeText(getContext(), "Joined waitlist, but failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                    )
                    .addOnFailureListener(e -> {
                        btnJoin.setEnabled(true);
                        Toast.makeText(getContext(), "Failed to join waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /***
     * Allows entrants to leave waitlist.
     * @param btnJoin Button that allows entrant to leave waitlist.
     */

    private void leaveWaitlist(MaterialButton btnJoin) {
        btnJoin.setEnabled(false);

        db.collection("events")
                .document(eventId)
                .update(
                        "waitlistedEntrantIds", FieldValue.arrayRemove(entrantId),
                        "waitlistCount", FieldValue.increment(-1)
                )
                .addOnSuccessListener(unused ->
                        db.collection("users")
                                .document(entrantId)
                                .update("registeredEventIds", FieldValue.arrayRemove(eventId))
                                .addOnSuccessListener(unused2 -> {
                                    isOnWaitlist = false;
                                    setJoinWaitlistStyle(btnJoin);
                                    btnJoin.setEnabled(true);
                                    Toast.makeText(getContext(), "Left waitlist successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    isOnWaitlist = false;
                                    setJoinWaitlistStyle(btnJoin);
                                    btnJoin.setEnabled(true);
                                    Toast.makeText(getContext(), "Left waitlist, but failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    btnJoin.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to leave waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void setJoinWaitlistStyle(MaterialButton btn) {
        btn.setText("Join Waitlist");
        btn.setBackgroundTintList(ColorStateList.valueOf(0xFF9575CD));
    }

    private void setLeaveWaitlistStyle(MaterialButton btn) {
        btn.setText("Leave Waitlist");
        btn.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        ));
    }

    /***
     * Asks entrant if they want to remain on waitlist after they did not get selected.
     * @param btnJoin The join/leave button.
     */
    public void showStayInList(MaterialButton btnJoin) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Not Selected")
                .setMessage("Stay in waiting list?")
                .setPositiveButton("Yes", (dialog, which) ->
                        waitlistService.stayInList(eventId, entrantId, new WaitlistCallback<Void>() {
                            @Override
                            public void onSuccess(Void r) {}

                            @Override
                            public void onError(Exception e) {}
                        }))
                .setNegativeButton("No", (dialog, which) -> leaveWaitlist(btnJoin))
                .setCancelable(false)
                .show();
    }
}
