package com.example.chessapp

import android.app.Application
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel

class ViewModel (application: Application) : AndroidViewModel(application) {

    var turn  = "white"
    var whiteXm = false
    var blackXm = false
    var prevColor = 0
    var pieceSelected = false
    var validMove = false
    var selectedSqaure = "none"
    var targetSquare = "none"
    var legalMoves = listOf("")
    var whiteCanCastleShort = true
    var whiteCanCastleLong = true
    var blackCanCastleShort = true
    var blackCanCastleLong = true
    var whiteCastledShort = false
    var whiteCastledLong = false
    var blackCastledShort = false
    var blackCastledLong = false
    var wkSquare = "04"
    var bkSquare =  "74"

    var board = arrayOf(arrayOf("wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"),
        arrayOf("wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"),
        arrayOf("br", "bn", "bb", "bq", "bk", "bb", "bn", "br"))



}