package com.bandconnect.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bandconnect.models.Event
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class EventViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadEvents() {
        _isLoading.value = true
        database.child("events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventList = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    eventSnapshot.getValue(Event::class.java)?.let { event ->
                        eventList.add(event)
                    }
                }
                _events.value = eventList
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
                _isLoading.value = false
            }
        })
    }

    fun createEvent(event: Event, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val eventId = database.child("events").push().key ?: return
        val updatedEvent = event.copy(eventId = eventId)
        
        database.child("events").child(eventId).setValue(updatedEvent)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to create event") }
    }

    fun registerForEvent(eventId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val eventRef = database.child("events").child(eventId)

        eventRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val event = mutableData.getValue(Event::class.java) ?: return Transaction.success(mutableData)

                if (event.currentRegistrations >= event.maxCapacity) {
                    return Transaction.abort() // Correct way to abort transaction
                }

                val updatedRegistrations = event.registeredUsers.toMutableMap()
                updatedRegistrations[userId] = true

                mutableData.value = event.copy(
                    currentRegistrations = event.currentRegistrations + 1,
                    registeredUsers = updatedRegistrations
                )

                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                dataSnapshot: DataSnapshot?
            ) {
                if (error != null) {
                    onError(error.message) // Handle failure
                } else {
                    onSuccess() // Handle success
                }
            }
        })

    fun updateEventStatus(eventId: String, newStatus: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        database.child("events").child(eventId).child("status").setValue(newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update event status") }
    }
}}