package com.example.owlpost.models

data class Message (
    val from: String,
    val subject: String,
    val messageBody: String,
    val date: String
)