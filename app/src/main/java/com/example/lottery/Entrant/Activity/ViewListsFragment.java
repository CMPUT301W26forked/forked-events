package com.example.lottery.Entrant.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewListsFragment extends Fragment {

    private String eventId;
    private String eventName;
    private RecyclerView rvEntrants;
    private EntrantAdapter adapter;
    private List<EntrantInfo> entrantList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvListTitle, btnWaitlist, btnPending, btnFinalList;

    public static ViewListsFragment newInstance(String eventId, String eventName) {
        ViewListsFragment fragment = new ViewListsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("event_name", eventName);
        fragment.setArguments(args);
        return fragment;
    }

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
        rvEntrants = view.findViewById(R.id.rvEntrants);

        rvEntrants.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EntrantAdapter(entrantList);
        rvEntrants.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnWaitlist.setOnClickListener(v -> loadListFromArray("waitlistedEntrantIds", "Waitlist"));
        btnPending.setOnClickListener(v -> loadListFromArray("pendingEntrantIds", "Pending List"));
        btnFinalList.setOnClickListener(v -> loadListFromArray("registeredEntrantIds", "Final List"));

        // Default load
        loadListFromArray("waitlistedEntrantIds", "Waitlist");

        return view;
    }

    /**
     * Logic:
     * 1. Get the event document.
     * 2. Get the array field specified (e.g., waitlistedEntrantIds).
     * 3. For each ID in that array, fetch the user's name from the 'users' collection.
     */
    private void loadListFromArray(String arrayField, String titleSuffix) {
        tvListTitle.setText(eventName + " " + titleSuffix);
        
        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (!eventDoc.exists()) return;

            List<String> entrantIds = (List<String>) eventDoc.get(arrayField);
            if (entrantIds == null || entrantIds.isEmpty()) {
                entrantList.clear();
                adapter.notifyDataSetChanged();
                return;
            }

            // Fetch user names for all IDs in the array
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
                            String uid = userDoc.getId();
                            entrantList.add(new EntrantInfo(uid, name != null ? name : "Unknown User"));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            });
        }).addOnFailureListener(e -> Log.e("ViewLists", "Error loading event", e));
    }

    private static class EntrantInfo {
        String id;
        String name;

        EntrantInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.ViewHolder> {
        private List<EntrantInfo> entrants;

        EntrantAdapter(List<EntrantInfo> entrants) {
            this.entrants = entrants;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvUserName.setText(entrants.get(position).name);
        }

        @Override
        public int getItemCount() {
            return entrants.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserName;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tvUserName);
            }
        }
    }
}
