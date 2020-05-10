package com.barneswebb.android.fencingcircuit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import android.view.View
import android.widget.ImageButton
import com.barneswebb.android.fencingcircuit.StopwatchService.StopwatchState.IS_PAUSED
import com.barneswebb.android.fencingcircuit.StopwatchService.StopwatchState.IS_RUNNING
import kotlinx.android.synthetic.main.activity_stopwatch.*
import java.util.stream.Collectors

class StopwatchActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = StopwatchActivity::class.java.simpleName
        private val TONE_STREAM = AudioManager.STREAM_ALARM
    }


//----------------------------------------------------------------------------------//
//  Activity lifecycle section                                                      //
//----------------------------------------------------------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        HandlerThread("My HandlerThread (y)", THREAD_PRIORITY_BACKGROUND).apply {// from https://developer.android.com/guide/components/services#ExtendingService
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }


    override fun onStart() {
        super.onStart()
        if (!isBound) {
            // Bind to StopwatchService
            Intent(this, StopwatchService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
        setExText(currentExIdx)//FIRST Ex.
    }

    override fun onResume() {
        super.onResume()
        if (isBound) {   //TODO This never gets run
            stopwatchStart(exercise_pause)
        }
    }


//----------------------------------------------------------------------------------//
//  Button management                                                               //
//----------------------------------------------------------------------------------//

    fun btn_playpause(view: View)
    {
        val playpauseBtn: ImageButton = view as ImageButton

        when (playpauseBtn.tag) {
            IS_RUNNING.tagText  -> stopwatchStart(playpauseBtn)
            IS_PAUSED.tagText   -> stopwatchPause(playpauseBtn)
        }
    }

    private fun stopwatchPause(playpauseBtn: ImageButton)
    {
        stopwatchService.startStopTimer(IS_PAUSED)
        playpauseBtn.tag = IS_RUNNING.tagText
        playpauseBtn.setImageResource(R.drawable.blueicons_play)
    }


    var isFirstRun = true
    fun stopwatchStart(playpauseBtn: ImageButton)
    {
        stopwatchService.startStopTimer(IS_RUNNING)
        playpauseBtn.tag = IS_PAUSED.tagText
        playpauseBtn.setImageResource(R.drawable.blueicons_pause)

        serviceHandler?.obtainMessage()?.also { msg ->
            serviceHandler?.sendMessage(msg)
        }

        if (isFirstRun) {   //This is pretty ugly TODO refactor
            repCountList[0] -= 1
            setExText(currentExIdx)//FIRST Ex.
            isFirstRun = false
        }
    }


//----------------------------------------------------------------------------------//
//  Exercise orchestration                                                          //
//----------------------------------------------------------------------------------//

    private var restStarted: Boolean = false
    private var currentExIdx = 0
    private var completedExTime: Long = 0

    private var repCountList = DataSource.getDataSet().stream().map { e -> e.noReps }.collect( Collectors.toList())


    private fun setExText(exIdx: Int) {
        exercise_name.text = "${exIdx+1}/${DataSource.getDataSet().size}) ${DataSource.getDataSet()[exIdx].exerciseType.title}"
        exercise_desc.text = DataSource.getDataSet()[exIdx].exerciseType.desc
        remaining_reps.text = "Reps remaining: ${repCountList[exIdx].toString()}"

        if (restStarted) {
            exercise_rest.visibility = View.VISIBLE
            next_exercise.visibility = View.VISIBLE
            exercise_rest.setBackgroundColor(Color.YELLOW)
        } else {
            exercise_rest.visibility = View.INVISIBLE
            next_exercise.visibility = View.INVISIBLE
            exercise_rest.setBackgroundColor(Color.LTGRAY)
            exercise_pause.bringToFront()
        }
    }

    private fun processTick(elapsedSecs: Long)
    {
        Log.d(Companion.TAG, "onChronometerTick> elapsedSecs: $elapsedSecs, restStarted: $restStarted")

        val exTime  = DataSource.getDataSet()[currentExIdx].exTime_s
        val restT   = DataSource.getDataSet()[currentExIdx].restTime_s
        val currExT = (elapsedSecs-completedExTime) //ie. total_time - total_time_of_completed_exercises

        this_exercise_time.text = "$currExT/[$exTime+$restT] "

        if ( currExT >= exTime )    //if time elapsed is more than length of current exercise
        {
            if ( currExT < (exTime + restT) ) {  //inside rest time
                if (!restStarted) startRest()
            } else
                startNewExercise(elapsedSecs)
        }
    }

    private fun getNextExIdx(): Int {
        if (currentExIdx < (DataSource.list.size-1))
            return currentExIdx + 1
        else
            return 0
    }


    private fun startRest()
    {
        Log.d(Companion.TAG, "startRest> ")
        restStarted = true

        val toneGen1 = ToneGenerator(Companion.TONE_STREAM, 100)
        toneGen1.startTone(ToneGenerator.TONE_SUP_PIP, 150)
        Handler().postDelayed({   //2nd beep
            val toneGen1 = ToneGenerator(Companion.TONE_STREAM, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 175)
        }, 150)

        setExText(getNextExIdx())
    }


    private fun startNewExercise(elapsedSecs: Long)
    {
        Log.d(Companion.TAG, "startNewExercise> currentExIdx: $currentExIdx")
        restStarted = false

        val toneGen1 = ToneGenerator(Companion.TONE_STREAM, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)

        completedExTime = elapsedSecs  //set completed exercise time
        currentExIdx = getNextExIdx()

        repCountList[currentExIdx] -=1

        setExText(currentExIdx)
    }

//----------------------------------------------------------------------------------//
//  Bind/Connection stuff to service                                                //
//----------------------------------------------------------------------------------//

    private lateinit var stopwatchService: StopwatchService
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection
    {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as StopwatchService.LocalBinder
            stopwatchService = binder.getService()
            isBound = true
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

//----------------------------------------------------------------------------------//
//  Thread to update display                                                        //
//----------------------------------------------------------------------------------//
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private inner class ServiceHandler(looper: Looper) : Handler(looper)//from https://developer.android.com/guide/components/services#ExtendingService
    {
        override fun handleMessage(msg: Message)
        {
            try
            {
                var lastTick = 0
                while (stopwatchService.stopwatchState == IS_RUNNING)
                {
                    this@StopwatchActivity.runOnUiThread( Runnable {
                        var elapsedSecs = stopwatchService.elapsedTime()/1_000
                        if (elapsedSecs >= lastTick) {
                            processTick(elapsedSecs)
                            lastTick = elapsedSecs.toInt()
                        }
                        stopwatch.text = stopwatchService.elapsedTimeStr()
                    })

                    Thread.sleep(50)
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Error during time_display update: ", e)
                Thread.currentThread().interrupt()                // Restore interrupt status.
            }
        }
    }


}
