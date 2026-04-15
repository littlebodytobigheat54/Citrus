package com.citra.android.model

data class GameEntry(
    val title:         String,
    val path:          String,
    val region:        String,
    val fileExtension: String,
    val iconPath:      String? = null,
    val fileSizeBytes: Long    = 0L
)
