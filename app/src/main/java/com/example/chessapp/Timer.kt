package com.example.chessapp

import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import kotlin.math.floor

class Timer(private var timer: TextView) {
    private var endMillis = 0L // Only read, not Atomic

    fun millisLeft(): Long {return endMillis - System.currentTimeMillis()}

    suspend fun timerCo(durationMillis: Long) {
        endMillis = System.currentTimeMillis() + durationMillis
        var currentMillis = System.currentTimeMillis()
        // End color of replayButton is  red
        val delayMillis = 100L // Time step for updates
        // XML button
        //timer.setBackgroundColor(Color.WHITE)

        while (coroutineContext.isActive
            && (endMillis > currentMillis)) {
            // XML TextView
//            timer.text = String.format(
//                "%1.1f",
//                (endMillis - currentMillis) / 1000.0f
//            )
            val total_seconds = (endMillis - currentMillis) / 1000.0
            val minutes = floor(total_seconds / 60.0).toInt()
            val seconds = (total_seconds % 60).toInt()

            val m = "%02d".format(minutes)
            val s = "%02d".format(seconds)

            val str = m + ":" + s
            timer.text = str

            delay(delayMillis)
            currentMillis = System.currentTimeMillis()
        }
        timer.text = "0:00"
    }
}