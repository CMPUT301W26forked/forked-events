package com.example.lottery.Entrant.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ViewListsFragment extends Fragment {

    private String eventId;
    private String eventName;
    private RecyclerView rvEntrants;
    private EntrantAdapter adapter;
    private List<EntrantInfo> entrantList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvListTitle, btnWaitlist, btnPending, btnFinalList, btnCancelled;

    /**
     * Creates a new instance of the fragment with event details.
     * Passes eventId and eventName via bundle arguments.
     */
    public static ViewListsFragment newInstance(String eventId, String eventName) {
        ViewListsFragment fragment = new ViewListsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("event_name", eventName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates layout, initializes UI components, and sets button listeners.
     * Loads default waitlist on first display.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_lists, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");
            eventName = getArguments().getString("event_name", "Event");
        }

        db = FirebaseFirestore.getInstance();

        tvListTitle = view.findViewById(R.id.tvListTitle);
        btnWaitlist = view.findViewById(R.id.btnWaitlist);
        btnPending = view.findViewById(R.id.btnPending);
        btnFinalList = view.findViewById(R.id.btnFinalList);
        btnCancelled = view.findViewById(R.id.btnCancelled);
        rvEntrants = view.findViewById(R.id.rvEntrants);

        rvEntrants.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Set button actions for different entrant lists
        btnWaitlist.setOnClickListener(v -> loadListFromArray("waitlistedEntrantIds", "Waitlist"));
        btnPending.setOnClickListener(v -> loadListFromArray("pendingEntrantIds", "Pending List"));
        btnFinalList.setOnClickListener(v -> loadListFromArray("registeredEntrantIds", "Final List"));
        btnCancelled.setOnClickListener(v -> loadListFromArray("cancelledEntrantIds", "Cancelled List"));

        // Default load
        loadListFromArray("waitlistedEntrantIds", "Waitlist");

        return view;
    }

    /**
     * Loads entrant list from Firestore based on the selected array field.
     * Fetches user details and updates RecyclerView.
     */
    private void loadListFromArray(String arrayField, String titleSuffix) {
        tvListTitle.setText(eventName + " " + titleSuffix);

        boolean isPending = "pendingEntrantIds".equals(arrayField);
        adapter = new EntrantAdapter(entrantList, isPending);
        rvEntrants.setAdapter(adapter);

        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (!eventDoc.exists()) return;

            List<String> entrantIds = (List<String>) eventDoc.get(arrayField);
            if (entrantIds == null || entrantIds.isEmpty()) {
                entrantList.clear();
                adapter.notifyDataSetChanged();
                return;
            }

            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String uid : entrantIds) {
                tasks.add(db.collection("users").document(uid).get());
            }

            Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
                entrantList.clear();
                for (Task<DocumentSnapshot> task : tasks) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userDoc = task.getResult();
                        if (userDoc.exists()) {
                            String name = userDoc.getString("name");
                            String email = userDoc.getString("email");
                            String uid = userDoc.getId();
                            entrantList.add(new EntrantInfo(uid, name != null ? name : "Unknown User", email != null ? email : "No email"));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            });
        }).addOnFailureListener(e -> Log.e("ViewLists", "Error loading event", e));
    }

    /**
     * Moves entrant from pending list to cancelled list in Firestore.
     * Updates UI and shows confirmation message.
     */
    private void cancelEntrant(EntrantInfo entrant, int position) {
        db.collection("events").document(eventId).update(
                "pendingEntrantIds",   FieldValue.arrayRemove(entrant.id),
                "cancelledEntrantIds", FieldValue.arrayUnion(entrant.id)
        ).addOnSuccessListener(v -> {
            entrantList.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(getContext(), entrant.name + " cancelled.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to cancel entrant.", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Simple model class representing entrant information.
     */
    private static class EntrantInfo {
        String id;
        String name;
        String email;

        EntrantInfo(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    /**
     * RecyclerView adapter for displaying entrant list.
     * Optionally shows cancel button for pending entrants.
     */
    private class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.ViewHolder> {
        private List<EntrantInfo> entrants;
        private boolean showCancelButton;

        /**
         * Initializes adapter with entrant data and UI behavior flag.
         */
        EntrantAdapter(List<EntrantInfo> entrants, boolean showCancelButton) {
            this.entrants = entrants;
            this.showCancelButton = showCancelButton;
        }

        /**
         * Inflates item layout for each entrant.
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Binds entrant data to UI elements and handles button actions.
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EntrantInfo entrant = entrants.get(position);
            holder.tvName.setText(entrant.name);
            holder.tvEmail.setText(entrant.email);

            if (showCancelButton) {
                holder.btnView.setVisibility(View.VISIBLE);
                holder.btnView.setText("Remove");
                holder.btnView.setOnClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Remove from Pending")
                            .setMessage("Remove " + entrant.name + " from pending entrants?")
                            .setPositiveButton("Remove", (dialog, which) -> cancelEntrant(entrant, position))
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            } else {
                holder.btnView.setVisibility(View.GONE);
            }
        }

        /**
         * Returns total number of entrants in the list.
         */
        @Override
        public int getItemCount() {
            return entrants.size();
        }

        /**
         * ViewHolder class for holding entrant item views.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail;
            MaterialButton btnView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvProfileName);
                tvEmail = itemView.findViewById(R.id.tvProfileEmail);
                btnView = itemView.findViewById(R.id.btnViewProfile);
            }
        }
    }
}