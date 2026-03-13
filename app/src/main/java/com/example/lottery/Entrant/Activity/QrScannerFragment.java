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

import com.example.lottery.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QrScannerFragment extends Fragment {

    private DecoratedBarcodeView barcodeScanner;
    private boolean isScanning = false;
    private FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();
        barcodeScanner = view.findViewById(R.id.barcodeScanner);

        barcodeScanner.setVisibility(View.GONE);
        barcodeScanner.pause();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            stopScanning();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EntrantEventsFragment())
                    .commit();

            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_events);
            }
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

    private void findAndOpenEvent(String scannedValue) {
        String scannedEventId = extractEventId(scannedValue);

        if (scannedEventId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid event QR.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(scannedEventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), "Event not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    openEventDetails(scannedEventId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load event.", Toast.LENGTH_SHORT).show()
                );
    }

    private String extractEventId(String scannedValue) {
        if (scannedValue.startsWith("EVENT_ID:")) {
            return scannedValue.substring("EVENT_ID:".length()).trim();
        }
        return scannedValue.trim();
    }

    private void openEventDetails(String eventId) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);

        EntrantEventDetailsFragment fragment = new EntrantEventDetailsFragment();
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