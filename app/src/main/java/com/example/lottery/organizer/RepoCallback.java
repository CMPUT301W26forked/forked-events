package com.example.lottery.organizer;

public interface RepoCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
