package com.jinbo.smartsleep.audio

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

object AudioUtils {

    /**
     * Calculates the Root Mean Square (RMS) of the audio signal.
     */
    fun calculateRMS(data: ShortArray): Double {
        var sum = 0.0
        for (sample in data) {
            sum += sample * sample
        }
        return sqrt(sum / data.size)
    }

    /**
     * Calculates the energy in a specific frequency band using a simplified DFT.
     * Returns the sum of squared magnitudes.
     * Note: This is unnormalized. To compare with time-domain energy, appropriate scaling is needed.
     */
    fun calculateBandEnergy(data: ShortArray, sampleRate: Int, startFreq: Double, endFreq: Double): Double {
        val n = data.size
        var totalEnergy = 0.0
        
        // Calculate bin range
        val startBin = (startFreq * n / sampleRate).toInt()
        val endBin = (endFreq * n / sampleRate).toInt()

        for (k in startBin..endBin) {
            var real = 0.0
            var imag = 0.0
            val omega = 2.0 * PI * k / n
            
            for (i in data.indices) {
                val sample = data[i].toDouble()
                real += sample * cos(omega * i)
                imag -= sample * sin(omega * i)
            }
            totalEnergy += (real * real + imag * imag)
        }
        
        return totalEnergy
    }

    /**
     * Calculates Zero Crossing Rate (ZCR).
     * Snoring typically has lower ZCR compared to noise like talking or hissing.
     */
    fun calculateZCR(data: ShortArray): Double {
        var crossings = 0
        for (i in 1 until data.size) {
            if ((data[i] >= 0 && data[i-1] < 0) || (data[i] < 0 && data[i-1] >= 0)) {
                crossings++
            }
        }
        return crossings.toDouble() / (data.size - 1)
    }
}
