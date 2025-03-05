package com.bandconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bandconnect.R
import com.bandconnect.models.Event
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private val onEventClick: (Event) -> Unit,
    private val onRegisterClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.eventTitleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.eventDescriptionTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.eventDateTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.eventLocationTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.eventStatusTextView)
        private val capacityTextView: TextView = itemView.findViewById(R.id.eventCapacityTextView)
        private val eventImageView: ImageView = itemView.findViewById(R.id.eventImageView)
        private val registerButton: Button = itemView.findViewById(R.id.registerButton)

        fun bind(event: Event) {
            titleTextView.text = event.title
            descriptionTextView.text = event.description
            dateTextView.text = formatDate(event.date)
            locationTextView.text = event.location
            statusTextView.text = event.status
            capacityTextView.text = "${event.currentRegistrations}/${event.maxCapacity}"

            event.imageUrl?.let { url ->
                Glide.with(itemView.context)
                    .load(url)
                    .centerCrop()
                    .into(eventImageView)
            }

            itemView.setOnClickListener { onEventClick(event) }
            
            registerButton.apply {
                isEnabled = event.status == "UPCOMING" && 
                           event.currentRegistrations < event.maxCapacity
                setOnClickListener { onRegisterClick(event) }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}