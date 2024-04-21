package com.example.chessapp

import android.R.attr.data
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chessapp.databinding.GameFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor


class GameFragment: Fragment() {

    private var _binding: GameFragmentBinding? = null
    private val viewModel: ViewModel by activityViewModels()
    private val binding get() = _binding!!

    private lateinit var whiteTimer: Timer
    private lateinit var blackTimer: Timer
    private lateinit var job: Job
    private var timerActive = false

    private var whitePieces = listOf("wp", "wb", "wr", "wn", "wq", "wk")
    private var blackPieces = listOf("bp", "bb", "br", "bn", "bq", "bk")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GameFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timerActive = false
        whiteTimer = Timer(binding.whiteTimer)
        blackTimer = Timer(binding.blackTimer)

        val clayout = binding.allContent
        for (i in 0 until clayout.childCount) {
            val v: View = clayout.getChildAt(i)
            if (v is ImageView) {
                setClickListeners(v)
            }
        }

        redrawBoard()
        Log.d("test", viewModel.whiteTime.toString())
        Log.d("test", viewModel.blackTime.toString())
        binding.whiteTimer.text = millisToMins(viewModel.whiteTime)
        binding.blackTimer.text = millisToMins(viewModel.blackTime)

        binding.newGameBut.setOnClickListener {
            //reset yellow highlighting for CPU move
            if (!viewModel.prevSquareCPU.equals("")) {
                val target = getViewByIndex(viewModel.prevSquareCPU)
                target.setBackgroundColor(viewModel.prevColorCPU)
            }
            resetGame()
            binding.allContent.setBackgroundColor(Color.WHITE)
        }

        viewModel.observeSFMove().observe(viewLifecycleOwner) {it2 ->

            if (!viewModel.returningFromSettings) {
                val move = it2
                val prow = move[1].digitToInt() - 1
                val pcol = letterToCol(move[0].toString())
                val nrow = move[3].digitToInt() - 1
                val ncol = letterToCol(move[2].toString())
                val ss = prow.toString() + pcol.toString() //selected square
                val ns = nrow.toString() + ncol.toString() //target square

                val prev = getViewByIndex(ss)
                val target = getViewByIndex(ns)

                val res = getDrawableOnSquare(prev.tag.toString())

                val viewColor = target.background as ColorDrawable
                val colorId = viewColor.color
                viewModel.prevColorCPU = colorId
                viewModel.prevSquareCPU = ns
                target.setBackgroundColor(Color.parseColor("#ADD8E6"))  // light blue

                val prevPiece = viewModel.board[prow][pcol]
                if (prevPiece.equals("bp") && nrow == 0) {
                    viewModel.board[nrow][ncol] = "bq"
                    target.setImageResource(R.drawable.bqueen2)
                } else {
                    viewModel.board[nrow][ncol] = prevPiece
                    target.setImageResource(res)
                }
                if (prevPiece.equals("bk")) {
                    viewModel.bkSquare = target.tag.toString()
                }

                handleCastle(prow, pcol, nrow, ncol, false)  //moves the rook and handles rights
                viewModel.board[prow][pcol] = "e"

                prev.setImageResource(0)
                viewModel.pieceSelected = false
                swapTurns()
                viewModel.syncBoard()
                calculateMaterial()
                viewModel.wkSquareTest = viewModel.wkSquare
                viewModel.bkSquareTest = viewModel.bkSquare

                if (checkXM()) {
                    viewModel.gameOver = true
                    var winner = ""
                    if (viewModel.turn.equals("black")) {
                        winner = "White"
                    } else {
                        winner = "Black"
                    }
                    Toast.makeText(
                        activity, winner + " wins by checkmate!",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.allContent.setBackgroundColor(Color.LTGRAY)
                }
                viewModel.blackTime = blackTimer.millisLeft()
                stopTimer()
                startTimer(whiteTimer)
            }

        }

        binding.PauseBut.setOnClickListener {
            if (timerActive && !viewModel.gameOver) {
                timerActive = false
                binding.PauseBut.text = ">"
                binding.PauseTv.text = "Start"
                if (viewModel.turn.equals("white")) {
                    viewModel.whiteTime = whiteTimer.millisLeft()
                }
                else {
                    viewModel.blackTime = blackTimer.millisLeft()
                }
                stopTimer()

            } else {
                if (!viewModel.gameOver) {
                    timerActive = true
                    binding.PauseBut.text = "||"
                    binding.PauseTv.text = "Pause"
                    if (viewModel.turn.equals("white")) {
                        startTimer(whiteTimer)
                    } else {
                        startTimer(blackTimer)
                    }
                }
            }
        }

        binding.OptionsBut.setOnClickListener {
            if (!viewModel.gameOver) {
                if (timerActive) {
                    if (viewModel.turn.equals("white")) {
                        viewModel.whiteTime = whiteTimer.millisLeft()
                    } else {
                        viewModel.blackTime = blackTimer.millisLeft()
                    }
                    stopTimer()
                }
                findNavController().navigate(R.id.settingsFragment)
            }
        }
    }

    fun startTimer(timer: Timer) {
        job = Job()
        val uiScope = CoroutineScope(Dispatchers.Main + job)
        uiScope.launch {
            val timerJob = async {
                timerActive = true
                if (viewModel.turn.equals("white")) {
                    timer.timerCo(viewModel.whiteTime)
                }
                else {
                    timer.timerCo(viewModel.blackTime)
                }
                timerActive = false
                viewModel.gameOver = true
                var winner = ""
                if (viewModel.turn.equals("black")) {
                    winner = "White"
                } else {
                    winner = "Black"
                }
                Toast.makeText(
                    activity, winner + " wins on time!",
                    Toast.LENGTH_LONG
                ).show()
                binding.allContent.setBackgroundColor(Color.LTGRAY)
            }
        }
    }

    fun stopTimer() {
        job.cancel()
    }

    fun setClickListeners(v: ImageView) {

        v.setOnClickListener{
            if (!viewModel.pieceSelected  && !viewModel.gameOver &&  timerActive) {
                if (isValidPiece(it)) {

                    val viewColor = it.background as ColorDrawable
                    val colorId = viewColor.color
                    viewModel.prevColor = colorId
                    it.setBackgroundColor(Color.parseColor("#ff0000"))  // red
                    viewModel.pieceSelected = true
                    viewModel.selectedSqaure = it.tag.toString()
                    calculateLegalMoves(it,false)

                }
            } else {
                if (isLegalMove(it) && !testMoveForCheck(it)  && !viewModel.gameOver && timerActive) {

                    //reset yellow highlighting for CPU move
                    if (!viewModel.prevSquareCPU.equals("")) {
                        val target = getViewByIndex(viewModel.prevSquareCPU)
                        target.setBackgroundColor(viewModel.prevColorCPU)
                    }

                    val prev = getViewByIndex(viewModel.selectedSqaure)
                    prev.setBackgroundColor(viewModel.prevColor)
                    val res = getDrawableOnSquare(prev.tag.toString())

                    val prow = prev.tag.toString()[0].digitToInt()
                    val pcol = prev.tag.toString()[1].digitToInt()
                    val nrow = it.tag.toString()[0].digitToInt()
                    val ncol = it.tag.toString()[1].digitToInt()

                    val prevPiece = viewModel.board[prow][pcol]
                    if (prevPiece.equals("wp")  && nrow == 7) {
                        viewModel.board[nrow][ncol] = "wq"
                        v.setImageResource(R.drawable.wqueen2)
                    }
                    else if (prevPiece.equals("bp")  && nrow == 0) {
                        viewModel.board[nrow][ncol] = "bq"
                        v.setImageResource(R.drawable.bqueen2)
                    }  else {
                        viewModel.board[nrow][ncol] = prevPiece
                        v.setImageResource(res)
                    }

                    if (prevPiece.equals("wk")) {
                        viewModel.wkSquare = it.tag.toString()
                    }
                    if (prevPiece.equals("bk")) {
                        viewModel.bkSquare = it.tag.toString()
                    }

                    handleCastle(prow, pcol, nrow, ncol, false)  //moves the rook and handles rights
                    viewModel.board[prow][pcol] = "e"

                    //v.setImageResource(res)
                    prev.setImageResource(0)

                    viewModel.pieceSelected = false
                    resetSelection()
                    swapTurns()
                    calculateMaterial()
                    viewModel.syncBoard()
                    viewModel.returningFromSettings = false
                    viewModel.wkSquareTest = viewModel.wkSquare
                    viewModel.bkSquareTest = viewModel.bkSquare

                    if (checkXM()) {
                        viewModel.gameOver = true
                        var winner = ""
                        if (viewModel.turn.equals("black"))  {
                            winner = "White"
                        }
                        else  {
                            winner  =  "Black"
                        }
                        Toast.makeText(
                            activity, winner + " wins by checkmate!",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.allContent.setBackgroundColor(Color.LTGRAY)
                    }
                    else {

                        if (viewModel.turn.equals("black")) {
                            viewModel.whiteTime = whiteTimer.millisLeft()
                            stopTimer()
                            startTimer(blackTimer)
                        }
                        else {
                            viewModel.blackTime = blackTimer.millisLeft()
                            stopTimer()
                            startTimer(whiteTimer)
                        }


                        viewModel.genFEN()
                        if (viewModel.turn.equals("black") && viewModel.mode.equals("cpu")) {
                            viewModel.netRefresh()
                        }
                    }

                }
                else {
                    //made illegal move
                    Log.d("tag", "illegal move")
                    if  (!viewModel.gameOver && timerActive) {
                        val prev = getViewByIndex(viewModel.selectedSqaure)
                        prev.setBackgroundColor(viewModel.prevColor)
                        viewModel.pieceSelected = false
                        viewModel.syncBoard()
                    }

                }
            }
        }
    }

    //each available piece is selected and it's legal moves are played on the test board
    //if no legal moves are found for any of the pieces then the player is in xm
    private fun checkXM(): Boolean  {

        var pieces  = listOf<String>()
        if (viewModel.turn == "white") {
            pieces = whitePieces
        } else{
            pieces =blackPieces
        }
        val selectedSqaureCopy = viewModel.selectedSqaure

        for (i in 0..7) {
            for (j in 0..7) {
                val piece = viewModel.testBoard[i][j]
                if (pieces.contains(piece))  {
                    val str = i.toString() + j.toString()
                    val v = getViewByIndex(str)
                    calculateLegalMoves(v, true) //viewmodel.legalmoves now has list of legals for this piece

                    viewModel.legalMoves.forEach {
                        viewModel.selectedSqaure = i.toString() + j.toString()
                        val target = getViewByIndex(it)
                        val nrow = target.tag.toString()[0].digitToInt()
                        val ncol = target.tag.toString()[1].digitToInt()
                        val targetSquareCopy = viewModel.testBoard[nrow][ncol]

                        val ch = testMoveForCheck(target)

                        //resetting for next try
                        viewModel.testBoard[i][j] = piece  //reset test board
                        viewModel.testBoard[nrow][ncol] = targetSquareCopy
                        viewModel.wkSquareTest = viewModel.wkSquare
                        viewModel.bkSquareTest = viewModel.bkSquare

                        if (!ch) {
                            //found a legal move that gets out of check and can return
                            viewModel.selectedSqaure = selectedSqaureCopy
                            return false
                        } else {
                            //this move doesn't work, need to reset and try another
                        }
                    }
                }
            }
        }
        return true
    }
    private fun isValidPiece(v: View): Boolean  {

        val nrow = v.tag.toString()[0].digitToInt()
        val ncol = v.tag.toString()[1].digitToInt()
        val piece = viewModel.board[nrow][ncol]
        if (viewModel.turn.equals("white") && whitePieces.contains(piece)){
            return true
        }
        if (viewModel.turn.equals("black") && blackPieces.contains(piece)){
            return true
        }
        return false
    }

    private fun calculateLegalMoves(v: View, test: Boolean) {

        val nrow = v.tag.toString()[0].digitToInt()
        val ncol = v.tag.toString()[1].digitToInt()

        var piece = ""
        if (test) {
            piece = viewModel.testBoard[nrow][ncol]
        }
        else {
            piece = viewModel.board[nrow][ncol]
        }

        var legalMoves = listOf<String>()

        when (piece) {
            "wp" -> legalMoves  = calculatePawnMoves(nrow, ncol, "white", test)
            "wr" -> legalMoves  = calculateRookMoves(nrow, ncol, "white", test)
            "wb" -> legalMoves  = calculateBishopMoves(nrow, ncol, "white", test)
            "wq" -> legalMoves  = calculateQueenMoves(nrow, ncol, "white", test)
            "wk" -> legalMoves  = calculateKingMoves(nrow, ncol, "white", test)
            "wn" -> legalMoves  = calculateKnightMoves(nrow, ncol, "white", test)
            "bp" -> legalMoves  = calculatePawnMoves(nrow, ncol, "black", test)
            "br" -> legalMoves  = calculateRookMoves(nrow, ncol, "black", test)
            "bb" -> legalMoves  = calculateBishopMoves(nrow, ncol, "black", test)
            "bq" -> legalMoves  = calculateQueenMoves(nrow, ncol, "black", test)
            "bk" -> legalMoves  = calculateKingMoves(nrow, ncol, "black", test)
            "bn" -> legalMoves  = calculateKnightMoves(nrow, ncol, "black", test)
        }
        viewModel.legalMoves = legalMoves
    }

    private fun getViewByIndex(s: String): ImageView {

        val ll = binding.allContent
        return (ll.findViewWithTag<View>(s) as ImageView?)!!
    }

    private fun isLegalMove(v: View): Boolean {

        val nrow = v.tag.toString()[0]
        val ncol = v.tag.toString()[1]
        val square = nrow.plus(ncol.toString())
        return viewModel.legalMoves.contains(square)

    }

    private fun resetSelection() {
        viewModel.selectedSqaure = "none"
        viewModel.targetSquare = "none"
    }

    private fun letterToCol(s: String):  Int  {

        var col = -1
        when (s) {
            "a" -> col = 0
            "b" -> col = 1
            "c" -> col = 2
            "d" -> col = 3
            "e" -> col = 4
            "f" -> col = 5
            "g" -> col = 6
            "h" -> col = 7
        }
        return col
    }

    private fun getDrawableOnSquare(tag: String): Int {

        val row = tag[0].digitToInt()
        val col = tag[1].digitToInt()
        val piece = viewModel.board[row][col]

        var draw = 0
        when (piece) {
            "wp" -> draw = R.drawable.wpawn2
            "wr" -> draw = R.drawable.wrook2
            "wb" -> draw = R.drawable.wbishop2
            "wq" -> draw = R.drawable.wqueen2
            "wk" -> draw = R.drawable.wking2
            "wn" -> draw = R.drawable.wknight2
            "bp" -> draw = R.drawable.bpawn2
            "br" -> draw = R.drawable.brook2
            "bb" -> draw = R.drawable.bbishop2
            "bq" -> draw = R.drawable.bqueen2
            "bk" -> draw = R.drawable.bking2
            "bn" -> draw = R.drawable.bknight2
            "e" -> draw = 0
        }
        return draw
    }

    private fun swapTurns()  {

        if (viewModel.turn .equals("white")){
            viewModel.turn  = "black"
        } else {
            viewModel.turn  = "white"
        }
    }

    private fun resetGame() {

        viewModel.resetGame()
        timerActive = false
        binding.PauseTv.text = "Start"
        binding.PauseBut.text = ">"
        binding.whiteMaterial.text = ""
        binding.blackMaterial.text = ""
        binding.whiteTimer.text = millisToMins(viewModel.whiteTime)
        binding.blackTimer.text = millisToMins(viewModel.blackTime)
        redrawBoard()
    }

    private fun redrawBoard() {

        for (i in 0..7) {
            for (j in 0..7) {
                val square =  i.toString() + j.toString()
                val d = getDrawableOnSquare(square)
                val p = getViewByIndex(square)
                p.setImageResource(d)
            }
        }
    }

    private fun millisToMins(millis: Long): String {

        val total_seconds = (millis) / 1000.0
        val minutes = floor(total_seconds / 60.0).toInt()
        val seconds = (total_seconds % 60).toInt()
        val m = "%02d".format(minutes)
        val s = "%02d".format(seconds)
        val str = m + ":" + s
        return str
    }

    private fun inCheck(color: String):  Boolean {

        //basic idea is to loop through all of opponent pieces
        //and see if any of them "see" the other king

        var oppColor  = listOf("")

        if (color == "white") {
            oppColor = blackPieces
        } else{
            oppColor = whitePieces
        }

        for (i in 0..7) {
            for (j in 0..7) {
                val piece = viewModel.testBoard[i][j]
                if (oppColor.contains(piece))  {
                    val str = i.toString() + j.toString()
                    val v = getViewByIndex(str)
                    calculateLegalMoves(v, true)
                    if (color.equals("white") && viewModel.legalMoves.contains(viewModel.wkSquareTest)) {
                        return  true
                    }
                    else if (color.equals("black") && viewModel.legalMoves.contains(viewModel.bkSquareTest)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun testMoveForCheck(v: View): Boolean {

        val prev = getViewByIndex(viewModel.selectedSqaure)

        val prow = prev.tag.toString()[0].digitToInt()
        val pcol = prev.tag.toString()[1].digitToInt()
        val nrow = v.tag.toString()[0].digitToInt()
        val ncol = v.tag.toString()[1].digitToInt()

        val prevPiece = viewModel.testBoard[prow][pcol]
        if (prevPiece.equals("wp")  && nrow == 7) {
            viewModel.testBoard[nrow][ncol] = "wq"
        }
        else if (prevPiece.equals("bp")  && nrow == 0) {
            viewModel.testBoard[nrow][ncol] = "bq"
        }  else {
            viewModel.testBoard[nrow][ncol] = prevPiece
        }

        if (prevPiece.equals("wk")) {
            viewModel.wkSquareTest = v.tag.toString()
        }
        if (prevPiece.equals("bk")) {
            viewModel.bkSquareTest = v.tag.toString()
        }
        handleCastle(prow, pcol, nrow, ncol, true)  //moves the rook and handles rights
        viewModel.testBoard[prow][pcol] = "e"

        return inCheck(viewModel.turn)
    }

    private fun  calculatePawnMoves(row: Int, col: Int, color: String, test: Boolean): List<String>{

        var board = viewModel.board
        if (test) {
            board = viewModel.testBoard
        }

        var legalMoves = mutableListOf<String>()
        if (color == "white") {
            if(row < 7 && board[row + 1][col] == "e")  {
                legalMoves.add((row+1).toString() + col.toString())
            }
            if (row == 1 && board[row + 2][col] == "e")  {
                legalMoves.add((row+2).toString() + col.toString())
            }

            if (col > 0 && row < 7) {
                val tl = board[row + 1][col - 1]
                if (blackPieces.contains(tl)){
                    legalMoves.add((row + 1).toString() + (col-1).toString())
                }
            }

            if (col < 7 && row < 7) {
                val tr = board[row + 1][col + 1]
                if (blackPieces.contains(tr)){
                    legalMoves.add((row + 1).toString() + (col+1).toString())
                }
            }
        }
        else {
            if(row > 0 && board[row - 1][col] == "e")  {
                legalMoves.add((row-1).toString() + col.toString())
            }
            if (row == 6 && board[row - 2][col] == "e")  {
                legalMoves.add((row-2).toString() + col.toString())
            }

            if (col > 0 && row > 0) {
                val bl = board[row - 1][col - 1]
                if (whitePieces.contains(bl)){
                    legalMoves.add((row - 1).toString() + (col-1).toString())
                }
            }

            if (col < 7 && row > 0) {
                val br = board[row - 1][col + 1]
                if (whitePieces.contains(br)){
                    legalMoves.add((row - 1).toString() + (col+1).toString())
                }
            }
        }

        return legalMoves.toList()
    }

    fun  calculateRookMoves(row: Int, col: Int, color: String, test: Boolean): List<String>{

        var board = viewModel.board
        if (test) {
            board = viewModel.testBoard
        }

        var legalMoves = mutableListOf<String>()
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        var ri = row
        var ci = col
        while (ri > 0 && !myColor.contains(board[ri - 1][ci])) {
            legalMoves.add((ri - 1).toString() + ci.toString())
            if (oppColor.contains(board[ri - 1][ci])) {
                break
            }
            ri -= 1
        }
        ri = row
        while (ri < 7 && !myColor.contains(board[ri + 1][ci])) {
            legalMoves.add((ri + 1).toString() + ci.toString())
            if (oppColor.contains(board[ri + 1][ci])) {
                break
            }
            ri += 1
        }
        ri = row
        while (ci > 0 && !myColor.contains(board[ri][ci - 1])) {
            legalMoves.add((ri).toString() + (ci - 1).toString())
            if (oppColor.contains(board[ri][ci - 1])) {
                break
            }
            ci -= 1
        }
        ci = col
        while (ci < 7 && !myColor.contains(board[ri][ci + 1])) {
            legalMoves.add((ri).toString() + (ci + 1).toString())
            if (oppColor.contains(board[ri][ci + 1])) {
                break
            }
            ci += 1
        }
        return legalMoves.toList()
    }

    fun  calculateBishopMoves(row: Int, col: Int, color: String, test: Boolean): List<String>{

        var board = viewModel.board
        if (test) {
            board = viewModel.testBoard
        }

        var legalMoves = mutableListOf<String>()
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        var ri = row
        var ci = col
        while (ri > 0 && ci  > 0 && !myColor.contains(board[ri - 1][ci-1])) {
            legalMoves.add((ri - 1).toString() + (ci-1).toString())
            if (oppColor.contains(board[ri - 1][ci-1])) {
                break
            }
            ri -= 1
            ci -= 1
        }
        ri = row
        ci = col
        while (ri < 7 && ci < 7 && !myColor.contains(board[ri + 1][ci+1])) {
            legalMoves.add((ri + 1).toString() + (ci+1).toString())
            if (oppColor.contains(board[ri + 1][ci+1])) {
                break
            }
            ri += 1
            ci += 1
        }
        ri = row
        ci =  col
        while (ci > 0 && ri < 7 && !myColor.contains(board[ri+1][ci - 1])) {
            legalMoves.add((ri+1).toString() + (ci - 1).toString())
            if (oppColor.contains(board[ri+1][ci - 1])) {
                break
            }
            ci -= 1
            ri+=1
        }
        ci = col
        ri = row
        while (ci < 7 && ri > 0  && !myColor.contains(board[ri-1][ci + 1])) {
            legalMoves.add((ri-1).toString() + (ci + 1).toString())
            if (oppColor.contains(board[ri-1][ci + 1])) {
                break
            }
            ci += 1
            ri -= 1
        }
        return legalMoves.toList()
    }

    fun  calculateKnightMoves(row: Int, col: Int, color: String, test: Boolean): List<String>{

        var board = viewModel.board
        if (test) {
            board = viewModel.testBoard
        }

        var legalMoves = mutableListOf<String>()
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        if (row > 0 && col > 1 && !myColor.contains(board[row - 1][col - 2])) {
            legalMoves.add((row - 1).toString() + (col-2).toString())
        }
        if (row > 1 && col > 0 && !myColor.contains(board[row - 2][col - 1])) {
            legalMoves.add((row - 2).toString() + (col-1).toString())
        }
        if (row > 1 && col < 7 && !myColor.contains(board[row - 2][col +1])) {
            legalMoves.add((row - 2).toString() + (col+1).toString())
        }
        if (row > 0 && col < 6 && !myColor.contains(board[row - 1][col + 2])) {
            legalMoves.add((row - 1).toString() + (col+2).toString())
        }
        if (row < 7 && col < 6 && !myColor.contains(board[row + 1][col + 2])) {
            legalMoves.add((row + 1).toString() + (col+2).toString())
        }
        if (row < 6 && col < 7 && !myColor.contains(board[row + 2][col + 1])) {
            legalMoves.add((row + 2).toString() + (col+1).toString())
        }
        if (row <  7  && col > 1 && !myColor.contains(board[row + 1][col - 2])) {
            legalMoves.add((row + 1).toString() + (col-2).toString())
        }
        if (row < 6 && col > 0 && !myColor.contains(board[row + 2][col - 1])) {
            legalMoves.add((row +2).toString() + (col-1).toString())
        }
        return legalMoves.toList()
    }

    fun  calculateQueenMoves(row: Int, col: Int, color: String, test: Boolean): List<String>{

        var board = viewModel.board
        if (test) {
            board = viewModel.testBoard
        }

        var legalMoves = mutableListOf<String>()
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        var ri = row
        var ci = col
        while (ri > 0 && !myColor.contains(board[ri - 1][ci])) {
            legalMoves.add((ri - 1).toString() + ci.toString())
            if (oppColor.contains(board[ri - 1][ci])) {
                break
            }
            ri -= 1
        }
        ri = row
        while (ri < 7 && !myColor.contains(board[ri + 1][ci])) {
            legalMoves.add((ri + 1).toString() + ci.toString())
            if (oppColor.contains(board[ri + 1][ci])) {
                break
            }
            ri += 1
        }
        ri = row
        while (ci > 0 && !myColor.contains(board[ri][ci - 1])) {
            legalMoves.add((ri).toString() + (ci - 1).toString())
            if (oppColor.contains(board[ri][ci - 1])) {
                break
            }
            ci -= 1
        }
        ci = col
        while (ci < 7 && !myColor.contains(board[ri][ci + 1])) {
            legalMoves.add((ri).toString() + (ci + 1).toString())
            if (oppColor.contains(board[ri][ci + 1])) {
                break
            }
            ci += 1
        }

        ri = row
        ci = col
        while (ri > 0 && ci  > 0 && !myColor.contains(board[ri - 1][ci-1])) {
            legalMoves.add((ri - 1).toString() + (ci-1).toString())
            if (oppColor.contains(board[ri - 1][ci-1])) {
                break
            }
            ri -= 1
            ci -= 1
        }
        ri = row
        ci = col
        while (ri < 7 && ci < 7 && !myColor.contains(board[ri + 1][ci+1])) {
            legalMoves.add((ri + 1).toString() + (ci+1).toString())
            if (oppColor.contains(board[ri + 1][ci+1])) {
                break
            }
            ri += 1
            ci += 1
        }
        ri = row
        ci =  col
        while (ci > 0 && ri < 7 && !myColor.contains(board[ri+1][ci - 1])) {
            legalMoves.add((ri+1).toString() + (ci - 1).toString())
            if (oppColor.contains(board[ri+1][ci - 1])) {
                break
            }
            ci -= 1
            ri+=1
        }
        ci = col
        ri = row
        while (ci < 7 && ri > 0  && !myColor.contains(board[ri-1][ci + 1])) {
            legalMoves.add((ri-1).toString() + (ci + 1).toString())
            if (oppColor.contains(board[ri-1][ci + 1])) {
                break
            }
            ci += 1
            ri -= 1
        }
        return legalMoves
    }

    fun  calculateKingMoves(row: Int, col: Int, color: String, test: Boolean): List<String>{

        var board = viewModel.board
        if (test) {
            board = viewModel.testBoard
        }

        var legalMoves = mutableListOf<String>()
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        if (row > 0 && col > 0 && !myColor.contains(board[row - 1][col - 1])) {
            legalMoves.add((row - 1).toString() + (col-1).toString())
        }
        if (col > 0 && !myColor.contains(board[row][col - 1])) {
            legalMoves.add((row).toString() + (col-1).toString())
        }
        if (row < 7 && col > 0 && !myColor.contains(board[row + 1][col -1])) {
            legalMoves.add((row + 1).toString() + (col-1).toString())
        }
        if (row < 7 && !myColor.contains(board[row + 1][col])) {
            legalMoves.add((row + 1).toString() + (col).toString())
        }
        if (row < 7 && col < 7 && !myColor.contains(board[row + 1][col + 1])) {
            legalMoves.add((row + 1).toString() + (col+1).toString())
        }
        if (col < 7 && !myColor.contains(board[row][col + 1])) {
            legalMoves.add((row).toString() + (col+1).toString())
        }
        if (row > 0  && col < 7 && !myColor.contains(board[row - 1][col +1])) {
            legalMoves.add((row - 1).toString() + (col+1).toString())
        }
        if (row > 0 && !myColor.contains(board[row -1][col])) {
            legalMoves.add((row -1).toString() + (col).toString())
        }

        //castling
        if (color == "white" && row == 0 && col == 4 && viewModel.whiteCanCastleShort) {
            if (!myColor.contains(board[0][5]) &&
                !oppColor.contains(board[0][5]) &&
                !myColor.contains(board[0][6]) &&
                !oppColor.contains(board[0][6])) {
                    legalMoves.add((0).toString() + (6).toString())
                }
        }
        if (color == "white" && row == 0 && col == 4 && viewModel.whiteCanCastleLong) {
            if (!myColor.contains(board[0][3]) &&
                !oppColor.contains(board[0][3]) &&
                !myColor.contains(board[0][2]) &&
                !oppColor.contains(board[0][2]) &&
                !myColor.contains(board[0][1]) &&
                !oppColor.contains(board[0][1])) {
                    legalMoves.add((0).toString() + (2).toString())
            }
        }
        if (color == "black" && row == 7 && col == 4 && viewModel.blackCanCastleShort) {
            if (!myColor.contains(board[7][5]) &&
                !oppColor.contains(board[7][5]) &&
                !myColor.contains(board[7][6]) &&
                !oppColor.contains(board[7][6])) {
                    legalMoves.add((7).toString() + (6).toString())
            }
        }
        if (color == "black" && row == 7 && col == 4 && viewModel.blackCanCastleLong) {
            if (!myColor.contains(board[7][3]) &&
                !oppColor.contains(board[7][3]) &&
                !myColor.contains(board[7][2]) &&
                !oppColor.contains(board[7][2]) &&
                !myColor.contains(board[7][1]) &&
                !oppColor.contains(board[7][1])
            ) {
                legalMoves.add((7).toString() + (2).toString())
            }
        }
        return legalMoves.toList()
    }

    private fun handleCastle(prow: Int, pcol: Int, nrow: Int, ncol: Int, test: Boolean)  {

        var piece = ""
        if (test) {
            piece = viewModel.testBoard[prow][pcol]
        }else  {
            piece = viewModel.board[prow][pcol]
        }


        if (piece.equals("wk") && nrow == 0 && ncol == 6 && prow == 0 && pcol == 4) {
            //white short castle
            if (test) {
                viewModel.testBoard[0][7] = "e"
                viewModel.testBoard[0][5] = "wr"
            }
            else {
                val rook = getViewByIndex("07")
                rook.setImageResource(0)
                val  newrook = getViewByIndex("05")
                newrook.setImageResource(R.drawable.wrook2)
                viewModel.board[0][7] = "e"
                viewModel.board[0][5] = "wr"
            }

        }
        else if (piece.equals("wk") && nrow == 0 && ncol == 2 && prow == 0 && pcol == 4){
            if (test) {
                viewModel.testBoard[0][0] = "e"
                viewModel.testBoard[0][3] = "wr"
            }
            else {
                val rook = getViewByIndex("00")
                rook.setImageResource(0)
                val newrook = getViewByIndex("03")
                newrook.setImageResource(R.drawable.wrook2)
                viewModel.board[0][0] = "e"
                viewModel.board[0][3] = "wr"
            }
        }
        else if (piece.equals("bk") && nrow == 7 && ncol == 2 && prow == 7 && pcol == 4){
            if (test) {
                viewModel.testBoard[7][0] = "e"
                viewModel.testBoard[7][3] = "br"
            } else {
                val rook = getViewByIndex("70")
                rook.setImageResource(0)
                val newrook = getViewByIndex("73")
                newrook.setImageResource(R.drawable.brook2)
                viewModel.board[7][0] = "e"
                viewModel.board[7][3] = "br"
            }
        }
        else if (piece.equals("bk") && nrow == 7 && ncol == 6 && prow == 7 && pcol == 4){
            if (test) {
                viewModel.testBoard[7][7] = "e"
                viewModel.testBoard[7][5] = "br"
            } else {
                val rook = getViewByIndex("77")
                rook.setImageResource(0)
                val newrook = getViewByIndex("75")
                newrook.setImageResource(R.drawable.brook2)
                viewModel.board[7][7] = "e"
                viewModel.board[7][5] = "br"
            }
        }

        if (piece.equals("wk") && !test) {
            viewModel.whiteCanCastleShort = false
            viewModel.whiteCanCastleLong = false
        }
        else if  (piece.equals("bk") && !test) {
            viewModel.blackCanCastleShort = false
            viewModel.blackCanCastleLong = false
        }

    }

    private fun calculateMaterial() {

        var whiteTotal = 0
        var blackTotal = 0

        for (i in 0..7) {
            for (j in 0..7) {
                val piece = viewModel.testBoard[i][j]
                if (whitePieces.contains(piece))  {
                    whiteTotal += getPieceVal(piece)
                } else if (blackPieces.contains(piece)){
                    blackTotal += getPieceVal(piece)
                }
            }
        }

        val diff = abs(whiteTotal - blackTotal)
        val vstring = "+$diff"

        if (whiteTotal > blackTotal) {
            binding.whiteMaterial.text = vstring
            binding.blackMaterial.text = ""
        }
        else if (blackTotal > whiteTotal) {
            binding.whiteMaterial.text = ""
            binding.blackMaterial.text = vstring
        } else {
            binding.whiteMaterial.text = ""
            binding.blackMaterial.text = ""
        }

    }

    private fun getPieceVal(p: String): Int {

        var value = 0
        when (p) {
            "wp" -> value= 1
            "wr" -> value = 5
            "wb" -> value = 3
            "wq" -> value = 9
            "wk" -> value = 0
            "wn" -> value = 3
            "bp" -> value = 1
            "br" -> value= 5
            "bb" -> value = 3
            "bq" -> value = 9
            "bk" -> value = 0
            "bn" -> value = 3
            "e" -> value = 0
        }
        return  value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
