package com.warmerhammer.personalnotes.SearchActivity

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.R
import kotlinx.coroutines.NonDisposableHandle.parent

class SearchActivityRVAdapter(
    private val context: Context
) : ListAdapter<User, SearchActivityRVAdapter.ViewHolder>(
    DiffCallback()
) {

    private class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePhoto: ImageView = view.findViewById(R.id.profile_image_iv)
        val profileName: TextView = view.findViewById(R.id.profile_name_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.searchable_profile_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = currentList[position]
        Glide.with(context).load(user.photoUrl)
            .error(R.drawable.ic_baseline_account_circle_24_black).into(holder.profilePhoto)
        holder.profileName.text = user.name
    }
}