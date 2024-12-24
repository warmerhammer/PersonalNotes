package com.warmerhammer.personalnotes.DocsListFragment

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.AnimatedFab
import com.warmerhammer.personalnotes.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
@AndroidEntryPoint
class DocsListFragment @Inject constructor() : Fragment() {

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val docsListViewModel: DocsListViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocsListRecyclerViewAdapter
    private var binding: FragmentHomeBinding? = null
    private var currentFolderId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // retrieve folder before initialization
        mainViewModel.currentFolder.observe(this as LifecycleOwner) {
            mainViewModel.actionBarTitle.postValue("Folder: ${it.name}")
            currentFolderId = it.id
            initRVAdapter()
            observeProjects()
            observeItemsToDelete()
        }
    }

    private fun observeItemsToDelete() {
        mainViewModel.itemsToDelete.observe(this as LifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                currentFolderId?.let {
                    docsListViewModel.deleteSelectedProjects(it, items)
                }
                items.forEach { prjct ->
                    mainViewModel.removeCheck(prjct)
                }
            }
        }
    }

    private fun initRVAdapter() {
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView = requireView().findViewById(R.id.project_list_recycler_view)
        recyclerView.layoutManager = layoutManager

        adapter = DocsListRecyclerViewAdapter(this, mainViewModel) { message, project, pos ->
            project as Project

            when (message) {
                "Delete project" -> docsListViewModel.deleteProject(project)

                "Detail fragment" -> {
                    currentFolderId?.let { folderId ->
                        val action =
                            when (project.category) {
                                "notes" -> {
                                    val localProject = project as Note
                                    DocsListFragmentDirections.toNoteFragment(
                                        localProject.id,
                                        folderId
                                    )
                                }

                                else -> {
                                    val localProject = project as TodoList
                                    DocsListFragmentDirections.toToDoListFragment(
                                        localProject.id,
                                        folderId
                                    )
                                }

                            }

                        findNavController().navigate(action) // navigate to details page
                    }

                }
            }
        }

        recyclerView.adapter = adapter
    }

    private fun observeProjects() {
        currentFolderId?.let {
            docsListViewModel.getDocsByFolderId(it)
            docsListViewModel.homePageDocs.observe(this as LifecycleOwner) { homePageDocs ->
                Log.d(TAG, "observeProjects: homePageDocs :: ${homePageDocs.size}")
                val rv = requireView().findViewById<RecyclerView>(R.id.project_list_recycler_view)

                if (homePageDocs.isNotEmpty()) {
                    requireActivity().findViewById<ProgressBar>(R.id.loader).visibility = View.GONE

                    requireActivity().findViewById<LinearLayout>(R.id.new_note_message).visibility =
                        View.GONE
                    rv.visibility = View.VISIBLE
                    requireActivity().findViewById<FrameLayout>(R.id.main_fab_button).setBackgroundResource(R.drawable.fab_background)
                    adapter.submitList(homePageDocs.toList())
                } else {
                    rv.visibility = View.GONE
                    requireActivity().findViewById<LinearLayout>(R.id.new_note_message).visibility =
                        View.VISIBLE
                    requireActivity().findViewById<FrameLayout>(R.id.main_fab_button).setBackgroundResource(R.drawable.fab_background_highlighted)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val TAG = "HomeProjectFragment.kt"

        @JvmStatic
        fun newInstance() =
            DocsListFragment.apply { }
    }
}