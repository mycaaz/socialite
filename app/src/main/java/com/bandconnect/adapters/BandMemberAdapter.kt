package com.bandconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bandconnect.R
import com.bandconnect.models.BandMember
import com.bumptech.glide.Glide

class BandMemberAdapter(
    private val bandMembers: List<BandMember>,
    private val onBandMemberClick: (BandMember) -> Unit
) : RecyclerView.Adapter<BandMemberAdapter.BandMemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BandMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_band_member, parent, false)
        return BandMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: BandMemberViewHolder, position: Int) {
        val bandMember = bandMembers[position]
        holder.bind(bandMember)
    }

    override fun getItemCount() = bandMembers.size

    inner class BandMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val instrumentTextView: TextView = itemView.findViewById(R.id.textViewInstrument)
        private val profileImageView: ImageView = itemView.findViewById(R.id.imageViewProfile)
        private val locationTextView: TextView = itemView.findViewById(R.id.textViewLocation)

        fun bind(bandMember: BandMember) {
            nameTextView.text = bandMember.name
            instrumentTextView.text = bandMember.instrument
            locationTextView.text = bandMember.location.address

            Glide.with(itemView.context)
                .load(bandMember.imageUrl)
                .placeholder(R.drawable.default_profile)
                .circleCrop()
                .into(profileImageView)

            itemView.setOnClickListener { onBandMemberClick(bandMember) }
        }
    }
}