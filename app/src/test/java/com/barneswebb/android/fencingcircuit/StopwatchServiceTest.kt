package com.barneswebb.android.fencingcircuit


import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

internal class StopwatchServiceTest {


    var stopw = StopwatchService()

    @Before fun setUp() {
    }

    @After fun tearDown() {
        //stopwatchService.stopSelf()
    }

    @Test fun getNextExIdx() {
        assertEquals(4, 2 + 2)

        for (x in 0..30) {
            println("idx: ${stopw.currentExIdx}> ${stopw.repCountList[stopw.currentExIdx]}/${DataSource.list[stopw.currentExIdx].noReps}")
            stopw.repCountList[stopw.currentExIdx] -= 1
            stopw.currentExIdx = stopw.getNextExIdx()
            if ( stopw.currentExIdx < 0 ) break
        }

    }
}