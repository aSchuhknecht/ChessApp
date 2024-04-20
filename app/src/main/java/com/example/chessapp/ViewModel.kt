package com.example.chessapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response


class ViewModel (application: Application) : AndroidViewModel(application) {

    private val chessApi = ChessAPI.create()
    private val chessRepo = Repository(chessApi)
    private val sfMove = MutableLiveData<String>()

    private var fen= "rn1q1rk1/pp2b1pp/2p2n2/3p1pB1/3P4/1QP2N2/PP1N1PPP/R4RK1 b - - 1 11"

    var turn  = "white"
    var gameOver = false
    var prevColor = 0
    var prevColorCPU = 0
    var prevSquareCPU = ""
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
    var wkSquareTest = "04"
    var bkSquareTest =  "74"
    var timerStarted = false
    var difficulty = "Med"
    var depth = 5
    var mode = "player"
    var returningFromSettings = false

    var whiteduration = "5:00"
    var blackduration = "5:00"

    var board = arrayOf(arrayOf("wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"),
        arrayOf("wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"),
        arrayOf("br", "bn", "bb", "bq", "bk", "bb", "bn", "br"))

    var testBoard = arrayOf(arrayOf("wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"),
        arrayOf("wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
        arrayOf("bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"),
        arrayOf("br", "bn", "bb", "bq", "bk", "bb", "bn", "br"))


    fun netRefresh() {
        // XXX Write me.  This is where the network request is initiated.
        viewModelScope.launch(
            context =  viewModelScope.coroutineContext + Dispatchers.IO )
        {
            try {
                Log.d("cor", "fetching")
                val res = chessRepo.getStock(fen, depth).string()
                val s = res.split(":",  limit= 6)
                val bm =  s[4]
                val moves = bm.split(" ", limit =3)
                val move = moves[1]
                sfMove.postValue(move)
                Log.d("stockfish", move)

            } catch(e : Exception){
                Log.d("cor", "too soon")
            }
        }
    }

    fun genFEN() {

        val br = board.reversed()
        var fenStr = ""
        br.forEach { it1 ->
            var str = ""
            var counter = 0
            it1.forEach {it2 ->
                val s = pieceToFENPiece(it2)
                if (s.equals("e")) {
                    counter += 1
                }
                else {
                    if (counter > 0) {
                        str += counter.toString()
                        counter = 0
                    }
                    str += s
                }
            }
            if (counter > 0) {
                str += counter.toString()
            }
            str += "/"
            fenStr += str
        }
        fenStr = fenStr.dropLast(1) + " b "

        if (whiteCanCastleShort) {
            fenStr += "K"
        }
        if (whiteCanCastleLong) {
            fenStr += "Q"
        }
        if (blackCanCastleShort) {
            fenStr += "k"
        }
        if (blackCanCastleLong) {
            fenStr += "q"
        }
        if (!blackCanCastleShort && !blackCanCastleLong &&
            !whiteCanCastleShort && !whiteCastledLong) {
            fenStr += "-"
        }
        fenStr += " - 0 1"
        Log.d("fenstr", fenStr)
        Log.d("fen", fen)
        fen = fenStr
    }

    fun pieceToFENPiece(piece: String): String  {

        var c = ""
        when (piece) {
            "wp" -> c  = "P"
            "wr" -> c  = "R"
            "wb" -> c  = "B"
            "wq" -> c  = "Q"
            "wk" -> c  = "K"
            "wn" -> c  = "N"
            "bp" -> c  = "p"
            "br" -> c  = "r"
            "bb" -> c  = "b"
            "bq" -> c  = "q"
            "bk" -> c  = "k"
            "bn" -> c  = "n"
            "e"  -> c  = "e"
        }
        return c
    }

    fun observeSFMove(): LiveData<String> {
        return sfMove
    }

    fun syncBoard() {
        for (i in 0..7) {
            for (j in 0..7) {
                testBoard[i][j] = board[i][j]
            }
        }
    }

    fun resetGame() {

        board  = arrayOf(arrayOf("wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"),
            arrayOf("wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"),
            arrayOf("br", "bn", "bb", "bq", "bk", "bb", "bn", "br"))

        testBoard  = arrayOf(arrayOf("wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"),
            arrayOf("wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("e", "e", "e", "e", "e", "e", "e", "e"),
            arrayOf("bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"),
            arrayOf("br", "bn", "bb", "bq", "bk", "bb", "bn", "br"))

        turn  = "white"
        gameOver = false
        prevColor = 0
        pieceSelected = false
        validMove = false
        selectedSqaure = "none"
        targetSquare = "none"
        legalMoves = listOf("")
        whiteCanCastleShort = true
        whiteCanCastleLong = true
        blackCanCastleShort = true
        blackCanCastleLong = true
        whiteCastledShort = false
        whiteCastledLong = false
        blackCastledShort = false
        blackCastledLong = false
        wkSquare = "04"
        bkSquare =  "74"
        wkSquareTest = "04"
        bkSquareTest =  "74"
        timerStarted = false
        whiteduration = "5:00"
        blackduration = "5:00"
        prevSquareCPU =  ""
        prevColorCPU  = 0
        depth = 5
        returningFromSettings = false
    }

    fun printTestBoard() {

        val bc = testBoard.reversed()
        bc.forEach { it1 ->

            var str = ""
            it1.forEach {it2 ->
                str += it2 + "\t"
            }
            Log.d("board",  str)
        }
    }

    fun printBoard() {

        val bc = board.reversed()
        bc.forEach { it1 ->

            var str = ""
            it1.forEach {it2 ->
                str += it2 + "\t"
            }
            Log.d("board",  str)
        }
    }


}