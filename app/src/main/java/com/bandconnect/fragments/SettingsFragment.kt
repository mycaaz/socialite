package com.bandconnect.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.bandconnect.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}