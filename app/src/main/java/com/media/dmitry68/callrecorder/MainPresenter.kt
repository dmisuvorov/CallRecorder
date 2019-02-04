package com.media.dmitry68.callrecorder

import android.util.Log
import com.media.dmitry68.callrecorder.permissions.PermissionManager
import com.media.dmitry68.callrecorder.preferences.ManagerPref
import com.media.dmitry68.callrecorder.service.ModeOfWork
import com.media.dmitry68.callrecorder.service.ServiceManager

class MainPresenter(
    private val mvpView: MVPView,
    private val serviceManager: ServiceManager,
    private val permissionManager: PermissionManager,
    private val managerPref: ManagerPref
): MVPPresenter {
    private val TAG = "LOG"
    private val model = MVPModel()

    override fun setUp() {
        serviceManager.presenter = this
        managerPref.presenter = this
        val initialState = serviceManager.isServiceRunning()
        model.stateOfService = initialState
        if (!permissionManager.checkPermission())
            permissionManager.requestPermission()
        else {
            onCheckPermission(true)
        }
    }

    override fun onCheckPermission(checkPermission: Boolean) {
        mvpView.showSwitchVisibility(checkPermission)
        if (checkPermission){
            initialSetModeOfWork()
            setSwitchCompatState(model.stateOfService)
        } else {
            setSwitchCompatState(false)
        }
    }

    override fun setSwitchCompatState(state: Boolean){
        mvpView.setSwitchMode(state)
    }

    override fun switchCompatChange(modeService: Boolean) {
        val isServiceRunning = serviceManager.isServiceRunning()
        Log.d(TAG, "Presenter: switchCompatChange $modeService and service run: $isServiceRunning")
        if (isServiceRunning != modeService) {
            Log.d(TAG, "presenter: switchChange to $modeService")
            if (modeService) {
                serviceManager.startCallService()
            } else {
                serviceManager.stopCallService()
            }
            model.stateOfService = modeService
        }
    }

    override fun onChangeModeOfWork(newModeOfWork: ModeOfWork) {
        Log.d(TAG, "Presenter: onChangeModeOfWork stateOfService ${model.stateOfService} newModeOfWork $newModeOfWork")
        if (model.stateOfService){
            initRestartService()
        }
        serviceManager.modeOfWork = newModeOfWork
    }

    override fun onStopServiceForHisRestart(){
        setSwitchCompatState(true)
    }

    override fun onStartPreferenceFragment() {
        managerPref.registerListenerOnSharedPref()
    }

    private fun initialSetModeOfWork(){
        val initialModeOfWork = managerPref.getModeOfWorkInSharedPref()
        serviceManager.modeOfWork = initialModeOfWork
        Log.d(TAG, "presenter: Setup in initialState: ${model.stateOfService} in mode of work: $initialModeOfWork")
    }

    private fun initRestartService(){
        Log.d(TAG, "Presenter: initRestartService")
        serviceManager.registerReceiverForRestartService()
        setSwitchCompatState(false)
    }
}