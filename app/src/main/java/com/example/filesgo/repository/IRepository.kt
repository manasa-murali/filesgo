package com.example.filesgo.repository

import com.example.filesgo.model.FileData

interface IRepository {

    suspend fun loadFilesFromStorage(searchString: String): List<FileData>

    suspend fun writeToFile(filesFound: List<FileData>)
}