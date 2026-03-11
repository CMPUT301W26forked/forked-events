package com.example.lottery;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventBuilderFragment extends Fragment {

    private String eventId = "test_event";
    private EventService service;
    private FSEventRepo repo;
    private ImageView ivPosterPreview;
    private TextView tvRegPeriod;
    private final ActivityResultLauncher<String> pickImg =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadPoster(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_builder, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnFinish).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        repo = new FSEventRepo();
        service = new EventService(repo, new PosterStorageService());

        ivPosterPreview = view.findViewById(R.id.ivPosterPreview);
        tvRegPeriod = view.findViewById(R.id.btnRegPeriod);

        View btnPickPoster = view.findViewById(R.id.btnPickPoster);

        btnPickPoster.setOnClickListener(v -> pickImg.launch("image/*"));
        tvRegPeriod.setOnClickListener(v -> pickRegistrationPeriod());

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
        pickDateTime(startMillis ->
                pickDateTime(endMillis -> {
                    Timestamp start = new Timestamp(new Date(startMillis));
                    Timestamp end = new Timestamp(new Date(endMillis));

                    service.setRegPeriod(eventId, start, end, new RepoCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(requireContext(), "Registration period saved", Toast.LENGTH_SHORT).show();
                            loadAndRender();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }));
    }

    private void uploadPoster(Uri localUri) {
        service.uploadPosterAndSaveURL(eventId, localUri, new RepoCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(requireContext(), "Poster uploaded", Toast.LENGTH_SHORT).show();
                loadAndRender();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Upload failed: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAndRender() {
        repo.getEvent(eventId, new RepoCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot result) {
                if (!result.exists()) {
                    tvRegPeriod.setText("Unset");
                    return;
                }

                String posterUri = result.getString("posterUri");
                Timestamp start = result.getTimestamp("registrationStart");
                Timestamp end = result.getTimestamp("registrationEnd");

                if (posterUri != null && !posterUri.isEmpty()) {
                    Glide.with(requireContext()).load(posterUri).into(ivPosterPreview);
                }
                tvRegPeriod.setText(formatPeriod(start, end));
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Loaded failed: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPeriod(Timestamp start, Timestamp end) {
        if (start == null || end == null) {
            return "Unset";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(start.toDate()) + " - " + sdf.format(end.toDate());
    }
}
