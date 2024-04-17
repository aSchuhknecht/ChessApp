package com.example.chessapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.chessapp.databinding.ActivityMainBinding
import com.example.chessapp.databinding.ContentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity(),
    CoroutineScope by MainScope() {

    private lateinit var contentMainBinding: ContentMainBinding
    private lateinit var timerWhite: Timer
    private lateinit var timerBlack: Timer
    private lateinit var game: Game
    private var timerActive = false

    override fun onDestroy() {
        super.onDestroy()
        cancel() // destroy all coroutines
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        contentMainBinding = activityMainBinding.contentMain
        setContentView(activityMainBinding.root)
    }
}