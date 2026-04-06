package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * Fragment that displays a map with the event location and markers for all entrants
 * who have joined the waitlist and shared their geolocation.
 */
public class WaitlistMapFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng defaultEventCenter = new LatLng(53.55, -113.49);
    private static final float defaultZoom = 10.5f;

    private String eventId = "test_event";
    private String eventName = "Event";
    private String eventLocationLabel = "Edmonton, Alberta, Canada";

    private FirebaseFirestore db;
    private GoogleMap googleMap;
    private TextView tvMapSubtitle;

    /**
     * Creates a new instance of WaitlistMapFragment with event details.
     * @param eventId The ID of the event.
     * @param eventName The name of the event.
     * @return A new instance of WaitlistMapFragment.
     */
    public static WaitlistMapFragment newInstance(String eventId, String eventName) {
        WaitlistMapFragment fragment = new WaitlistMapFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("event_name", eventName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the fragment layout and initializes Firestore and subtitle UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waitlist_map, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id", "test_event");
            eventName = getArguments().getString("event_name", "Event");
        }

        db = FirebaseFirestore.getInstance();
        tvMapSubtitle = view.findViewById(R.id.tvMapSubtitle);

        renderSubtitle("Loading map");

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);

        if (mapFragment == null) {
            Toast.makeText(requireContext(), "Map fragment failed to load", Toast.LENGTH_SHORT).show();
            return view;
        }

        mapFragment.getMapAsync(this);
        return view;
    }

    /**
     * Called when the map is ready to be used.
     * Initializes map settings and triggers data loading.
     * @param map The GoogleMap instance.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        loadMapData();
    }

    /**
     * Fetches event location data from Firestore, places the event marker,
     * and moves the camera to the event location.
     */
    private void loadMapData() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || googleMap == null) return;

                    double eventLat = defaultEventCenter.latitude;
                    double eventLon = defaultEventCenter.longitude;

                    if (doc.exists()) {
                        Double storedLat = doc.getDouble("eventLatitude");
                        Double storedLng = doc.getDouble("eventLongitude");
                        String storedLocation = doc.getString("location");

                        if (storedLat != null) eventLat = storedLat;
                        if (storedLng != null) eventLon = storedLng;
                        if (storedLocation != null && !storedLocation.trim().isEmpty()) {
                            eventLocationLabel = storedLocation;
                        }
                    }

                    LatLng eventLatLng = new LatLng(eventLat, eventLon);

                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions()
                            .position(eventLatLng)
                            .title(eventName)
                            .snippet(eventLocationLabel)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, defaultZoom));
                    renderSubtitle("Loading entrant markers");
                    loadEntrantMarkers();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;

                    renderSubtitle("Failed to load event location");
                    Toast.makeText(requireContext(), "Failed to load event map data", Toast.LENGTH_SHORT).show();

                    if (googleMap != null) {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions()
                                .position(defaultEventCenter)
                                .title(eventName)
                                .snippet(eventLocationLabel)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultEventCenter, defaultZoom));
                    }
                });
    }

    /**
     * Fetches the waitlist subcollection for the event and adds markers for entrants
     * who have shared their coordinates.
     */
    private void loadEntrantMarkers() {
        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded() || googleMap == null) return;

                    int sharedLocationCount = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Double latitude = doc.getDouble("latitude");
                        Double longitude = doc.getDouble("longitude");

                        if (latitude == null || longitude == null) {
                            continue;
                        }

                        String entrantName = doc.getString("entrantName");
                        if (entrantName == null || entrantName.trim().isEmpty()) {
                            entrantName = "Waitlisted entrant";
                        }

                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(entrantName)
                                .snippet("Current location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        sharedLocationCount++;
                    }

                    if (sharedLocationCount == 0) {
                        renderSubtitle("No entrants have shared location yet.");
                    } else {
                        renderSubtitle(sharedLocationCount + " entrant location shown.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    renderSubtitle("Failed to load entrant locations.");
                    Toast.makeText(requireContext(), "Failed to load entrant markers", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the subtitle text with event info and a status message.
     * @param baseMessage The status message to append.
     */
    private void renderSubtitle(String baseMessage) {
        if (tvMapSubtitle == null) return;

        String text = eventName + "\n" + eventLocationLabel;
        if (baseMessage != null && !baseMessage.isEmpty()) {
            text += "\n" + baseMessage;
        }
        tvMapSubtitle.setText(text);
    }
}
