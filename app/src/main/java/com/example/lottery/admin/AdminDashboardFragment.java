package com.example.lottery.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.lottery.R;
import com.example.lottery.Entrant.Activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardFragment extends Fragment {

    private TextView tvAdminId;
    private View cvEventModeration, cvImageModeration, cvProfileModeration;
    private Button btnTempLogout;
    private TextView tvImageModCount, tvEventModCount, tvProfileModCount;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        db = FirebaseFirestore.getInstance();

        tvAdminId = view.findViewById(R.id.tvAdminId);
        cvEventModeration = view.findViewById(R.id.cvEventModeration);
        cvImageModeration = view.findViewById(R.id.cvImageModeration);
        cvProfileModeration = view.findViewById(R.id.cvProfileModeration);
        btnTempLogout = view.findViewById(R.id.btnTempLogout);
        
        tvImageModCount = view.findViewById(R.id.tvImageModCount);
        tvEventModCount = view.findViewById(R.id.tvEventModCount);
        tvProfileModCount = view.findViewById(R.id.tvProfileModCount);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tvAdminId.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        loadModerationCounts();
        setupClickListeners();

        return view;
    }

    /**
     * Loads the counts for different moderation categories from Firestore
     */
    private void loadModerationCounts() {
        // Load Image Moderation Count (Events with posters)
        db.collection("events")
                .whereGreaterThan("posterUri", "")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvImageModCount != null) {
                        tvImageModCount.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load image count", Toast.LENGTH_SHORT).show();
                    }
                });

        // Load Event Moderation Count (Total events)
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvEventModCount != null) {
                        tvEventModCount.setText(String.valueOf(querySnapshot.size()));
                    }
                });

        // Load Profile Moderation Count (Users with profile pictures)
        db.collection("users")
                .whereGreaterThan("profilePictureUri", "")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvProfileModCount != null) {
                        tvProfileModCount.setText(String.valueOf(querySnapshot.size()));
                    }
                });
    }

    private void setupClickListeners() {
        cvEventModeration.setOnClickListener(v -> {
            // Navigate to Event Moderation
        });

        cvImageModeration.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, new ImageModerationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cvProfileModeration.setOnClickListener(v -> {
            // Navigate to Profile Moderation
        });

        btnTempLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
