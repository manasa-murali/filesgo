package com.example.filesgo.model

data class FileData(
    val id: Int,
    val name: String,
    val fileType: FileType,
    val extension: String,
    val path: String,
    val size: Long,
    val dateCreated: Long,
    val dateModified: Long,
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


