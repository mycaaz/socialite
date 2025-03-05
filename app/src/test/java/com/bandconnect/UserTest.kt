package com.bandconnect

import org.junit.Test
import org.junit.Assert.*
import com.bandconnect.models.User

class UserTest {
    @Test
    fun testUserCreation() {
        val user = User(
            id = "test123",
            name = "John Doe",
            email = "john@example.com",
            isAdmin = false
        )
        
        assertEquals("test123", user.id)
        assertEquals("John Doe", user.name)
        assertEquals("john@example.com", user.email)
        assertFalse(user.isAdmin)
    }

    @Test
    fun testAdminPrivileges() {
        val adminUser = User(
            id = "admin123",
            name = "Admin User",
            email = "admin@bandconnect.com",
            isAdmin = true
        )
        
        assertTrue(adminUser.isAdmin)
    }
}