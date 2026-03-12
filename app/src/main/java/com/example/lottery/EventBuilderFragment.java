package com.example.lottery;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lottery.organizer.EventService;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.PosterStorageService;
import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * Organizer event setup/edit page
 * Organizer can upload/update an event poster; Set the registration start/end time;
 */
public class EventBuilderFragment extends Fragment {
    private String eventId;
    private EventService service;
    private FSEventRepo repo;
    private ImageView ivPosterPreview;
    private TextView tvRegPeriod;
    private EditText etEventName, etLocation, etCapacity, etWaitingListLimit, etDescription, etOrganizer;
    private CheckBox cbGeoLocation;

    private Timestamp startTimestamp;
    private Timestamp endTimestamp;
    private String posterUrl;

    private final ActivityResultLauncher<String> pickImg =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadPoster(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");
        }

        if (eventId == null || eventId.isEmpty() || eventId.equals("test_event")) {
            eventId = FirebaseFirestore.getInstance().collection("events").document().getId();
        }

        View view = inflater.inflate(R.layout.fragment_event_builder, container, false);

        etEventName = view.findViewById(R.id.etEventName);
        etOrganizer = view.findViewById(R.id.etOrganizer);
        etDescription = view.findViewById(R.id.etDescription);
        etLocation = view.findViewById(R.id.etLocation);
        etCapacity = view.findViewById(R.id.etCapacity);
        etWaitingListLimit = view.findViewById(R.id.etWaitingListLimit);
        cbGeoLocation = view.findViewById(R.id.cbGeoLocation);
        ivPosterPreview = view.findViewById(R.id.ivPosterPreview);
        tvRegPeriod = view.findViewById(R.id.btnRegPeriod);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnFinish).setOnClickListener(v -> saveEvent());
        view.findViewById(R.id.btnPickPoster).setOnClickListener(v -> pickImg.launch("image/*"));
        tvRegPeriod.setOnClickListener(v -> pickRegistrationPeriod());

        repo = new FSEventRepo();
        service = new EventService(repo, new PosterStorageService());

        loadAndRender();

        return view;
    }

    private interface MillisCallback {
        void onPicked(long millis);
    }

    private void pickDateTime(MillisCallback cb) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            new TimePickerDialog(requireContext(), (tp, hour, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cb.onPicked(cal.getTimeInMillis());
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickRegistrationPeriod() {
        pickDateTime(startMillis -> pickDateTime(endMillis -> {
            startTimestamp = new Timestamp(new Date(startMillis));
            endTimestamp = new Timestamp(new Date(endMillis));
            tvRegPeriod.setText(formatPeriod(startTimestamp, endTimestamp));
        }));
    }

    private void uploadPoster(Uri localUri) {
        service.uploadPoster(eventId, localUri, new RepoCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (!isAdded()) return;
                posterUrl = result;
                Glide.with(requireContext()).load(posterUrl).into(ivPosterPreview);
                Toast.makeText(requireContext(), "Poster uploaded", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEvent() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", etEventName.getText().toString());
        eventData.put("organizer", etOrganizer.getText().toString());
        eventData.put("organizerId", currentUserId);
        eventData.put("description", etDescription.getText().toString());
        eventData.put("location", etLocation.getText().toString());
        eventData.put("registrationStart", startTimestamp);
        eventData.put("registrationEnd", endTimestamp);
        eventData.put("posterUri", posterUrl);
        eventData.put("status", "open");
        eventData.put("confirmedCount", 0);
        eventData.put("waitlistCount", 0);
        
        // Arrays for entrant tracking
        eventData.put("waitlistedEntrantIds", new ArrayList<String>());
        eventData.put("pendingEntrantIds", new ArrayList<String>()); // For Pending list
        eventData.put("registeredEntrantIds", new ArrayList<String>()); // For Final list
        eventData.put("cancelledEntrantIds", new ArrayList<String>()); // For Cancelled list
        
        // Handle numerical fields
        try {
            String capacityStr = etCapacity.getText().toString();
            eventData.put("totalSpots", capacityStr.isEmpty() ? 0 : Integer.parseInt(capacityStr));
            
            String limitStr = etWaitingListLimit.getText().toString();
            eventData.put("waitListLimit", limitStr.isEmpty() ? 50 : Integer.parseInt(limitStr));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        service.saveEventWithOrganizer(currentUserId, eventId, eventData, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Event saved", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAndRender() {
        repo.getEvent(eventId, new RepoCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot result) {
                if (!isAdded() || !result.exists()) return;

                etEventName.setText(result.getString("name"));
                etOrganizer.setText(result.getString("organizer"));
                etDescription.setText(result.getString("description"));
                etLocation.setText(result.getString("location"));
                
                posterUrl = result.getString("posterUri");
                startTimestamp = result.getTimestamp("registrationStart");
                endTimestamp = result.getTimestamp("registrationEnd");

                if (posterUrl != null && !posterUrl.isEmpty()) {
                    Glide.with(requireContext()).load(posterUrl).into(ivPosterPreview);
                }
                tvRegPeriod.setText(formatPeriod(startTimestamp, endTimestamp));
                
                Long totalSpots = result.getLong("totalSpots");
                if (totalSpots != null) etCapacity.setText(String.valueOf(totalSpots));
                
                Long waitListLimit = result.getLong("waitListLimit");
                if (waitListLimit != null) etWaitingListLimit.setText(String.valueOf(waitListLimit));
            }
            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPeriod(Timestamp start, Timestamp end) {
        if (start == null || end == null) return "Unset";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(start.toDate()) + " - " + sdf.format(end.toDate());
    }
}
