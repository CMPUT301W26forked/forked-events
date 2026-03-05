package com.example.forked_proj.Organizer.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.forked_proj.MainActivity;
import com.example.forked_proj.Organizer.Repo.FSEventRepo;
import com.example.forked_proj.Organizer.Repo.RepoCallback;
import com.example.forked_proj.Organizer.Service.EventService;
import com.example.forked_proj.Organizer.Service.PosterStorageService;
import com.example.forked_proj.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity {
    //test use
    private final String eventId = "test_event";

    private EventService service;
    private FSEventRepo repo;

    private ImageView posterView;
    private TextView tvRegPeriod;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                uploadPoster(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_editor);

        repo = new FSEventRepo();
        service = new EventService(repo, new PosterStorageService());

        posterView = findViewById(R.id.posterView);
        tvRegPeriod = findViewById(R.id.tvRegPeriod);

        Button btn_PickPoster = findViewById(R.id.btn_PickPoster);
        btn_PickPoster.setOnClickListener(v -> pickImage.launch("image/*"));

        Button btn_SetPeriod = findViewById(R.id.btn_SetPeriod);
        btn_SetPeriod.setOnClickListener(v -> setPeriod());

        Intent ToMain = new Intent(EditorActivity.this, MainActivity.class);
        Button btn_ToEventEditor = findViewById(R.id.btn_edb);
        btn_ToEventEditor.setOnClickListener(v -> startActivity(ToMain));

        loadAndRender();
    }

    private interface MillisCallback {void onPicked(long millis); }
    private void pickDateTime(MillisCallback cb) {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);

            new TimePickerDialog(this, (tp, hour, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cb.onPicked(cal.getTimeInMillis());
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void setPeriod() {
        pickDateTime(startMillis -> {
            pickDateTime(endMillis -> {
                Timestamp start = new Timestamp(new Date(startMillis));
                Timestamp end = new Timestamp(new Date(endMillis));

                service.setRegPeriod(eventId, start, end, new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getApplicationContext(), "Registration saved", Toast.LENGTH_SHORT).show();
                        loadAndRender();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            });
        });
    }
    private void uploadPoster(Uri localUri) {
        service.uploadPosterAndSaveURL(eventId, localUri, new RepoCallback<String>() {
            @Override
            public void onSuccess(String ignored) {
                Toast.makeText(getApplicationContext(), "Poster uploaded", Toast.LENGTH_SHORT).show();
                loadAndRender();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAndRender() {
        repo.getEvent(eventId, new RepoCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (!doc.exists()) {
                    tvRegPeriod.setText("Registration: (event doc not found yet)");
                    return;
                }

                String posterURL = doc.getString("posterUri");
                Timestamp start = doc.getTimestamp("registrationStart");
                Timestamp end = doc.getTimestamp("registrationEnd");

                if (posterURL != null && !posterURL.isEmpty()) {
                    Glide.with(EditorActivity.this)
                            .load(posterURL)
                            .into(posterView);
                } else {
                    posterView.setImageResource(android.R.color.darker_gray);
                }
                tvRegPeriod.setText(formatPeriod(start, end));
            }


            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), "Loaded failed: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPeriod(Timestamp start, Timestamp end) {
        if (start == null || end == null) {
            return "Registration: (unset)";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return "Registration: " + sdf.format(start.toDate()) + " to " + sdf.format(end.toDate());
    }
}
