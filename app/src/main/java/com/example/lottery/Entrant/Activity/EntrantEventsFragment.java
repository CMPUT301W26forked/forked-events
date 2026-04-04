package com.example.lottery.Entrant.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Event;
import com.example.lottery.EventAdapter;
import com.example.lottery.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EntrantEventsFragment extends Fragment {

    private static final String TAG = "EntrantEventsFragment";
    private RecyclerView rvEvents;
    private EditText searchBar;
    private ImageView ivFilter;

    private final ArrayList<Event> allEvents = new ArrayList<>();
    private final ArrayList<Event> filteredEvents = new ArrayList<>();

    private EventAdapter adapter;
    private FirebaseFirestore db;

    // keyword search state
    private String currentKeyword = "";

    // filter state
    private Long availableFromMillis = null;
    private Long availableToMillis = null;
    private boolean onlyShowAvailableSpots = false;

    public EntrantEventsFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        rvEvents = view.findViewById(R.id.recyclerView);
        searchBar = view.findViewById(R.id.search_bar);
        ivFilter = view.findViewById(R.id.ivFilter);

        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(filteredEvents, event -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());

            EntrantEventDetailsFragment detailsFragment = new EntrantEventDetailsFragment();
            detailsFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvEvents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        setupSearch();
        setupFilter();
        loadEvents();

        return view;
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s == null ? "" : s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
    }

    private void setupFilter() {
        ivFilter.setOnClickListener(v -> openFilterDialog());
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            
                            // Log the event name and isPrivate status for debugging
                            Log.d(TAG, "Event: " + event.getTitle() + " | isPrivate: " + event.isPrivate());
                            
                            // Only add to allEvents if it is NOT private
                            if (!event.isPrivate()) {
                                allEvents.add(event);
                            }
                        }
                    }

                    applyFilters();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * This is the key method for the user story:
     * keyword search + filters are applied together.
     */
    private void applyFilters() {
        filteredEvents.clear();

        String keyword = currentKeyword.toLowerCase().trim();

        for (Event event : allEvents) {
            if (matchesKeyword(event, keyword)
                    && matchesAvailability(event)
                    && matchesCapacity(event)) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private boolean matchesKeyword(Event event, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }

        String title = safeLower(event.getTitle());
        String description = safeLower(event.getDescription());
        String location = safeLower(event.getLocation());
        String status = safeLower(event.getStatus());

        return title.contains(keyword)
                || description.contains(keyword)
                || location.contains(keyword)
                || status.contains(keyword);
    }

    private boolean matchesAvailability(Event event) {
        if (availableFromMillis == null && availableToMillis == null) {
            return true;
        }

        Timestamp startTimestamp = event.getRegistrationStart();
        Timestamp endTimestamp = event.getRegistrationEnd();

        if (startTimestamp == null || endTimestamp == null) {
            return false;
        }

        long eventStart = startTimestamp.toDate().getTime();
        long eventEnd = endTimestamp.toDate().getTime();

        long filterStart = (availableFromMillis != null) ? availableFromMillis : Long.MIN_VALUE;
        long filterEnd = (availableToMillis != null) ? availableToMillis : Long.MAX_VALUE;

        // overlap check
        return eventEnd >= filterStart && eventStart <= filterEnd;
    }

    private boolean matchesCapacity(Event event) {
        if (!onlyShowAvailableSpots) {
            return true;
        }

        long totalSpots = event.getTotalSpots();
        long joinedCount = event.getWaitListCount();

        if (totalSpots <= 0) {
            return false;
        }

        // if count is unavailable, assume spots exist if totalSpots > 0
        if (joinedCount < 0) {
            return true;
        }

        return joinedCount < totalSpots;
    }

    private void openFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_event_filter, null);

        EditText etAvailableFrom = dialogView.findViewById(R.id.etAvailableFrom);
        EditText etAvailableTo = dialogView.findViewById(R.id.etAvailableTo);
        CheckBox cbSpotsAvailable = dialogView.findViewById(R.id.cbSpotsAvailable);

        etAvailableFrom.setText(formatDateForInput(availableFromMillis));
        etAvailableTo.setText(formatDateForInput(availableToMillis));
        cbSpotsAvailable.setChecked(onlyShowAvailableSpots);

        etAvailableFrom.setOnClickListener(v ->
                showDatePicker(selectedMillis -> {
                    availableFromMillis = selectedMillis;
                    etAvailableFrom.setText(formatDateForInput(selectedMillis));
                }));

        etAvailableTo.setOnClickListener(v ->
                showDatePicker(selectedMillis -> {
                    availableToMillis = selectedMillis;
                    etAvailableTo.setText(formatDateForInput(selectedMillis));
                }));

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter Events")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    onlyShowAvailableSpots = cbSpotsAvailable.isChecked();

                    if (availableFromMillis != null && availableToMillis != null
                            && availableFromMillis > availableToMillis) {
                        Toast.makeText(getContext(),
                                "Available from date cannot be after available to date",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    applyFilters();
                })
                .setNeutralButton("Clear", (dialog, which) -> {
                    availableFromMillis = null;
                    availableToMillis = null;
                    onlyShowAvailableSpots = false;
                    applyFilters();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    selectedCalendar.set(Calendar.MINUTE, 0);
                    selectedCalendar.set(Calendar.SECOND, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);

                    listener.onDateSelected(selectedCalendar.getTimeInMillis());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private String formatDateForInput(Long millis) {
        if (millis == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.getDefault());
    }

    private interface OnDateSelectedListener {
        void onDateSelected(long millis);
    }
}