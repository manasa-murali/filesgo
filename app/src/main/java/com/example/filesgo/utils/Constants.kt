package com.example.filesgo.utils

object Constants {

    const val FILES_FOUND: String = "Files Found: "
    const val CHANNEL_ID = "CHANNEL1"
    const val SEARCH_RESULT = "SEARCH RESULT"
    const val EXTERNAL: String = "external"

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