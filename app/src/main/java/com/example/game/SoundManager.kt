package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {
    var isMuted: Boolean = false

    private var musicJob: kotlinx.coroutines.Job? = null
    var isMusicPlaying: Boolean = false
        private set

    fun startBackgroundMusic() {
        if (isMusicPlaying) return
        isMusicPlaying = true
        musicJob = CoroutineScope(Dispatchers.Default).launch {
            // A beautiful minimal pentatonic loop
            val melody = floatArrayOf(
                261.63f, 329.63f, 392.00f, 329.63f, // C4, E4, G4, E4
                293.66f, 349.23f, 440.00f, 349.23f, // D4, F4, A4, F4
                329.63f, 392.00f, 493.88f, 392.00f, // E4, G4, B4, G4
                349.23f, 440.00f, 523.25f, 440.00f  // F4, A4, C5, A4
            )
            var index = 0
            while (isMusicPlaying) {
                if (!isMuted) {
                    val freq = melody[index]
                    val samples = generateSweep(freq, freq, 0.22f, 0.05f) // Ultra-soft ambient sine notes
                    playSamples(samples)
                }
                delay(650) // Pleasant tempo
                index = (index + 1) % melody.size
            }
        }
    }

    fun stopBackgroundMusic() {
        isMusicPlaying = false
        musicJob?.cancel()
        musicJob = null
    }

    private fun playSamples(samples: ShortArray) {
        if (isMuted) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(44100)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                
                // Allow sound to play fully and then release resources safely
                val durationMs = (samples.size * 1000L) / 44100
                delay(durationMs + 100)
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateSweep(startFreq: Float, endFreq: Float, durationSec: Float, volumeFactor: Float = 0.35f): ShortArray {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            // Linearly interpolate the frequency at this moment
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            
            // Increment phase safely to avoid clicking sound artifacts
            phase += 2.0 * PI * currentFreq / sampleRate
            if (phase > 2.0 * PI) {
                phase -= 2.0 * PI
            }
            
            // Fade envelope to avoid clicks at start and end of sweeps
            val startEnvelope = if (progress < 0.15f) progress / 0.15f else 1.0f
            val endEnvelope = if (progress > 0.8f) (1.0f - progress) / 0.2f else 1.0f
            val envelope = startEnvelope * endEnvelope
            
            val value = (sin(phase) * 32767.0 * volumeFactor * envelope).toInt()
            samples[i] = value.coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    fun playPlaceSound() {
        // Light, pleasant tap beep
        val samples = generateSweep(350f, 500f, 0.06f)
        playSamples(samples)
    }

    fun playClearSound() {
        // Upward energetic sweep followed by a sparkling higher pitch chord chirp
        val samples1 = generateSweep(520f, 780f, 0.08f, 0.3f)
        val samples2 = generateSweep(784f, 1174f, 0.12f, 0.35f)
        
        val combined = ShortArray(samples1.size + samples2.size)
        System.arraycopy(samples1, 0, combined, 0, samples1.size)
        System.arraycopy(samples2, 0, combined, samples1.size, samples2.size)
        playSamples(combined)
    }

    fun playComboSound(combo: Int) {
        // High-pitched rapid arpeggio multiplier sound
        val pitchMultiplier = (1 + (combo.coerceAtMost(5) * 0.2f))
        val baseFreq = 650f * pitchMultiplier
        val sweepFreq = 1000f * pitchMultiplier
        
        val samples = generateSweep(baseFreq, sweepFreq, 0.15f, 0.35f)
        playSamples(samples)
    }

    fun playGameOverSound() {
        // Sad sliding downward scale
        val samples = generateSweep(300f, 90f, 0.55f, 0.4f)
        playSamples(samples)
    }
}
