package com.example.lottery.Entrant.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Common.Utils.DeviceManager;
import com.example.lottery.Entrant.Model.EntrantInvitation;
import com.example.lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Fragment that displays invitations for an entrant.
 * Loads invitations from Firestore and shows them in a list.
 */
public class EntrantInvitationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private ArrayList<EntrantInvitation> invitationList;
    private EntrantNotificationsAdapter adapter;
    private FirebaseFirestore db;

    public EntrantInvitationsFragment() {
        // Required empty public constructor
    }

    /**
     * Initializes UI and loads invitations.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        rvNotifications = view.findViewById(R.id.rvNotifications);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        invitationList = new ArrayList<>();
        adapter = new EntrantNotificationsAdapter(invitationList, requireContext());
        rvNotifications.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadInvitations();

        return view;
    }

    /**
     * Loads invitations from Firestore for the current user.
     */
    private void loadInvitations() {
        String entrantId = DeviceManager.getDeviceId(requireContext());

        db.collection("invitations")
                .whereEqualTo("entrantId", entrantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    invitationList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        EntrantInvitation invitation = doc.toObject(EntrantInvitation.class);
                        if (invitation != null) {
                            invitation.setInvitationId(doc.getId());
                            invitationList.add(invitation);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load invitations", Toast.LENGTH_SHORT).show()
                );
    }
}