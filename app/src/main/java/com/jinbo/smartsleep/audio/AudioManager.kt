package com.jinbo.smartsleep.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

/**
 * Manages audio recording for snore events
 */
class AudioManager(private val context: Context) {

    companion object {
        private const val RECORDING_DURATION_MS = 20000L // 20 seconds (10s before + 10s after)
        private const val PRE_SNORE_SECONDS = 10000 // 10 seconds before detection
        private const val POST_SNORE_SECONDS = 10000 // 10 seconds after detection
    }

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentRecordingFile: File? = null
    private var recordingStartTime: Long = 0

    /**
     * Start recording audio
     * @return File path where recording will be saved, or null if failed
     */
    fun startRecording(): String? {
        if (isRecording) {
            return currentRecordingFile?.absolutePath
        }

        // Create recordings directory
        val recordingsDir = File(context.getExternalFilesDir(null), "snore_recordings")
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        // Create recording file
        val timestamp = System.currentTimeMillis()
        currentRecordingFile = File(recordingsDir, "snore_$timestamp.m4a")

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(currentRecordingFile?.absolutePath)

                prepare()
                start()
            }

            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            return currentRecordingFile?.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            currentRecordingFile = null
            isRecording = false
            return null
        }
    }

    /**
     * Stop recording and trim to desired duration
     * @param snoreTimestamp The timestamp when snore was detected (center point)
     * @return Pair of (filePath, actualDurationMs)
     */
    fun stopRecording(snoreTimestamp: Long): Pair<String, Long>? {
        if (!isRecording) {
            return null
        }

        return try {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            isRecording = false

            val filePath = currentRecordingFile?.absolutePath
            val actualDuration = System.currentTimeMillis() - recordingStartTime

            // TODO: Implement audio trimming to keep only 10s before and 10s after snore
            // For now, we keep the full recording and handle trimming during playback

            if (filePath != null) {
                Pair(filePath, actualDuration)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Cancel current recording
     */
    fun cancelRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                isRecording = false

                // Delete the file since we cancelled
                currentRecordingFile?.delete()
                currentRecordingFile = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Release resources
     */
    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
    }

    /**
     * Delete a recording file
     */
    fun deleteRecording(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get all recording files
     */
    fun getAllRecordings(): List<File> {
        val recordingsDir = File(context.getExternalFilesDir(null), "snore_recordings")
        if (!recordingsDir.exists()) {
            return emptyList()
        }
        return recordingsDir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * Clean up old recordings (older than specified timestamp)
     */
    fun cleanupOldRecords(olderThanTimestamp: Long): Int {
        val recordings = getAllRecordings()
        var deletedCount = 0

        recordings.forEach { file ->
            if (file.lastModified() < olderThanTimestamp) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }

        return deletedCount
    }
}
