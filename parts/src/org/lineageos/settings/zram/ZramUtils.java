/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 * Author: rehanseekap
 */

package org.lineageos.settings.zram;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.PreferenceManager;
import java.io.BufferedReader;
import java.io.FileReader;

public final class ZramUtils {
    private static final String TAG = "ZramUtils";
    public static final String PREF_ZRAM_ENABLED = "zram_master_switch";
    public static final String PREF_ZRAM_SIZE_MB = "zram_custom_mb";
    public static final String PROP_ZRAM_SIZE = "persist.sys.zram_size";
    public static final String SETTINGS_KEY_ZRAM_SIZE = "persist.sys.zram_size";

    private ZramUtils() {}

    /**
     * Reads MemTotal from /proc/meminfo and returns total physical RAM in MB.
     */
    public static int getTotalRamMb() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemTotal:")) {
                    String[] parts = line.split("\\s+");
                    long kb = Long.parseLong(parts[1]);
                    return (int) (kb / 1024);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to read MemTotal", e);
        }
        return 6144; // Default fallback to 6GB
    }

    /**
     * Total physical RAM rounded up to GB (e.g. 6 or 8).
     */
    public static int getTotalRamGb() {
        int mb = getTotalRamMb();
        return (int) Math.round((double) mb / 1024.0);
    }

    /**
     * Reads current active SwapTotal in MB from /proc/meminfo.
     * 100% accessible to system_app without SELinux restrictions.
     */
    public static int getActiveZramMb() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SwapTotal:")) {
                    String[] parts = line.split("\\s+");
                    long kb = Long.parseLong(parts[1]);
                    return (int) (kb / 1024);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to read SwapTotal from /proc/meminfo", e);
        }
        return 0;
    }

    /**
     * Gets user-configured ZRAM size in MB.
     * Priority: SharedPreferences -> Settings.Secure -> SystemProperties -> -1 (not configured)
     */
    public static int getConfiguredZramMb(Context context) {
        if (context != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains(PREF_ZRAM_SIZE_MB)) {
                try {
                    return Integer.parseInt(prefs.getString(PREF_ZRAM_SIZE_MB, "-1"));
                } catch (Exception ignored) {}
            }
            try {
                int sec = Settings.Secure.getInt(context.getContentResolver(), SETTINGS_KEY_ZRAM_SIZE, -1);
                if (sec >= 0) return sec;
            } catch (Exception ignored) {}
        }
        return SystemProperties.getInt(PROP_ZRAM_SIZE, -1);
    }

    /**
     * Saves user-configured ZRAM size in MB to SharedPreferences, Settings.Secure, and SystemProperties.
     */
    public static void setConfiguredZramMb(Context context, int mb) {
        if (context != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit()
                 .putBoolean(PREF_ZRAM_ENABLED, mb > 0)
                 .putString(PREF_ZRAM_SIZE_MB, String.valueOf(mb))
                 .apply();
            try {
                Settings.Secure.putInt(context.getContentResolver(), SETTINGS_KEY_ZRAM_SIZE, mb);
            } catch (Exception ignored) {}
        }
        try {
            SystemProperties.set(PROP_ZRAM_SIZE, String.valueOf(mb));
        } catch (Exception e) {
            Log.w(TAG, "Failed to setprop " + PROP_ZRAM_SIZE, e);
        }
    }
}
