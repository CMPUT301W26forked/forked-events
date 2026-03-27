package com.example.lottery;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment for searching and displaying user profiles
 */
public class SearchProfilesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<EntrantProfile> profileList;
    private List<EntrantProfile> filteredList;
    private FirebaseFirestore db;
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_profiles, container, false);

        db = FirebaseFirestore.getInstance();
        profileList = new ArrayList<>();
        filteredList = new ArrayList<>();

        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.rvProfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProfileAdapter(filteredList, profile -> {
            // handle profile click - navigate to details if needed
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        setupSearch();
        fetchAllProfiles();

        return view;
    }

    /**
     * sets up text listener for search input
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * fetches all user profiles from firestore
     */
    private void fetchAllProfiles() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    profileList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        EntrantProfile profile = doc.toObject(EntrantProfile.class);
                        profile.setId(doc.getId());
                        profileList.add(profile);
                    }
                    filterProfiles(etSearch.getText().toString());
                });
    }

    /**
     * filters the profile list based on query string
     * @param query search text
     */
    private void filterProfiles(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase().trim();

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(profileList);
        } else {
            for (EntrantProfile profile : profileList) {
                String name = profile.getName() != null ? profile.getName().toLowerCase() : "";
                String email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";
                
                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filteredList.add(profile);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
