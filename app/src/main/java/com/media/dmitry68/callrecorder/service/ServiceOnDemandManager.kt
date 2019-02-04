package com.media.dmitry68.callrecorder.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.media.dmitry68.callrecorder.R
import com.media.dmitry68.callrecorder.notification.NotifyManager
import com.media.dmitry68.callrecorder.preferences.ManagerPref
import com.media.dmitry68.callrecorder.receiver.CallReceiver
import com.media.dmitry68.callrecorder.recorder.Recorder
import com.media.dmitry68.callrecorder.stateCall.Caller
import com.media.dmitry68.callrecorder.stopwatch.StopwatchManager
import com.media.dmitry68.callrecorder.vibrator.VibrateManager

class ServiceOnDemandManager(private val appContext: Context,
                             private val notificationManager: NotifyManager) {
    val vibrateManager = VibrateManager(appContext)
    private val innerReceiverForManageRecorder = ReceiverOfManageRecorder()
    private val localBroadcastManager = LocalBroadcastManager.getInstance(appContext)
    private val prefManager = ManagerPref(appContext)
    private val modeOfWork = prefManager.getModeOfWorkInSharedPref()
    private var caller = Caller()
    private var flagCall = false
    private lateinit var recorder: Recorder
    private lateinit var stopwatchManager: StopwatchManager
    private val TAG = "LOG"
    //TODO: make manager of resource
    private val actionStartRecorderText = appContext.getString(R.string.notification_action_start_recorder)
    private val actionStopRecorderText = appContext.getString(R.string.notification_action_stop_recorder)
    private val iconStartRecord = R.drawable.start_record_button_notification
    private val iconStopRecord = R.drawable.stop_record_button_notification

    fun initOnDemandWithCallMode() {
        Log.d(TAG, "ServiceOnDemandManager: InitOnDemandWithCallMode")
        localBroadcastManager.registerReceiver(innerReceiverForManageRecorder, IntentFilter(ON_CALL_STATE_START).apply {
            addAction(ON_CALL_STATE_STOP)
            addAction(CallForegroundService.STOP_FOREGROUND_SERVICE)
        })
        localBroadcastManager.sendBroadcast(Intent(CallForegroundService.START_CALL_RECEIVER))
    }

    fun initButtonMode(){
        Log.d(TAG, "ServiceOnDemandManager: Init button mode")
        appContext.registerReceiver(innerReceiverForManageRecorder, IntentFilter(START_RECORD_ACTION_ON_BUTTON_MODE))
        localBroadcastManager.registerReceiver(innerReceiverForManageRecorder, IntentFilter(CallForegroundService.STOP_FOREGROUND_SERVICE))
        notificationManager.addAction(START_RECORD_ACTION_ON_BUTTON_MODE, actionStartRecorderText, iconStartRecord)
    }

    fun startRecordOnShakeMode(){
        Log.d(TAG, "ServiceOnDemandManager: Start record on shake mode")
        vibrateManager.vibrate(1000L)
        onStartRecordActionOnDemandMode()
        localBroadcastManager.sendBroadcast(Intent(CallForegroundService.STOP_REGISTER_SHAKE_DETECTOR))
    }

    private fun startRecordOnButtonMode(){
        Log.d(TAG, "ServiceOnDemandManager: Start record on button mode")
        unregisterInnerReceiverAndClearActionInNotification()
        onStartRecordActionOnDemandMode()
    }

    private fun onStartRecordActionOnDemandMode(){
        Log.d(TAG, "ServiceOnDemandManager: Start record on demand")
        onStartRecord()
        appContext.registerReceiver(innerReceiverForManageRecorder, IntentFilter(STOP_RECORD_ACTION_ON_DEMAND_MODE))
        localBroadcastManager.registerReceiver(innerReceiverForManageRecorder, IntentFilter(ON_CALL_STATE_START).apply {
            addAction(ON_CALL_STATE_STOP)
            addAction(CallForegroundService.STOP_FOREGROUND_SERVICE)
        })
        localBroadcastManager.sendBroadcast(Intent(CallForegroundService.START_CALL_RECEIVER))
    }

    private fun onStartRecord(){
        initRecorder()
        stopwatchManager = StopwatchManager(notificationManager)
        stopwatchManager.start()
        notificationManager.addAction(STOP_RECORD_ACTION_ON_DEMAND_MODE, actionStopRecorderText, iconStopRecord)
    }

    private fun initRecorder() {
        recorder = Recorder(caller, appContext).apply { startRecord() }
    }

    private fun onStopRecordActionOnDemandMode(){
        unregisterInnerReceiverAndClearActionInNotification()
        onStopRecord()
        when (modeOfWork) {
            ModeOfWork.Background -> {
                throw IllegalStateException("Error: STOP_RECORD_ACTION_ON_DEMAND_MODE with mode of work Background")
            }
            ModeOfWork.OnDemandShake -> {
                localBroadcastManager.sendBroadcast(Intent(CallForegroundService.STOP_CALL_RECEIVER))
                localBroadcastManager.sendBroadcast(Intent(CallForegroundService.START_REGISTER_SHAKE_DETECTOR))
                notificationManager.addText(notificationManager.contentText)
            }
            ModeOfWork.OnDemandButton -> {
                localBroadcastManager.sendBroadcast(Intent(CallForegroundService.STOP_CALL_RECEIVER))
                initButtonMode()
                notificationManager.addText(notificationManager.contentText)
            }
        }
    }

    private fun unregisterInnerReceiverAndClearActionInNotification(){
        localBroadcastManager.unregisterReceiver(innerReceiverForManageRecorder)
        if (notificationManager.isEmptyOfNotificationActions()){
            return
        } else {
            notificationManager.removeAction()
            appContext.unregisterReceiver(innerReceiverForManageRecorder)
        }
    }

    private fun onStopRecord(){
        Log.d(TAG, "ServiceOnDemandManager: onStopRecordAction")
        stopRecorder()
        stopwatchManager.stop()
    }

    private fun stopRecorder(){
        Log.d(TAG, "Stop record on Demand Manager")
        recorder.stopRecord()
        if (flagCall)
            recorder.addToAudioFileCallNumberAndDirection()
        caller = Caller()
        flagCall = false
    }

    companion object {
        const val STOP_SERVICE_ON_DEMAND = "com.media.dmitry68.callrecorder.service.STOP_SERVICE_ON_DEMAND"
        const val START_RECORD_ACTION_ON_BUTTON_MODE = "com.media.dmitry68.callrecorder.service.START_RECORD_ACTION_ON_BUTTON_MODE"
        const val STOP_RECORD_ACTION_ON_DEMAND_MODE = "com.media.dmitry68.callrecorder.service.STOP_RECORD_ACTION_ON_DEMAND_MODE"
        const val ON_CALL_STATE_START = "com.media.dmitry68.callrecorder.service.ON_CALL_STATE_START"
        const val ON_CALL_STATE_STOP = "com.media.dmitry68.callrecorder.service.ON_CALL_STATE_STOP"
    }

    inner class ReceiverOfManageRecorder : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action){
                START_RECORD_ACTION_ON_BUTTON_MODE -> {//TODO: make START_RECORD_ACTION_ON_DEMAND_MODE
                    Log.d(TAG, "ServiceOnDemandManager: onReceive START_RECORD_ACTION_ON_BUTTON_MODE")
                    startRecordOnButtonMode()
                }
                STOP_RECORD_ACTION_ON_DEMAND_MODE -> {
                    Log.d(TAG, "ServiceOnDemandManager: onReceive STOP_RECORD_ACTION_ON_DEMAND_MODE $modeOfWork")
                    onStopRecordActionOnDemandMode()
                }
                CallForegroundService.STOP_FOREGROUND_SERVICE -> {
                    Log.d(TAG, "ServiceOnDemandManager: onReceive STOP_FOREGROUND_SERVICE")
                    unregisterInnerReceiverAndClearActionInNotification()
                    if (Recorder.flagStarted)
                        onStopRecord()
                    notificationManager.removeNotification(NotifyManager.NOTIFICATION_ID)
                    localBroadcastManager.sendBroadcast(Intent(STOP_SERVICE_ON_DEMAND))
                }
                ON_CALL_STATE_START -> {
                    Log.d(TAG, "ServiceOnDemandManager: onReceive ON_CALL_STATE_START")
                    if (prefManager.getFlagStartModeOnlyWithCall()){
                        when(modeOfWork) {
                            ModeOfWork.Background -> {
                                throw IllegalStateException("Error: ON_CALL_STATE_START with mode of work Background")
                            }
                            ModeOfWork.OnDemandShake -> {
                                localBroadcastManager.sendBroadcast(Intent(CallForegroundService.START_REGISTER_SHAKE_DETECTOR))
                            }
                            ModeOfWork.OnDemandButton -> {
                                initButtonMode()
                            }
                        }
                    } else if (prefManager.getFlagSpeakerphone()) {//TODO: make good job of speakerphone and recorder
                        notificationManager.removeAction()
                        onStopRecord()
                        onStartRecord()
                    }
                    flagCall = true
                    caller.number = intent.getStringExtra(CallReceiver.CALL_NUMBER)
                    caller.directCallState = intent.getStringExtra(CallReceiver.DIRECT_CALL)
                }
                ON_CALL_STATE_STOP -> {
                    Log.d(TAG, "ServiceOnDemandManager: onReceive ON_CALL_STATE_STOP")
                    caller.number = intent.getStringExtra(CallReceiver.CALL_NUMBER)
                    caller.directCallState = intent.getStringExtra(CallReceiver.DIRECT_CALL)
                    if (Recorder.flagStarted) {
                        onStopRecordActionOnDemandMode() //TODO: make it preference if flag only with call false
                    }
                    if (prefManager.getFlagStartModeOnlyWithCall()){
                        if (modeOfWork == ModeOfWork.OnDemandShake){
                            localBroadcastManager.sendBroadcast(Intent(CallForegroundService.STOP_REGISTER_SHAKE_DETECTOR))
                        }
                        unregisterInnerReceiverAndClearActionInNotification()
                        initOnDemandWithCallMode()
                    }
                }
            }
        }
    }
}