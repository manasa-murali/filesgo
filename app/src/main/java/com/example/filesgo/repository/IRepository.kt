package com.example.filesgo.repository

import com.example.filesgo.model.FileData

interface IRepository {

    suspend fun loadFilesFromStorage(): List<FileData>

    suspend fun searchFiles(searchString: String, filesList: List<FileData>): List<FileData>

    suspend fun writeToFile(filesFound: List<FileData>)
}