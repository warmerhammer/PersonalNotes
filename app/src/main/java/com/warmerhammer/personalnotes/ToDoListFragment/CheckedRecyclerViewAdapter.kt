package com.warmerhammer.personalnotes.ToDoListFragment

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.TodoListDiffUtil
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class CheckedRecyclerViewAdapter @Inject constructor(
    tdItems: List<TodoListItem>,
    private val context: Context,
    private val onCheck: (pos: Int) -> Unit,
    private val onDelete: (pos: Int) -> Unit
) : RecyclerView.Adapter<CheckedRecyclerViewAdapter.ViewHolder>() {

    private var checkedItems = tdItems.toList()


    fun clear() {
        checkedItems = listOf()
    }

    fun setItems(list: List<TodoListItem>) {
        TodoListDiffUtil(checkedItems, list).let {
            val diffResult = DiffUtil.calculateDiff(it, true)
            checkedItems = list
            diffResult.dispatchUpdatesTo(this)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val editText: EditText = view.findViewById(R.id.add_todo_et)
        private val checkbox: ImageView = view.findViewById(R.id.checkboxIcon)
        private val deleteButton: ImageView = view.findViewById(R.id.deleteTodoItemButton)

        init {
            editText.foreground = ContextCompat.getDrawable(context, R.drawable.strikethrough)
            editText.isEnabled = false
            editText.setBackgroundResource(android.R.color.transparent)
            checkbox.setImageResource(R.drawable.checked_checkbox)
            checkbox.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            checkbox.setOnClickListener {
                onCheck(absoluteAdapterPosition)
            }
            deleteButton.setOnClickListener {
                onDelete(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = checkedItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoListItem = checkedItems[position]
        holder.editText.setText(todoListItem.text)
    }

    companion object {
        val TAG : String = CheckedRecyclerViewAdapter::class.java.simpleName
    }
}

