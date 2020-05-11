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
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.barneswebb.android.fencingcircuit.StopwatchService.StopwatchState.IS_PAUSED
import kotlinx.android.synthetic.main.activity_stopwatch.*
import com.barneswebb.android.fencingcircuit.StopwatchService.StopwatchState.IS_RUNNING as STOPWATCH_IS_RUNNING


class StopwatchActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = StopwatchActivity::class.java.simpleName
    }


//----------------------------------------------------------------------------------//
//  Activity lifecycle section                                                      //
//----------------------------------------------------------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        if (isBound) {   //TODO This never gets run
            stopwatchStart(exercise_pause)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(uiUpdateMessageReceiver,       IntentFilter(StopwatchService.INTENT_NAME_PROC_TICK))
        LocalBroadcastManager.getInstance(this).registerReceiver(timerDisplayMessageReceiver,   IntentFilter(StopwatchService.INTENT_NAME_UPDATE_TIMESTR));
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiUpdateMessageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerDisplayMessageReceiver)
        super.onPause()
    }

//----------------------------------------------------------------------------------//
//  Button management                                                               //
//----------------------------------------------------------------------------------//

    fun btn_playpause(view: View)
    {
        val playpauseBtn: ImageButton = view as ImageButton

        when (playpauseBtn.tag) {
            STOPWATCH_IS_RUNNING.tagText  -> stopwatchStart(playpauseBtn)
            IS_PAUSED.tagText   -> stopwatchPause(playpauseBtn)
        }
    }

    private fun stopwatchPause(playpauseBtn: ImageButton)
    {
        stopwatchService.startStopTimer(IS_PAUSED)
        playpauseBtn.tag = STOPWATCH_IS_RUNNING.tagText
        playpauseBtn.setImageResource(R.drawable.blueicons_play)
    }


    fun stopwatchStart(playpauseBtn: ImageButton)
    {
        stopwatchService.startStopTimer(STOPWATCH_IS_RUNNING)
        playpauseBtn.tag = IS_PAUSED.tagText
        playpauseBtn.setImageResource(R.drawable.blueicons_pause)

        if (stopwatchService.isFirstRun) {   //This is pretty ugly TODO refactor
            stopwatchService.repCountList[0] -= 1
            setExText(
                stopwatchService.currentExIdx,
                "[-]-"
            )//FIRST Ex.
            stopwatchService.isFirstRun = false
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

            stopwatchStart(exercise_pause)
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
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
            stopwatch.text = intent.getStringExtra(StopwatchService.INTENT_NAME_UPDATE_TIMESTR)
        }
    }


}
