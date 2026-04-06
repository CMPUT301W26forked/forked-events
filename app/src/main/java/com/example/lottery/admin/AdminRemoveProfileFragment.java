package com.example.lottery.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminRemoveProfileFragment extends Fragment {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String profilePictureUri;

    private FirebaseFirestore db;
    private AlertDialog confirmDialog;

    /**
     * Creates a new instance of this fragment with user data passed as arguments.
     * This is used when navigating to the remove profile screen from admin panel.
     *
     * @param userId         Unique identifier for the user
     * @param name           Display name of the user
     * @param email          Email address of the user
     * @param phone          Phone number of the user
     * @param role           Role assigned to the user
     * @param profilePictureUri URI string pointing to the user's profile picture
     */
    public static AdminRemoveProfileFragment newInstance(String userId, String name, String email,
                                                         String phone, String role, String profilePictureUri) {
        AdminRemoveProfileFragment fragment = new AdminRemoveProfileFragment();
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        args.putString("name", name);
        args.putString("email", email);
        args.putString("phone", phone);
        args.putString("role", role);
        args.putString("profile_picture_uri", profilePictureUri);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the UI, retrieves passed user data, and sets up button listeners.
     * Also loads profile image and displays user information.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_remove_profile, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("user_id");
            name = getArguments().getString("name");
            email = getArguments().getString("email");
            phone = getArguments().getString("phone");
            role = getArguments().getString("role");
            profilePictureUri = getArguments().getString("profile_picture_uri");
        }

        db = FirebaseFirestore.getInstance();

        ImageView ivProfile = view.findViewById(R.id.ivProfile);
        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvUid = view.findViewById(R.id.tvProfileUid);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvPhone = view.findViewById(R.id.tvProfilePhone);
        TextView tvRole = view.findViewById(R.id.tvProfileRole);

        tvName.setText(!TextUtils.isEmpty(name) ? name : "Unnamed User");
        tvUid.setText(!TextUtils.isEmpty(userId) ? userId : "Unknown ID");
        tvEmail.setText(!TextUtils.isEmpty(email) ? email : "No email");
        tvPhone.setText(!TextUtils.isEmpty(phone) ? phone : "No phone");
        tvRole.setText(!TextUtils.isEmpty(role) ? role : "entrant");

        if (!TextUtils.isEmpty(profilePictureUri)) {
            Glide.with(requireContext())
                    .load(profilePictureUri)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .centerCrop()
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_nav_profile);
        }

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnConfirmRemoveProfile).setOnClickListener(v ->
                showConfirmDialog());

        return view;
    }

    /**
     * Displays a confirmation dialog before deleting the user profile.
     * Ensures admin explicitly confirms destructive action.
     */
    private void showConfirmDialog() {
        if (!isAdded()) {
            return;
        }

        dismissConfirmDialog();

        confirmDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Remove profile")
                .setMessage("This will permanently delete the profile.")
                .setPositiveButton("Confirm", (dialog, which) -> removeProfile())
                .setNegativeButton("Cancel", null)
                .create();
        confirmDialog.show();
    }

    private void dismissConfirmDialog() {
        if (confirmDialog != null) {
            confirmDialog.dismiss();
            confirmDialog = null;
        }
    }

    /**
     * Deletes the user profile and cleans up all related data (notifications, event references).
     * Uses Firestore batch operations to ensure all deletions/updates happen together.
     */
    private void removeProfile() {
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(getContext(), "User ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // First get user's notifications, then all events, then do one batch
        db.collection("users")
                .document(userId)
                .collection("notification")
                .get()
                .addOnSuccessListener(notificationSnapshot ->

                        db.collection("events")
                                .get()
                                .addOnSuccessListener(eventSnapshot -> {
                                    WriteBatch batch = db.batch();

                                    // delete user's notifications
                                    for (QueryDocumentSnapshot notificationDoc : notificationSnapshot) {
                                        batch.delete(notificationDoc.getReference());
                                    }

                                    // clean up event references
                                    for (DocumentSnapshot eventDoc : eventSnapshot.getDocuments()) {
                                        String eventId = eventDoc.getId();

                                        List<String> confirmed = (List<String>) eventDoc.get("confirmedEntrantIds");
                                        List<String> invited = (List<String>) eventDoc.get("invitedEntrantIds");
                                        List<String> waitlisted = (List<String>) eventDoc.get("waitlistedEntrantIds");

                                        Map<String, Object> updates = new HashMap<>();
                                        boolean shouldUpdate = false;

                                        if (confirmed != null && confirmed.contains(userId)) {
                                            updates.put("confirmedEntrantIds", FieldValue.arrayRemove(userId));
                                            updates.put("confirmedCount", Math.max(0, confirmed.size() - 1));
                                            shouldUpdate = true;
                                        }

                                        if (invited != null && invited.contains(userId)) {
                                            updates.put("invitedEntrantIds", FieldValue.arrayRemove(userId));
                                            shouldUpdate = true;
                                        }

                                        if (waitlisted != null && waitlisted.contains(userId)) {
                                            updates.put("waitlistedEntrantIds", FieldValue.arrayRemove(userId));
                                            updates.put("waitlistCount", Math.max(0, waitlisted.size() - 1));
                                            shouldUpdate = true;
                                        }

                                        if (shouldUpdate) {
                                            batch.update(eventDoc.getReference(), updates);
                                        }

                                        // remove waitlist subcollection document if it exists
                                        batch.delete(
                                                db.collection("events")
                                                        .document(eventId)
                                                        .collection("waitlist")
                                                        .document(userId)
                                        );
                                    }

                                    // delete main user document last
                                    batch.delete(db.collection("users").document(userId));

                                    batch.commit()
                                            .addOnSuccessListener(unused -> {
                                                dismissConfirmDialog();
                                                Toast.makeText(requireContext(), "Profile removed", Toast.LENGTH_SHORT).show();
                                                getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                getParentFragmentManager().beginTransaction()
                                                        .replace(R.id.adminFragmentContainer, new AdminDashboardFragment())
                                                        .commit();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(requireContext(), "Failed to remove profile", Toast.LENGTH_SHORT).show()
                                            );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                                )

                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load user notifications", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onDestroyView() {
        dismissConfirmDialog();
        super.onDestroyView();
    }
}
