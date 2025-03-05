/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.socialite.ui.home.settings

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.performance.DevicePerformance
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.samples.socialite.data.ContactDao
import com.google.android.samples.socialite.data.DatabaseManager
import com.google.android.samples.socialite.model.Contact
import com.google.android.samples.socialite.model.UserRole
import com.google.android.samples.socialite.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "SocialiteMusicBandPrefs"
private const val KEY_LOCATION_SHARING = "location_sharing_enabled"
private const val KEY_PRIORITY_NOTIFICATIONS = "priority_notifications_enabled"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val repository: ChatRepository,
    private val databaseManager: DatabaseManager,
    private val contactDao: ContactDao,
    devicePerformance: DevicePerformance,
) : ViewModel() {
    val mediaPerformanceClass = devicePerformance.mediaPerformanceClass
    
    // Shared Preferences for various settings
    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Current user data
    private var currentUser: Contact? = null
    
    init {
        // Load current user data when ViewModel is created
        viewModelScope.launch {
            currentUser = contactDao.getContactById(0L) // "You" user has ID 0
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            repository.clearMessages()
            withContext(Dispatchers.IO) {
                databaseManager.wipeAndReinitializeDatabase()
            }
            Toast.makeText(
                application.applicationContext,
                "Messages have been reset",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    val isBotEnabledFlow = repository.isBotEnabled

    fun toggleChatbot() {
        viewModelScope.launch {
            repository.toggleChatbotSetting()
        }
    }
    
    // User role functions
    fun isUserBandMember(): Boolean = currentUser?.role == UserRole.BAND_MEMBER
    
    fun isUserAdmin(): Boolean = currentUser?.role == UserRole.ADMIN
    
    // Band profile functions
    fun getBandName(): String = currentUser?.bandName ?: ""
    
    fun getGenre(): String = currentUser?.genre ?: ""
    
    fun updateBandName(name: String) {
        viewModelScope.launch {
            currentUser?.let { user ->
                val updatedUser = user.copy(bandName = name)
                contactDao.updateContact(updatedUser)
                currentUser = updatedUser
            }
        }
    }
    
    fun updateGenre(genre: String) {
        viewModelScope.launch {
            currentUser?.let { user ->
                val updatedUser = user.copy(genre = genre)
                contactDao.updateContact(updatedUser)
                currentUser = updatedUser
            }
        }
    }
    
    // Concert functions
    fun getUpcomingConcert(): String = currentUser?.upcomingConcert ?: ""
    
    fun updateUpcomingConcert(concert: String) {
        viewModelScope.launch {
            currentUser?.let { user ->
                val updatedUser = user.copy(upcomingConcert = concert)
                contactDao.updateContact(updatedUser)
                currentUser = updatedUser
            }
        }
    }
    
    // Location sharing functions
    fun isLocationSharingEnabled(): Boolean = prefs.getBoolean(KEY_LOCATION_SHARING, false)
    
    fun toggleLocationSharing() {
        val current = isLocationSharingEnabled()
        prefs.edit {
            putBoolean(KEY_LOCATION_SHARING, !current)
        }
    }
    
    // Admin functions
    fun arePriorityNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_PRIORITY_NOTIFICATIONS, false)
    
    fun togglePriorityNotifications() {
        val current = arePriorityNotificationsEnabled()
        prefs.edit {
            putBoolean(KEY_PRIORITY_NOTIFICATIONS, !current)
        }
    }
}