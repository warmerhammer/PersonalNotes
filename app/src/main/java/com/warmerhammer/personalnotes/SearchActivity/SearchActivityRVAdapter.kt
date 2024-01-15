package com.warmerhammer.personalnotes.SearchActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.R
import kotlinx.coroutines.NonDisposableHandle.parent

class SearchActivityRVAdapter : ListAdapter<Any, SearchActivityRVAdapter.ViewHolder>(
    DiffCallback()
) {

    private class DiffCallback: DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem as String == newItem as String
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
        val searchResult = currentList[position] as String
        holder.profileName.text = searchResult
    }

}