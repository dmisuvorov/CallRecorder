package com.media.dmitry68.callrecorder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.media.dmitry68.callrecorder.MainActivity
import com.media.dmitry68.callrecorder.R
import com.media.dmitry68.callrecorder.preferences.ManagerPref

class NotifyManager(private val context: Context) {
    private val TAG = "LOG"
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val contentTitle: String = context.getString(com.media.dmitry68.callrecorder.R.string.notification_title)
    private var channelName: String = context.getString(com.media.dmitry68.callrecorder.R.string.channel_name)//TODO: make manager resource
    private val managerPref = ManagerPref(context)
    private val modeOfWorkString = managerPref.getStringModeOfWorkInSharedPref()
    var contentText: String = StringBuilder().apply{
            append(context.getString(R.string.notification_text_mode_of_work))
            append(": ")
            append(modeOfWorkString)
           // append(context.getString(R.string.notification_text_recorder_status))
        }.toString()

    fun builder() : NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel()
        }
        notificationBuilder=
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))

        val resultIntent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context).apply {
            addParentStack(MainActivity::class.java)
            addNextIntent(resultIntent)
        }
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.setContentIntent(resultPendingIntent)
        return notificationBuilder
    }

    fun addAction(actionIntent: String, actionText: String, actionIcon: Int){
        val intentForBroadcast = Intent(actionIntent)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intentForBroadcast, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(NotificationCompat.Action(actionIcon, actionText, pendingIntent))
        Thread.sleep(1000)
        updateNotification()
        Log.d(TAG, "NotifyManager: addAction $actionText")
    }

    fun removeAction(){
        notificationBuilder.mActions.clear()
        updateNotification()
        Log.d(TAG, "NotifyManager: removeAction")
    }

    fun isEmptyOfNotificationActions() : Boolean {
        val isEmptyNotification = notificationBuilder.mActions.isEmpty()
        Log.d(TAG, "NotifyManager: empty of action $isEmptyNotification")
        return isEmptyNotification
    }

    fun removeNotification(idNotification: Int) {
        Log.d(TAG, "NotifyManager: remove notification")
        notificationManager.cancel(idNotification)
        Thread.sleep(1000) //TODO: On the some phone notification cancelled through the time
        var isRemoveNotification = true
        while (true) {
            val activeNotifications = notificationManager.activeNotifications
            for (activeNotification in activeNotifications) {
                if (activeNotification.id == idNotification){
                    isRemoveNotification = false
                }
            }
            if (isRemoveNotification)
                return
        }
    }

    fun addText(text: String){
        notificationBuilder.setContentText(text)
        updateNotification()
    }

    fun isActiveNotification(idNotification: Int) : Boolean {
        val activeNotifications = notificationManager.activeNotifications
        for (activeNotification in activeNotifications) {
            if (activeNotification.id == idNotification){
                return true
            }
        }
        return false
    }

    private fun updateNotification() = notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(){
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "100"
    }
}