package com.warmerhammer.personalnotes.ToDoListFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.TypingListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
@AndroidEntryPoint
class ToDoListFragment @Inject constructor() : Fragment() {

    private val viewModel: MainActivityViewModel by activityViewModels()
    private val todoListViewModel: TodoListViewModel by viewModels()
    private val args: ToDoListFragmentArgs by navArgs()
    private lateinit var adapter: TodoListRecyclerViewAdapter
    private lateinit var checkedAdapter: CheckedRecyclerViewAdapter
    private lateinit var checkedRV: RecyclerView
    private lateinit var uncheckedRV: RecyclerView
    private lateinit var titleView: EditText
    private lateinit var todoList: TodoList
    private lateinit var checkedItems: List<TodoListItem>
    private lateinit var uncheckedItems: List<TodoListItem>
    private var todoListId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // retrieve list or create new one
        todoListViewModel.initTodoListId(args.id, args.folderId)
        checkedItems = listOf()
        uncheckedItems = listOf()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_do_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.actionBarTitle.postValue("Todo List")
        // init titleView
        titleView = requireView().findViewById(R.id.toDoListTitle)


        // retrieve tdListId before observing
        todoListViewModel.todoListId.observe(this as LifecycleOwner) { id ->
            todoListId = id
            todoListId?.let {
                todoListViewModel.loadAllTodoListItemsByTodoListId(it)
                observeTodoList()
                observeUncheckedItems()
                observeCheckedItems()
            }
        }

        // add item button
        view.findViewById<LinearLayout>(R.id.add_todo_item_button).setOnClickListener {
            addTodoItem()
        }

        // bottom sheet
        val bottomSheetBehavior =
            BottomSheetBehavior.from(view.findViewById(R.id.checked_list))
        bottomSheetBehavior.peekHeight =
            view.findViewById<View?>(R.id.app_bar_checked_list).height + 400
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isDraggable = true
    }

    private fun observeUncheckedItems() {
        // unchecked RV
        adapter = TodoListRecyclerViewAdapter(
            uncheckedItems,
            onTypingComplete = { idx, value ->
                if (
                    idx >= 0 &&
                    idx < uncheckedItems.size &&
                    uncheckedItems[idx].text != value
                ) {
                    uncheckedItems.toMutableList().apply {
                        this[idx].text = value
                    }
                    todoListViewModel.saveTodoListItem(uncheckedItems[idx])
                }
            },
            onCheck = { idx ->
                uncheckedItems.toMutableList().apply {
                    this[idx].checked = true
                }.toList()
                todoListViewModel.saveTodoListItem(uncheckedItems[idx])
            },
            onDelete = { idx ->
                todoListViewModel.deleteTodoListItem(uncheckedItems[idx])
            }
        )

        uncheckedRV = requireView().findViewById(R.id.todo_list_rv)
        uncheckedRV.adapter = adapter

        // observe updates to unchecked items
        todoListViewModel.uncheckedItems.observe(this as LifecycleOwner) { items ->
            uncheckedItems = items
            adapter.setItems(uncheckedItems)
        }
    }


    private fun observeCheckedItems() {
        checkedAdapter = CheckedRecyclerViewAdapter(
            checkedItems,
            requireContext(),
            onCheck = { idx ->
                checkedItems.toMutableList().apply {
                    this[idx].checked = false
                }.toList()
                todoListViewModel.saveTodoListItem(checkedItems[idx])
            },
            onDelete = { idx ->
                todoListViewModel.deleteTodoListItem(checkedItems[idx])
            }
        )
        checkedRV = requireView().findViewById(R.id.checkedRV)
        checkedRV.adapter = checkedAdapter

        // checkedList RV
        todoListViewModel.checkedItems.observe(this as LifecycleOwner) { items ->
            checkedItems = items
            checkedAdapter.setItems(checkedItems)
        }
    }

    private fun observeTodoList() {

        todoListId?.let {
            todoListViewModel.loadTodoListById(it).observe(viewLifecycleOwner) { tdList ->
                // set title if it's updated or when initially loaded
                if (tdList.title != titleView.text.toString()) { titleView.setText(tdList.title) }
                todoList = tdList
            }

            titleView.addTextChangedListener(TypingListener { typing ->
                if (!typing) {
                    Log.d(TAG, "not typing...")
                    todoList.title = titleView.text.toString()
                    todoListViewModel.saveTodoList(todoList)
                }
            })
        }
    }

    private fun addTodoItem() {

        val doesNotContainAddItem = uncheckedItems.none { it.text == "Add Item" }

        if (doesNotContainAddItem) {
            todoListId?.let {
                val newTdListItem = TodoListItem(
                    todoListId = it,
                    text = "Add Item",
                    checked = false
                )
                todoListViewModel.saveTodoListItem(newTdListItem)
            }
        }

        uncheckedRV.scrollToPosition(uncheckedItems.size - 1)
    }

    override fun onStop() {
        super.onStop()
        if (
            todoList.title!!.isEmpty() &&
            uncheckedItems.isEmpty() &&
            checkedItems.isEmpty()
        ) {
            todoListViewModel.deleteTodoList(todoList)
        } else {
            todoListViewModel.saveTodoList(todoList)
        }

        adapter.clear()
        checkedAdapter.clear()
    }

    companion object {
        private const val TAG = "ToDoListFragment.kt"
    }
}