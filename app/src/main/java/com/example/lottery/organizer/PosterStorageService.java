package com.example.lottery.organizer;

import android.net.Uri;

import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * handles event poster uploads in storage
 */
public class PosterStorageService {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();


    private StorageReference posterRef(String eventId) {
        return storage.getReference().child("posters/" + eventId + ".jpg");
    }

    /**
     * uploads the local image to storage and return download URI via cb
     * @param eventId
     * @param localUri
     * @param cb
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
