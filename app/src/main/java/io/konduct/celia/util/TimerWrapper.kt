/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.konduct.celia.util

import java.util.*

/**
 * The timer used by the app is customizable. This way tests can run synchronously and very fast.
 *
 * See [DefaultTimer] for the default implementation and IntervalTimerViewModelTest.kt for a
 * test implementation.
 */
interface Timer {

    fun reset()
    fun start(task: TimerTask)
    fun getElapsedTime(): Long
}

/**
 * The default timer is used in the normal execution of the app.
 */
object DefaultTimer : Timer {

    private const val TIMER_PERIOD_MS = 100L

    private var startTime = System.currentTimeMillis()


    override fun getElapsedTime() : Long {
        val now = System.currentTimeMillis()
        val elapsedTime = now - startTime
        startTime = now
        return elapsedTime
    }

    private var timer = java.util.Timer()

    override fun reset() {
        timer.cancel()
    }

    override fun start(task: TimerTask) {
        timer = java.util.Timer()
        startTime = System.currentTimeMillis()
        timer.scheduleAtFixedRate(task, 0, TIMER_PERIOD_MS)
    }
}
