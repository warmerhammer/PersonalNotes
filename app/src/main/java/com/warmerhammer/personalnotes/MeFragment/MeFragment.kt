package com.warmerhammer.personalnotes.MeFragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.fragment.app.activityViewModels
import com.warmerhammer.personalnotes.Data.DatabaseUtil
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import com.warmerhammer.personalnotes.R
import java.util.UUID

class MeFragment : Fragment() {

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calculateStorageRemaining()
        mainViewModel.actionBarTitle.postValue("Me")
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateStorageRemaining() {
        val fiveHundredMB: Float = 524288000f
        val oneByte: Float = 1048576f
        // 1. calculate current local storage being used
        val roomDbSizeInBytes: Long =
            DatabaseUtil.getRoomDatabaseSize(requireContext(), "app-database")

        // 2. calculate storage available
        val filesDir = requireContext().filesDir
        val storageManager = requireContext().getSystemService<StorageManager>()!!
        val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(filesDir)
        val availableBytes: Long = storageManager.getAllocatableBytes(appSpecificInternalDirUuid)

        val memoryUsedPercentage: Float = roomDbSizeInBytes / fiveHundredMB
        val progressInteger: Int =
            if ((memoryUsedPercentage * 100) > 3) {
                (memoryUsedPercentage * 100).toInt()
            } else {
                3
            }

        requireView().findViewById<ProgressBar>(R.id.memory_used_pb).progress = progressInteger

        requireView().findViewById<TextView>(R.id.mb_remaining_tv).text =
            "Memory Remaining ${String.format("%.2f", roomDbSizeInBytes / oneByte)} MB / 500 MB"
    }

    companion object {
        private const val TAG = "MeFragment.kt"
    }
}