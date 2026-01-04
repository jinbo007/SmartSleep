package com.jinbo.smartsleep.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

class AudioRecorder(
    private val sampleRate: Int = 16000,
    private val bufferSize: Int = 1024
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null

    interface AudioDataCallback {
        fun onAudioData(data: ShortArray)
    }

    private var callback: AudioDataCallback? = null

    fun setCallback(callback: AudioDataCallback) {
        this.callback = callback
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val size = if (bufferSize > minBufferSize) bufferSize else minBufferSize

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                size
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioRecorder", "AudioRecord initialization failed")
                return
            }

            audioRecord?.startRecording()
            isRecording = true

            recordingThread = Thread {
                val buffer = ShortArray(bufferSize)
                while (isRecording) {
                    val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (readResult > 0) {
                        callback?.onAudioData(buffer.copyOf())
                    }
                }
            }
            recordingThread?.start()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error starting recording", e)
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            recordingThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
    }
}
