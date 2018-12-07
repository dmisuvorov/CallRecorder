package com.media.dmitry68.callrecorder.service

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat

class ServiceManager(val context: Context){

    fun startCallService() {
        val intent = Intent().apply {
            setClass(context, CallService::class.java)
            setAction(CallService.START_FOREGROUND_ACTION)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopCallService() {
        val intent = Intent().apply {
            setClass(context, CallService::class.java)
            setAction(CallService.STOP_FOREGROUND_ACTION)
        }
        ContextCompat.startForegroundService(context, intent)
    }
}