package com.barneswebb.android.fencingcircuit

import com.barneswebb.android.fencingcircuit.models.ExerciseSetting
import com.barneswebb.android.fencingcircuit.models.ExerciseType
import com.google.gson.Gson


class DataSource{

    companion object{

        val gson = Gson()

        private const val REPS_1: Int = 1
        private const val REPS_2: Int = 2
        private const val SECONDS_30: Int = 30
        private const val SECONDS_10: Int = 10
        private const val SECONDS_05: Int = 5
        private const val MINUTES_1: Int = 60

        var list = ArrayList<ExerciseSetting>()

        fun getDataSet(): ArrayList<ExerciseSetting>  {
            if (this.list.isEmpty()) { this.list = createDataSet() }
           return this.list
        }

        private fun createDataSet(): ArrayList<ExerciseSetting> {
            val list = ArrayList<ExerciseSetting>()

            list.add(
                ExerciseSetting(
                    ExerciseType(
                        "Steps forward and back",
                        "Steps forward and back with some variety in the number, size and type of step and the frequency of direction changes"
                    ),
                    SECONDS_05,
                    SECONDS_05,
                    REPS_2
                )
            )
            list.add(
                ExerciseSetting(
                    ExerciseType(
                        "Steps between 2 points",
                        "Steps forward and back between 2 points, performed at speed and correctly."
                    ),
                    SECONDS_05,
                    SECONDS_05,
                    REPS_2
                )
            )
            /*list.add(
                ExcerciseSetting(
                    ExcerciseType(
                        "Lunge with partial recovery",
                        "From the lunge position, recover without withdrawing the front foot and return to the lunge position"
                    ),
                    MINUTES_1,
                    SECONDS_30,
                    REPS_2
                )
            )
            list.add(
                ExcerciseSetting(
                    ExcerciseType(
                        "Steps with lunges",
                        "Steps forward and back combined with lunges"
                    ),
                    MINUTES_1,
                    SECONDS_30,
                    REPS_2
                )
            )
            list.add(
                ExcerciseSetting(
                    ExcerciseType(
                        "Lunge with straightening front leg",
                        "From the lunge position, completely straighten the front leg without bending the rear leg, lifting oneself up and return to the lunge position"
                    ),
                    MINUTES_1,
                    SECONDS_30,
                    REPS_2
                )
            )
            list.add(
                ExcerciseSetting(
                    ExcerciseType(
                        "Step lunge, recover, step back",
                        "Step lunge, recover, step back "
                    ),
                    MINUTES_1,
                    SECONDS_30,
                    REPS_2
                )
            )
            list.add(
                ExcerciseSetting(
                    ExcerciseType(
                        "Steps and squat jumps",
                        "3 steps followed by 1 squat jump. from the on guard stance, jump down and back up"
                    ),
                    MINUTES_1,
                    SECONDS_30,
                    REPS_2
                )
            )
            list.add(
                ExcerciseSetting(
                    ExcerciseType(
                        "Squat jumps",
                        "From the on guard stance, jump down and back up"
                    ),
                    MINUTES_1,
                    SECONDS_30,
                    REPS_2
                )
            )*/
            return list
        }
    }
}