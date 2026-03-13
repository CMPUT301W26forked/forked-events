package com.example.lottery.organizer;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * handles event poster uploads in storage
 */
public class PosterStorageService {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * returns a storage reference for an event poster
     * @param eventId unique identifier for the event
     * @return storage reference for the poster image
     */
    private StorageReference posterRef(String eventId) {
        return storage.getReference().child("posters/" + eventId + ".jpg");
    }

    /**
     * uploads local image to storage and returns download uri via callback
     * @param eventId unique identifier for the event
     * @param localUri local uri of the image file
     * @param cb callback returning the download uri
     */
    public void uploadPoster(String eventId, Uri localUri, RepoCallback<Uri> cb) {
        StorageReference ref = posterRef(eventId);

        ref.putFile(localUri)
                .addOnSuccessListener(v ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(cb::onSuccess)
                                .addOnFailureListener(cb::onError)
                )
                .addOnFailureListener(cb::onError);
    }
}
