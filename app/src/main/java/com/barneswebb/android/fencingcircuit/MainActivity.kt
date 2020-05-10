/**
 * FencingCircuit
 * Copyright (C) 2020 Richard Barnes-Webb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.barneswebb.android.fencingcircuit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()

        excerciseSettingAdapter.submitList(DataSource.getDataSet())

    }

    lateinit var excerciseSettingAdapter    : ExcerciseSettingListRecylingAdapter

    private fun initRecyclerView()
    {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            excerciseSettingAdapter = ExcerciseSettingListRecylingAdapter()
            adapter = excerciseSettingAdapter
            addItemDecoration(TopSpacingItemDecoration(30))
        }
    }

    fun restart(view: View) {}

    fun play(view: View) {
        Log.d(this.javaClass.canonicalName, "Starting play: $view")
        startActivity(Intent(this, StopwatchActivity::class.java))
    }


}
