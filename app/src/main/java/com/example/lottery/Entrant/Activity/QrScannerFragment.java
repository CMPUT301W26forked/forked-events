package com.example.lottery.Entrant.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lottery.Event;
import com.example.lottery.R;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.List;

public class QrScannerFragment extends Fragment {

    private DecoratedBarcodeView barcodeScanner;
    private List<Event> eventList;
    private boolean isScanning = false;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startScanning();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show();
                }
            });

    public QrScannerFragment() {
        super(R.layout.fragment_qr_scanner);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeScanner = view.findViewById(R.id.barcodeScanner);

        // Hide scanner preview at first
        barcodeScanner.setVisibility(View.GONE);
        barcodeScanner.pause();

        eventList = new ArrayList<>();
        eventList.add(new Event(
                "Swimming Lessons - Kids",
                "Open",
                "Fun and safe swimming lessons for children aged 6-10. Learn basic strokes, water safety, and build...",
                "West Side Pool",
                "3/14/2026 - 5/14/2026",
                "20 spots available",
                "Waitlist Open\ncloses 2/16/2026",
                "47 Joined"
        ));
        eventList.add(new Event(
                "Adult Basketball League",
                "Lottery Pending",
                "Fun and safe swimming lessons for children aged 6-10. Learn basic strokes, water safety, and build...",
                "City Gym",
                "4/01/2026 - 6/01/2026",
                "10 spots available",
                "Lottery closes\n3/01/2026",
                "12 Joined"
        ));

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            stopScanning();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EntrantEventsFragment())
                    .commit();

            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNav);

            bottomNav.setSelectedItemId(R.id.nav_events);
        });

        view.findViewById(R.id.btnStartScanning).setOnClickListener(v -> {
            if (!isScanning) {
                checkCameraPermission();
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startScanning() {
        if (barcodeScanner == null) return;

        isScanning = true;
        barcodeScanner.setVisibility(View.VISIBLE);
        barcodeScanner.resume();

        barcodeScanner.decodeSingle(result -> {
            String scannedText = result.getText();

            stopScanning();

            if (scannedText == null || scannedText.trim().isEmpty()) {
                Toast.makeText(requireContext(), "Invalid QR code.", Toast.LENGTH_SHORT).show();
                return;
            }

            findAndOpenEvent(scannedText.trim());
        });
    }

    private void stopScanning() {
        isScanning = false;
        if (barcodeScanner != null) {
            barcodeScanner.pause();
            barcodeScanner.setVisibility(View.GONE);
        }
    }

    private void findAndOpenEvent(String scannedTitle) {
        for (Event event : eventList) {
            if (event.getTitle().equalsIgnoreCase(scannedTitle)) {
                openEventDetails(event);
                return;
            }
        }

        Toast.makeText(requireContext(), "Event not found.", Toast.LENGTH_SHORT).show();
    }

    private void openEventDetails(Event event) {
        Bundle bundle = new Bundle();
        bundle.putString("title", event.getTitle());
        bundle.putString("status", event.getStatus());
        bundle.putString("description", event.getDescription());
        bundle.putString("location", event.getLocation());
        bundle.putString("date", event.getDate());
        bundle.putString("spots", event.getSpots());
        bundle.putString("lotteryInfo", event.getWaitlistInfo());
        bundle.putString("joinedInfo", event.getJoinedCount());

        com.example.lottery.Entrant.Activity.QrEventDetailsFragment fragment = new QrEventDetailsFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScanning();
    }

    @Override
    public void onDestroyView() {
        stopScanning();
        barcodeScanner = null;
        super.onDestroyView();
    }
}