package com.example.lottery.admin;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileModerationFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvProfileCount;
    private AdminProfileAdapter adapter;
    private final List<AdminProfileItem> profileList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile_moderation, container, false);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        tvProfileCount = view.findViewById(R.id.tvProfileCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminProfileAdapter(profileList, item -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, AdminRemoveProfileFragment.newInstance(
                            item.getUid(),
                            item.getName(),
                            item.getEmail(),
                            item.getPhone(),
                            item.getRole(),
                            item.getProfilePictureUri()
                    ))
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        loadProfiles();

        return view;
    }

    private void loadProfiles() {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    profileList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String role = doc.getString("role");

                        // organizers are handled by organizer removal
                        if ("admin".equalsIgnoreCase(role) || "organizer".equalsIgnoreCase(role)) {
                            continue;
                        }

                        String uid = doc.getId();
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String profilePictureUri = doc.getString("profilePictureUri");

                        profileList.add(new AdminProfileItem(
                                uid,
                                TextUtils.isEmpty(name) ? "" : name,
                                TextUtils.isEmpty(email) ? "" : email,
                                TextUtils.isEmpty(phone) ? "" : phone,
                                TextUtils.isEmpty(role) ? "entrant" : role,
                                TextUtils.isEmpty(profilePictureUri) ? "" : profilePictureUri
                        ));
                    }

                    tvProfileCount.setText(String.valueOf(profileList.size()));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load profiles", Toast.LENGTH_SHORT).show()
                );
    }
}