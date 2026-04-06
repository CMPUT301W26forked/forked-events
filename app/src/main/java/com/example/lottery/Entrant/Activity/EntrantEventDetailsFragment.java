package com.example.lottery.Entrant.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lottery.Common.Utils.DeviceManager;
import com.example.lottery.Entrant.Repo.WaitlistCallback;
import com.example.lottery.Entrant.Service.EntrantService;
import com.example.lottery.Entrant.Service.WaitlistService;
import com.example.lottery.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * EntrantEventDetailsFragment
 *
 * This fragment handles:
 * - Displaying event details
 * - Joining and leaving the waitlist
 * - Viewing and posting comments
 * - Replying to comments (nested threads)
 * - Reacting to comments
 * - Optional location-based waitlist joining
 *
 * Data is managed using Firebase Firestore.
 */

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
    private ImageButton btnShowQr;

    private List<Comment> commentList;
    private CommentsAdapter commentsAdapter;
    private ListenerRegistration commentsListener;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private MaterialButton pendingJoinButton;
    private String pendingEntrantName;

    private Comment selectedReplyComment = null;

    public EntrantEventDetailsFragment() {
    }

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), res -> {
                if (res) {
                    fetchCurrentLocationAndJoin();
                } else {
                    fallbackToJoinWithoutLocation();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnShowQr = view.findViewById(R.id.btnShowQr);
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
        TextView tvCoOrganizerLabel = view.findViewById(R.id.tvCoOrganizerLabel);
        TextView tvCoOrganizers = view.findViewById(R.id.tvCoOrganizers);
        ImageView ivEventPoster = view.findViewById(R.id.ivEventPoster);

        rvComments = view.findViewById(R.id.rvComments);
        etComment = view.findViewById(R.id.etComment);
        btnPostComment = view.findViewById(R.id.btnPostComment);

        db = FirebaseFirestore.getInstance();
        entrantService = new EntrantService();
        waitlistService = new WaitlistService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        commentList = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            entrantId = currentUser.getUid();
        } else {
            entrantId = DeviceManager.getDeviceId(requireContext());
        }

        commentsAdapter = new CommentsAdapter(
                commentList,
                entrantId,
                comment -> {
                    if (selectedReplyComment != null
                            && selectedReplyComment.getCommentId() != null
                            && selectedReplyComment.getCommentId().equals(comment.getCommentId())) {
                        clearReplyMode();
                        Toast.makeText(getContext(), "Reply cancelled", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedReplyComment = comment;

                    String replyName = comment.getUserName();
                    if (replyName == null || replyName.trim().isEmpty()) {
                        replyName = "user";
                    }

                    etComment.setHint("Replying to @" + replyName);
                    etComment.requestFocus();
                    etComment.setSelection(etComment.getText() != null ? etComment.getText().length() : 0);

                    Toast.makeText(getContext(),
                            "Replying to @" + replyName + ". Tap Reply again to cancel.",
                            Toast.LENGTH_SHORT).show();
                },
                (comment, reactionType) -> toggleReaction(comment, reactionType)
        );

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(commentsAdapter);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        loadEventDetails(tvEventName, tvDescription, tvStatusTag,
                tvTotalSpots, tvWaitlist, tvConfirmed,
                tvEventDates, tvLocation, tvOrganizer,
                tvCoOrganizerLabel, tvCoOrganizers,
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

    /**
     * Loads and listens for comments on the current event.
     */
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

    /**
     * Builds a nested list of comments for display in the RecyclerView.
     * @param allComments Flat list of all comments.
     * @return Nested list of comments with depth information.
     */
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

    /**
     * Recursively adds replies to the display list.
     * @param parent The parent comment.
     * @param childrenMap Map of parent IDs to lists of child comments.
     * @param displayList The list being built for display.
     * @param depth Current nesting depth.
     */
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

    /**
     * Posts a new comment or reply to Firestore.
     */
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

                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("userId", entrantId);
                    commentData.put("entrantId", entrantId);
                    commentData.put("userName", userName);
                    commentData.put("authorName", userName);
                    commentData.put("text", commentText);
                    commentData.put("timestamp", Timestamp.now());
                    commentData.put("reactions", new HashMap<String, Object>());

                    if (selectedReplyComment != null) {
                        commentData.put("parentCommentId", selectedReplyComment.getCommentId());
                        commentData.put("replyToEntrantId", selectedReplyComment.getUserId());
                        commentData.put("replyToAuthorName", selectedReplyComment.getUserName());

                        List<String> mentionedNames = new ArrayList<>();
                        if (selectedReplyComment.getUserName() != null
                                && !selectedReplyComment.getUserName().trim().isEmpty()) {
                            mentionedNames.add(selectedReplyComment.getUserName());
                        }
                        commentData.put("mentionedUserNames", mentionedNames);
                    } else {
                        commentData.put("parentCommentId", null);
                        commentData.put("replyToEntrantId", null);
                        commentData.put("replyToAuthorName", null);
                        commentData.put("mentionedUserNames", new ArrayList<String>());
                    }

                    db.collection("events")
                            .document(eventId)
                            .collection("comments")
                            .add(commentData)
                            .addOnSuccessListener(documentReference -> {
                                boolean wasReply = selectedReplyComment != null;
                                etComment.setText("");
                                clearReplyMode();

                                if (wasReply) {
                                    Toast.makeText(getContext(), "Reply posted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Comment posted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to post comment", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to get user info", Toast.LENGTH_SHORT).show());
    }

    /**
     * Toggles a reaction (like, love, fire) on a comment.
     * @param comment The comment to react to.
     * @param reactionType The type of reaction.
     */
    private void toggleReaction(Comment comment, String reactionType) {
        if (comment == null || comment.getCommentId() == null || eventId == null || entrantId == null) {
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(comment.getCommentId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    Object reactionsObj = snapshot.get("reactions");
                    Map<String, List<String>> reactions = new HashMap<>();

                    if (reactionsObj instanceof Map) {
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
                    }

                    if (!reactions.containsKey("like")) reactions.put("like", new ArrayList<>());
                    if (!reactions.containsKey("love")) reactions.put("love", new ArrayList<>());
                    if (!reactions.containsKey("fire")) reactions.put("fire", new ArrayList<>());

                    List<String> reactionUsers = reactions.get(reactionType);
                    boolean alreadyReactedToSame = reactionUsers != null && reactionUsers.contains(entrantId);

                    for (List<String> users : reactions.values()) {
                        users.remove(entrantId);
                    }

                    if (!alreadyReactedToSame) {
                        List<String> targetList = reactions.get(reactionType);
                        if (targetList != null) {
                            targetList.add(entrantId);
                        }
                    }

                    db.collection("events")
                            .document(eventId)
                            .collection("comments")
                            .document(comment.getCommentId())
                            .update("reactions", reactions)
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to update reaction", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update reaction", Toast.LENGTH_SHORT).show());
    }

    /**
     * Clears the current reply mode.
     */
    private void clearReplyMode() {
        selectedReplyComment = null;
        etComment.setHint("Write a comment");
        etComment.setError(null);
    }

    /**
     * Loads event details from Firestore and updates the UI.
     * Also handles visibility of joining and QR buttons based on user roles and event privacy.
     */
    private void loadEventDetails(TextView tvEventName, TextView tvDescription,
                                  TextView tvStatusTag, TextView tvTotalSpots,
                                  TextView tvWaitlist, TextView tvConfirmed,
                                  TextView tvEventDates, TextView tvLocation,
                                  TextView tvOrganizer,
                                  TextView tvCoOrganizerLabel, TextView tvCoOrganizers,
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

                    // Handle co-organizers display
                    List<String> coOrganizerIds = (List<String>) doc.get("coOrganizerIds");
                    if (coOrganizerIds != null && !coOrganizerIds.isEmpty()) {
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String id : coOrganizerIds) {
                            tasks.add(db.collection("users").document(id).get());
                        }
                        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
                            List<String> usernames = new ArrayList<>();
                            for (Task<DocumentSnapshot> task : tasks) {
                                if (task.isSuccessful() && task.getResult().exists()) {
                                    String username = task.getResult().getString("username");
                                    if (username == null || username.trim().isEmpty()) {
                                        username = task.getResult().getString("name");
                                    }
                                    if (username != null) usernames.add(username);
                                }
                            }
                            if (!usernames.isEmpty()) {
                                tvCoOrganizerLabel.setVisibility(View.VISIBLE);
                                tvCoOrganizers.setVisibility(View.VISIBLE);
                                tvCoOrganizers.setText(String.join(", ", usernames));
                                
                                // Update label to singular if there's only one
                                if (usernames.size() == 1) {
                                    tvCoOrganizerLabel.setText("Co-organizer");
                                } else {
                                    tvCoOrganizerLabel.setText("Co-organizers");
                                }
                            }
                        });
                    } else {
                        tvCoOrganizerLabel.setVisibility(View.GONE);
                        tvCoOrganizers.setVisibility(View.GONE);
                    }

                    // Check if current user is an organizer or co-organizer
                    boolean isOrganizer = entrantId.equals(organizerId);
                    boolean isCoOrganizer = coOrganizerIds != null && coOrganizerIds.contains(entrantId);

                    if (isOrganizer || isCoOrganizer) {
                        btnJoin.setVisibility(View.GONE);
                        // No need for further join/waitlist logic if they are organizers
                        if (btnShowQr != null) {
                            Boolean isPrivate = doc.getBoolean("isPrivate");
                            btnShowQr.setVisibility((isPrivate != null && isPrivate) ? View.GONE : View.VISIBLE);
                        }
                        return;
                    }

                    // Hide QR button for private events
                    Boolean isPrivate = doc.getBoolean("isPrivate");
                    if (isPrivate != null && isPrivate) {
                        if (btnShowQr != null) {
                            btnShowQr.setVisibility(View.GONE);
                        }
                        // Hide Join Waitlist button for private events
                        btnJoin.setVisibility(View.GONE);
                    } else if (btnShowQr != null) {
                        btnShowQr.setVisibility(View.VISIBLE);
                    }

                    List<String> registeredIds = (List<String>) doc.get("registeredEntrantIds");
                    List<String> cancelledIds = (List<String>) doc.get("cancelledEntrantIds");
                    if ((registeredIds != null && registeredIds.contains(entrantId))
                            || (cancelledIds != null && cancelledIds.contains(entrantId))) {
                        btnJoin.setVisibility(View.GONE);
                        return;
                    }

                    // Only handle waitlist style if not already hidden by isPrivate check
                    if (btnJoin.getVisibility() != View.GONE) {
                        List<String> waitlistedIds = (List<String>) doc.get("waitlistedEntrantIds");
                        if (waitlistedIds != null && waitlistedIds.contains(entrantId)) {
                            isOnWaitlist = true;
                            setLeaveWaitlistStyle(btnJoin);
                        } else {
                            isOnWaitlist = false;
                            setJoinWaitlistStyle(btnJoin);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Initiates the process to join the waitlist.
     * @param btnJoin The join button UI element.
     */
    private void joinWaitlist(MaterialButton btnJoin) {
        if (!"Open".equalsIgnoreCase(eventStatus)) {
            Toast.makeText(getContext(), "Registration is closed.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            Long waitListLimit = doc.getLong("waitListLimit");
            Long waitlistCount = doc.getLong("waitlistCount");
            Date now = new Date();
            Date regStart = doc.getDate("registrationStart");
            Date regEnd = doc.getDate("registrationEnd");

            if ((regStart != null && now.before(regStart)) || (regEnd != null && now.after(regEnd))) {
                Toast.makeText(getContext(), "Registration is closed.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (waitListLimit != null && waitlistCount != null && waitlistCount >= waitListLimit) {
                Toast.makeText(getContext(), "Waitlist is full.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnJoin.setEnabled(false);

            resolveEntrantName(entrantName -> showLocationChoiceDialog(btnJoin, entrantName));
        }).addOnFailureListener(e -> {
            btnJoin.setEnabled(true);
            Toast.makeText(getContext(), "Failed to join waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Resolves the current user's name for waitlist registration.
     * @param cb Callback with the resolved name.
     */
    private void resolveEntrantName(EntrantNameCallBack cb) {
        db.collection("users")
                .document(entrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String entrantName = documentSnapshot.getString("name");

                    if (entrantName == null || entrantName.trim().isEmpty()) {
                        entrantName = documentSnapshot.getString("username");
                    }

                    if (entrantName == null || entrantName.trim().isEmpty()) {
                        entrantName = "Anonymous user";
                    }

                    cb.onResult(entrantName);
                })
                .addOnFailureListener(e -> cb.onResult("Anonymous user"));
    }

    /**
     * Shows a dialog asking the user if they want to share their location.
     * @param btnJoin The join button UI element.
     * @param entrantName The resolved name of the entrant.
     */
    private void showLocationChoiceDialog(MaterialButton btnJoin, String entrantName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Use current location?")
                .setMessage("Allow the app to get your current location for the wait list map?")
                .setPositiveButton("Allow", (dialog, which) -> {
                    pendingJoinButton = btnJoin;
                    pendingEntrantName = entrantName;
                    requestLocationForWaitlistJoin();
                })
                .setNegativeButton("Not now", (dialog, which) -> {
                    finalizeWaitlistJoin(btnJoin, entrantName, null, null);
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Requests location permissions if not already granted.
     */
    private void requestLocationForWaitlistJoin() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocationAndJoin();
            return;
        }

        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Fetches current location and proceeds to finalize join.
     */
    @SuppressLint("MissingPermission")
    private void fetchCurrentLocationAndJoin() {
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        completePendingJoinWithLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        tryLastKnownLocation();
                    }
                })
                .addOnFailureListener(e -> tryLastKnownLocation());
    }

    /**
     * Attempts to fetch the last known location if current location fetch fails.
     */
    @SuppressLint("MissingPermission")
    private void tryLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        completePendingJoinWithLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        fallbackToJoinWithoutLocation("Could not fetch your location");
                    }
                })
                .addOnFailureListener(e -> fallbackToJoinWithoutLocation("Could not fetch your location"));
    }

    /**
     * Completes the join process with provided location data.
     */
    private void completePendingJoinWithLocation(Double latitude, Double longitude) {
        MaterialButton joinButton = pendingJoinButton;
        String entrantName = pendingEntrantName;
        clearPendingJoinState();

        if (joinButton == null || entrantName == null) return;

        finalizeWaitlistJoin(joinButton, entrantName, latitude, longitude);
    }

    /**
     * Fallback to join without location.
     */
    private void fallbackToJoinWithoutLocation() {
        fallbackToJoinWithoutLocation(null);
    }

    /**
     * Fallback to join without location with an optional toast message.
     */
    private void fallbackToJoinWithoutLocation(String message) {
        MaterialButton joinButton = pendingJoinButton;
        String entrantName = pendingEntrantName;
        clearPendingJoinState();

        if (joinButton != null && entrantName != null) {
            finalizeWaitlistJoin(joinButton, entrantName, null, null);

            if (message != null && isAdded()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Clears pending state for joining waitlist.
     */
    private void clearPendingJoinState() {
        pendingEntrantName = null;
        pendingJoinButton = null;
    }

    /**
     * Finalizes the waitlist join by updating Firestore.
     */
    private void finalizeWaitlistJoin(MaterialButton btnJoin, String entrantName,
                                      Double latitude, Double longitude) {
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
                                .addOnSuccessListener(unused2 ->
                                        writeWaitlistEntry(btnJoin, entrantName, latitude, longitude, null))
                                .addOnFailureListener(e ->
                                        writeWaitlistEntry(btnJoin, entrantName, latitude, longitude,
                                                "Joined but failed to update profile: " + e.getMessage()))
                )
                .addOnFailureListener(e -> {
                    btnJoin.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to join waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Writes waitlist entry data to the database.
     */
    private void writeWaitlistEntry(MaterialButton btnJoin, String entrantName,
                                    Double latitude, Double longitude, String partialWarning) {
        waitlistService.joinWaitList(eventId, entrantId, entrantName, latitude, longitude,
                new WaitlistCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        createWaitlistNotification();
                        isOnWaitlist = true;
                        setLeaveWaitlistStyle(btnJoin);
                        btnJoin.setEnabled(true);
                        Toast.makeText(getContext(),
                                partialWarning != null ? partialWarning : "Joined waitlist successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        isOnWaitlist = true;
                        setLeaveWaitlistStyle(btnJoin);
                        btnJoin.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Joined waitlist, but failed to update profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Creates a local notification entry in Firestore when joining a waitlist.
     */
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

    /**
     * Removes the current user from the waitlist in Firestore.
     * @param btnJoin The join button UI element.
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
                                .addOnSuccessListener(unused2 ->
                                        removeWaitlistEntry(btnJoin, "Left waitlist successfully"))
                                .addOnFailureListener(e ->
                                        removeWaitlistEntry(btnJoin,
                                                "Left waitlist, but failed to update profile: " + e.getMessage()))
                )
                .addOnFailureListener(e -> {
                    btnJoin.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to leave waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Removes waitlist map entry data from the database.
     */
    private void removeWaitlistEntry(MaterialButton btnJoin, String baseMessage) {
        waitlistService.leaveWaitList(eventId, entrantId, new WaitlistCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isOnWaitlist = false;
                setJoinWaitlistStyle(btnJoin);
                btnJoin.setEnabled(true);
                Toast.makeText(getContext(), baseMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                isOnWaitlist = false;
                setJoinWaitlistStyle(btnJoin);
                btnJoin.setEnabled(true);
                Toast.makeText(getContext(),
                        baseMessage + " waitlist map data cleanup failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets the join waitlist button style.
     */
    private void setJoinWaitlistStyle(MaterialButton btn) {
        btn.setText("Join Waitlist");
        btn.setBackgroundTintList(ColorStateList.valueOf(0x7ab531));
    }

    /**
     * Sets the leave waitlist button style.
     */
    private void setLeaveWaitlistStyle(MaterialButton btn) {
        btn.setText("Leave Waitlist");
        btn.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        ));
    }

    /**
     * Shows a dialog asking the user if they want to stay in the waitlist after not being selected.
     */
    public void showStayInList(MaterialButton btnJoin) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Not Selected")
                .setMessage("Stay in waiting list?")
                .setPositiveButton("Yes", (dialog, which) ->
                        waitlistService.stayInList(eventId, entrantId, new WaitlistCallback<Void>() {
                            @Override
                            public void onSuccess(Void r) {
                            }

                            @Override
                            public void onError(Exception e) {
                            }
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

    private interface EntrantNameCallBack {
        void onResult(String entrantName);
    }
}
