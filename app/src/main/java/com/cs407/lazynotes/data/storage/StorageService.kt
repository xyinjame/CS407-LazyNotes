package com.cs407.lazynotes.data.storage

import java.io.File

interface StorageService {
    /**
     * Upload local audio to storage on cloud
     * @param localFile local audio
     * @return public HTTPS url for downloading return null if fails。
     */
    suspend fun uploadAudioFile(localFile: File): String?
}