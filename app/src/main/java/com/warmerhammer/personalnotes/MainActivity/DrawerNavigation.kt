package com.warmerhammer.personalnotes.MainActivity

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.warmerhammer.personalnotes.CustomDialogFragment.CustomDialogFragment
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.R

class DrawerNavigation(
    context: Context,
    view: View,
    private val viewModel: MainActivityViewModel,
    val onClick: (project: Folder) -> Unit
) : DrawerLayout.DrawerListener {

    private var adapter : DrawerNavigationRVAdapter

    init {
        // maintains list of folders for callback
        lateinit var folders : List<Folder>
        // RecyclerView for Drawer Navigation
        val drawerNavigationRVAdapter =
            view.findViewById<RecyclerView>(R.id.nav_drawer_recyclerView)
        adapter = DrawerNavigationRVAdapter { pos ->
            onClick(folders[pos])
        }
        drawerNavigationRVAdapter.adapter = adapter
        // Retrieves and observes all folders
        viewModel.getAllFolders()
        viewModel.allFolders.observe(context as LifecycleOwner) { folderList ->
            Log.i(TAG, "folderLIST : $folderList")
            folders = folderList
            adapter.submitList(folderList)
        }
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        val drawerList = drawerView.findViewById<NavigationView>(R.id.nav_view)
        val movefactor = drawerList.width * slideOffset
        val mainContentView =
            drawerView.findViewById<FragmentContainerView>(R.id.nav_host_fragment_content_main)

        mainContentView?.translationX = movefactor
    }

    override fun onDrawerOpened(drawerView: View) {
        drawerView.elevation = 1f
    }

    override fun onDrawerClosed(drawerView: View) {
        drawerView.visibility = View.GONE
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    fun getDragInstance(context: Context) = adapter.getDragInstance(context)

    companion object {
        val TAG: String = DrawerNavigation::class.java.simpleName + ".kt"
    }
}