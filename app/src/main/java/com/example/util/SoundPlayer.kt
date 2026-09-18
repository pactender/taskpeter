package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean

class SoundPlayer {
    private var audioTrack: AudioTrack? = null
    private val isPlaying = AtomicBoolean(false)
    private var playbackThread: Thread? = null

    fun playAmbientSound(soundType: String) {
        stopAmbientSound()
        isPlaying.set(true)

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            playbackThread = Thread {
                val buffer = ShortArray(bufferSize / 2)
                var angle = 0.0
                val random = Random()

                while (isPlaying.get()) {
                    when (soundType) {
                        "Cyber Hum" -> {
                            // Gentle 110Hz + 220Hz harmonic hum with subtle modulation
                            val freq = 110.0
                            val increment = (2.0 * Math.PI * freq) / sampleRate
                            for (i in buffer.indices) {
                                val sample = (Math.sin(angle) * 0.4 + Math.sin(angle * 2.0) * 0.2) * 8000
                                buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
                                angle += increment
                                if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                            }
                        }
                        "White Noise" -> {
                            // Soft filtered white noise
                            for (i in buffer.indices) {
                                val noise = (random.nextGaussian() * 2200).toInt()
                                buffer[i] = noise.coerceIn(-32768, 32767).toShort()
                            }
                        }
                        else -> {
                            // Deep Zen 55Hz drone
                            val freq = 55.0
                            val increment = (2.0 * Math.PI * freq) / sampleRate
                            for (i in buffer.indices) {
                                val sample = Math.sin(angle) * 7000
                                buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
                                angle += increment
                                if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                            }
                        }
                    }

                    audioTrack?.write(buffer, 0, buffer.size)
                }
            }.apply {
                isDaemon = true
                start()
            }
        } catch (_: Exception) {
            isPlaying.set(false)
        }
    }

    fun stopAmbientSound() {
        isPlaying.set(false)
        try {
            playbackThread?.interrupt()
            playbackThread = null
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }

    fun playChime() {
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 600
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val envelope = 1.0 - progress
                    // Dual tone 880Hz + 1760Hz bell chime
                    val sample = (Math.sin(2.0 * Math.PI * 880.0 * i / sampleRate) * 0.7 +
                                  Math.sin(2.0 * Math.PI * 1760.0 * i / sampleRate) * 0.3) * envelope * 20000
                    buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                Thread.sleep(700)
                track.release()
            } catch (_: Exception) {}
        }.start()
    }
}
