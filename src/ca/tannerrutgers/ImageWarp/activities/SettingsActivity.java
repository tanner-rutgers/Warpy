package ca.tannerrutgers.ImageWarp.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import ca.tannerrutgers.ImageWarp.R;
import ca.tannerrutgers.ImageWarp.dialogs.MaskSizePreference;

/**
 * Created by Tanner on 24/01/14.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve maximum allowed mask size
        int maxMaskSize = getIntent().getIntExtra("max_mask_size", MaskSizePreference.SIZE_DEFAULT_MAX);

        // Display preference fragment as main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment(maxMaskSize))
                .commit();
    }

    private class SettingsFragment extends PreferenceFragment {

        private int mMaxMaskSize;

        public SettingsFragment(int maxMaskSize) {
            mMaxMaskSize = maxMaskSize;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from xml preferences layout and set max mask size
            addPreferencesFromResource(R.xml.preferences);
            MaskSizePreference maskSizePref = (MaskSizePreference) getPreferenceScreen().findPreference("pref_mask_size");
            maskSizePref.setMaxSize(mMaxMaskSize);
        }
    }
}
