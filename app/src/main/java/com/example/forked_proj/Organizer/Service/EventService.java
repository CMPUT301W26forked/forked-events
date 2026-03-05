package com.example.forked_proj.Organizer.Service;

import android.net.Uri;

import com.example.forked_proj.Organizer.Repo.EventRepo;
import com.example.forked_proj.Organizer.Repo.RepoCallback;
import com.google.firebase.Timestamp;

public class EventService {
    private final EventRepo repo;
    private final PosterStorageService posterStorage;

    public EventService(EventRepo repo, PosterStorageService posterStorage) {
        this.repo = repo;
        this.posterStorage = posterStorage;
    }

    public void setRegPeriod(String eventId, Timestamp start, Timestamp end, RepoCallback<Void> cb) {
        if (start == null || end == null) {
            cb.onError(new IllegalArgumentException("start/end required"));
            return;
        }
        if (start.compareTo(end) >= 0) {
            cb.onError(new IllegalArgumentException("illegal start/end time"));
            return;
        }
        repo.setRegStartPeriod(eventId, start, end, cb);
    }

    public void uploadPosterAndSaveURL(String eventId, Uri localUri, RepoCallback<String> cb) {
        if (localUri == null) {
            cb.onError(new IllegalArgumentException("localUri required"));
            return;
        }

        posterStorage.uploadPoster(eventId, localUri, new RepoCallback<Uri>() {
            @Override
            public void onSuccess(Uri downloadURL) {
                String url = downloadURL.toString();
                repo.setPosterUrl(eventId, url, new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {cb.onSuccess(url);}
                    @Override
                    public void onError(Exception e) {cb.onError(e);}
                });
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

}
