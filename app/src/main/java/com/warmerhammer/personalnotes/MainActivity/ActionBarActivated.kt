package com.warmerhammer.personalnotes.MainActivity

import android.transition.Scene
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.Data.DataClasses.MenuTitle
import com.warmerhammer.personalnotes.PopupWindow.PWindow
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.AlertDialogFragment
import javax.inject.Inject

class ActionBarActivated @Inject constructor(
    val appCompatActivity: AppCompatActivity,
    val clickListener: (message: String) -> Unit
) {

    private val rootView =
        appCompatActivity.findViewById<ViewGroup>(R.id.main_activity_tool_bar_container)

    private val inactiveActionBar =
        appCompatActivity.layoutInflater.inflate(R.layout.material_toolbar, rootView, false)
    private val inactiveScene = Scene(rootView, inactiveActionBar)

    private val actionBarLayout =
        appCompatActivity.layoutInflater.inflate(
            R.layout.selected_item_action_menu,
            rootView,
            false
        )
    private val activeScene = Scene(rootView, actionBarLayout)

    private var selectedItemTVText: TextView? = null

    private var isEmpty = true

    private lateinit var adapter: PWindow
    private lateinit var menuRV: RecyclerView
    private val emptyMenu =
        LayoutInflater.from(appCompatActivity).inflate(R.layout.empty_menu_recyclerview, null)
    private val popup = PopupWindow(
        emptyMenu,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    fun setCancelClickListener() {
        actionBarLayout.findViewById<ImageButton>(R.id.close_selected_item_action_menu_ib)
            .setOnClickListener {
                clickListener("cancel_selected_items")
            }
    }

    fun setTrashCanClickListener() {
        actionBarLayout.findViewById<ImageButton>(R.id.delete_selected_items_ib)
            .setOnClickListener {
                AlertDialogFragment("Are you sure you want to delete these items?") { positiveClick ->
                    if (positiveClick) {
                        clickListener("delete_selected_items")
                    }
                }.show(appCompatActivity.supportFragmentManager, "delete_selected_items")
            }
    }

    fun setOptionsMenuClickListener() {
        menuRV = emptyMenu.findViewById(R.id.menu_recyclerView)
        val layoutManager = LinearLayoutManager(appCompatActivity)
        menuRV.layoutManager = layoutManager

        adapter = PWindow(appCompatActivity) {
            Log.i("ActionBarActivated.kt", "Menu Item Clicked")
        }
        menuRV.adapter = adapter

        val listOfActionItems = listOf(
            MenuTitle(
                "Move Item",
                null
            )
        )

        adapter.submitList(
            listOfActionItems
        )

        actionBarLayout.findViewById<ImageButton>(R.id.optionsMenu).setOnClickListener {

            if (popup.isShowing) {
                popup.dismiss()
            } else
                popup.showAsDropDown(it) // use option menu as anchor
        }
    }

    fun setCollabListener() {
        actionBarLayout.findViewById<ImageButton>(R.id.share_selected_items_ib).setOnClickListener {
            AlertDialogFragment("Share these item(s) with friends?") { positiveClick ->
                if (positiveClick) {
                    clickListener("collab")
                }
            }.show(appCompatActivity.supportFragmentManager, "share_selected_items")
        }
    }

    fun transitionToActive(selectedItemCount: String) {
        if (isEmpty) {
            TransitionManager.go(activeScene)
            isEmpty = false
            selectedItemTVText = actionBarLayout.findViewById(R.id.selected_item_count_tv)
            selectedItemTVText?.text = selectedItemCount
        } else {
            selectedItemTVText?.text = selectedItemCount
        }
    }

    fun transitionToInactive() {
        TransitionManager.go(inactiveScene)
        isEmpty = true
    }
}