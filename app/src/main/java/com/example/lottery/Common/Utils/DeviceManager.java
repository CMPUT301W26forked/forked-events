package com.example.lottery.Common.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Utility class for managing a unique device identifier.
 * <p>
 * Generates and persists a UUID the first time it is requested,
 * then returns the same ID on all subsequent calls. The ID is
 * stored in {@link SharedPreferences} and survives app restarts.
 * </p>
 */
public class DeviceManager {
    /** Name of the SharedPreferences file used for storage. */
    private static final String PREF_NAME = "entrant_prefs";
    /** Key used to store and retrieve the device ID in SharedPreferences. */
    private static final String KEY_DEVICE_ID = "device_id";

    /**
     * Returns the unique device ID, generating one if it does not yet exist.
     * <p>
     * On the first call, a random UUID is created, saved to SharedPreferences,
     * and returned. On all subsequent calls, the previously saved ID is returned
     * without modification.
     * </p>
     *
     * @param context the application or activity context used to access
     *                SharedPreferences; must not be null
     * @return a non-null String representing the unique device ID
     */
    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(KEY_DEVICE_ID, null);

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }

        return deviceId;
    }
}
