package com.barneswebb.android.fencingcircuit.models

data class ExerciseSetting(

    var exerciseType:   ExerciseType,
    var exTime_s:       Int,
    var restTime_s:     Int,
    var noReps:         Int

){}