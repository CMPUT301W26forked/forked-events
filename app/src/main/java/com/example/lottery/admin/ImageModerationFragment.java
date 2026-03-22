package com.example.lottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lottery.R;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.PosterStorageService;
import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ImageModerationFragment extends Fragment {

    private RecyclerView recyclerView;
    private ModerationAdapter adapter;
    private List<ModerationItem> moderationList;
    private FirebaseFirestore db;
    private FSEventRepo repo;
    private PosterStorageService storageService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_moderation, container, false);

        db = FirebaseFirestore.getInstance();
        repo = new FSEventRepo();
        storageService = new PosterStorageService();

        recyclerView = view.findViewById(R.id.rvModerationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moderationList = new ArrayList<>();
        adapter = new ModerationAdapter(moderationList, this::handleRemoveImage);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        loadImagesFromFirestore();

        return view;
    }

    /**
     * Loads all events with poster images from Firestore
     */
    private void loadImagesFromFirestore() {
        db.collection("events")
                .whereGreaterThan("posterUri", "")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moderationList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String eventId = doc.getId();
                        String eventName = doc.getString("name");
                        String posterUri = doc.getString("posterUri");

                        if (eventName != null && posterUri != null) {
                            moderationList.add(new ModerationItem(eventId, eventName, posterUri));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles removal of an image (from Storage and Firestore)
     */
    private void handleRemoveImage(String eventId, int position) {
        // First delete from Firebase Storage
        storageService.deletePoster(eventId, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Then clear the posterUri field from Firestore
                repo.removePosterUrl(eventId, new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Find current index by eventId — position may have shifted during async ops
                        int idx = -1;
                        for (int i = 0; i < moderationList.size(); i++) {
                            if (moderationList.get(i).getEventId().equals(eventId)) {
                                idx = i;
                                break;
                            }
                        }
                        if (idx >= 0) {
                            moderationList.remove(idx);
                            adapter.notifyItemRemoved(idx);
                        }
                        Toast.makeText(getContext(), "Image removed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "Failed to remove from database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to delete image file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
