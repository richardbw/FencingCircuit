package com.barneswebb.android.fencingcircuit

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barneswebb.android.fencingcircuit.models.ExerciseSetting
import kotlinx.android.synthetic.main.layout_excercisesetting_list_item.view.*


class ExerciseSettingListRecyclingAdapter : Adapter<ViewHolder>() {
    companion object {
        private val TAG: String = ExerciseSettingListRecyclingAdapter::class.java.simpleName
    }

    private var items: List<ExerciseSetting> = ArrayList()

    fun submitList(exerciseSettingList: List<ExerciseSetting>) {
        items = exerciseSettingList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ExerciseSettingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_excercisesetting_list_item, parent, false)
        )
    }


    fun updateData(viewModels: ArrayList<ExerciseSetting>) {
        notifyDataSetChanged()
    }

    override fun onViewRecycled(holder: ViewHolder) {
        //items[holder.adapterPosition] = holder.  .mEditText.getText().toString()
        Log.d(TAG, "${holder}")
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ExerciseSettingViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    class ExerciseSettingViewHolder constructor(
        itemView: View
    ):ViewHolder(itemView) {

        private val  exerciseTitle     : TextView  = itemView.excercise_title
        private val  exerciseDesc      : TextView  = itemView.exercise_desc
        private val  exerciseTime      : EditText  = itemView.exTime_s
        private val  exerciseRest      : EditText  = itemView.restTime_s
        private val  exerciseReps      : EditText  = itemView.noReps

        fun bind(exerciseSetting: ExerciseSetting) {
            exerciseTitle.text   = exerciseSetting.exerciseType.title
            exerciseDesc.text    = exerciseSetting.exerciseType.desc
            exerciseTime  .setText(exerciseSetting.exTime_s.toString())
            exerciseRest  .setText(exerciseSetting.restTime_s.toString())
            exerciseReps  .setText(exerciseSetting.noReps.toString())
        }
    }

}