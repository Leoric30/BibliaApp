package com.example.blesearchum.network

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleVerse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Servicio REST de Bolls Bible.
 *
 * Nota: la guía de laboratorio menciona el código "RVR1960", pero la API
 * actualmente identifica a la Reina-Valera 1960 como "RV1960".
 */
interface BibleApiService {

    @GET("get-books/$TRANSLATION/")
    suspend fun getBooks(): List<BibleBook>

    @GET("get-verse/$TRANSLATION/{bookId}/{chapter}/{verse}/")
    suspend fun getVerse(
        @Path("bookId") bookId: Int,
        @Path("chapter") chapter: Int,
        @Path("verse") verse: Int
    ): BibleVerse

    /** Devuelve todos los versículos de un capítulo; se usa para conocer cuántos tiene. */
    @GET("get-text/$TRANSLATION/{bookId}/{chapter}/")
    suspend fun getChapter(
        @Path("bookId") bookId: Int,
        @Path("chapter") chapter: Int
    ): List<BibleVerse>

    companion object {
        const val TRANSLATION = "RV1960"
    }
}

object RetrofitInstance {
    private const val BASE_URL = "https://bolls.life/"

    val api: BibleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BibleApiService::class.java)
    }
}
