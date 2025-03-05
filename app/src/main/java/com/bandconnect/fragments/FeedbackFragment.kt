package com.bandconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bandconnect.models.Feedback
import com.bandconnect.viewmodels.FeedbackViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.bandconnect.R

class FeedbackFragment : Fragment() {
    private val viewModel: FeedbackViewModel by viewModels()
    private lateinit var ratingBar: RatingBar
    private lateinit var reviewEditText: TextInputEditText
    private lateinit var submitButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ratingBar = view.findViewById(R.id.ratingBar)
        reviewEditText = view.findViewById(R.id.reviewEditText)
        submitButton = view.findViewById(R.id.submitButton)

        val bandId = arguments?.getString("bandId") ?: return

        submitButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val rating = ratingBar.rating
            val review = reviewEditText.text?.toString() ?: ""

            if (rating == 0f) {
                Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val feedback = Feedback(
                userId = userId,
                bandId = bandId,
                rating = rating,
                review = review
            )

            viewModel.submitFeedback(feedback)
            Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
            clearForm()
        }

        viewModel.getFeedbackForBand(bandId)
    }

    private fun clearForm() {
        ratingBar.rating = 0f
        reviewEditText.text?.clear()
    }

    companion object {
        fun newInstance(bandId: String): FeedbackFragment {
            return FeedbackFragment().apply {
                arguments = Bundle().apply {
                    putString("bandId", bandId)
                }
            }
        }
    }
}