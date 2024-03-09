package com.warmerhammer.personalnotes.FolderListFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.databinding.FragmentFolderListBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
@AndroidEntryPoint
class FolderListFragment @Inject constructor() : Fragment() {

    private val folderListViewModel: FolderListViewModel by viewModels()
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private var binding: FragmentFolderListBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FolderListRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFolderListBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.fragment_folder_list, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.actionBarTitle.postValue("Select Destination")
        initRVAdapter()
        observeFiles()
    }

    private fun observeFiles() {
        folderListViewModel.getAllFolders().observe(this as LifecycleOwner) { folders ->
            Log.i("FOLDERLISTFRAGMENT.KT", "folders: $folders")
            adapter.submitList(folders.toList())
        }
    }

    private fun initRVAdapter() {
        recyclerView = requireView().findViewById(R.id.folder_list_recycler_view)
        adapter = FolderListRecyclerViewAdapter()
        recyclerView.adapter = adapter
    }
}

