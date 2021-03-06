package com.media.dmitry68.callrecorder.shaker

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.media.dmitry68.callrecorder.service.ServiceOnDemandManager


class ShakeDetector(private val serviceOnDemandManager: ServiceOnDemandManager) : SensorEventListener, ShakeListener {
    private val vibrateManager = serviceOnDemandManager.vibrateManager
    private var shakeTimeStamp = 0L
    private var shakeCount = 0
    var flagVibrate = false
    var countOfShakeForEvent = 3
    var shakeThresholdGravity = 2.7F
    private val TAG = "LOG"

    override fun onSensorChanged(event: SensorEvent?) {
        val x = event!!.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble())

        if (gForce > shakeThresholdGravity){
            Log.d(TAG, "Sensor detector $shakeCount changes")
            val now = System.currentTimeMillis()
            if (shakeTimeStamp + SHAKE_STOP_TIME_MS > now)
                return

            if (shakeTimeStamp + SHAKE_COUNT_RESET_TIME_MS < now)
                shakeCount = 0

            shakeTimeStamp = now
            shakeCount++

            onShake(shakeCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onShake(count: Int) {
        Log.d(TAG, "Detect $count shake")
        if (flagVibrate)
            vibrateManager.vibrate(VIBRATE_TIME_MS_ON_EVERY_SHAKE)
        if (count == countOfShakeForEvent) {
            flagVibrate = false
            serviceOnDemandManager.startRecordOnShakeMode()
        }
    }

    companion object {
        const val SHAKE_STOP_TIME_MS = 500
        const val SHAKE_COUNT_RESET_TIME_MS = 3000
        const val VIBRATE_TIME_MS_ON_EVERY_SHAKE = 500L
    }


}