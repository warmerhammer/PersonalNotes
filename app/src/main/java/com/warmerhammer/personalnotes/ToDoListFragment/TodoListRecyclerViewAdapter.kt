package com.warmerhammer.personalnotes.ToDoListFragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.TodoListDiffUtil
import com.warmerhammer.personalnotes.Utils.TypingListener

class TodoListRecyclerViewAdapter(
    items : List<TodoListItem>,
    private val onTypingComplete: (
        idx: Int,
        value: String
    ) -> Unit,
    private val onCheck: (
        idx: Int
    ) -> Unit,
    private val onDelete: (
        idx: Int
    ) -> Unit
) : RecyclerView.Adapter<TodoListRecyclerViewAdapter.ViewHolder>() {

    private var todoListItems = items.toList()

    fun setItems(list: List<TodoListItem>) {
        TodoListDiffUtil(todoListItems, list).let {
            val diffResult = DiffUtil.calculateDiff(it, true)
            todoListItems = list
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun clear() {
        todoListItems = listOf()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var checkBox: ImageView =
            view.findViewById(R.id.checkboxIcon)
        val editText: EditText = view.findViewById(R.id.add_todo_et)
        private val deleteButton: ImageView =
            view.findViewById(R.id.deleteTodoItemButton)

        init {
            editText.imeOptions = EditorInfo.IME_ACTION_DONE
            editText.maxLines = Integer.MAX_VALUE
            editText.inputType = EditorInfo.TYPE_TEXT_FLAG_IME_MULTI_LINE
            editText.maxLines = 5
            editText.setHorizontallyScrolling(false)

            editText.addTextChangedListener(TypingListener { typing ->
                if (!typing) {
                    val pos = absoluteAdapterPosition
                    onTypingComplete(
                        pos,
                        editText.text.toString()
                    )
                }
            })

            deleteButton.setOnClickListener {
                onDelete(absoluteAdapterPosition)
            }

            checkBox.setOnClickListener {
                onCheck(absoluteAdapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoListItem = todoListItems[position]
        when (todoListItem.text) {
            "Add Item" -> {
                holder.editText.text.clear()
                holder.editText.hint = todoListItem.text
            }

            else -> {
                holder.editText.setText(todoListItem.text)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = todoListItems.size

    companion object {
        val TAG : String = TodoListRecyclerViewAdapter::class.java.simpleName
    }
}