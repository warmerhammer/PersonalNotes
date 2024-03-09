package com.warmerhammer.personalnotes.FolderListFragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.DiffCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.R
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class FolderListRecyclerViewAdapter @Inject constructor(

) : ListAdapter<Folder, FolderListRecyclerViewAdapter.ViewHolder>(
    DiffCallback()
) {

    private class DiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var folderName: TextView = view.findViewById(R.id.folder_title)
        var modifiedDate: TextView = view.findViewById(R.id.edit_date)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FolderListRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderListRecyclerViewAdapter.ViewHolder, position: Int) {
        val folder = currentList[position] as Folder
        val dateFormat = "MM.dd.yy"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        holder.folderName.text = folder.name
        Log.i("FolderListRecyclerViewAdapter.kt", "timestamp: ${folder.timestamp}")
        holder.modifiedDate.text = formatter.format(folder.timestamp ?: System.currentTimeMillis())
    }

}