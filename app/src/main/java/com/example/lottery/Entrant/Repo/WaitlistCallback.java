package com.example.lottery.Entrant.Repo;


/***
 * Handles the success and failure results.
 * @param <T> data type that will be returned by callback.
 */
public interface WaitlistCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}