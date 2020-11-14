package com.example.owlpost.models.email

import java.io.File

class EmailFolder {
    val folderName: String
    val totalCount: Int
    val unreadCount: Int

    constructor(_folderName: String, _totalCount: Int, _unreadCount: Int){
        folderName = _folderName
        totalCount = _totalCount
        unreadCount = _unreadCount
    }

    constructor(path: String, _folderName: String){
        val file = File("$path/$_folderName/index")
        val reader = file.bufferedReader()
        val info = reader.readLine().split("/")
        folderName = _folderName
        totalCount = info[0].toInt()
        unreadCount = info[1].toInt()
    }

    fun writeTo(path: String){
        File("$path/${folderName}").mkdirs()
        val file = File("$path/${folderName}/index")
        val writer = file.bufferedWriter()
        writer.write("${totalCount}/${unreadCount}")
        writer.close()
    }
}