package com.example.lottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.lottery.R;

public class AdminDashboardFragment extends Fragment {

    private TextView tvAdminId;
    private View cvEventModeration, cvImageModeration, cvProfileModeration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        tvAdminId = view.findViewById(R.id.tvAdminId);
        cvEventModeration = view.findViewById(R.id.cvEventModeration);
        cvImageModeration = view.findViewById(R.id.cvImageModeration);
        cvProfileModeration = view.findViewById(R.id.cvProfileModeration);

        setupClickListeners();

        return view;
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
    }
}
