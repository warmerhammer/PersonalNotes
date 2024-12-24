package com.warmerhammer.personalnotes.SearchActivity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.SimpleCursorAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.ServiceManager
import com.warmerhammer.personalnotes.NoteFragment.NoteFragment
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.TypingListener
import com.warmerhammer.personalnotes.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import javax.inject.Singleton

@ActivityScoped
@AndroidEntryPoint
class SearchActivity @Inject constructor() : AppCompatActivity() {

    private val viewModel : SearchViewModel by viewModels()
    private lateinit var binding: ActivitySearchBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchActivityRVAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init RV adapter
        recyclerView = findViewById(R.id.search_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

         // set back button
        binding.backArrow.setOnClickListener {
            finish()
        }

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                Log.i(TAG, "query :: $query")
            }
        }

        initSearchListener()
        observeSearchSuggestions()
    }

    private fun initSearchListener() {

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.activitySearchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        binding.activitySearchView.requestFocus()
        binding.activitySearchView.isIconified = false
        binding.activitySearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText)
                return true
            }
        })
//        title.addTextChangedListener(TypingListener { typing ->
//            if (!typing) {
//                note.title = title.text.toString()
//            }
//        })


    }

    private fun observeSearchSuggestions() {
        recyclerView = binding.searchRecyclerView
        adapter = SearchActivityRVAdapter(this)
        recyclerView.adapter = adapter

        viewModel.suggestions.observe(this as LifecycleOwner) { userSuggestions ->
            Log.i(TAG, "userSuggestions :: $userSuggestions")
            adapter.submitList(userSuggestions.toList())
        }
    }

    companion object {
        val TAG = SearchActivity::class.java.simpleName + ".kt"
    }
}