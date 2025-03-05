package com.bandconnect

import org.junit.Test
import org.junit.Assert.*
import com.bandconnect.models.Message
import java.util.Date

class MessageTest {
    @Test
    fun testMessageCreation() {
        val timestamp = Date()
        val message = Message(
            id = "msg123",
            senderId = "user1",
            receiverId = "band1",
            content = "Hello Band!",
            timestamp = timestamp,
            isRead = false
        )
        
        assertEquals("msg123", message.id)
        assertEquals("user1", message.senderId)
        assertEquals("band1", message.receiverId)
        assertEquals("Hello Band!", message.content)
        assertEquals(timestamp, message.timestamp)
        assertFalse(message.isRead)
    }

    @Test
    fun testMessageReadStatus() {
        val message = Message(
            id = "msg456",
            senderId = "band1",
            receiverId = "user1",
            content = "Thank you for your support!",
            timestamp = Date(),
            isRead = false
        )
        
        assertFalse(message.isRead)
        message.isRead = true
        assertTrue(message.isRead)
    }
}