package com.example.chessapp

import com.google.gson.annotations.SerializedName
data class StockFishMove(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("bestmove")
    val bestmove: String
)
