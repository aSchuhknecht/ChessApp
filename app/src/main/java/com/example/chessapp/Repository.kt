package com.example.chessapp

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call

class Repository(private val api: ChessAPI) {

    suspend fun getStock(fen: String, depth: Int): ResponseBody {
        return api.getStockFishResponse(fen, depth)
    }
}