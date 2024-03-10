package com.warmerhammer.personalnotes.homescreen

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
import com.warmerhammer.personalnotes.FolderListFragment.FolderListRecyclerViewAdapter
import com.warmerhammer.personalnotes.R
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class RecentDocsRVAdapter @Inject constructor() :
    ListAdapter<Project, RecentDocsRVAdapter.ViewHolder>(
        DiffCallback()
    ) {

    private class DiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldProject: Project, newProject: Project): Boolean {
            return oldProject == newProject
        }

        override fun areContentsTheSame(oldProject: Project, newProject: Project): Boolean {
            return oldProject.id == newProject.id
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var docTitle: TextView = view.findViewById(R.id.doc_title)
        var memorySize: TextView = view.findViewById(R.id.memory_size)
        var modifiedDate: TextView = view.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentDocsRVAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_documents_rv_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecentDocsRVAdapter.ViewHolder, position: Int) {
        val doc = currentList[position] as Project
        val dateFormat = "MM.dd.yy"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        holder.docTitle.text = doc.title
        holder.modifiedDate.text = formatter.format(doc.timestamp ?: System.currentTimeMillis())
        holder.memorySize.text = "680 KB"
    }
}