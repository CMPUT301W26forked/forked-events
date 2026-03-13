package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.RepoCallback;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * organizer can send DIY notification/message to entrant
 */
public class SendNotificationsFragment extends Fragment {
    private String EventId;
    private FSEventRepo repo;
    private EditText etmsg;

    public static SendNotificationsFragment newInstance(String eventId) {
        SendNotificationsFragment fragment = new SendNotificationsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_notifications, container, false);

        if (getArguments() != null) {
            EventId = getArguments().getString("event_id");
        }

        repo = new FSEventRepo();
        etmsg = view.findViewById(R.id.etMessage);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnSelectSelected).setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String message = etmsg.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        repo.getPendingEntrantIds(EventId, new RepoCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> ids) {
                repo.sendMessageToPending(EventId, ids, message, new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                        etmsg.setText("");
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
