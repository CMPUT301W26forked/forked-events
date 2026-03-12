package com.example.lottery.organizer;

import android.net.Uri;

import com.example.lottery.organizer.EventRepo;
import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.Timestamp;

import java.util.Collections;
import java.util.List;

/**
 * Logic for organizer event management
 * Validates input and coordinates with sorage/FS
 */
public class EventService {
    private final EventRepo repo;
    private final PosterStorageService posterStorage;

    public EventService(EventRepo repo, PosterStorageService posterStorage) {
        this.repo = repo;
        this.posterStorage = posterStorage;
    }

    /**
     * validates registration period and updates the event document
     * @param eventId
     * @param start
     * @param end
     * @param cb
     */
    public void setRegPeriod(String eventId, Timestamp start, Timestamp end, RepoCallback<Void> cb) {
        if (start == null || end == null) {
            cb.onError(new IllegalArgumentException("start/end required"));
            return;
        }
        // input protection
        if (start.compareTo(end) >= 0) {
            cb.onError(new IllegalArgumentException("illegal start/end time"));
            return;
        }
        repo.setRegStartPeriod(eventId, start, end, cb);
    }

    /**
     * uploads event poster to storage and saves the download URI in event document
     * @param eventId
     * @param localUri
     * @param cb
     */
    public void uploadPosterAndSaveURL(String eventId, Uri localUri, RepoCallback<String> cb) {
        if (localUri == null) {
            cb.onError(new IllegalArgumentException("localUri required"));
            return;
        }

        posterStorage.uploadPoster(eventId, localUri, new RepoCallback<Uri>() {
            @Override
            public void onSuccess(Uri downloadURI) {
                String url = downloadURI.toString();
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

    /**
     * run lottery/sampling service by randomly selecting a specified number of waiting entrants
     * selected entrants are marked as SELECTED
     * @param eventId
     * @param sampleSize
     * @param cb
     */
    public void runLottery(String eventId, int sampleSize, RepoCallback<Void> cb) {
        if (sampleSize <= 0) {
            cb.onError(new IllegalArgumentException("Illegal sample size"));
            return;
        }

        repo.getWaitingEntrantIds(eventId, new RepoCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> ids) {
                if (ids.isEmpty()) {
                    cb.onError(new IllegalArgumentException("No waiting entrants"));
                    return;
                }
                // random select & value protection
                Collections.shuffle(ids);
                int count = Math.min(sampleSize, ids.size());

                if (count == 0) {
                    cb.onSuccess(null);
                    return;
                }

                final int[] done = {0};
                final boolean[] failed = {false};

                for (int i = 0; i < count; i ++) {
                    String entrantId = ids.get(i);

                    repo.markEntrantSelected(eventId, entrantId, new RepoCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // entrant will be notified if selected
                            repo.createNotification(eventId,
                                    entrantId,
                                    "You have been selected for the event",
                                    new RepoCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            if (failed[0]) return;
                                            done[0]++;
                                            if (done[0] == count) {
                                                cb.onSuccess(null);
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            if (failed[0]) return;
                                            failed[0] = true;
                                            cb.onError(e);
                                        }
                                    });
                        }

                        @Override
                        public void onError(Exception e) {
                            if (failed[0]) return;
                            failed[0] = true;
                            cb.onError(e);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

}
