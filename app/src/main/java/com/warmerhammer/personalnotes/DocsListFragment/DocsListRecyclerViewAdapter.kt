package com.warmerhammer.personalnotes.DocsListFragment

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.*
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import java.util.*
import javax.inject.Inject

class DocsListRecyclerViewAdapter @Inject constructor(
    private val activity: DocsListFragment,
    private val viewModel: MainActivityViewModel,
    val clickListener: (
        message: String,
        project: Any,
        pos: Int
    ) -> Unit
) : ListAdapter<Any, DocsListRecyclerViewAdapter.ViewHolder>(
    DiffCallback()
) {


    private class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return (oldItem as Project) == (newItem as Project)
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Folder && newItem is Folder -> {
                    oldItem.id == newItem.id
                }

                oldItem is Note && newItem is Note -> {
                    oldItem.id.toInt() == newItem.id.toInt()
                }

                oldItem is TodoList && newItem is TodoList -> {
                    oldItem.id.toInt() == newItem.id.toInt()
                }

                else -> {
                    false
                }
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var projectItem: View = view.findViewById(R.id.project_item)
        var projectTitle: TextView = view.findViewById(R.id.project_title)
        var lastModifiedDate: TextView = view.findViewById(R.id.project_date)
        var projectImage: ImageView = view.findViewById(R.id.folder_image)
        var folderIconDescription: TextView = view.findViewById(R.id.folder_icon_description)
        var itemCheckbox: ImageView = view.findViewById(R.id.item_checkbox)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocsListRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.project_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DocsListRecyclerViewAdapter.ViewHolder,
        idx: Int
    ) {
        val project = currentList[idx] as Project
        val highlightColor = Color.CYAN
        val normalColor = ContextCompat.getColor(activity.requireContext(), R.color.app_yellow)
        val dateFormat = "MM.dd.yy"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        var checked = false
        holder.projectImage.setImageResource(project.image!!)
        holder.projectTitle.text = project.title
        holder.lastModifiedDate.text = formatter.format(project.timestamp)
        if (project.category == "todoLists") {
            holder.folderIconDescription.text = "list"
        } else {
            holder.folderIconDescription.text = "note"
        }

        // observe state of checked items
        viewModel.checkedSet.observe(activity as LifecycleOwner) { checkedSet ->
            Log.d(TAG, "onBindViewHolder: checkedSet: $checkedSet")
            if (checkedSet.isNotEmpty()) {
                Log.d(TAG, "onBindViewHolder: checkedSet is not empty")
                holder.itemCheckbox.visibility = View.VISIBLE
                holder.projectImage.visibility = View.GONE

                checked = if (checkedSet.contains(project)) {
                    holder.itemCheckbox.setImageResource(R.drawable.checked_checkbox)
                    holder.projectItem.setBackgroundColor(highlightColor)
                    true
                } else {
                    holder.itemCheckbox.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24)
                    holder.projectItem.setBackgroundColor(normalColor)
                    false
                }

                holder.projectItem.setOnClickListener {
                    when (checked) {
                        true -> {
                            holder.itemCheckbox.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24)
                            holder.projectItem.setBackgroundColor(normalColor)
                            checked = false
                            viewModel.removeCheck(project)
                        }

                        false -> {
                            holder.projectItem.setBackgroundColor(highlightColor)
                            holder.itemCheckbox.setImageResource(R.drawable.checked_checkbox)
                            checked = true
                            viewModel.markChecked(project)
                        }
                    }
                }
            } else {
                holder.itemCheckbox.visibility = View.GONE
                holder.projectImage.visibility = View.VISIBLE
                holder.projectItem.setBackgroundColor(normalColor)
                // set appropriate click listeners
                holder.projectItem.setOnClickListener {
                    clickListener("Detail fragment", project, idx)
                }
                // set long click listener
                holder.projectItem.setOnLongClickListener {
                    holder.projectItem.setBackgroundColor(highlightColor)
                    holder.itemCheckbox.visibility = View.VISIBLE
                    holder.itemCheckbox.setImageResource(R.drawable.checked_checkbox)
                    holder.projectImage.visibility = View.GONE
                    viewModel.markChecked(project)
                    checked = true

                    true
                }
            }
        }
    }

    companion object {
        private val TAG = DocsListRecyclerViewAdapter::class.java.simpleName + ".kt"
    }

}