package com.bandconnect.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bandconnect.models.Feedback
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FeedbackViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val feedbackRef = database.getReference("feedback")

    private val _feedbacks = MutableLiveData<List<Feedback>>()
    val feedbacks: LiveData<List<Feedback>> = _feedbacks

    private val _averageRating = MutableLiveData<Float>()
    val averageRating: LiveData<Float> = _averageRating

    fun submitFeedback(feedback: Feedback) {
        val feedbackId = feedbackRef.push().key ?: return
        val newFeedback = feedback.copy(id = feedbackId)
        feedbackRef.child(feedbackId).setValue(newFeedback)
    }

    fun getFeedbackForBand(bandId: String) {
        feedbackRef.orderByChild("bandId").equalTo(bandId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val feedbackList = mutableListOf<Feedback>()
                    var totalRating = 0f
                    snapshot.children.forEach { child ->
                        child.getValue(Feedback::class.java)?.let { feedback ->
                            feedbackList.add(feedback)
                            totalRating += feedback.rating
                        }
                    }
                    _feedbacks.value = feedbackList
                    _averageRating.value = if (feedbackList.isNotEmpty()) {
                        totalRating / feedbackList.size
                    } else 0f
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}