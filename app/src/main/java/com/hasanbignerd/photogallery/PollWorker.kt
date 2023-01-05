package com.hasanbignerd.photogallery

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hasanbignerd.photogallery.api.FlickrFetch
import com.hasanbignerd.photogallery.model.GalleryItem
import com.hasanbignerd.photogallery.utilities.QueryPreferences

private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetch(context).fetchPhotosRequest()
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } else {
            FlickrFetch(context).searchPhotosRequest(query)
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } ?: emptyList()
        if (items.isEmpty()) {
            return Result.success()
        }
        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreferences.setLastResultId(context, resultId)

            val intent = PhotoGalleryActivity.newIntent(context)

            //this can be useful for android 12
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                pendingIntent = PendingIntent.getActivity(
//                    context,
//                    0,
//                    intent,
//                    PendingIntent.FLAG_MUTABLE
//                )
//            } else {
//                pendingIntent = PendingIntent.getActivity(
//                    context,
//                    0,
//                    intent,
//                    PendingIntent.FLAG_ONE_SHOT
//                )
//            }
            val pendingInt = PendingIntent.getActivity(context,0,intent,0)

            val resources = context.resources

            val notification = NotificationCompat
                .Builder(context,NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingInt)
                .setAutoCancel(true)
                .build()

//            val notificationManager = NotificationManagerCompat.from(context)
//            notificationManager.notify(0,notification)
//
//            context.sendBroadcast(Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE)

            showBackgroundNotification(0, notification)
        }
        return Result.success()
    }


    private fun showBackgroundNotification(
        requestCode: Int,
        notification: Notification
    ) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }
        context.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }

    companion object{
        const val PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE"
        const val ACTION_SHOW_NOTIFICATION = "com.hasanbignerd.photogallery.SHOW_NOTIFICATION"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }

}