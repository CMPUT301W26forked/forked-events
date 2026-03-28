package com.example.lottery.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lottery.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * admin fragment for organizer info check and removal
 */
public class AdminRemoveOrganizerFragment extends Fragment {
    private String organizerId;
    private String organizerName;
    private FirebaseFirestore db;
    private TextView tvOrganizerName;
    private TextView tvOrganizerId;
    private TextView tvEventCount;
    private LinearLayout EventList;

    public static AdminRemoveOrganizerFragment newInstance(String organizerId, String organizerName) {
        AdminRemoveOrganizerFragment fragment = new AdminRemoveOrganizerFragment();
        Bundle args = new Bundle();
        args.putString("organizer_id", organizerId);
        args.putString("organizer_name", organizerName);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_remove_organizer, container, false);

        if (getArguments() != null) {
            organizerId = getArguments().getString("organizer_id");
            organizerName = getArguments().getString("organizer_name");
        }

        db = FirebaseFirestore.getInstance();

        tvOrganizerName = view.findViewById(R.id.tvOrganizerName);
        tvOrganizerId = view.findViewById(R.id.tvOrganizerId);
        tvEventCount = view.findViewById(R.id.tvEventCount);
        EventList = view.findViewById(R.id.llEventList);

        tvOrganizerName.setText(organizerName != null ? organizerName : "Unkown name");
        tvOrganizerId.setText(organizerId != null ? organizerId : "Unknown ID");

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnConfirmRemoveOrganizer).setOnClickListener(v ->
                showConfirmDialog()
        );

        loadOrganizerEvents();
        return view;
    }

    /**
     * load organizer events
     */
    private void loadOrganizerEvents() {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(qs -> {
                    EventList.removeAllViews();
                    tvEventCount.setText(String.valueOf(qs.size()));

                    if (qs.isEmpty()) {
                        addEventRow("(No events found)");
                        return;
                    }

                    for (QueryDocumentSnapshot doc: qs) {
                        String eventName = doc.getString("name");
                        addEventRow(eventName != null ? eventName: doc.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load organizer events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * add event row
     * @param text
     */
    private void addEventRow(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(14f);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        tv.setPadding(16, 20, 16, 20);
        tv.setBackgroundResource(R.drawable.bg_toggle_container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 20;
        tv.setLayoutParams(params);
        EventList.addView(tv);
    }

    /**
     * organizer removal
     */
    private void removeOrganizer() {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(qs -> {
                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot doc: qs) {
                        batch.delete(doc.getReference());
                    }

                    Map<String, Object> blockedData = new HashMap<>();
                    blockedData.put("uid", organizerId);
                    blockedData.put("name", organizerName);
                    blockedData.put("removedAt", Timestamp.now());

                    batch.set(db.collection("blocked_organizers").document(organizerId), blockedData);

                    batch.commit()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(requireContext(), "Organizer removed", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.adminFragmentContainer, new AdminDashboardFragment())
                                        .commit();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to remove organizer", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load organizer events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * show removal confirmation
     */
    private void showConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove organizer")
                .setMessage("This will delete all events created by this organizer and block organizer access")
                .setPositiveButton("Confirm", ((dialog, which) -> removeOrganizer()))
                .setNegativeButton("Cancel", null)
                .show();
    }

}
