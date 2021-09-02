package com.example.filesgo.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.*
import com.example.filesgo.model.Audio
import com.example.filesgo.model.FileData
import com.example.filesgo.model.Image
import com.example.filesgo.model.Video
import java.nio.charset.Charset

class FileRepository(private val contentResolver: ContentResolver) : IRepository {

    override suspend fun loadFilesFromStorage(): List<FileData> {

        val filesDataList = ArrayList<FileData>()
        val contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

        val projection = arrayOf(
            _ID,
            DISPLAY_NAME,
            MEDIA_TYPE,
            WIDTH,
            HEIGHT,
            _ID,
            DATA,
            DATE_ADDED,
            DURATION
        )
        //Retrieving all files
        val filesList =
            contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
                val idColumn =
                    cursor.getColumnIndexOrThrow(_ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(DISPLAY_NAME)
                val mediaTypeColumn =
                    cursor.getColumnIndexOrThrow(MEDIA_TYPE)
                val widthColumn =
                    cursor.getColumnIndexOrThrow(WIDTH)
                val heightColumn =
                    cursor.getColumnIndexOrThrow(HEIGHT)
                val dataColumn =
                    cursor.getColumnIndexOrThrow(DATA)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(DATE_ADDED)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(DURATION)

                while (cursor.moveToNext()) {
                    val mediaType = cursor.getString(mediaTypeColumn)
                    val id = cursor.getInt(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val extension = displayName.split(".").last()
                    val path = cursor.getString(dataColumn)

                    when (mediaType) {
                        MEDIA_TYPE_IMAGE.toString() -> {
                            val fileData = FileData(id,displayName,
                                Image(
                                    cursor.getInt(widthColumn),
                                    cursor.getInt(heightColumn),
                                    cursor.getInt(dateAddedColumn),
                                ), extension, path)
                            filesDataList.add(fileData)
                        }
                        MEDIA_TYPE_AUDIO.toString() -> {
                            val fileData = FileData(id,displayName,
                                Audio(
                                    cursor.getInt(durationColumn),
                                    cursor.getInt(dateAddedColumn)
                                ), extension, path)
                            filesDataList.add(fileData)
                        }
                        MEDIA_TYPE_VIDEO.toString() -> {
                            val fileData = FileData(id,displayName,
                                Video(
                                    cursor.getInt(durationColumn),
                                    cursor.getInt(dateAddedColumn)
                                ), extension, path)
                            filesDataList.add(fileData)
                        }
                    }
                }
                filesDataList.toList()
            } ?: listOf()

        return filesList
    }

    override suspend fun searchFiles(
        searchString: String,
        filesList: List<FileData>,
    ): List<FileData> {
        return filesList.filter {
            it.name.contains(searchString)
        }
    }

    override suspend fun writeToFile(filesFound: List<FileData>) {
        val contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Search Result")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOCUMENTS + "/My Files Go")
        }
        contentResolver.insert(contentUri, values)?.also { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->
                filesFound.forEach {
                    outputStream?.write((it.name + "\n").toByteArray(Charset.defaultCharset()))
                }
                outputStream?.close()
            }

        }
    }
}