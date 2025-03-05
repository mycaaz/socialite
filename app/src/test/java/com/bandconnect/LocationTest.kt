package com.bandconnect

import org.junit.Test
import org.junit.Assert.*
import com.bandconnect.models.Location
import com.google.android.gms.maps.model.LatLng

class LocationTest {
    @Test
    fun testLocationCreation() {
        val location = Location(
            id = "loc123",
            name = "Concert Hall",
            address = "123 Music Street",
            coordinates = LatLng(1.2345, 6.7890),
            createdBy = "band1"
        )
        
        assertEquals("loc123", location.id)
        assertEquals("Concert Hall", location.name)
        assertEquals("123 Music Street", location.address)
        assertEquals(1.2345, location.coordinates.latitude, 0.0001)
        assertEquals(6.7890, location.coordinates.longitude, 0.0001)
        assertEquals("band1", location.createdBy)
    }

    @Test
    fun testLocationValidation() {
        val location = Location(
            id = "loc456",
            name = "Practice Room",
            address = "456 Band Avenue",
            coordinates = LatLng(-90.0, 180.0),
            createdBy = "user1"
        )
        
        assertTrue(location.coordinates.latitude >= -90.0 && location.coordinates.latitude <= 90.0)
        assertTrue(location.coordinates.longitude >= -180.0 && location.coordinates.longitude <= 180.0)
    }
}