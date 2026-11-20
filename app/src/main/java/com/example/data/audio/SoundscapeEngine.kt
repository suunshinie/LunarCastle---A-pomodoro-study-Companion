package com.example.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.data.model.SoundscapeChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val TAG = "SoundscapeEngine"

/**
 * Synthesizes gentle ambient audio on-device via AudioTrack so soundscape chips
 * produce genuine multi-channel atmospheric sound offline without external audio streams.
 */
class SoundscapeEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var playbackJob: Job? = null
    @Volatile
    private var activeChannels: Set<SoundscapeChannel> = emptySet()

    fun updateChannels(channels: Set<SoundscapeChannel>) {
        activeChannels = channels
        if (channels.isEmpty()) {
            stop()
        } else if (playbackJob?.isActive != true) {
            start()
        }
    }

    private fun start() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            val sampleRate = 22050
            val minBuffer = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuffer <= 0) {
                Log.w(TAG, "AudioTrack unavailable on this device ($minBuffer)")
                return@launch
            }
            val bufferSamples = (minBuffer / 2).coerceAtLeast(2048)
            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                val buffer = ShortArray(bufferSamples)
                val rng = Random(42)
                var rainLP = 0.0
                var hearthLP = 0.0
                var thunderLP = 0.0
                var thunderPhase = 0.0
                var sampleCounter = 0L

                while (isActive && activeChannels.isNotEmpty()) {
                    val channels = activeChannels
                    for (i in buffer.indices) {
                        val white = rng.nextDouble(-1.0, 1.0)
                        var mix = 0.0

                        if (SoundscapeChannel.NIGHT_RAIN in channels) {
                            rainLP = rainLP * 0.86 + white * 0.14
                            val swell = 0.75 + 0.25 * sin(sampleCounter * 0.00008)
                            mix += rainLP * 0.34 * swell
                        }

                        if (SoundscapeChannel.LIBRARY_HEARTH in channels) {
                            hearthLP = hearthLP * 0.965 + white * 0.035
                            val crackle = if (rng.nextInt(1400) == 0) rng.nextDouble(-0.5, 0.5) else 0.0
                            mix += hearthLP * 0.55 + crackle * 0.22
                        }

                        if (SoundscapeChannel.PARCHMENT_SCROLL in channels) {
                            val cycle = (sampleCounter % 17640).toInt()
                            val strokeEnv = if (cycle in 1200..3600) {
                                sin(((cycle - 1200) / 2400.0) * PI)
                            } else {
                                0.0
                            }
                            val paperNoise = (white - rainLP) * 0.16 * strokeEnv
                            mix += paperNoise
                        }

                        if (SoundscapeChannel.DISTANT_THUNDER in channels) {
                            thunderPhase += 0.00011
                            val rumbleGate = ((sin(thunderPhase) + 0.3).coerceAtLeast(0.0)) * 0.55
                            thunderLP = thunderLP * 0.992 + white * 0.008
                            mix += thunderLP * 1.45 * rumbleGate
                        }

                        sampleCounter++
                        val clamped = (mix * 0.45).coerceIn(-0.95, 0.95)
                        buffer[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Soundscape playback stopped gracefully: ${e.message}")
            } finally {
                try {
                    track?.pause()
                    track?.flush()
                    track?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun stop() {
        activeChannels = emptySet()
        playbackJob?.cancel()
        playbackJob = null
    }
}
