package com.example.filesgo.model

import android.net.Uri

data class FileData(
    val id: Long,
    val name: String,
    val fileType: FileType,
    val extension: String,
    val path: String,
    val size: Long,
    val dateCreated: Long,
    val dateModified: Long,
    val contentUri: Uri,
)

//Holds type of file and its metadata
sealed class FileType {
    object UnSupported : FileType()
    object Audio : FileType()
    object Video : FileType()
    data class Image(
        val width: Int,
        val height: Int,
    ) : FileType()
}


