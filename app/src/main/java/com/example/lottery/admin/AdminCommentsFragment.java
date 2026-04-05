package com.example.lottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.EventComment;
import com.example.lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for managing comments in the admin panel.
 * Lists all comments and allows deleting individual ones.
 */
public class AdminCommentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminCommentAdapter adapter;
    private final List<EventComment> commentList = new ArrayList<>();
    private TextView tvCommentCountLabel;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_comments, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        tvCommentCountLabel = view.findViewById(R.id.tvCommentCountLabel);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Call deleteComment directly instead of showing a confirmation dialog
        adapter = new AdminCommentAdapter(commentList, this::deleteComment);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        loadComments();

        return view;
    }

    /**
     * Loads all comments from Firestore using collectionGroup.
     */
    private void loadComments() {
        db.collectionGroup("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        EventComment comment = doc.toObject(EventComment.class);
                        if (comment != null) {
                            comment.setCommentId(doc.getId());
                            commentList.add(comment);
                        }
                    }
                    if (tvCommentCountLabel != null) {
                        tvCommentCountLabel.setText(String.valueOf(commentList.size()));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Deletes the specified comment from Firestore.
     * Finds the document by ID using collectionGroup and deletes it.
     */
    private void deleteComment(EventComment comment) {
        db.collectionGroup("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        if (doc.getId().equals(comment.getCommentId())) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (isAdded()) {
                                            Toast.makeText(getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                                            loadComments();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isAdded()) {
                                            Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            break;
                        }
                    }
                });
    }
}
