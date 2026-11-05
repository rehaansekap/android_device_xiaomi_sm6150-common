/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 * Author: rehanseekap
 */

package org.lineageos.settings.zram;

import android.os.Bundle;
import android.view.MenuItem;
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

public class ZramSettingsActivity extends CollapsingToolbarBaseActivity {
    private static final String TAG_ZRAM = "zram";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                        new ZramSettingsFragment(), TAG_ZRAM)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
