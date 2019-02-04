package com.media.dmitry68.callrecorder.preferences

import android.content.*
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.PreferenceManager
import android.util.Log
import com.media.dmitry68.callrecorder.MainPresenter
import com.media.dmitry68.callrecorder.R
import com.media.dmitry68.callrecorder.service.ModeOfWork

class ManagerPref(private val context : Context){
    var presenter: MainPresenter? = null
    private val receiverOnChangePref = ReceiverOnChangePref()
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val localBroadcastManager = LocalBroadcastManager.getInstance(context)
    private val TAG = "LOG"

    fun getFileName() = sharedPref.getString(KEY_PREF_FILE_NAME, context.getString(R.string.pref_file_name))!!

    fun getFlagShowDirectionCall() = sharedPref.getBoolean(KEY_PREF_FLAG_SHOW_DIRECTION_CALL, true)

    fun getFlagShowNumber() = sharedPref.getBoolean(KEY_PREF_FLAG_SHOW_NUMBER, true)

    fun getAudioSource() = sharedPref.getString(KEY_PREF_AUDIO_SOURCE, getPrefAudioSourceVoiceCommunication())!!

    fun getFlagSpeakerphone() = sharedPref.getBoolean(KEY_PREF_SPEAKERPHONE, true)

    fun getModeOfWorkInSharedPref() : ModeOfWork {
        return when (getStringModeOfWorkInSharedPref()){
            getPrefModeOfWorkDefault() -> {
                ModeOfWork.Background
            }
            getPrefModeOfWorkOnDemandShake() -> {
                ModeOfWork.OnDemandShake
            }
            getPrefModeOfWorkOnDemandButton() -> {
                ModeOfWork.OnDemandButton
            }
            else -> {
                ModeOfWork.Background
            }
        }
    }

    fun getStringModeOfWorkInSharedPref() = sharedPref.getString(KEY_PREF_MODE_OF_WORK, getPrefModeOfWorkDefault())!!

    fun getFlagStartModeOnlyWithCall() = sharedPref.getBoolean(KEY_PREF_FLAG_START_MODE_ONLY_WITH_CALL, true)

    fun getCountOfShake() = sharedPref.getInt(KEY_PREF_COUNT_OF_SHAKE, 3)

    fun getSensitivityShake() = sharedPref.getInt(KEY_PREF_SENSITIVITY_SHAKE, 70)

    fun getModeVibrateOnShake() = sharedPref.getBoolean(KEY_PREF_FLAG_VIBRATE_ON_SHAKE, false)

    //TODO: make manager of resource for next fun
    private fun getPrefModeOfWorkDefault() : String = context.getString(R.string.pref_mode_of_work_default)

    fun getPrefModeOfWorkOnDemandButton(): String = context.getString(R.string.pref_mode_of_work_on_button)

    fun getPrefModeOfWorkOnDemandShake() : String = context.getString(R.string.pref_mode_of_work_on_shake)

    fun getPrefAudioSourceVoiceCommunication() : String = context.getString(R.string.pref_audio_source_voice_communication)

    fun getPrefAudioSourceMic() : String = context.getString(R.string.pref_audio_source_mic)

    fun getPrefAudioSourceVoiceCall() : String = context.getString(R.string.pref_audio_source_voice_call)

    fun getPrefAudioSourceDefault() : String = context.getString(R.string.pref_audio_source_default)

    fun registerListenerOnSharedPref() {
        Log.d(TAG, "ManagerPref register on SharedPreferenceChangeReceiver")
        val intentFilterOnChangeModeOfWork = IntentFilter(SettingsFragment.CHANGE_PREFERENCE_MODE_OF_WORK).apply {
            addAction(SettingsFragment.DESTROY_PREFERENCE_FRAGMENT)
            addAction(SettingsFragment.CHANGE_PREFERENCE_START_MODE_ONLY_WITH_CALL)
        }
        localBroadcastManager.registerReceiver(receiverOnChangePref, intentFilterOnChangeModeOfWork)
    }

    private fun unRegisterListenerOnSharedPref() {
        Log.d(TAG, "ManagerPref unregister on SharedPreferenceChangeReceiver")
        localBroadcastManager.unregisterReceiver(receiverOnChangePref)
    }

    companion object {
        const val KEY_PREF_FILE_NAME = "pref_file_name"
        const val KEY_PREF_FLAG_SHOW_DIRECTION_CALL = "pref_flag_show_direction_call"
        const val KEY_PREF_FLAG_SHOW_NUMBER = "pref_flag_show_number"
        const val KEY_PREF_AUDIO_SOURCE = "pref_audio_source"
        const val KEY_PREF_SPEAKERPHONE = "pref_speakerphone"
        const val KEY_PREF_MODE_OF_WORK = "pref_mode_of_work"
        const val KEY_PREF_FLAG_START_MODE_ONLY_WITH_CALL = "pref_flag_start_mode_only_with_call"
        const val KEY_PREF_CATEGORY_GENERAL = "pref_category_general"
        const val KEY_PREF_CATEGORY_ON_SHAKE_MODE = "pref_category_on_shake_mode"
        const val KEY_PREF_COUNT_OF_SHAKE = "pref_count_of_shake"
        const val KEY_PREF_SENSITIVITY_SHAKE = "pref_sensitivity_shake"
        const val KEY_PREF_FLAG_VIBRATE_ON_SHAKE = "pref_flag_vibrate_on_shake"
    }

    inner class ReceiverOnChangePref: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SettingsFragment.DESTROY_PREFERENCE_FRAGMENT -> {
                    unRegisterListenerOnSharedPref()
                }
                SettingsFragment.CHANGE_PREFERENCE_MODE_OF_WORK -> {
                    Log.d(TAG, "ManagerPref: CHANGE_PREFERENCE_MODE_OF_WORK")
                    presenter?.onChangeModeOfWork(getModeOfWorkInSharedPref())
                }
                SettingsFragment.CHANGE_PREFERENCE_START_MODE_ONLY_WITH_CALL -> {
                    Log.d(TAG, "ManagerPref: CHANGE_PREFERENCE_START_MODE_ONLY_WITH_CALL")
                    presenter?.onChangeModeOfWork(getModeOfWorkInSharedPref())
                }
            }
        }
    }
}