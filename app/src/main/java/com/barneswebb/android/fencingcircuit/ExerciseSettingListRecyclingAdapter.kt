package com.barneswebb.android.fencingcircuit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.barneswebb.android.fencingcircuit.models.ExerciseSetting
import kotlinx.android.synthetic.main.layout_excercisesetting_list_item.view.*

class ExerciseSettingListRecyclingAdapter : Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ExerciseSetting> = ArrayList()

    fun submitList(exerciseSettingList: List<ExerciseSetting>) {
        items = exerciseSettingList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ExerciseSettingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_excercisesetting_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ExerciseSettingViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    class ExerciseSettingViewHolder constructor(
        itemView: View
    ):RecyclerView.ViewHolder(itemView) {

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