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
 * organizer can send DIY notification/message to entrants
 */
public class SendNotificationsFragment extends Fragment {
    private String EventId;
    private String EventName = "Event";
    private FSEventRepo repo;
    private EditText etmsg;

    public static SendNotificationsFragment newInstance(String eventId, String EventName) {
        SendNotificationsFragment fragment = new SendNotificationsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("eventName", EventName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_notifications, container, false);

        if (getArguments() != null) {
            EventId = getArguments().getString("event_id");
            EventName = getArguments().getString("eventName", "Event");
        }

        repo = new FSEventRepo();
        etmsg = view.findViewById(R.id.etMessage);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnSelectCancelled).setOnClickListener(v -> sendMessage(true));
        view.findViewById(R.id.btnSelectSelected).setOnClickListener(v -> sendMessage(false));

        return view;
    }

    public void setRepo(FSEventRepo repo) {
        this.repo = repo;
    }

    public void setEtmsg(EditText etmsg) {
        this.etmsg = etmsg;
    }

    /**
     * send message to specified group
     * @param sendToCancelled
     */
    public void sendMessage(Boolean sendToCancelled) {
        String message = etmsg.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sendToCancelled) {
            repo.getRegisteredEntrantIds(EventId, new RepoCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> ids) {
                    repo.sendMessageToEntrant(EventId, EventName, ids, message, "selected", new RepoCallback<Void>() {
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
        } else {
            repo.getCancelledEntrantIds(EventId, new RepoCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> ids) {
                    repo.sendMessageToEntrant(EventId, EventName, ids, message, "cancelled", new RepoCallback<Void>() {
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
}
