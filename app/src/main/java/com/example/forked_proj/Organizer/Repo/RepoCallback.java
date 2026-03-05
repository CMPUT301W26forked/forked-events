package com.example.forked_proj.Organizer.Repo;

public interface RepoCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
