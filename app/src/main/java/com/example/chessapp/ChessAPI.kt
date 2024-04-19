package com.example.chessapp

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
interface ChessAPI {

    @GET("api/s/v2.php?")
    suspend fun getStockFishResponse(@Query("fen") fen: String, @Query("depth") depth: Int) : ResponseBody

    data class StockfishResponse(val results: StockFishMove)

    companion object {

        var url = HttpUrl.Builder()
            .scheme("https")
            .host("stockfish.online")
            .build()

        fun create(): ChessAPI = create(url)
        private fun create(httpUrl: HttpUrl): ChessAPI{
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    this.level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChessAPI::class.java)
        }
    }
}