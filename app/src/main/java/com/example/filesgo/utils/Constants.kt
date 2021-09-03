package com.example.filesgo.utils

object Constants {

    const val FILE_FETCH_ERROR: String = "Error Fetching Files From Device"
    const val GRANT_PERMISSION: String = "Permission Denied,Grant access through Settings"
    const val FILES_FOUND: String = "Files Found: "
    const val FILES_WRITTEN: String = "Check Document Folder For Search Result "
    const val FILES_NOT_WRITTEN: String = "Nothing to Write"
    const val CHANNEL_ID = "CHANNEL1"
    const val SEARCH_RESULT = "SEARCH RESULT"

    fun convertSectoHMS(duration: Int): String {
        val hours = duration / 3600000
        val minutes = (duration / 60000) % 60000
        val seconds = (duration % 60000) / 1000
        val stringBuilder = StringBuilder()
        if (hours != 0) {
            stringBuilder.append(hours).append(" H:")
        }
        if (hours == 0 && minutes == 0) {
            stringBuilder.append(seconds).append(" S")
        } else {
            stringBuilder.append(minutes).append(" M:")
            stringBuilder.append(seconds).append(" S")
        }
        return stringBuilder.toString()
    }
}