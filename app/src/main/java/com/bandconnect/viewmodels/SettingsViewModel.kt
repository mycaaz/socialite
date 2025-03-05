package com.bandconnect.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    fun applyTheme() {
        when (preferences.getString("theme_mode", "system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun isNotificationsEnabled(): Boolean = 
        preferences.getBoolean("notifications_enabled", true)

    fun getUpdateFrequency(): String = 
        preferences.getString("update_frequency", "daily") ?: "daily"

    fun isLocationSharingEnabled(): Boolean = 
        preferences.getBoolean("location_sharing", false)

    fun isProfileVisible(): Boolean = 
        preferences.getBoolean("profile_visibility", true)
}