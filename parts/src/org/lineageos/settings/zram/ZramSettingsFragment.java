/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 * Author: rehanseekap
 */

package org.lineageos.settings.zram;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SeekBarPreference;
import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;
import org.lineageos.settings.R;

public class ZramSettingsFragment extends SettingsBasePreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_MASTER_SWITCH = "zram_master_switch";
    private static final String KEY_CURRENT_STATUS = "zram_current_status";
    private static final String KEY_CONFIG_CATEGORY = "zram_config_category";
    private static final String KEY_SLIDER_LEVEL = "zram_slider_level";
    private static final String KEY_CUSTOM_MB = "zram_custom_mb";

    private MainSwitchPreference mMasterSwitch;
    private Preference mStatusPref;
    private PreferenceCategory mConfigCategory;
    private SeekBarPreference mSliderPref;
    private EditTextPreference mCustomMbPref;

    private int mTotalRamMb;
    private int mTotalRamGb;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.zram_settings);

        mTotalRamMb = ZramUtils.getTotalRamMb();
        mTotalRamGb = Math.max(2, ZramUtils.getTotalRamGb());

        mMasterSwitch = findPreference(KEY_MASTER_SWITCH);
        mStatusPref = findPreference(KEY_CURRENT_STATUS);
        mConfigCategory = findPreference(KEY_CONFIG_CATEGORY);
        mSliderPref = findPreference(KEY_SLIDER_LEVEL);
        mCustomMbPref = findPreference(KEY_CUSTOM_MB);

        int activeMb = ZramUtils.getActiveZramMb();
        int configuredMb = ZramUtils.getConfiguredZramMb(getContext());
        
        // If user explicitly configured, use it. Otherwise, use active swap from /proc/meminfo.
        int currentEffectiveMb = (configuredMb >= 0) ? configuredMb : activeMb;
        boolean isEnabled = (configuredMb != 0);

        mMasterSwitch.setChecked(isEnabled);
        mMasterSwitch.setOnPreferenceChangeListener(this);

        updateStatus(activeMb);

        int displayMb = (currentEffectiveMb > 0) ? currentEffectiveMb : (mTotalRamGb / 2) * 1024;
        int initialGb = Math.min(mTotalRamGb, Math.max(1, (int) Math.round((double) displayMb / 1024.0)));

        if (mSliderPref != null) {
            mSliderPref.setMin(1);
            mSliderPref.setMax(mTotalRamGb);
            mSliderPref.setValue(initialGb);
            mSliderPref.setOnPreferenceChangeListener(this);
        }

        if (mCustomMbPref != null) {
            mCustomMbPref.setDialogTitle(getString(R.string.zram_custom_dialog_title, String.valueOf(mTotalRamMb)));
            int customDisplay = isEnabled ? displayMb : 0;
            mCustomMbPref.setText(String.valueOf(customDisplay));
            mCustomMbPref.setSummary(customDisplay + " MB");
            mCustomMbPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setSelection(editText.getText().length());
            });
            mCustomMbPref.setOnPreferenceChangeListener(this);
        }

        updateEnabledState(isEnabled);
    }

    private void updateStatus(int activeMb) {
        if (mStatusPref != null) {
            if (activeMb > 0) {
                double gb = Math.round((activeMb / 1024.0) * 10.0) / 10.0;
                mStatusPref.setSummary(getString(R.string.zram_current_status_summary,
                        String.valueOf(activeMb), String.valueOf(gb)));
            } else {
                mStatusPref.setSummary(getString(R.string.zram_current_status_disabled));
            }
        }
    }

    private void updateEnabledState(boolean enabled) {
        if (mConfigCategory != null) {
            mConfigCategory.setEnabled(enabled);
        }
        if (mSliderPref != null) {
            mSliderPref.setEnabled(enabled);
        }
        if (mCustomMbPref != null) {
            mCustomMbPref.setEnabled(enabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Context context = getContext();
        if (preference == mMasterSwitch) {
            boolean enabled = (Boolean) newValue;
            updateEnabledState(enabled);
            if (!enabled) {
                ZramUtils.setConfiguredZramMb(context, 0);
                if (mCustomMbPref != null) {
                    mCustomMbPref.setText("0");
                    mCustomMbPref.setSummary("0 MB");
                }
            } else {
                int gb = (mSliderPref != null) ? mSliderPref.getValue() : (mTotalRamGb / 2);
                int mb = gb * 1024;
                ZramUtils.setConfiguredZramMb(context, mb);
                if (mCustomMbPref != null) {
                    mCustomMbPref.setText(String.valueOf(mb));
                    mCustomMbPref.setSummary(mb + " MB");
                }
            }
            showRebootDialog();
            return true;
        } else if (preference == mSliderPref) {
            int gb = (Integer) newValue;
            int mb = gb * 1024;
            if (mCustomMbPref != null) {
                mCustomMbPref.setText(String.valueOf(mb));
                mCustomMbPref.setSummary(mb + " MB");
            }
            ZramUtils.setConfiguredZramMb(context, mb);
            showRebootDialog();
            return true;
        } else if (preference == mCustomMbPref) {
            String str = (String) newValue;
            try {
                int mb = Integer.parseInt(str.trim());
                if (mb < 512 || mb > mTotalRamMb) {
                    Toast.makeText(context,
                            getString(R.string.zram_custom_invalid, String.valueOf(mTotalRamMb)),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                mCustomMbPref.setSummary(mb + " MB");
                if (mSliderPref != null) {
                    int gb = Math.min(mTotalRamGb, Math.max(1, (int) Math.round((double) mb / 1024.0)));
                    mSliderPref.setValue(gb);
                }
                ZramUtils.setConfiguredZramMb(context, mb);
                showRebootDialog();
                return true;
            } catch (NumberFormatException e) {
                Toast.makeText(context,
                        getString(R.string.zram_custom_invalid, String.valueOf(mTotalRamMb)),
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return false;
    }

    private void showRebootDialog() {
        Context context = getContext();
        if (context == null) return;

        new AlertDialog.Builder(context)
                .setTitle(R.string.zram_reboot_dialog_title)
                .setMessage(R.string.zram_reboot_dialog_message)
                .setPositiveButton(R.string.zram_reboot_now, (dialog, which) -> {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (pm != null) {
                        pm.reboot(null);
                    }
                })
                .setNegativeButton(R.string.zram_reboot_later, null)
                .show();
    }
}
