/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    NOTE TO SELF:
This class is for display only.
I should have as little logic as possible

 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
package com.barneswebb.android.fencingcircuit

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.barneswebb.android.fencingcircuit.StopwatchService.Companion.INTENT_NAME_UPDATE_TIMESTR
import com.barneswebb.android.fencingcircuit.StopwatchService.Companion.INTENT_NAME_UPDATE_COUNTDOWNSTR
import kotlinx.android.synthetic.main.activity_stopwatch.*
import com.barneswebb.android.fencingcircuit.StopwatchService.StopwatchState.IS_PAUSED as STOPWATCH_IS_PAUSED
import com.barneswebb.android.fencingcircuit.StopwatchService.StopwatchState.IS_RUNNING as STOPWATCH_IS_RUNNING


class StopwatchActivity : AppCompatActivity() {

    companion object {
        private val TAG = StopwatchActivity::class.java.simpleName
    }


//----------------------------------------------------------------------------------//
//  Activity lifecycle section                                                      //
//----------------------------------------------------------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "Creating service ========================================")
        setContentView(R.layout.activity_stopwatch)
    }


    override fun onStart() {
        super.onStart()
        if (!isBound) {
            // Bind to StopwatchService
            Intent(this, StopwatchService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(uiUpdateMessageReceiver,       IntentFilter(StopwatchService.INTENT_NAME_PROC_TICK))
        LocalBroadcastManager.getInstance(this).registerReceiver(timerDisplayMessageReceiver,   IntentFilter(
            INTENT_NAME_UPDATE_TIMESTR
        ))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiUpdateMessageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerDisplayMessageReceiver)
        super.onPause()
    }

//----------------------------------------------------------------------------------//
//  Button & Display management                                                               //
//----------------------------------------------------------------------------------//

    fun btnPlaypause(view: View)
    {
        when (exercise_pause.tag) {
            STOPWATCH_IS_RUNNING.tagText  -> stopwatchService.startStopTimer(STOPWATCH_IS_RUNNING)
            STOPWATCH_IS_PAUSED.tagText   -> stopwatchService.startStopTimer(STOPWATCH_IS_PAUSED)
        }
        setExercisePauseBtn()
    }

    private fun setExercisePauseBtn() {
        when (stopwatchService.stopwatchState)
        {
            STOPWATCH_IS_RUNNING    -> {
                exercise_pause.tag = STOPWATCH_IS_PAUSED.tagText
                exercise_pause.setImageResource(R.drawable.blueicons_pause)
            }
            STOPWATCH_IS_PAUSED    -> {
                exercise_pause.tag = STOPWATCH_IS_RUNNING.tagText
                exercise_pause.setImageResource(R.drawable.blueicons_play)
            }
        }
    }

    private fun setExText(exIdx: Int, exDisplayString: String)
    {
        this_exercise_time.text = exDisplayString

        exercise_name.text = "${exIdx+1}/${DataSource.getDataSet().size}) ${DataSource.getDataSet()[exIdx].exerciseType.title}"
        exercise_desc.text = DataSource.getDataSet()[exIdx].exerciseType.desc
        remaining_reps.text = "Reps remaining: ${stopwatchService.repCountList[exIdx].toString()}"

        if (stopwatchService.restStarted) {
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

            setUIonConnect()
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private fun setUIonConnect() {
        if (stopwatchService.isFirstRun) {
            stopwatchService.startStopTimer(STOPWATCH_IS_RUNNING) // start running automatically on start
        } else
        {
            //stopwatch.text = stopwatchService.elapsedTimeStr() // TODO figure out how to get correct exercise string
            setExText(
                stopwatchService.currentExIdx,
                "[-/-]" //TODO move this out of stopwatchService.processTick()
            )
            setExercisePauseBtn()
        }
    }


    // https://stackoverflow.com/questions/30629071/sending-a-simple-message-from-service-to-activity
    private val uiUpdateMessageReceiver: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent) {
            setExText(
                intent.getIntExtra(StopwatchService.INTENTXTR_EXIDX_TO_DISPL, -1),
                intent.getStringExtra(StopwatchService.INTENTXTR_CUREX_T_DISPL)
            )
        }
    }
    private val timerDisplayMessageReceiver: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent) {
            stopwatch.text          = intent.getStringExtra(INTENT_NAME_UPDATE_TIMESTR)
            countdown_timer.text    = intent.getStringExtra(INTENT_NAME_UPDATE_COUNTDOWNSTR)
        }
    }

//********************* FYI only: *********************//{{{

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "Destroying activity ========================================")
    }
//}}

}
