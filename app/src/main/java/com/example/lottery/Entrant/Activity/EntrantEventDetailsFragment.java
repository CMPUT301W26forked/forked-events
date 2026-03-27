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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lottery.Common.Utils.DeviceManager;
import com.example.lottery.Entrant.Activity.CommentsAdapter;
import com.example.lottery.Entrant.Activity.Comment;
import com.example.lottery.Entrant.Repo.WaitlistCallback;
import com.example.lottery.Entrant.Service.EntrantService;
import com.example.lottery.Entrant.Service.WaitlistService;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntrantEventDetailsFragment extends Fragment {

    private String eventId;
    private String eventName;
    private FirebaseFirestore db;
    private EntrantService entrantService;
    private boolean isOnWaitlist = false;
    public String entrantId;
    public WaitlistService waitlistService;
    private String eventStatus = "Open";

    private RecyclerView rvComments;
    private TextInputEditText etComment;
    private MaterialButton btnPostComment;

    private List<Comment> commentList;
    private CommentsAdapter commentsAdapter;
    private ListenerRegistration commentsListener;

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

        rvComments = view.findViewById(R.id.rvComments);
        etComment = view.findViewById(R.id.etComment);
        btnPostComment = view.findViewById(R.id.btnPostComment);

        db = FirebaseFirestore.getInstance();
        entrantService = new EntrantService();
        waitlistService = new WaitlistService();

        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(commentsAdapter);

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

        loadComments();

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

        btnPostComment.setOnClickListener(v -> postComment());

        return view;
    }

    private void loadComments() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Event ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        commentsListener = db.collection("events")
                .document(eventId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load comments", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    commentList.clear();

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                commentList.add(comment);
                            }
                        }
                    }

                    commentsAdapter.notifyDataSetChanged();

                    if (!commentList.isEmpty()) {
                        rvComments.scrollToPosition(commentList.size() - 1);
                    }
                });
    }

    private void postComment() {
        String commentText = etComment.getText() != null
                ? etComment.getText().toString().trim()
                : "";

        if (commentText.isEmpty()) {
            etComment.setError("Comment cannot be empty");
            return;
        }

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Event ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(entrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("name");

                    if (userName == null || userName.trim().isEmpty()) {
                        userName = documentSnapshot.getString("username");
                    }

                    if (userName == null || userName.trim().isEmpty()) {
                        userName = "Anonymous User";
                    }

                    Comment comment = new Comment(
                            entrantId,
                            userName,
                            commentText,
                            Timestamp.now()
                    );

                    db.collection("events")
                            .document(eventId)
                            .collection("comments")
                            .add(comment)
                            .addOnSuccessListener(documentReference -> {
                                etComment.setText("");
                                Toast.makeText(getContext(), "Comment posted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to post comment", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to get user info", Toast.LENGTH_SHORT).show());
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

                    eventName = doc.getString("name");
                    tvEventName.setText(eventName);
                    tvDescription.setText(doc.getString("description"));
                    tvStatusTag.setText(doc.getString("status"));
                    if (doc.getString("status") != null) {
                        eventStatus = doc.getString("status");
                    } else {
                        eventStatus = "Closed";
                    }
                    tvLocation.setText(doc.getString("location"));
                    tvOrganizer.setText(doc.getString("organizer"));

                    String organizerId = doc.getString("organizerId");
                    if (organizerId != null) {
                        commentsAdapter.setOrganizerId(organizerId);
                    }

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

                    List<String> registeredIds = (List<String>) doc.get("registeredEntrantIds");
                    List<String> cancelledIds = (List<String>) doc.get("cancelledEntrantIds");
                    if ((registeredIds != null && registeredIds.contains(entrantId))
                            || (cancelledIds != null && cancelledIds.contains(entrantId))) {
                        btnJoin.setVisibility(View.GONE);
                        return;
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
                                        createWaitlistNotification();
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

    private void createWaitlistNotification() {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("message", "You have joined the waitlist for " + eventName);
        notification.put("type", "JOIN_WAITLIST");
        notification.put("status", "WAITLISTED");
        notification.put("createdAt", Timestamp.now());

        db.collection("users")
                .document(entrantId)
                .collection("notification")
                .add(notification);
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }
}