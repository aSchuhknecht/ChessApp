package com.example.chessapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.chessapp.databinding.GameFragmentBinding


class GameFragment: Fragment() {

    private var _binding: GameFragmentBinding? = null
    private val viewModel: ViewModel by activityViewModels()
    private val binding get() = _binding!!

    private lateinit var timerWhite: Timer
    private lateinit var timerBlack: Timer
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

        val clayout = binding.allContent
        for (i in 0 until clayout.childCount) {
            val v: View = clayout.getChildAt(i)
            if (v is ImageView) {
                setClickListeners(v)
            }
        }
    }

    fun setClickListeners(v: ImageView) {

        v.setOnClickListener{
            if (!viewModel.pieceSelected) {
                if (isValidPiece(it)) {
                    val viewColor = it.background as ColorDrawable
                    val colorId = viewColor.color
                    viewModel.prevColor = colorId
                    it.setBackgroundColor(Color.parseColor("#ff0000"))  // red
                    viewModel.pieceSelected = true
                    viewModel.selectedSqaure = it.tag.toString()
                    calculateLegalMoves(it)

                }
            } else {
                if (isLegalMove(it)) {

                    val prev = getViewByIndex(viewModel.selectedSqaure)
                    prev.setBackgroundColor(viewModel.prevColor)
                    val res = getDrawableOnSquare(prev.tag.toString())

                    val prow = prev.tag.toString()[0].digitToInt()
                    val pcol = prev.tag.toString()[1].digitToInt()
                    val nrow = it.tag.toString()[0].digitToInt()
                    val ncol = it.tag.toString()[1].digitToInt()

                    val prevPiece = viewModel.board[prow][pcol]
                    viewModel.board[nrow][ncol] = prevPiece
                    if (prevPiece.equals("wk")) {
                        viewModel.wkSquare = it.tag.toString()
                    }
                    if (prevPiece.equals("bk")) {
                        viewModel.bkSquare = it.tag.toString()
                    }

                    handleCastle(prow, pcol, nrow, ncol)  //moves the rook and handles rights
                    viewModel.board[prow][pcol] = "e"

                    v.setImageResource(res)
                    prev.setImageResource(0)

                    val check = inCheck(viewModel.turn)
                    Log.d("tag", check.toString())

                    viewModel.pieceSelected = false
                    resetSelection()
                    viewModel.pieceSelected = false
                    swapTurns()
                }
                else {
                    //made illegal move
                    Log.d("tag", "illegal move")
                    val prev = getViewByIndex(viewModel.selectedSqaure)
                    prev.setBackgroundColor(viewModel.prevColor)
                    viewModel.pieceSelected = false

                }
            }
        }
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

    private fun calculateLegalMoves(v: View) {

        val nrow = v.tag.toString()[0].digitToInt()
        val ncol = v.tag.toString()[1].digitToInt()
        val piece = viewModel.board[nrow][ncol]
        var legalMoves = listOf("")

        when (piece) {
            "wp" -> legalMoves  = calculatePawnMoves(nrow, ncol, "white")
            "wr" -> legalMoves  = calculateRookMoves(nrow, ncol, "white")
            "wb" -> legalMoves  = calculateBishopMoves(nrow, ncol, "white")
            "wq" -> legalMoves  = calculateQueenMoves(nrow, ncol, "white")
            "wk" -> legalMoves  = calculateKingMoves(nrow, ncol, "white")
            "wn" -> legalMoves  = calculateKnightMoves(nrow, ncol, "white")
            "bp" -> legalMoves  = calculatePawnMoves(nrow, ncol, "black")
            "br" -> legalMoves  = calculateRookMoves(nrow, ncol, "black")
            "bb" -> legalMoves  = calculateBishopMoves(nrow, ncol, "black")
            "bq" -> legalMoves  = calculateQueenMoves(nrow, ncol, "black")
            "bk" -> legalMoves  = calculateKingMoves(nrow, ncol, "black")
            "bn" -> legalMoves  = calculateKnightMoves(nrow, ncol, "black")
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

    private fun getDrawableOnSquare(tag: String): Int {

        val row = tag[0].digitToInt()
        val col = tag[1].digitToInt()
        val piece = viewModel.board[row][col]

        var draw = 0
        when (piece) {
            "wp" -> draw = R.drawable.wpawn
            "wr" -> draw = R.drawable.wrook
            "wb" -> draw = R.drawable.wbishop
            "wq" -> draw = R.drawable.wqueen
            "wk" -> draw = R.drawable.wking
            "wn" -> draw = R.drawable.wknight
            "bp" -> draw = R.drawable.bpawn
            "br" -> draw = R.drawable.brook
            "bb" -> draw = R.drawable.bbishop
            "bq" -> draw = R.drawable.bqueen
            "bk" -> draw = R.drawable.bking
            "bn" -> draw = R.drawable.bknight
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

    private fun inCheck(color: String):  Boolean {

        //basic idea is to loop through all of opponent pieces
        //and see if any of them "see" the other king

        var totalLegalMoves = mutableListOf("")
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        for (i in 0..7) {
            for (j in 0..7) {
                val piece = viewModel.board[i][j]
                if (oppColor.contains(piece))  {
                    val str = i.toString() + j.toString()
                    val v = getViewByIndex(str)
                    calculateLegalMoves(v)
                    if (color.equals("white") && viewModel.legalMoves.contains(viewModel.wkSquare)) {
                        return  true
                    }
                    else if (color.equals("black") && viewModel.legalMoves.contains(viewModel.bkSquare)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun  calculatePawnMoves(row: Int, col: Int, color: String): List<String>{

        var legalMoves = mutableListOf("")
        if (color == "white") {
            if(row < 7 && viewModel.board[row + 1][col] == "e")  {
                legalMoves.add((row+1).toString() + col.toString())
            }
            if (row == 1 && viewModel.board[row + 2][col] == "e")  {
                legalMoves.add((row+2).toString() + col.toString())
            }

            if (col > 0 && row < 7) {
                val tl = viewModel.board[row + 1][col - 1]
                if (blackPieces.contains(tl)){
                    legalMoves.add((row + 1).toString() + (col-1).toString())
                }
            }

            if (col < 7 && row < 7) {
                val tr = viewModel.board[row + 1][col + 1]
                if (blackPieces.contains(tr)){
                    legalMoves.add((row + 1).toString() + (col+1).toString())
                }
            }
        }
        else {
            if(row > 0 && viewModel.board[row - 1][col] == "e")  {
                legalMoves.add((row-1).toString() + col.toString())
            }
            if (row == 6 && viewModel.board[row - 2][col] == "e")  {
                legalMoves.add((row-2).toString() + col.toString())
            }

            if (col > 0 && row > 0) {
                val bl = viewModel.board[row - 1][col - 1]
                if (whitePieces.contains(bl)){
                    legalMoves.add((row - 1).toString() + (col-1).toString())
                }
            }

            if (col < 7 && row > 0) {
                val br = viewModel.board[row - 1][col + 1]
                if (whitePieces.contains(br)){
                    legalMoves.add((row - 1).toString() + (col+1).toString())
                }
            }
        }

        return legalMoves.toList()
    }

    fun  calculateRookMoves(row: Int, col: Int, color: String): List<String>{

        var legalMoves = mutableListOf("")
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
        while (ri > 0 && !myColor.contains(viewModel.board[ri - 1][ci])) {
            legalMoves.add((ri - 1).toString() + ci.toString())
            if (oppColor.contains(viewModel.board[ri - 1][ci])) {
                break
            }
            ri -= 1
        }
        ri = row
        while (ri < 7 && !myColor.contains(viewModel.board[ri + 1][ci])) {
            legalMoves.add((ri + 1).toString() + ci.toString())
            if (oppColor.contains(viewModel.board[ri + 1][ci])) {
                break
            }
            ri += 1
        }
        ri = row
        while (ci > 0 && !myColor.contains(viewModel.board[ri][ci - 1])) {
            legalMoves.add((ri).toString() + (ci - 1).toString())
            if (oppColor.contains(viewModel.board[ri][ci - 1])) {
                break
            }
            ci -= 1
        }
        ci = col
        while (ci < 7 && !myColor.contains(viewModel.board[ri][ci + 1])) {
            legalMoves.add((ri).toString() + (ci + 1).toString())
            if (oppColor.contains(viewModel.board[ri][ci + 1])) {
                break
            }
            ci += 1
        }
        return legalMoves.toList()
    }

    fun  calculateBishopMoves(row: Int, col: Int, color: String): List<String>{

        var legalMoves = mutableListOf("")
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
        while (ri > 0 && ci  > 0 && !myColor.contains(viewModel.board[ri - 1][ci-1])) {
            legalMoves.add((ri - 1).toString() + (ci-1).toString())
            if (oppColor.contains(viewModel.board[ri - 1][ci-1])) {
                break
            }
            ri -= 1
            ci -= 1
        }
        ri = row
        ci = col
        while (ri < 7 && ci < 7 && !myColor.contains(viewModel.board[ri + 1][ci+1])) {
            legalMoves.add((ri + 1).toString() + (ci+1).toString())
            if (oppColor.contains(viewModel.board[ri + 1][ci+1])) {
                break
            }
            ri += 1
            ci += 1
        }
        ri = row
        ci =  col
        while (ci > 0 && ri < 7 && !myColor.contains(viewModel.board[ri+1][ci - 1])) {
            legalMoves.add((ri+1).toString() + (ci - 1).toString())
            if (oppColor.contains(viewModel.board[ri+1][ci - 1])) {
                break
            }
            ci -= 1
            ri+=1
        }
        ci = col
        ri = row
        while (ci < 7 && ri > 0  && !myColor.contains(viewModel.board[ri-1][ci + 1])) {
            legalMoves.add((ri-1).toString() + (ci + 1).toString())
            if (oppColor.contains(viewModel.board[ri-1][ci + 1])) {
                break
            }
            ci += 1
            ri -= 1
        }
        return legalMoves.toList()
    }

    fun  calculateKnightMoves(row: Int, col: Int, color: String): List<String>{

        var legalMoves = mutableListOf("")
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        if (row > 0 && col > 1 && !myColor.contains(viewModel.board[row - 1][col - 2])) {
            legalMoves.add((row - 1).toString() + (col-2).toString())
        }
        if (row > 1 && col > 0 && !myColor.contains(viewModel.board[row - 2][col - 1])) {
            legalMoves.add((row - 2).toString() + (col-1).toString())
        }
        if (row > 1 && col < 7 && !myColor.contains(viewModel.board[row - 2][col +1])) {
            legalMoves.add((row - 2).toString() + (col+1).toString())
        }
        if (row > 0 && col < 6 && !myColor.contains(viewModel.board[row - 1][col + 2])) {
            legalMoves.add((row - 1).toString() + (col+2).toString())
        }
        if (row < 7 && col < 6 && !myColor.contains(viewModel.board[row + 1][col + 2])) {
            legalMoves.add((row + 1).toString() + (col+2).toString())
        }
        if (row < 6 && col < 7 && !myColor.contains(viewModel.board[row + 2][col + 1])) {
            legalMoves.add((row + 2).toString() + (col+1).toString())
        }
        if (row <  7  && col > 1 && !myColor.contains(viewModel.board[row + 1][col - 2])) {
            legalMoves.add((row + 1).toString() + (col-2).toString())
        }
        if (row < 6 && col > 0 && !myColor.contains(viewModel.board[row + 2][col - 1])) {
            legalMoves.add((row +2).toString() + (col-1).toString())
        }
        return legalMoves.toList()
    }

    fun  calculateQueenMoves(row: Int, col: Int, color: String): List<String>{

        var legalMoves = mutableListOf("")
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
        while (ri > 0 && !myColor.contains(viewModel.board[ri - 1][ci])) {
            legalMoves.add((ri - 1).toString() + ci.toString())
            if (oppColor.contains(viewModel.board[ri - 1][ci])) {
                break
            }
            ri -= 1
        }
        ri = row
        while (ri < 7 && !myColor.contains(viewModel.board[ri + 1][ci])) {
            legalMoves.add((ri + 1).toString() + ci.toString())
            if (oppColor.contains(viewModel.board[ri + 1][ci])) {
                break
            }
            ri += 1
        }
        ri = row
        while (ci > 0 && !myColor.contains(viewModel.board[ri][ci - 1])) {
            legalMoves.add((ri).toString() + (ci - 1).toString())
            if (oppColor.contains(viewModel.board[ri][ci - 1])) {
                break
            }
            ci -= 1
        }
        ci = col
        while (ci < 7 && !myColor.contains(viewModel.board[ri][ci + 1])) {
            legalMoves.add((ri).toString() + (ci + 1).toString())
            if (oppColor.contains(viewModel.board[ri][ci + 1])) {
                break
            }
            ci += 1
        }

        ri = row
        ci = col
        while (ri > 0 && ci  > 0 && !myColor.contains(viewModel.board[ri - 1][ci-1])) {
            legalMoves.add((ri - 1).toString() + (ci-1).toString())
            if (oppColor.contains(viewModel.board[ri - 1][ci-1])) {
                break
            }
            ri -= 1
            ci -= 1
        }
        ri = row
        ci = col
        while (ri < 7 && ci < 7 && !myColor.contains(viewModel.board[ri + 1][ci+1])) {
            legalMoves.add((ri + 1).toString() + (ci+1).toString())
            if (oppColor.contains(viewModel.board[ri + 1][ci+1])) {
                break
            }
            ri += 1
            ci += 1
        }
        ri = row
        ci =  col
        while (ci > 0 && ri < 7 && !myColor.contains(viewModel.board[ri+1][ci - 1])) {
            legalMoves.add((ri+1).toString() + (ci - 1).toString())
            if (oppColor.contains(viewModel.board[ri+1][ci - 1])) {
                break
            }
            ci -= 1
            ri+=1
        }
        ci = col
        ri = row
        while (ci < 7 && ri > 0  && !myColor.contains(viewModel.board[ri-1][ci + 1])) {
            legalMoves.add((ri-1).toString() + (ci + 1).toString())
            if (oppColor.contains(viewModel.board[ri-1][ci + 1])) {
                break
            }
            ci += 1
            ri -= 1
        }
        return legalMoves
    }

    fun  calculateKingMoves(row: Int, col: Int, color: String): List<String>{

        var legalMoves = mutableListOf("")
        var myColor = listOf("")
        var oppColor  = listOf("")

        if (color == "white") {
            myColor = whitePieces
            oppColor = blackPieces
        } else{
            myColor = blackPieces
            oppColor = whitePieces
        }

        if (row > 0 && col > 0 && !myColor.contains(viewModel.board[row - 1][col - 1])) {
            legalMoves.add((row - 1).toString() + (col-1).toString())
        }
        if (col > 0 && !myColor.contains(viewModel.board[row][col - 1])) {
            legalMoves.add((row).toString() + (col-1).toString())
        }
        if (row < 7 && col > 0 && !myColor.contains(viewModel.board[row + 1][col -1])) {
            legalMoves.add((row + 1).toString() + (col-1).toString())
        }
        if (row < 7 && !myColor.contains(viewModel.board[row + 1][col])) {
            legalMoves.add((row + 1).toString() + (col).toString())
        }
        if (row < 7 && col < 7 && !myColor.contains(viewModel.board[row + 1][col + 1])) {
            legalMoves.add((row + 1).toString() + (col+1).toString())
        }
        if (col < 7 && !myColor.contains(viewModel.board[row][col + 1])) {
            legalMoves.add((row).toString() + (col+1).toString())
        }
        if (row > 0  && col < 7 && !myColor.contains(viewModel.board[row - 1][col +1])) {
            legalMoves.add((row - 1).toString() + (col+1).toString())
        }
        if (row > 0 && !myColor.contains(viewModel.board[row -1][col])) {
            legalMoves.add((row -1).toString() + (col).toString())
        }

        //castling
        if (color == "white" && row == 0 && col == 4 && viewModel.whiteCanCastleShort) {
            if (!myColor.contains(viewModel.board[0][5]) &&
                !oppColor.contains(viewModel.board[0][5]) &&
                !myColor.contains(viewModel.board[0][6]) &&
                !oppColor.contains(viewModel.board[0][6])) {
                    legalMoves.add((0).toString() + (6).toString())
                }
        }
        if (color == "white" && row == 0 && col == 4 && viewModel.whiteCanCastleLong) {
            if (!myColor.contains(viewModel.board[0][3]) &&
                !oppColor.contains(viewModel.board[0][3]) &&
                !myColor.contains(viewModel.board[0][2]) &&
                !oppColor.contains(viewModel.board[0][2]) &&
                !myColor.contains(viewModel.board[0][1]) &&
                !oppColor.contains(viewModel.board[0][1])) {
                    legalMoves.add((0).toString() + (2).toString())
            }
        }
        if (color == "black" && row == 7 && col == 4 && viewModel.blackCanCastleShort) {
            if (!myColor.contains(viewModel.board[7][5]) &&
                !oppColor.contains(viewModel.board[7][5]) &&
                !myColor.contains(viewModel.board[7][6]) &&
                !oppColor.contains(viewModel.board[7][6])) {
                    legalMoves.add((7).toString() + (6).toString())
            }
        }
        if (color == "black" && row == 7 && col == 4 && viewModel.blackCanCastleLong) {
            if (!myColor.contains(viewModel.board[7][3]) &&
                !oppColor.contains(viewModel.board[7][3]) &&
                !myColor.contains(viewModel.board[7][2]) &&
                !oppColor.contains(viewModel.board[7][2]) &&
                !myColor.contains(viewModel.board[7][1]) &&
                !oppColor.contains(viewModel.board[7][1])
            ) {
                legalMoves.add((7).toString() + (2).toString())
            }
        }
        return legalMoves.toList()
    }

    private fun handleCastle(prow: Int, pcol: Int, nrow: Int, ncol: Int)  {

        val piece = viewModel.board[prow][pcol]

        if (piece.equals("wk") && nrow == 0 && ncol == 6) {
            //white short castle
            val rook = getViewByIndex("07")
            rook.setImageResource(0)
            val  newrook = getViewByIndex("05")
            newrook.setImageResource(R.drawable.wrook)
            viewModel.board[0][7] = "e"
            viewModel.board[0][5] = "wr"
        }
        else if (piece.equals("wk") && nrow == 0 && ncol == 2){
            val rook = getViewByIndex("00")
            rook.setImageResource(0)
            val  newrook = getViewByIndex("03")
            newrook.setImageResource(R.drawable.wrook)
            viewModel.board[0][0] = "e"
            viewModel.board[0][3] = "wr"
        }
        else if (piece.equals("bk") && nrow == 7 && ncol == 2){
            val rook = getViewByIndex("70")
            rook.setImageResource(0)
            val  newrook = getViewByIndex("73")
            newrook.setImageResource(R.drawable.brook)
            viewModel.board[7][0] = "e"
            viewModel.board[7][3] = "br"
        }
        else if (piece.equals("wk") && nrow == 7 && ncol == 6){
            val rook = getViewByIndex("77")
            rook.setImageResource(0)
            val  newrook = getViewByIndex("75")
            newrook.setImageResource(R.drawable.brook)
            viewModel.board[7][7] = "e"
            viewModel.board[7][5] = "br"
        }

        if (piece.equals("wk")) {
            viewModel.whiteCanCastleShort = false
            viewModel.whiteCanCastleLong = false
        }
        else if  (piece.equals("bk")) {
            viewModel.blackCanCastleShort = false
            viewModel.blackCanCastleLong = false
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
