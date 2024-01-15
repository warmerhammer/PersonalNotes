package com.warmerhammer.personalnotes.Utils

import androidx.recyclerview.widget.DiffUtil
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem


class TodoListDiffUtil(
    private val oldList: List<TodoListItem>,
    private val newList: List<TodoListItem>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        //Log.i(TAG, "oldList item :: ${oldList[oldItemPosition].name} vs ${newList[newItemPosition].name} :: newList Item")
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].text == newList[newItemPosition].text
    }
}

