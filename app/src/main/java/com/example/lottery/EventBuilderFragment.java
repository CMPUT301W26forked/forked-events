package com.example.lottery;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.PlaceAutocomplete;
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * Fragment for creating or editing an event.
 * Allows organizers to set event details, upload posters, and add co-organizers.
 */
public class EventBuilderFragment extends Fragment {
    private String eventId;
    private EventService service;
    private FSEventRepo repo;
    private ImageView ivPosterPreview;
    private TextView tvRegPeriod, tvCoOrganizerName;
    private EditText etEventName, etLocation, etCapacity, etWaitingListLimit, etDescription, etOrganizer;
    private CheckBox cbGeoLocation, cbIsPrivate;
    private Timestamp startTimestamp;
    private Timestamp endTimestamp;
    private String posterUrl;
    private String coOrganizerId;
    private static final String defaultEventLocation = "Edmonton, Alberta, Canada";
    private static final double defaultEventLat = 53.55;
    private static final double defaultEventLon = -113.49;

    private PlacesClient placesClient;
    private String selectedPlaceId;
    private Double selectedEventLat;
    private Double selectedEventLon;
    private String selectedFormattedAddress;


    private final ActivityResultLauncher<String> pickImg =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadPoster(uri);
                }
            });

    private final ActivityResultLauncher<Intent> placeAutocompleteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent intent = result.getData();
                        if (result.getResultCode() == PlaceAutocompleteActivity.RESULT_OK && intent != null) {
                            AutocompletePrediction predication = PlaceAutocomplete.getPredictionFromIntent(intent);
                            AutocompleteSessionToken sessionToken = PlaceAutocomplete.getSessionTokenFromIntent(intent);

                            fetchSelectedPlaceDetails(predication.getPlaceId(), sessionToken);
                            return;
                        }

                        if (result.getResultCode() == PlaceAutocompleteActivity.RESULT_ERROR && intent != null) {
                            Status status = PlaceAutocomplete.getResultStatusFromIntent(intent);
                            Toast.makeText(requireContext(), "Place search failed: " + (status != null? status.getStatusMessage(): "Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("co_organizer_selected", this, (requestKey, result) -> {
            coOrganizerId = result.getString("co_organizer_id");
            String name = result.getString("co_organizer_name");
            if (tvCoOrganizerName != null) {
                tvCoOrganizerName.setText(name != null ? name : "None");
            }
        });
    }

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
        cbIsPrivate = view.findViewById(R.id.cbIsPrivate);
        ivPosterPreview = view.findViewById(R.id.ivPosterPreview);
        tvRegPeriod = view.findViewById(R.id.btnRegPeriod);
        tvCoOrganizerName = view.findViewById(R.id.tvCoOrganizerName);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnFinish).setOnClickListener(v -> saveEvent());
        view.findViewById(R.id.btnPickPoster).setOnClickListener(v -> pickImg.launch("image/*"));
        tvRegPeriod.setOnClickListener(v -> pickRegistrationPeriod());

        view.findViewById(R.id.btnCoOrganizer).setOnClickListener(v -> {
            SearchProfilesFragment fragment = SearchProfilesFragment.newInstance(eventId, etEventName.getText().toString(), true);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        repo = new FSEventRepo();
        service = new EventService(repo, new PosterStorageService());

        initPlacesClient();
        configureLocationPicker();

        loadAndRender();

        return view;
    }

    private interface MillisCallback {
        void onPicked(long millis);
    }

    /**
     * Opens a date and time picker dialog.
     * @param cb Callback invoked with the selected time in milliseconds.
     */
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

    /**
     * Orchestrates picking the start and end of the registration period.
     */
    private void pickRegistrationPeriod() {
        pickDateTime(startMillis -> pickDateTime(endMillis -> {
            startTimestamp = new Timestamp(new Date(startMillis));
            endTimestamp = new Timestamp(new Date(endMillis));
            tvRegPeriod.setText(formatPeriod(startTimestamp, endTimestamp));
        }));
    }

    /**
     * Uploads the selected image as the event poster.
     * @param localUri URI of the local image file.
     */
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

    /**
     * Collects data from UI fields and saves the event to Firestore.
     */
    private void saveEvent() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String visibleLocation = etLocation.getText().toString().trim();
        String finalLocation;
        String finalPlaceId;
        double finalLat;
        double finalLon;

        if (visibleLocation.isEmpty()) {
            finalLocation = defaultEventLocation;
            finalPlaceId = null;
            finalLat = defaultEventLat;
            finalLon = defaultEventLon;
        } else {
            if (selectedEventLat == null || selectedEventLon == null || selectedFormattedAddress == null) {
                etLocation.setError("Illegal input, choose a location");
                Toast.makeText(requireContext(), "Select a suggested location", Toast.LENGTH_SHORT).show();
                return;
            }

            finalLocation = selectedFormattedAddress;
            finalPlaceId = selectedPlaceId;
            finalLat = selectedEventLat;
            finalLon = selectedEventLon;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", etEventName.getText().toString());
        eventData.put("organizer", etOrganizer.getText().toString());
        eventData.put("organizerId", currentUserId);
        
        if (coOrganizerId != null) {
            eventData.put("coOrganizerIds", FieldValue.arrayUnion(coOrganizerId));
        }
        
        eventData.put("description", etDescription.getText().toString());
        eventData.put("location", finalLocation);
        eventData.put("placeId", finalPlaceId);
        eventData.put("eventLatitude", finalLat);
        eventData.put("eventLongitude", finalLon);
        eventData.put("registrationStart", startTimestamp);
        eventData.put("registrationEnd", endTimestamp);
        eventData.put("posterUri", posterUrl);
        eventData.put("status", "open");
        eventData.put("confirmedCount", 0);
        eventData.put("waitlistCount", 0);
        eventData.put("isPrivate", cbIsPrivate.isChecked());
        
        eventData.put("waitlistedEntrantIds", new ArrayList<String>());
        eventData.put("pendingEntrantIds", new ArrayList<String>());
        eventData.put("registeredEntrantIds", new ArrayList<String>());
        eventData.put("cancelledEntrantIds", new ArrayList<String>());
        
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

    /**
     * Loads existing event data if editing an event.
     */
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

                List<String> coOrganizerIds = (List<String>) result.get("coOrganizerIds");
                if (coOrganizerIds != null && !coOrganizerIds.isEmpty()) {
                    coOrganizerId = coOrganizerIds.get(0);
                }

                if (posterUrl != null && !posterUrl.isEmpty()) {
                    Glide.with(requireContext()).load(posterUrl).into(ivPosterPreview);
                }
                tvRegPeriod.setText(formatPeriod(startTimestamp, endTimestamp));
                
                Long totalSpots = result.getLong("totalSpots");
                if (totalSpots != null) etCapacity.setText(String.valueOf(totalSpots));
                
                Long waitListLimit = result.getLong("waitListLimit");
                if (waitListLimit != null) etWaitingListLimit.setText(String.valueOf(waitListLimit));

                Boolean isPrivate = result.getBoolean("isPrivate");
                if (isPrivate != null) cbIsPrivate.setChecked(isPrivate);

                selectedPlaceId = result.getString("placeId");
                selectedFormattedAddress = result.getString("location");
                selectedEventLat = result.getDouble("eventLatitude");
                selectedEventLon = result.getDouble("eventLongitude");

                if (coOrganizerId != null) {
                    FirebaseFirestore.getInstance().collection("users").document(coOrganizerId)
                            .get().addOnSuccessListener(userDoc -> {
                                if (isAdded() && userDoc.exists()) {
                                    tvCoOrganizerName.setText(userDoc.getString("name"));
                                }
                            });
                }
            }
            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Formats a start and end timestamp into a human-readable date range string.
     * @param start Start timestamp.
     * @param end End timestamp.
     * @return Formatted date range string.
     */
    private String formatPeriod(Timestamp start, Timestamp end) {
        if (start == null || end == null) return "Unset";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(start.toDate()) + " - " + sdf.format(end.toDate());
    }

    /**
     * Initializes the Google Places client.
     */
    private void initPlacesClient() {
        String apikey = "YOUR_APIKEY";

        if (TextUtils.isEmpty(apikey)) {
            Toast.makeText(requireContext(), "Places API key is not configured", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(requireContext().getApplicationContext(), apikey);
        }

        placesClient = Places.createClient(requireContext());
    }

    /**
     * Configures the location EditText to launch the Places Autocomplete UI.
     */
    private void configureLocationPicker() {
        etLocation.setFocusable(false);
        etLocation.setFocusableInTouchMode(false);
        etLocation.setClickable(true);
        etLocation.setLongClickable(false);

        etLocation.setOnClickListener(v -> launchPlaceAutocomplete());
    }

    /**
     * Launches the Google Places Autocomplete intent.
     */
    private void launchPlaceAutocomplete() {
        if (placesClient == null) {
            Toast.makeText(requireContext(), "Places is not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        AutocompleteSessionToken sessionToken = AutocompleteSessionToken.newInstance();

        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .setAutocompleteSessionToken(sessionToken)
                .setCountries(Arrays.asList("CA","US"))
                .setInitialQuery(etLocation.getText().toString().trim())
                .build(requireContext());
        placeAutocompleteLauncher.launch(intent);
    }

    /**
     * Fetches detailed information for a selected place from Google Places.
     * @param placeId The ID of the selected place.
     * @param sessionToken The current autocomplete session token.
     */
    private void fetchSelectedPlaceDetails(String placeId, AutocompleteSessionToken sessionToken) {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION
        );
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields)
                .setSessionToken(sessionToken)
                .build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();

                    if (place.getLocation() == null || place.getFormattedAddress() == null) {
                        Toast.makeText(requireContext(), "Selected place is missing location info", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedPlaceId = place.getId();
                    selectedFormattedAddress = place.getFormattedAddress();
                    selectedEventLat = place.getLocation().latitude;
                    selectedEventLon = place.getLocation().longitude;

                    etLocation.setText(selectedFormattedAddress);
                    etLocation.setError(null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load place details", Toast.LENGTH_SHORT).show();
                });
    }
}
