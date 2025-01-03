package com.warmerhammer.personalnotes.homescreen

import android.annotation.SuppressLint
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.warmerhammer.personalnotes.Data.DatabaseUtil
import com.warmerhammer.personalnotes.FolderListFragment.HomeScreenViewModel
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.AnimatedFab
import com.warmerhammer.personalnotes.databinding.HomeScreenFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import java.util.UUID
import javax.inject.Inject

@FragmentScoped
@AndroidEntryPoint
class HomeScreenFragment @Inject constructor() : Fragment() {

    private var binding: HomeScreenFragmentBinding? = null
    private val homeScreenViewModel: HomeScreenViewModel by viewModels()
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recentDocsRvAdapter: RecentDocsRVAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = HomeScreenFragmentBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.home_screen_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.actionBarTitle.postValue("Home Page")
        initRecentDocsRVAdapter()
        observeDocs()
    }

    private fun observeDocs() {
        homeScreenViewModel.getAllDocs().observe(this as LifecycleOwner) { prjctList ->
            recentDocsRvAdapter.submitList(prjctList)
        }
    }

    private fun initRecentDocsRVAdapter() {
        recyclerView = requireView().findViewById(R.id.recent_documents_rv)
        recentDocsRvAdapter = RecentDocsRVAdapter()
        recyclerView.adapter = recentDocsRvAdapter
    }


}