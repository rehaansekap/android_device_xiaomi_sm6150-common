/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 * Author: rehanseekap
 */

package org.lineageos.settings.zram;

import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import org.lineageos.settings.utils.FileUtils;

public final class ZramUtils {
    private static final String TAG = "ZramUtils";
    public static final String PROP_ZRAM_SIZE = "persist.sys.zram_size";
    public static final String SYSFS_ZRAM_DISKSIZE = "/sys/block/zram0/disksize";

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
     * Reads current active ZRAM disksize in MB from sysfs.
     */
    public static int getActiveZramMb() {
        String val = FileUtils.readOneLine(SYSFS_ZRAM_DISKSIZE);
        if (val != null) {
            try {
                long bytes = Long.parseLong(val.trim());
                return (int) (bytes / (1024 * 1024));
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse zram disksize", e);
            }
        }
        return 0;
    }

    /**
     * Gets the user configured ZRAM size in MB from persistent system properties.
     * Returns -1 if not explicitly set.
     */
    public static int getConfiguredZramMb() {
        return SystemProperties.getInt(PROP_ZRAM_SIZE, -1);
    }

    /**
     * Saves configured ZRAM size in MB.
     */
    public static void setConfiguredZramMb(int mb) {
        SystemProperties.set(PROP_ZRAM_SIZE, String.valueOf(mb));
    }
}
