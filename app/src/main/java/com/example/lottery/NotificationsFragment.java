package com.example.lottery;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment for notification service
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.rvNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        String uid = FirebaseAuth.getInstance().getUid();
        adapter = new NotificationsAdapter(notificationList, uid, requireContext());
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        loadNotification();

        return view;
    }

    /**
     * load notification
     */
    private void loadNotification() {
        String curUid = FirebaseAuth.getInstance().getUid();
        if (curUid == null) {
            Toast.makeText(requireContext(), "Invalid login status", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(curUid)
                .collection("notification")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(qs -> {
                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : qs) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setNotificationId(doc.getId());
                            notificationList.add(notification);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}