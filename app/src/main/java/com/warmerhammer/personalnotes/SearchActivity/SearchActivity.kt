package com.warmerhammer.personalnotes.SearchActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init RV adapter
        recyclerView = findViewById(R.id.search_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SearchActivityRVAdapter()
        recyclerView.adapter = adapter

        // set back button
        binding.backArrow.setOnClickListener {
            finish()
        }

        initSearchListener()

    }

    private fun initSearchListener() {
        val searchText: EditText = binding.searchEditText

        searchText.addTextChangedListener(TypingListener { typing ->
            when {
                typing -> {
                    Log.i(TAG, "initSearchListener: ${searchText.text}")
                    viewModel.searchUser(searchText.text.toString())
                }
            }
        })
    }

    companion object {
        val TAG = SearchActivity::class.java.simpleName + ".kt"
    }
}