/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    NOTE TO SELF:
This class should contain all the timing & exercise logic
The attached clients/activities should be 'dumb Views'

 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
package com.barneswebb.android.fencingcircuit

import android.app.*
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.stream.Collectors


class StopwatchService : Service() {

    companion object {
        private val TAG: String = StopwatchService::class.java.simpleName
        private val CHANNEL_ID: String = "$TAG:NotifChanl"
        private const val ONGOING_NOTIFICATION_ID: Int = 1234
        private const val TONE_STREAM = AudioManager.STREAM_ALARM

        const val INTENT_NAME_UPDATE_TIMESTR= "stopwatchTimeStr"
        const val INTENT_NAME_PROC_TICK     = "exerciseIdxToDisplay"
        const val INTENTXTR_EXIDX_TO_DISPL  = "exIdx"
        const val INTENTXTR_CUREX_T_DISPL   = "thisExProgressStr"
    }



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        Log.v(TAG, "Starting service ========================================")


        HandlerThread("ClockTickHandlerThread", Process.THREAD_PRIORITY_BACKGROUND).apply {// from https://developer.android.com/guide/components/services#ExtendingService
            start()
            tickLooper = looper
            tickHandler = ClockTickHandler(looper)
        }

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
//  Thread to tell clients to update display                                        //
//----------------------------------------------------------------------------------//
    private var tickLooper: Looper? = null
    private var tickHandler: ClockTickHandler? = null
    private inner class ClockTickHandler(looper: Looper) : Handler(looper)//from https://developer.android.com/guide/components/services#ExtendingService
    {
        override fun handleMessage(msg: Message)
        {
            try
            {
                var lastTick = 0
                while (stopwatchState == StopwatchState.IS_RUNNING)
                {
                    val elapsedSecs = elapsedTime()/1_000
                    if (elapsedSecs >= lastTick) {
                        processTick(elapsedSecs)
                        lastTick = elapsedSecs.toInt()
                    }
                    broadcastElapsedTimeStr()
                    Thread.sleep(50)
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Error during time_display update: ", e)
                Thread.currentThread().interrupt()                // Restore interrupt status.
            }
        }
    }

//----------------------------------------------------------------------------------//
//  Connection/binding stuff                                                                 //
//----------------------------------------------------------------------------------//

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



//----------------------------------------------------------------------------------//
//  StopWatch stuff                                                                 //
//----------------------------------------------------------------------------------//

    enum class StopwatchState(val tagText: String) {
        IS_PAUSED("Pause"),
        IS_RUNNING("Play")
    }

    var isFirstRun = true

    // Start and end times in milliseconds
    private var startTime: Long = 0  // Start and end times in milliseconds
    private var pauseTime: Long = 0

    // Is the service tracking time?
    var stopwatchState = StopwatchState.IS_PAUSED


    fun startStopTimer(targetState: StopwatchState) {
        when (targetState) {
            StopwatchState.IS_RUNNING ->    startTime = System.currentTimeMillis() - pauseTime
            StopwatchState.IS_PAUSED ->     pauseTime = elapsedTime()
        }
        stopwatchState = targetState

        tickHandler?.obtainMessage()?.also { msg ->
            tickHandler?.sendMessage(msg)
        }
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

    fun broadcastElapsedTimeStr()
    {
        val intent = Intent(INTENT_NAME_UPDATE_TIMESTR)
        intent.putExtra(INTENT_NAME_UPDATE_TIMESTR, elapsedTimeStr())
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


//----------------------------------------------------------------------------------//
//  Exercise orchestration                                                          //
//----------------------------------------------------------------------------------//

    var restStarted: Boolean = false
    var currentExIdx = 0
    private var completedExTime: Long = 0

    var repCountList: MutableList<Int> = DataSource.getDataSet().stream().map { e -> e.noReps }.collect( Collectors.toList())


    private fun processTick(elapsedSecs: Long)
    {
        Log.d(TAG, "onChronometerTick> elapsedSecs: $elapsedSecs, restStarted: $restStarted")

        val exTime  = DataSource.getDataSet()[currentExIdx].exTime_s
        val restT   = DataSource.getDataSet()[currentExIdx].restTime_s
        val currExT = (elapsedSecs-completedExTime) //ie. total_time - total_time_of_completed_exercises

        if ( currExT >= exTime )    //if time elapsed is more than length of current exercise
        {
            if ( currExT < (exTime + restT) ) {  //inside rest time
                if (!restStarted) startRest()
            } else {
                startNewExercise(elapsedSecs)
            }
        }

        val intent = Intent(INTENT_NAME_PROC_TICK)
        if (restStarted) {
            intent.putExtra(INTENTXTR_EXIDX_TO_DISPL, getNextExIdx()) //if resting, override current & display next ex.
        } else {
            intent.putExtra(INTENTXTR_EXIDX_TO_DISPL, currentExIdx)
        }
        intent.putExtra(INTENTXTR_CUREX_T_DISPL, "$currExT/[$exTime+$restT]")

        // https://stackoverflow.com/questions/30629071/sending-a-simple-message-from-service-to-activity
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun getNextExIdx(): Int {
        if (currentExIdx < (DataSource.list.size-1))
            return currentExIdx + 1
        else
            return 0
    }


    private fun startRest()
    {
        Log.d(TAG, "startRest> ")
        restStarted = true

        val toneGen1 = ToneGenerator(TONE_STREAM, 100)
        toneGen1.startTone(ToneGenerator.TONE_SUP_PIP, 150)
        Handler().postDelayed({   //2nd beep
            val toneGen2 = ToneGenerator(TONE_STREAM, 100)
            toneGen2.startTone(ToneGenerator.TONE_CDMA_PIP, 175)
        }, 150)

    }


    private fun startNewExercise(elapsedSecs: Long)
    {
        Log.d(TAG, "startNewExercise> currentExIdx: $currentExIdx")
        restStarted = false

        val toneGen1 = ToneGenerator(TONE_STREAM, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)

        completedExTime = elapsedSecs  //set completed exercise time
        currentExIdx = getNextExIdx()

        repCountList[currentExIdx] -=1
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