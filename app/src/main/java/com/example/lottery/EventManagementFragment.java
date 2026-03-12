package com.example.lottery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lottery.Entrant.Activity.ViewListsFragment;
import com.example.lottery.organizer.EventService;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.PosterStorageService;
import com.example.lottery.organizer.RepoCallback;

/**
 * Organizer event management page
 * Allows organizer sampling entrants with status WAITING (notify if selected)
 */
public class EventManagementFragment extends Fragment {

    private String eventId = "test_event";
    private String eventName = "Event";
    private FSEventRepo repo;
    private EventService service;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id", "test_event");
            eventName = getArguments().getString("event_name", "Event");
        }

        repo = new FSEventRepo();
        service = new EventService(repo, new PosterStorageService());

        // Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager().popBackStack();
            }
        });

        View btnRunLottery = view.findViewById(R.id.btnRunLottery);
        btnRunLottery.setOnClickListener(v -> showSampleSizeDiaglog());

        View btnViewLists = view.findViewById(R.id.btnViewLists);
        btnViewLists.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ViewListsFragment.newInstance(eventId, eventName))
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    /**
     * Let organizer inputs the number of entrants to be selected
     */
    private void showSampleSizeDiaglog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter sample size");

        new AlertDialog.Builder(requireContext())
                .setTitle("Sample Size")
                .setView(input)
                .setPositiveButton("Run", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(requireContext(), "Illegal input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int sampleSize = Integer.parseInt(text);

                    service.runLottery(eventId, sampleSize, new RepoCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(requireContext(), "Lottery completed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
