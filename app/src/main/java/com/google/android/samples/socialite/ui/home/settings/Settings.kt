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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.model.UserRole
import com.google.android.samples.socialite.ui.home.HomeAppBar
import com.google.android.samples.socialite.ui.home.HomeBackground
import com.google.android.samples.socialite.ui.navigation.TopLevelDestination
import kotlinx.coroutines.flow.map

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            HomeAppBar(title = stringResource(TopLevelDestination.Settings.label))
        },
    ) { contentPadding ->
        HomeBackground(modifier = Modifier.fillMaxSize())

        LazyColumn(
            contentPadding = contentPadding,
        ) {
            item {
                // Message history section
                SettingsSection(title = "Message History") {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { viewModel.clearMessages() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) {
                            Text(text = stringResource(R.string.clear_message_history))
                        }
                    }
                }

                // AI Chatbot
                val chatbotStatusResource = viewModel.isBotEnabledFlow.map {
                    if (it) {
                        R.string.ai_chatbot_setting_enabled
                    } else {
                        R.string.ai_chatbot_setting_disabled
                    }
                }.collectAsState(initial = R.string.ai_chatbot_setting_enabled).value

                SettingsSection(title = "AI Assistant") {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { viewModel.toggleChatbot() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) {
                            Text(text = "${stringResource(id = R.string.ai_chatbot_setting)}: ${stringResource(chatbotStatusResource)}")
                        }
                    }
                }

                // Band Profile Section (for band members)
                if (viewModel.isUserBandMember() || viewModel.isUserAdmin()) {
                    SettingsSection(title = "Band Profile") {
                        BandProfileSettings(
                            bandName = viewModel.getBandName(),
                            genre = viewModel.getGenre(),
                            onBandNameChange = { viewModel.updateBandName(it) },
                            onGenreChange = { viewModel.updateGenre(it) }
                        )
                    }
                }

                // Concert Settings (for band members)
                if (viewModel.isUserBandMember() || viewModel.isUserAdmin()) {
                    SettingsSection(title = "Concert Settings") {
                        ConcertSettings(
                            upcomingConcert = viewModel.getUpcomingConcert(),
                            enableLocationSharing = viewModel.isLocationSharingEnabled(),
                            onConcertChange = { viewModel.updateUpcomingConcert(it) },
                            onLocationSharingChange = { viewModel.toggleLocationSharing() }
                        )
                    }
                }

                // Admin Section
                if (viewModel.isUserAdmin()) {
                    SettingsSection(title = "Admin Controls") {
                        AdminSettings(
                            priorityNotifications = viewModel.arePriorityNotificationsEnabled(),
                            onPriorityNotificationsChange = { viewModel.togglePriorityNotifications() }
                        )
                    }
                }

                // Media Performance Class
                SettingsSection(title = "System") {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(
                                R.string.performance_class_level,
                                viewModel.mediaPerformanceClass,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            content()
        }
    }
}

@Composable
fun BandProfileSettings(
    bandName: String,
    genre: String,
    onBandNameChange: (String) -> Unit,
    onGenreChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TextField(
            value = bandName,
            onValueChange = onBandNameChange,
            label = { Text("Band Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        TextField(
            value = genre,
            onValueChange = onGenreChange,
            label = { Text("Music Genre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        Button(
            onClick = { /* Save changes */ },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("Save Profile")
        }
    }
}

@Composable
fun ConcertSettings(
    upcomingConcert: String,
    enableLocationSharing: Boolean,
    onConcertChange: (String) -> Unit,
    onLocationSharingChange: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TextField(
            value = upcomingConcert,
            onValueChange = onConcertChange,
            label = { Text("Upcoming Concert (Date - Venue)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enable Concert Location Sharing",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = enableLocationSharing,
                onCheckedChange = { onLocationSharingChange() }
            )
        }
        
        Text(
            text = "When enabled, fans can see the venue location on a map",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        Button(
            onClick = { /* Save changes */ },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp)
        ) {
            Text("Save Concert Info")
        }
    }
}

@Composable
fun AdminSettings(
    priorityNotifications: Boolean,
    onPriorityNotificationsChange: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enable Priority Notifications for Bands",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = priorityNotifications,
                onCheckedChange = { onPriorityNotificationsChange() }
            )
        }
        
        Text(
            text = "When enabled, notifications from band members will get priority treatment",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Button(
            onClick = { /* Reset database */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Reset Community Database")
        }
    }
}