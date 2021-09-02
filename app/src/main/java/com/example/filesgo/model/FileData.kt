package com.example.filesgo.model

data class FileData(
    val id: Int,
    val name: String,
    val fileType: FileType,
    val extension: String,
    val path: String,
)

// Holds type of file and its metadata
sealed class FileType
data class Image(val width: Int, val height: Int, val dateCreated: Int, val dateModified: Int) :
    FileType()

data class Audio(
    val duration: Int,
    val dateCreated: Int,
    val dateModified: Int,
    val album: String,
) : FileType()

data class Video(val duration: Int, val dateCreated: Int, val dateModified: Int) : FileType()

