package com.example.blesearchum.model

import com.google.gson.annotations.SerializedName

data class BibleBook(
    @SerializedName("bookid") val bookId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("chapters") val chapters: Int
)

data class BibleVerse(
    @SerializedName("pk") val pk: Int,
    @SerializedName("verse") val verse: Int,
    @SerializedName("text") val text: String
)
