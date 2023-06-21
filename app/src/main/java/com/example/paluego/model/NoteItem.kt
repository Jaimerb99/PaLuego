package com.example.paluego.model

data class NoteItem(
    val id: String,
    val title: String,
    val description: String,
    val audio: Boolean = false
)
