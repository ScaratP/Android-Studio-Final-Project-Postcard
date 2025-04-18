package com.example.afinal.data

data class Postcard(
    val id: Int,
    val title: String,
    val comment: String,
    val sendDate: String,
    var targetDate: String,
    val image: String
)
