package com.cs407.lazynotes.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * A utility object for file-related operations, particularly for handling content URIs.
 */
object FileUtil {

    /**
     * Creates a temporary local [File] from a content [Uri].
     *
     * This is necessary because content URIs (like from a file picker) cannot be used directly
     * for uploads. We need to copy the content to a temporary file in our app's cache
     * directory, which we can then access directly.
     *
     * @param context The application context.
     * @param uri The content URI of the file to be copied.
     * @return A [File] object pointing to the temporary copy.
     * @throws IOException if the file stream cannot be read or written.
     */
    @Throws(IOException::class)
    fun from(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        // Create a temporary file in the app's cache directory
        val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)
        // Ensure the temporary file is deleted when the app exits
        tempFile.deleteOnExit()

        // Use Kotlin's stream extension functions to copy the data
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }
}
