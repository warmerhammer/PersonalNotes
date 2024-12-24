package com.warmerhammer.personalnotes.MainActivity

import android.animation.ObjectAnimator
import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.init
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
        var layout: ConstraintLayout = view.findViewById(R.id.nav_drawer_rv_item)
        private var toggleButton: ImageButton = view.findViewById(R.id.folder_toggle)
        private var folderIcon: ImageView = view.findViewById(R.id.folder_icon)
        private var docsRV : RecyclerView = view.findViewById(R.id.nav_drawer_docs_RV)

        init {
            folderIcon.setOnClickListener {
                onClick(bindingAdapterPosition)
            }

            folderTitle.setOnClickListener {
                onClick(bindingAdapterPosition)
            }
            view.setOnDragListener(DrawerNavigationDragListener(context))

            var folded = true
            toggleButton.setOnClickListener {
                if (folded) {
                    ObjectAnimator.ofFloat(it, "rotation", 0f, 90f).apply {
                        duration = 300
                        start()
                    }

                    docsRV.visibility = View.VISIBLE

                    folded = false
                } else {
                    ObjectAnimator.ofFloat(it, "rotation", 0f).apply {
                        duration = 300
                        start()
                    }
                    docsRV.visibility = View.GONE

                    folded = true
                }

            }
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