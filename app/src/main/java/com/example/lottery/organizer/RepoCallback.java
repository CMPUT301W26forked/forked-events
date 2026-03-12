package com.example.lottery.organizer;

/**
 * call back interface
 * @param <T>
 */
public interface RepoCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
