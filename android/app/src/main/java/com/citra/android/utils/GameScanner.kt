package com.citra.android.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.citra.android.model.GameEntry

object GameScanner {

    private val SUPPORTED_EXTENSIONS = setOf("3ds", "cci", "cxi", "cia")

    suspend fun scanFolder(context: Context, folderUri: Uri): List<GameEntry> {
        val root = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()
        return scanRecursive(context, root)
    }

    private fun scanRecursive(context: Context, dir: DocumentFile): List<GameEntry> {
        val results = mutableListOf<GameEntry>()

        dir.listFiles().forEach { file ->
            if (file.isDirectory) {
                results.addAll(scanRecursive(context, file))
            } else if (file.isFile) {
                val name = file.name ?: return@forEach
                val ext  = name.substringAfterLast('.', "").lowercase()

                if (ext in SUPPORTED_EXTENSIONS) {
                    results.add(
                        GameEntry(
                            title         = name.substringBeforeLast('.'),
                            path          = file.uri.toString(),
                            region        = detectRegion(name),
                            fileExtension = ext,
                            fileSizeBytes = file.length()
                        )
                    )
                }
            }
        }

        return results.sortedBy { it.title.lowercase() }
    }

    private fun detectRegion(filename: String): String {
        val lower = filename.lowercase()
        return when {
            lower.contains("(usa)") || lower.contains("(u)")   -> "USA"
            lower.contains("(europe)") || lower.contains("(e)") -> "EUR"
            lower.contains("(japan)") || lower.contains("(j)")  -> "JPN"
            lower.contains("(world)")                           -> "WLD"
            lower.contains("(korea)")                           -> "KOR"
            lower.contains("(china)")                           -> "CHN"
            else                                                -> "UNK"
        }
    }
}
