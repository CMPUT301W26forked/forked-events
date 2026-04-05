package com.example.lottery.organizer;

import android.net.Uri;
import android.provider.Settings;

import com.example.lottery.EventComment;
import com.example.lottery.organizer.EventRepo;
import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.Timestamp;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Logic for organizer event management
 * Validates input and coordinates with storage/FS
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
     * Uploads poster to storage but does NOT save to database.
     */
    public void uploadPoster(String eventId, Uri localUri, RepoCallback<String> cb) {
        if (localUri == null) {
            cb.onError(new IllegalArgumentException("localUri required"));
            return;
        }
        posterStorage.uploadPoster(eventId, localUri, new RepoCallback<Uri>() {
            @Override
            public void onSuccess(Uri downloadURI) {
                cb.onSuccess(downloadURI.toString());
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

    public void saveEvent(String eventId, Map<String, Object> eventData, RepoCallback<Void> cb) {
        Timestamp start = (Timestamp) eventData.get("registrationStart");
        Timestamp end = (Timestamp) eventData.get("registrationEnd");
        
        if (start != null && end != null && start.compareTo(end) >= 0) {
            cb.onError(new IllegalArgumentException("illegal start/end time"));
            return;
        }
        repo.saveEvent(eventId, eventData, cb);
    }

    public void saveEventWithOrganizer(String userId, String eventId, Map<String, Object> eventData, RepoCallback<Void> cb) {
        saveEvent(eventId, eventData, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                String eventName = (String) eventData.get("name");
                repo.linkEventToOrganizer(userId, eventId, eventName, cb);
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
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
     * run lottery/sampling service by randomly selecting a specified number of waitingEntrantIds
     * selected entrants are moved to pendingEntrantIds
     * @param eventId
     * @param sampleSize
     * @param cb
     */
    public void runLottery(String eventId, String eventName, int sampleSize, RepoCallback<Void> cb) {
        if (sampleSize <= 0) {
            cb.onError(new IllegalArgumentException("Illegal sample size"));
            return;
        }

        repo.getWaitingEntrantIds(eventId, new RepoCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> rawIds) {
                if (rawIds == null || rawIds.isEmpty()) {
                    cb.onError(new IllegalArgumentException("No waiting entrants"));
                    return;
                }

                List<String> ids = new ArrayList<>(rawIds);
                // random select & value protection
                Collections.shuffle(ids, new SecureRandom());
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
                            repo.createNotification(
                                    eventId,
                                    entrantId,
                                    eventName,
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
                for (int i = count; i < ids.size(); i++) {
                    String loserId = ids.get(i);
                    repo.createLossNotification(
                            eventId,
                            loserId,
                            eventName,
                            new RepoCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {}

                                @Override
                                public void onError(Exception e) {}
                            }
                    );
                }
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

    /**
     * validation for comment retrieval
     * @param eventId
     * @param cb
     */
    public void getComments(String eventId, RepoCallback<List<EventComment>> cb) {
        if (eventId == null || eventId.isEmpty()) {
            cb.onError(new IllegalArgumentException("eventId required"));
            return;
        }
        repo.getEventComments(eventId, cb);
    }

    /**
     * validation for comment deletion
     * @param eventId
     * @param cb
     */
    public void deleteComment(String eventId, String commentId, RepoCallback<Void> cb) {
        if (eventId == null || eventId.isEmpty()) {
            cb.onError(new IllegalArgumentException("eventId required"));
            return;
        }
        if (commentId == null || commentId.isEmpty()) {
            cb.onError(new IllegalArgumentException("commentId required"));
            return;
        }
        repo.deleteEventComment(eventId, commentId, cb);
    }

}
