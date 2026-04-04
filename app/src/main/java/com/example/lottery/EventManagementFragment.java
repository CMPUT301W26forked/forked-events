package com.example.lottery;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.lottery.Entrant.Activity.ViewListsFragment;
import com.example.lottery.organizer.EventService;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.PosterStorageService;
import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Organizer event management page
 * Allows organizer sampling entrants with status WAITING (notify if selected)
 */
public class EventManagementFragment extends Fragment {

    private String eventId = "test_event";
    private String eventName = "Event";
    private FSEventRepo repo;
    private EventService service;
    private long waitlistCount = 0;
    private long waitlistLimit = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);

        repo = new FSEventRepo();
        service = new EventService(repo, new PosterStorageService());
        if (getArguments() != null) {
            eventId = getArguments().getString("event_id", "test_event");
            eventName = getArguments().getString("event_name", "Event");
        }
        loadWaitinglistInfo();

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

        // btn jump to message/notification
        View btnToMessage = view.findViewById(R.id.btnToMessage);
        btnToMessage.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SendNotificationsFragment.newInstance(eventId, eventName))
                    .addToBackStack(null)
                    .commit();
        });

        // Export CSV
        View btnExportCsv = view.findViewById(R.id.btnExportCsv);
        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> exportEntrantsToCsv());
        }

        // Invite Entrants
        View btnInvite = view.findViewById(R.id.btnInvite);
        if (btnInvite != null) {
            btnInvite.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, SearchProfilesFragment.newInstance(eventId, eventName))
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    private void exportEntrantsToCsv() {
        repo.getEvent(eventId, new RepoCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc != null && doc.exists()) {
                    List<String> waitlisted = (List<String>) doc.get("waitlistedEntrantIds");
                    List<String> registered = (List<String>) doc.get("registeredEntrantIds");
                    List<String> pending = (List<String>) doc.get("pendingEntrantIds");
                    List<String> cancelled = (List<String>) doc.get("cancelledEntrantIds");

                    StringBuilder csvContent = new StringBuilder();
                    csvContent.append("List Type,Entrant ID\n");

                    appendIdsToCsv(csvContent, "Waitlisted", waitlisted);
                    appendIdsToCsv(csvContent, "Registered", registered);
                    appendIdsToCsv(csvContent, "Pending", pending);
                    appendIdsToCsv(csvContent, "Cancelled", cancelled);

                    saveAndShareCsv(csvContent.toString());
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to fetch event data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void appendIdsToCsv(StringBuilder sb, String type, List<String> ids) {
        if (ids != null) {
            for (String id : ids) {
                sb.append(type).append(",").append(id).append("\n");
            }
        }
    }

    private void saveAndShareCsv(String content) {
        String fileName = "entrants_" + eventId + ".csv";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = requireContext().getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream os = resolver.openOutputStream(uri)) {
                        if (os != null) {
                            os.write(content.getBytes());
                            Toast.makeText(requireContext(), "CSV saved to Downloads folder", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else {

                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) downloadDir.mkdirs();
                File file = new File(downloadDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(content.getBytes());
                    Toast.makeText(requireContext(), "CSV saved to Downloads", Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            Log.e("CSV_EXPORT", "Error saving CSV", e);
            Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Let organizer inputs the number of entrants to be selected
     */
    private void showSampleSizeDiaglog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Current waitlist: " + waitlistCount + " / " + waitlistLimit);

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

                    service.runLottery(eventId, eventName, sampleSize, new RepoCallback<Void>() {
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

    /**
     * loading waitlist info for hint
     */
    private void loadWaitinglistInfo() {
        repo.getEvent(eventId, new RepoCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc != null && doc.exists()) {
                    Long count = doc.getLong("waitlistCount");
                    Long limit = doc.getLong("waitListLimit");
                    waitlistCount = count != null ? count : 0;
                    waitlistLimit = limit != null ? limit : 0;
                }
            }

            @Override
            public void onError(Exception e) {
                waitlistLimit = 0;
                waitlistCount = 0;
            }
        });
    }
}
