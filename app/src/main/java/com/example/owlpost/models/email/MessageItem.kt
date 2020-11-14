package com.example.owlpost.models.email


data class MessageItem(
    val from: String,
    val subject: String,
    val text: String,
    val date: String,
    val seen: Boolean,
    val encrypted: Boolean,
    val signed: Boolean
)