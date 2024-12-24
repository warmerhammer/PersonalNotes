package com.warmerhammer.personalnotes.SharedScreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.SearchActivity.SearchActivity
import com.warmerhammer.personalnotes.databinding.SharedScreenFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
@AndroidEntryPoint
class SharedScreenFragment @Inject constructor(
) : Fragment() {
    private var binding: SharedScreenFragmentBinding? = null
    private val mainViewModel: MainActivityViewModel by activityViewModels()

    private val getContactFromSearch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i("MainActivity.kt", "result :: ${result.data}")
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SharedScreenFragmentBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.shared_screen_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.actionBarTitle.postValue("Shared")

        view.findViewById<TextView>(R.id.search_edit_text).setOnClickListener {
            val i = Intent(requireContext(), SearchActivity::class.java)
//            val transitionAnimation = ActivityOptionsCompat.makeSceneTransitionAnimation(AppCompatActivity())

            getContactFromSearch.launch(i)

            // Apply activity transition
//            startActivity(
//                i,
//                ActivityOptions.makeSceneTransitionAnimation(AppCompatActivity()).toBundle()
//            )
        }

    }
}