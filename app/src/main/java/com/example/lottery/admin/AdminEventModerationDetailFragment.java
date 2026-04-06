package com.example.lottery.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lottery.Common.Utils.DeviceManager;
import com.example.lottery.Entrant.Activity.Comment;
import com.example.lottery.Entrant.Activity.CommentsAdapter;
import com.example.lottery.R;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.RepoCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminEventModerationDetailFragment extends Fragment {
    private String eventId;
    private String eventName;
    private FirebaseFirestore db;
    private RecyclerView rvComments;
    private List<Comment> commentList;
    private CommentsAdapter commentsAdapter;
    private String organizerId;
    private String organizerName;
    private LinearLayout adminActions;
    private MaterialButton btnToggleActions;
    private ListenerRegistration commentsListener;
    private String currentUserId;

    public static AdminEventModerationDetailFragment newInstance(String eventId) {
        AdminEventModerationDetailFragment fragment = new AdminEventModerationDetailFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_event_detail, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");
        }

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            currentUserId = DeviceManager.getDeviceId(requireContext());
        }

        MaterialButton btnBack = view.findViewById(R.id.btnBack);
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
        btnToggleActions = view.findViewById(R.id.btnToggleActions);
        adminActions = view.findViewById(R.id.adminActionsPanel);
        rvComments = view.findViewById(R.id.rvComments);

        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(
                commentList,
                currentUserId,
                comment -> {
                    // Admin view is read-only for replies
                },
                (comment, reactionType) -> {
                    // Admin view is read-only for reactions
                }
        );

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(commentsAdapter);

        loadEventDetails(tvEventName, tvDescription, tvStatusTag,
                tvTotalSpots, tvWaitlist, tvConfirmed,
                tvEventDates, tvLocation, tvOrganizer,
                ivEventPoster);

        loadComments();

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnToggleActions.setOnClickListener(v -> {
            boolean expanded = adminActions.getVisibility() == View.VISIBLE;
            adminActions.setVisibility(expanded ? View.GONE : View.VISIBLE);
            btnToggleActions.setText(expanded ? "Admin Actions" : "Hide Actions");
        });

        view.findViewById(R.id.btnRemoveOrganizer).setOnClickListener(v -> {
            if (organizerId == null || organizerId.isEmpty()) {
                Toast.makeText(getContext(), "Organizer not available", Toast.LENGTH_SHORT).show();
                return;
            }

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer,
                            AdminRemoveOrganizerFragment.newInstance(organizerId, organizerName))
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnRemoveEvent).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete " + eventName + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FSEventRepo repo = new FSEventRepo();
                        repo.deleteEvent(eventId, new RepoCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Toast.makeText(getContext(), "Event deleted!", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(getContext(), "Failed to delete event.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        setupEditableFields(tvEventName, tvDescription, tvStatusTag, tvLocation);

        return view;
    }

    private void setupEditableFields(TextView tvEventName, TextView tvDescription,
                                     TextView tvStatusTag, TextView tvLocation) {
        tvEventName.setOnClickListener(v -> showEditDialog("Edit Event Name", "name", tvEventName));
        tvDescription.setOnClickListener(v -> showEditDialog("Edit Description", "description", tvDescription));
        tvStatusTag.setOnClickListener(v -> showEditDialog("Edit Status", "status", tvStatusTag));
        tvLocation.setOnClickListener(v -> showEditDialog("Edit Location", "location", tvLocation));
    }

    private void showEditDialog(String title, String fieldName, TextView targetView) {
        EditText input = new EditText(requireContext());
        input.setText(targetView.getText());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newVal = input.getText().toString().trim();
                    if (newVal.isEmpty()) {
                        Toast.makeText(getContext(), "Illegal Input", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("events")
                            .document(eventId)
                            .update(fieldName, newVal)
                            .addOnSuccessListener(v -> {
                                targetView.setText(newVal);
                                Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadComments() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Event ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        commentsListener = db.collection("events")
                .document(eventId)
                .collection("comments")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load comments", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Comment> allComments = new ArrayList<>();

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = new Comment();

                            comment.setCommentId(doc.getId());

                            String userId = doc.getString("userId");
                            if (userId == null || userId.trim().isEmpty()) {
                                userId = doc.getString("entrantId");
                            }
                            comment.setUserId(userId);

                            String userName = doc.getString("userName");
                            if (userName == null || userName.trim().isEmpty()) {
                                userName = doc.getString("authorName");
                            }
                            if (userName == null || userName.trim().isEmpty()) {
                                userName = doc.getString("name");
                            }
                            if (userName == null || userName.trim().isEmpty()) {
                                userName = "Unknown User";
                            }
                            comment.setUserName(userName);

                            String text = doc.getString("text");
                            if (text == null || text.trim().isEmpty()) {
                                text = doc.getString("comment");
                            }
                            if (text == null || text.trim().isEmpty()) {
                                text = "(empty comment)";
                            }
                            comment.setText(text);

                            Timestamp timestamp = doc.getTimestamp("timestamp");
                            if (timestamp == null) {
                                timestamp = doc.getTimestamp("createdAt");
                            }
                            comment.setTimestamp(timestamp);

                            comment.setParentCommentId(doc.getString("parentCommentId"));
                            comment.setReplyToEntrantId(doc.getString("replyToEntrantId"));
                            comment.setReplyToAuthorName(doc.getString("replyToAuthorName"));

                            Object mentioned = doc.get("mentionedUserNames");
                            if (mentioned instanceof List) {
                                comment.setMentionedUserNames((List<String>) mentioned);
                            }

                            Object reactionsObj = doc.get("reactions");
                            if (reactionsObj instanceof Map) {
                                Map<String, List<String>> reactions = new HashMap<>();
                                Map<?, ?> rawMap = (Map<?, ?>) reactionsObj;

                                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                                    String key = String.valueOf(entry.getKey());
                                    List<String> userIds = new ArrayList<>();

                                    if (entry.getValue() instanceof List) {
                                        List<?> rawList = (List<?>) entry.getValue();
                                        for (Object item : rawList) {
                                            if (item != null) {
                                                userIds.add(String.valueOf(item));
                                            }
                                        }
                                    }

                                    reactions.put(key, userIds);
                                }

                                comment.setReactions(reactions);
                            } else {
                                comment.setReactions(new HashMap<>());
                            }

                            allComments.add(comment);
                        }
                    }

                    allComments.sort((c1, c2) -> {
                        Timestamp t1 = c1.getTimestamp();
                        Timestamp t2 = c2.getTimestamp();

                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return -1;
                        if (t2 == null) return 1;
                        return t1.compareTo(t2);
                    });

                    List<Comment> nestedComments = buildNestedDisplayList(allComments);

                    commentList.clear();
                    commentList.addAll(nestedComments);
                    commentsAdapter.notifyDataSetChanged();

                    if (!commentList.isEmpty()) {
                        rvComments.scrollToPosition(0);
                    }
                });
    }

    private List<Comment> buildNestedDisplayList(List<Comment> allComments) {
        List<Comment> displayList = new ArrayList<>();
        Map<String, List<Comment>> childrenMap = new HashMap<>();
        List<Comment> parentComments = new ArrayList<>();

        for (Comment comment : allComments) {
            String parentId = comment.getParentCommentId();

            if (parentId == null || parentId.trim().isEmpty()) {
                parentComments.add(comment);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
            }
        }

        for (Comment parent : parentComments) {
            parent.setDepth(0);
            displayList.add(parent);
            addRepliesRecursive(parent, childrenMap, displayList, 1);
        }

        return displayList;
    }

    private void addRepliesRecursive(Comment parent,
                                     Map<String, List<Comment>> childrenMap,
                                     List<Comment> displayList,
                                     int depth) {
        if (parent.getCommentId() == null) return;

        List<Comment> replies = childrenMap.get(parent.getCommentId());
        if (replies == null) return;

        for (Comment reply : replies) {
            reply.setDepth(depth);
            displayList.add(reply);
            addRepliesRecursive(reply, childrenMap, displayList, depth + 1);
        }
    }

    private void loadEventDetails(TextView tvEventName, TextView tvDescription,
                                  TextView tvStatusTag, TextView tvTotalSpots,
                                  TextView tvWaitlist, TextView tvConfirmed,
                                  TextView tvEventDates, TextView tvLocation,
                                  TextView tvOrganizer,
                                  ImageView ivEventPoster) {
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
                    tvLocation.setText(doc.getString("location"));

                    organizerName = doc.getString("organizer");
                    organizerId = doc.getString("organizerId");

                    tvOrganizer.setText(organizerName);

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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
    }
}