package com.warmerhammer.personalnotes.MainActivity

import android.content.Context
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.PopupWindow.PWindow
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Data.DataClasses.MenuTitle
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GoogleIconButton @Inject constructor(
    @ActivityContext private val context: Context,
    private val anchor: View,
    private val clickListener: (command: String) -> Unit,
) {

    private lateinit var menuRCView: RecyclerView
    private lateinit var adapter: PWindow
    private val menuItems = mutableListOf(
        MenuTitle("GoogleDocs", null),
        MenuTitle("Delete User", null),
        MenuTitle("Sign In", null)
    )
    private val emptyMenu =
        LayoutInflater.from(context).inflate(R.layout.empty_menu_recyclerview, null)
    private val popup = PopupWindow(
        emptyMenu,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    fun showDropDown(photoUrl: String?) {
        Log.d(TAG, "photoUrl :: $photoUrl")
        when {
            photoUrl != null -> {
                menuItems[2] = MenuTitle("Sign Out", photoUrl)
            }

            else -> {
                menuItems[2] = MenuTitle("Sign In", null)
            }
        }

        menuRCView = emptyMenu.findViewById(R.id.menu_recyclerView)
        val layoutManager = LinearLayoutManager(context)
        menuRCView.layoutManager = layoutManager

        adapter = PWindow(context) { command ->
            popup.dismiss()
            clickListener(command)
        }

        menuRCView.adapter = adapter
        adapter.submitList(menuItems.toList())

        if (popup.isShowing) {
            popup.dismiss()
        } else
            popup.showAsDropDown(anchor, -90, 0, Gravity.CENTER)
    }

    companion object {
        const val TAG = "GoogleIconButton.kt"
    }


}