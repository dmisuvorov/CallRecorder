package com.media.dmitry68.callrecorder.shaker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.media.dmitry68.callrecorder.preferences.ManagerPref
import com.media.dmitry68.callrecorder.preferences.SettingsFragment.Companion.CHANGE_PREFERENCE_COUNT_OF_SHAKE
import com.media.dmitry68.callrecorder.preferences.SettingsFragment.Companion.CHANGE_PREFERENCE_SENSITIVITY
import com.media.dmitry68.callrecorder.preferences.SettingsFragment.Companion.CHANGE_PREFERENCE_VIBRATE_ON_SHAKE
import com.media.dmitry68.callrecorder.service.ServiceOnDemandManager

class ShakeManager(context: Context,
                   private val shakeDetector: ShakeDetector) {

    private val managerPref = ManagerPref(context)
    private val localBroadcastManager = LocalBroadcastManager.getInstance(context)
    private val innerReceiver = ReceiverOfManageShakeDetectorPref()
    private val TAG = "LOG"


    fun registerInnerReceiver(){
        Log.d(TAG, "ShakeManager: register inner receiver")
        val intentFilterForChangeShakePref = IntentFilter(CHANGE_PREFERENCE_COUNT_OF_SHAKE).apply {
            addAction(CHANGE_PREFERENCE_SENSITIVITY)
            addAction(CHANGE_PREFERENCE_VIBRATE_ON_SHAKE)
        }
        localBroadcastManager.registerReceiver(innerReceiver, intentFilterForChangeShakePref)
        changeCountOfShake(managerPref.getCountOfShake())
        changeSensitivity(managerPref.getSensitivityShake())
        changeModeVibrateOnShake(managerPref.getModeVibrateOnShake())
    }

    fun unRegisterInnerReceiver(){
        Log.d(TAG, "ShakeManager: unregister inner receiver")
        localBroadcastManager.unregisterReceiver(innerReceiver)
        localBroadcastManager.sendBroadcast(Intent(ServiceOnDemandManager.STOP_SERVICE_ON_DEMAND))
    }

    private fun changeCountOfShake(newCountOfShake: Int){
        shakeDetector.countOfShakeForEvent = newCountOfShake
    }

    private fun changeSensitivity(newSensitivity: Int){
        val percentSensitivity = 1 - (newSensitivity.toFloat() / 100)//TODO: test formula of threshold
        val thresholdGravity = 2 + (2.5F * percentSensitivity)
        Log.d(TAG, "ShakeManager: new ThresholdGravity: $thresholdGravity")
        shakeDetector.shakeThresholdGravity = thresholdGravity
    }

    private fun changeModeVibrateOnShake(newMode: Boolean){
        shakeDetector.flagVibrate = newMode
    }

    inner class ReceiverOfManageShakeDetectorPref : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                CHANGE_PREFERENCE_COUNT_OF_SHAKE -> {
                    Log.d(TAG, "ShakeManager: CHANGE_PREFERENCE_COUNT_OF_SHAKE")
                    changeCountOfShake(managerPref.getCountOfShake())
                }
                CHANGE_PREFERENCE_SENSITIVITY -> {
                    Log.d(TAG, "ShakeManager: CHANGE_PREFERENCE_SENSITIVITY")
                    changeSensitivity(managerPref.getSensitivityShake())
                }
                CHANGE_PREFERENCE_VIBRATE_ON_SHAKE -> {
                    Log.d(TAG, "ShakeManager: CHANGE_PREFERENCE_VIBRATE_ON_SHAKE")
                    changeModeVibrateOnShake(managerPref.getModeVibrateOnShake())
                }
            }
        }
    }
}