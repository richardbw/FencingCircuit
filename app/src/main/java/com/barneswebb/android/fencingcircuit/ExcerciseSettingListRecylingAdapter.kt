package com.barneswebb.android.fencingcircuit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.barneswebb.android.fencingcircuit.models.ExcerciseSetting
import kotlinx.android.synthetic.main.layout_excercisesetting_list_item.view.*

class ExcerciseSettingListRecylingAdapter : Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ExcerciseSetting> = ArrayList()

    fun submitList(exerciseSettingList: List<ExcerciseSetting>) {
        items = exerciseSettingList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ExcerciseSettingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_excercisesetting_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ExcerciseSettingViewHolder -> {
                holder.bind(items.get(position))
            }
        }
    }

    class ExcerciseSettingViewHolder constructor(
        itemView: View
    ):RecyclerView.ViewHolder(itemView) {

        private val  excercise_title     : TextView  = itemView.excercise_title
        private val  excercise_desc      : TextView  = itemView.exercise_desc
        private val  excercise_time      : EditText  = itemView.exTime_s
        private val  excercise_rest      : EditText  = itemView.restTime_s
        private val  excercise_reps      : EditText  = itemView.noReps

        fun bind(exerciseSetting: ExcerciseSetting) {
            excercise_title.text   = exerciseSetting.exerciseType.title
            excercise_desc.text    = exerciseSetting.exerciseType.desc
            excercise_time  .setText(exerciseSetting.exTime_s.toString())
            excercise_rest  .setText(exerciseSetting.restTime_s.toString())
            excercise_reps  .setText(exerciseSetting.noReps.toString())
        }
    }

}