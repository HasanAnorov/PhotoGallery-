package com.hasanbignerd.photogallery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

private const val TAG = "VISIBLE_FRAGMENT"

open class VisibleFragment() : Fragment(){

    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: is on")
            Toast.makeText(requireContext(),
                "Got a broadcast: ${intent.action}",
                Toast.LENGTH_LONG)
                .show()

            Log.i(TAG, "canceling notification")
            resultCode = Activity.RESULT_CANCELED
        }
    }
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            PollWorker.PERM_PRIVATE,
            null
        )
    }
    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }

}