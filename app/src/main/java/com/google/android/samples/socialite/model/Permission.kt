package com.google.android.samples.socialite.model

import android.Manifest

enum class PermissionType {
    NOTIFICATION,
    CAMERA,
    LOCATION
}

data class AppPermission(
    val type: PermissionType,
    val permission: String,
    val title: String,
    val description: String,
    val buttonText: String
)

val requiredPermissions = listOf(
    AppPermission(
        type = PermissionType.NOTIFICATION,
        permission = "android.permission.POST_NOTIFICATIONS",
        title = "Enable Notifications",
        description = "Stay updated with band announcements, messages from artists, and event notifications.",
        buttonText = "Enable"
    ),
    AppPermission(
        type = PermissionType.CAMERA,
        permission = Manifest.permission.CAMERA,
        title = "Enable Camera",
        description = "Capture moments at concerts and share them with the community.",
        buttonText = "Enable"
    ),
    AppPermission(
        type = PermissionType.LOCATION,
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        title = "Enable Location",
        description = "Find nearby events and share your location with band members.",
        buttonText = "Enable"
    )
)
