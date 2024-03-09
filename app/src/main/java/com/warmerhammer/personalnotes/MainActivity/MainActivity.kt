package com.warmerhammer.personalnotes.MainActivity

import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.AnimatedFab
import com.warmerhammer.personalnotes.Utils.DragListener
import com.warmerhammer.personalnotes.Utils.OnDragEvent
import com.warmerhammer.personalnotes.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Singleton
import androidx.appcompat.widget.Toolbar
import com.warmerhammer.personalnotes.CustomDialogFragment.CustomDialogFragment
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.DataClasses.MenuTitle
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.PopupWindow.PWindow
import com.warmerhammer.personalnotes.SearchActivity.SearchActivity

@HiltAndroidApp
class MainApplication : Application()

@AndroidEntryPoint
@Singleton
class MainActivity @Inject constructor(
) : AnimatedFab.FabActionListener, DragListener,
    AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
//    private lateinit var animatedFab: AnimatedFab
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerNavigation: DrawerNavigation
    private lateinit var navDrawerRV: RecyclerView
    private lateinit var observeSignOnActivity: SignOnForActivityResult
    private lateinit var user: User
    private lateinit var toolbar: Toolbar
    private lateinit var checkedSet: Set<Project>
    private lateinit var currentFolder: Folder
    private lateinit var allFolders: List<Folder>
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = binding.toolbar

        setSupportActionBar(toolbar)
        // set up navController with nav_drawer
        navDrawerRV = binding.mainActivityAppContainer.findViewById(R.id.nav_drawer_recyclerView)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        setUpNavControllerWithDrawerLayout()
        // instantiate animated FAB object
//        animatedFab = AnimatedFab(this, this)
        // observe changes to action bar title
        observeActionBarTitle()
        // load homePage folder
        viewModel.getHomePageFolder()
        viewModel.currentFolder.observe(this as LifecycleOwner) {
            currentFolder = it
        }
//        observeUser()
        observeSignOnActivity =
            SignOnForActivityResult(
                this,
                this.activityResultRegistry,
                viewModel
            ) {
                Log.i(TAG, "observeSignOnActivity()")
            }
        lifecycle.addObserver(observeSignOnActivity)

        viewModel.isSignOn.observe(this as LifecycleOwner) { isSignOn ->
            if (isSignOn) {
                observeSignOnActivity.launchSignOn()
            }
        }

        observeAllFolders()
        observeCheckedItems()
    }

    private fun observeAllFolders() {
        viewModel.allFolders.observe(this as LifecycleOwner) {
            allFolders = it
        }
    }

    private fun observeCheckedItems() {
        val actionBar =
            ActionBarActivated(this) { message ->
                when (message) {
                    "delete_selected_items" -> viewModel.deleteSelectedProjects()
                    "cancel_selected_items" -> viewModel.clearCheckedSet()
                    "move_item" -> {
                        navController.navigate(R.id.folderListFragment)
                    }
                    "collab" -> {
                        val i = Intent(this, SearchActivity::class.java)
                        startActivity(i)
                    }
                }
            }.apply {
                setCollabListener()
                setTrashCanClickListener()
                setCancelClickListener()
                setOptionsMenuClickListener()
            }

        viewModel.checkedSet.observe(this as LifecycleOwner) {
            checkedSet = it
            if (checkedSet.isNotEmpty()) {
                val selectedItemCount = getString(R.string.selected_item_count, checkedSet.size)
                actionBar.transitionToActive(selectedItemCount)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                actionBar.transitionToInactive()
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    private fun observeUser() {
//        viewModel.observeUser()
        viewModel.currUser.observe(this as LifecycleOwner) {
            user = it
            Log.d(TAG, "observeUser() user :: $user")
            createAccountToolbarIcon()
        }
    }

    private fun createAccountToolbarIcon() {
        val accountIcon = findViewById<ImageButton>(R.id.google_icon)
        Glide
            .with(this)
            .load(user.photoUrl)
            .error(R.drawable.ic_baseline_account_circle_24)
            .into(accountIcon)

        val accountIconButton = GoogleIconButton(
            this,
            accountIcon,
        ) { command ->
            Log.i(TAG, "createAccountToolbarIcon() command :: $command")
            when (command) {
                "Sign In" -> {
                    observeSignOnActivity.launchSignOn()
                }

                "Sign Out" -> {
                    Firebase.auth.signOut()
                    viewModel.addUser()
                }

                "Delete User" -> {
                    viewModel.deleteUserCache(user)
                }
            }
        }

        accountIcon.setOnClickListener {
            accountIconButton.showDropDown(user.photoUrl)
        }
    }

    private fun observeActionBarTitle() {
        viewModel.actionBarTitle.observe(this as LifecycleOwner) { actionBarTitle ->
            toolbar.findViewById<TextView>(R.id.app_bar_title).text = actionBarTitle
            // disable new project button if not in home fragment
//            if (navController.currentDestination!!.label != "HomePage" && navController.currentDestination!!.label != "Project") {
//                animatedFab.hide()
//            } else {
////                animatedFab.show()
//            }
        }
    }

    private fun setUpNavControllerWithDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout)

        drawerLayout.findViewById<Button>(R.id.add_project_button).setOnClickListener {
            CustomDialogFragment("New Folder") { projectName, isConfirm ->
                if (isConfirm) {
                    viewModel.saveNewFolder(Folder(name = projectName))
                }
            }.show(supportFragmentManager, "New Folder")
        }

        drawerNavigation = DrawerNavigation(this, drawerLayout, viewModel) { folder ->
            viewModel.setCurrentFolder(folder)
            drawerLayout.close()
            // clear current projects recycler view adapter for faster loading
//            viewModel.clearCurrentDocs()
        }

        drawerLayout.addDrawerListener(drawerNavigation)
        drawerLayout.drawerElevation = 0f
        drawerLayout.findViewById<NavigationView>(R.id.nav_view)
            .setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // set up drag listener for nav drawer
        navDrawerRV.setOnDragListener(drawerNavigation.getDragInstance(this))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    // New Note
    override fun onFabAction1Clicked() {
//        if (animatedFab.expanded) {
//            val bundle = Bundle()
//            bundle.putLong("id", -1)
//            bundle.putLong("folderId", currentFolder.id)
//            animatedFab.hide()
//
//            navController.navigate(R.id.toNoteFragment, bundle)
//        }
    }

    // New TodoList
    override fun onFabAction2Clicked() {
//        if (animatedFab.expanded) {
//            val bundle = Bundle()
//            bundle.putLong("id", -1)
//            Log.d(TAG, "currentFolder.name :: ${currentFolder.name}")
//            bundle.putLong("folderId", currentFolder.id)
//            animatedFab.hide()
//
//            navController.navigate(R.id.toToDoListFragment, bundle)
//        }

    }

    // New Project
    override fun onFabAction3Clicked() {
        //todo
    }

    override fun startDrawerDrag(
        clipData: ClipData,
        shadowBuilder: View.DragShadowBuilder,
        view: View
    ) {
        navDrawerRV.setOnDragListener(DrawerNavigationDragListener(this))
        drawerLayout.open()
        view.startDragAndDrop(
            clipData,
            shadowBuilder,
            view,
            0
        )
    }

    override fun startFabDrag(
        clipData: ClipData,
        shadowBuilder: View.DragShadowBuilder,
        viewGroup: ViewGroup
    ) {
        binding.mainActivityAppContainer.setOnDragListener(OnDragEvent(this))
        viewGroup.startDragAndDrop(
            clipData,
            shadowBuilder,
            viewGroup,
            0
        )
    }

    override fun sendDrawerDropData(
        projectId: String,
        category: String,
        parentFolder: String,
        newFolder: String
    ) {
        viewModel.moveProject(projectId, category, parentFolder, newFolder)
        drawerLayout.close()
    }

    companion object {
        private const val TAG = "MainActivity.kt"
    }
}