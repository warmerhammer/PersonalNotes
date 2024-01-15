package com.warmerhammer.personalnotes.MainActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.R

class DrawerNavigationRVAdapter(
    val onClick: (pos: Int) -> Unit
) : ListAdapter<Folder, DrawerNavigationRVAdapter.ViewHolder>(DiffCallback()) {

    private lateinit var context: Context

    private class DiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldFolder: Folder, newFolder: Folder): Boolean {
            return oldFolder == newFolder
        }

        override fun areContentsTheSame(oldFolder: Folder, newFolder: Folder): Boolean {
            return oldFolder.id == newFolder.id
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var folderTitle: TextView = view.findViewById(R.id.folder_title)
        var folderIcon: ImageView = view.findViewById(R.id.folder_icon)
        var layout : LinearLayout = view.findViewById(R.id.nav_drawer_rv)

        init {
            view.setOnClickListener {
                onClick(bindingAdapterPosition)
            }

            view.setOnDragListener(DrawerNavigationDragListener(context))
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DrawerNavigationRVAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.individual_nav_drawer_folder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = currentList[position] as Folder
        holder.folderTitle.text = project.name
        holder.layout.tag = project.id

    }

    fun getDragInstance(ctxt: Context): DrawerNavigationDragListener {
        context = ctxt
        return DrawerNavigationDragListener(context)
    }

    companion object {
        private val TAG = DrawerNavigationRVAdapter::class.java.simpleName + ".kt"
    }
}