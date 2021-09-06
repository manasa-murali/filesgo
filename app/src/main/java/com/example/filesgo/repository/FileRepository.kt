package com.example.filesgo.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.*
import com.example.filesgo.model.FileData
import com.example.filesgo.model.FileType
import com.example.filesgo.utils.Constants
import java.nio.charset.Charset

class FileRepository(private val contentResolver: ContentResolver) : IRepository {

    override suspend fun loadFilesFromStorage(): List<FileData> {

        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri(Constants.EXTERNAL)
        }

        val projection = arrayOf(
            _ID,
            DISPLAY_NAME,
            MEDIA_TYPE,
            WIDTH,
            HEIGHT,
            DATA,
            DATE_ADDED,
            SIZE,
            DATE_MODIFIED,
        )

        //Retrieving all files
        val filesList = contentResolver.query(
            contentUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val arrayList = ArrayList<FileData>()
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
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(DATE_MODIFIED)
            val fileSizeColumn =
                cursor.getColumnIndexOrThrow(SIZE)

            while (cursor.moveToNext()) {
                val mediaType = cursor.getInt(mediaTypeColumn)

                val fileType: FileType = when (mediaType) {
                    MEDIA_TYPE_IMAGE -> FileType.Image(
                        width = cursor.getInt(widthColumn),
                        height = cursor.getInt(heightColumn),
                    )
                    MEDIA_TYPE_AUDIO -> FileType.Audio
                    MEDIA_TYPE_VIDEO -> FileType.Video
                    else -> FileType.UnSupported
                }

                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val extension = displayName.split(".").last()
                val path = cursor.getString(dataColumn)
                val dateCreated = cursor.getLong(dateAddedColumn)
                val dateModified = cursor.getLong(dateModifiedColumn)
                val size = cursor.getLong(fileSizeColumn)

                val fileData = FileData(
                    id = id,
                    name = displayName,
                    extension = extension,
                    path = path,
                    dateCreated = dateCreated,
                    dateModified = dateModified,
                    size = size,
                    fileType = fileType,
                    contentUri = getContentUri(contentUri, id)
                )
                arrayList.add(fileData)
            }
            cursor.close()
            arrayList
        } ?: listOf()

        return filesList
    }

    private fun getContentUri(contentUri: Uri, id: Long): Uri {
        return Uri.withAppendedPath(contentUri, "" + id)
    }

    override suspend fun searchFiles(
        searchString: String,
        filesList: List<FileData>,
    ): List<FileData> {
        return filesList.filter {
            it.name.lowercase().contains(searchString)
        }
    }

    override suspend fun writeToFile(filesFound: List<FileData>) {
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri(Constants.EXTERNAL)
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Search Result")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(
                MediaStore.MediaColumns.DATA,
                Environment.DIRECTORY_DOCUMENTS + "/My Files Go"
            )
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