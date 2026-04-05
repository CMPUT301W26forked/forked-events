package com.example.lottery.admin;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lottery.Common.Utils.DeviceManager;
import com.example.lottery.Entrant.Activity.Comment;
import com.example.lottery.Entrant.Activity.CommentsAdapter;
import com.example.lottery.Entrant.Activity.QrDisplayFragment;
import com.example.lottery.Entrant.Repo.WaitlistCallback;
import com.example.lottery.Entrant.Service.EntrantService;
import com.example.lottery.Entrant.Service.WaitlistService;
import com.example.lottery.R;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.RepoCallback;
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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * event detail for admin moderation
 */
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
        commentsAdapter = new CommentsAdapter(commentList, null);
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
            adminActions.setVisibility(expanded ? View.GONE: View.VISIBLE);
            btnToggleActions.setText(expanded ? "Admin Actions" : "Hide Actions");
        });

        view.findViewById(R.id.btnRemoveOrganizer).setOnClickListener(v -> {
            if (organizerId == null || organizerId.isEmpty()) {
                Toast.makeText(getContext(), "Organizer not available", Toast.LENGTH_SHORT).show();
                return;
            }

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, AdminRemoveOrganizerFragment.newInstance(organizerId, organizerName))
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnRemoveEvent).setOnClickListener(v->{
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete "+eventName+"?")
                    .setPositiveButton("Delete", (dialog,which) -> {
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

    /**
     * show edit info
     * @param tvEventName
     * @param tvDescription
     * @param tvStatusTag
     * @param tvLocation
     */
    private void setupEditableFields(TextView tvEventName, TextView tvDescription, TextView tvStatusTag, TextView tvLocation) {
        tvEventName.setOnClickListener(v -> showEditDialog("Edit Event Name", "name", tvEventName));
        tvDescription.setOnClickListener(v -> showEditDialog("Edit Description", "description", tvDescription));
        tvStatusTag.setOnClickListener(v -> showEditDialog("Edit Status", "status", tvStatusTag));
        tvLocation.setOnClickListener(v -> showEditDialog("Edit Location", "location", tvLocation));
    }

    /**
     * show dialog for event info edit
     * @param title
     * @param fieldName
     * @param targetView
     */
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
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancell", null)
                .show();
    }

    /**
     * load comment
     */
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

    /**
     * load event detail
     * @param tvEventName
     * @param tvDescription
     * @param tvStatusTag
     * @param tvTotalSpots
     * @param tvWaitlist
     * @param tvConfirmed
     * @param tvEventDates
     * @param tvLocation
     * @param tvOrganizer
     * @param ivEventPoster
     */
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
                        Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * listener removal
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
    }




}
