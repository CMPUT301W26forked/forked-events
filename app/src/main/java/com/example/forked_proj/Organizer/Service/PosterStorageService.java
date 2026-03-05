package com.example.forked_proj.Organizer.Service;

import android.net.Uri;

import com.example.forked_proj.Organizer.Repo.RepoCallback;
import com.google.firebase.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class PosterStorageService {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private StorageReference posterRef(String eventId) {
        return storage.getReference().child("posters/" + eventId + ".jpg");
    }

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
