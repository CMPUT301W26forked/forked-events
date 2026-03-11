package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class WaitlistFragment extends Fragment {

    private Event event;
    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private List<Entrant> entrantList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waitlist, container, false);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView tvTitle = view.findViewById(R.id.tvWaitlistTitle);
        if (event != null) {
            tvTitle.setText(event.getTitle() + " Waitlist");
        }

        recyclerView = view.findViewById(R.id.rvWaitlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        entrantList = new ArrayList<>();
        adapter = new WaitlistAdapter(entrantList);
        recyclerView.setAdapter(adapter);

        fetchWaitlist();

        return view;
    }

    private void fetchWaitlist() {
        if (event == null || event.getId() == null) return;

        db.collection("events").document(event.getId())
                .collection("waiting_list")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Entrant entrant = document.toObject(Entrant.class);
                        entrantList.add(entrant);
                    }
                    adapter.notifyDataSetChanged();
                    if (entrantList.isEmpty()) {
                        Toast.makeText(getContext(), "No entrants in waitlist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}