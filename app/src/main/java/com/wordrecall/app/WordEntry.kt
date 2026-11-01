package com.wordrecall.app

data class WordEntry(
    val id: String,
    val english: String,
    val chinese: String,
    val learned: Boolean = false
)
