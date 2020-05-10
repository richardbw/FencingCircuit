package com.barneswebb.android.fencingcircuit

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log


class StopwatchService : Service() {

    companion object {
        val TAG: String = StopwatchService::class.java.simpleName
        const val ONGOING_NOTIFICATION_ID: Int = 1234
        const val CHANNEL_ID: String = "testing testing"
    }

    private val binder = LocalBinder()// Binder given to clients
    inner class LocalBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService// Return this instance of LocalService so clients can call public methods
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind(${intent}) & startForeground = = = = = = = = = = =")
        startForegroundService(intent) //trigger onStartCommand()
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        stopSelf()
        return false
    }




    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "Starting service ========================================")

        val pendingIntent: PendingIntent =
            Intent(this, StopwatchActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        createNotificationChannel()

        startForeground(
            ONGOING_NOTIFICATION_ID,
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Stopwatch Service")
                .setContentText("Stopwatch service is running")
                .setSmallIcon(R.drawable.fencer_emoji)
                .setContentIntent(pendingIntent)
                .setTicker("ticker_text")
                .build()
        )

        return START_STICKY
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java)!!
            .createNotificationChannel(
                NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ) )
    }


//----------------------------------------------------------------------------------//
//  StopWatch stuff                                                                 //
//----------------------------------------------------------------------------------//

    enum class StopwatchState(val tagText: String) {
        IS_PAUSED("Pause"),
        IS_RUNNING("Play")
    }

    // Start and end times in milliseconds
    private var startTime: Long = 0  // Start and end times in milliseconds
    private var pauseTime: Long = 0

    // Is the service tracking time?
    var stopwatchState = StopwatchState.IS_PAUSED


    fun startStopTimer(targetState: StopwatchState) {
        when (targetState) {
            StopwatchState.IS_RUNNING ->  startTime = System.currentTimeMillis() - pauseTime
            StopwatchState.IS_PAUSED ->  pauseTime = elapsedTime()
        }
        stopwatchState = targetState
    }

    fun resetTimer() {
        pauseTime = 0
        startTime = System.currentTimeMillis()
    }


    fun elapsedTime(): Long {
        return  (System.currentTimeMillis() - startTime)
    }


    fun elapsedTimeStr(): String   // idea stolen from https://coderwall.com/p/wkdefg/converting-milliseconds-to-hh-mm-ss-mmm
    {
        var elapsedTime =   elapsedTime()
        var milliseconds =  (elapsedTime%1000)/10
        var seconds =       (elapsedTime/1000)%60
        var minutes =       (elapsedTime/(1000*60))%60
        var hours =         (elapsedTime/(1000*60*60))%24

        return "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, milliseconds)//
    }


//********************* FYI only: *********************//{{{

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "Creating service ========================================")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "Destroying service ========================================")
    }
//}}

}