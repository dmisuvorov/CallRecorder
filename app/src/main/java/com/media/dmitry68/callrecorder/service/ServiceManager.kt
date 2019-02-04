package com.media.dmitry68.callrecorder.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.media.dmitry68.callrecorder.MVPPresenter
import com.media.dmitry68.callrecorder.notification.NotifyManager

class ServiceManager(private val context: Context){

    var presenter: MVPPresenter? = null
    lateinit var modeOfWork: ModeOfWork
    private val innerReceiverOnRestartService = ReceiverOnRestartService()
    private val notificationServiceManager = NotifyManager(context)
    private val TAG = "LOG"

    fun startCallService() {
        when(modeOfWork) {
            is ModeOfWork.Background -> {
                //TODO: start service on boot device
                Log.d(TAG, "Service manager: START_FOREGROUND_AUTO_CALL_RECORD_ACTION")
                manageForegroundCallService(CallForegroundService.START_FOREGROUND_AUTO_CALL_RECORD_ACTION)
            }
            is ModeOfWork.OnDemandShake -> {
                Log.d(TAG, "Service manager: START_FOREGROUND_ON_DEMAND_SHAKE_RECORD_ACTION")
                manageForegroundCallService(CallForegroundService.START_FOREGROUND_ON_DEMAND_SHAKE_RECORD_ACTION)
            }
            is ModeOfWork.OnDemandButton -> {
                Log.d(TAG, "Service manager: START_FOREGROUND_ON_DEMAND_BUTTON_RECORD_ACTION")
                manageForegroundCallService(CallForegroundService.START_FOREGROUND_ON_DEMAND_BUTTON_RECORD_ACTION)
            }
        }
    }

    fun stopCallService() {
        when(modeOfWork) {
            is ModeOfWork.Background -> {
                Log.d(TAG, "Service manager: STOP_FOREGROUND_AUTO_CALL_RECORD_ACTION")
                manageForegroundCallService(CallForegroundService.STOP_FOREGROUND_AUTO_CALL_RECORD_ACTION)
            }
            is ModeOfWork.OnDemandShake -> {
                Log.d(TAG, "Service manager: STOP_FOREGROUND_ON_DEMAND_SHAKE_RECORD_ACTION")
                manageForegroundCallService(CallForegroundService.STOP_FOREGROUND_ON_DEMAND_SHAKE_RECORD_ACTION)
            }
            is ModeOfWork.OnDemandButton -> {
                Log.d(TAG, "Service manager: STOP_FOREGROUND_ON_DEMAND_BUTTON_RECORD_ACTION")
                manageForegroundCallService(CallForegroundService.STOP_FOREGROUND_ON_DEMAND_BUTTON_RECORD_ACTION)
            }
        }
    }

    fun registerReceiverForRestartService(){
        Log.d(TAG, "ServiceManager: registerReceiverForRestartService")
        val intentFilterOnStopCallService = IntentFilter()
        when (modeOfWork) {
            ModeOfWork.Background -> {
                intentFilterOnStopCallService.addAction(CallForegroundService.STOP_FOREGROUND_AUTO_CALL_SERVICE)
            }
            ModeOfWork.OnDemandShake, ModeOfWork.OnDemandButton -> {
                intentFilterOnStopCallService.addAction(ServiceOnDemandManager.STOP_SERVICE_ON_DEMAND)
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(innerReceiverOnRestartService, intentFilterOnStopCallService)
    }

    fun isServiceRunning() : Boolean{
        val isEmptyNotification = notificationServiceManager.isActiveNotification(NotifyManager.NOTIFICATION_ID)
        Log.d(TAG, "ServiceManager: isServiceRunning: $isEmptyNotification")
        return isEmptyNotification
    }

    private fun onStopServiceForHisRestart() {
        presenter?.onStopServiceForHisRestart()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(innerReceiverOnRestartService)
    }

    //TODO: test service on xiaomi device;
    // TODO: stopwatch in background mode; notification on new file

    private fun manageForegroundCallService(actionStopOrStart: String){
        val intent = Intent().apply {
            setClass(context, CallForegroundService::class.java)
            action = actionStopOrStart
        }
        ContextCompat.startForegroundService(context, intent)
    }

    inner class ReceiverOnRestartService: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "ReceiverOnRestartService: onReceive ${intent!!.action}")
            when (intent.action) {
                CallForegroundService.STOP_FOREGROUND_AUTO_CALL_SERVICE, ServiceOnDemandManager.STOP_SERVICE_ON_DEMAND -> {
                    onStopServiceForHisRestart()
                }
            }
        }
    }
}