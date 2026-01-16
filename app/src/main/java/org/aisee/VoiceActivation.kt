package org.aisee

import android.util.Log
import com.aisee.voice_activation_uart.VoiceActivation

class VoiceActivation {

    private val VAD_DEVICE_PATH = "/dev/ttyS0"
    private val VAD_BAUD_RATE = 9600

    private var voiceActivation: VoiceActivation? = null

    fun start() {
        try {
            voiceActivation = VoiceActivation.create(VAD_DEVICE_PATH, VAD_BAUD_RATE)

            voiceActivation?.events { event ->
                when (event) {
                    is VoiceActivation.Event.Command -> {
                        Log.i(TAG, "cmdId=${event.cmdId}")

                        when (event.cmdId) {
                            CMD_WAKE_UP -> {
                                Log.d(TAG, "Command: I-SEE-WAKE-UP detected")
                                // TODO: Add any desired logic for this voice command
                            }

                            CMD_HEY_I_SEE -> {
                                Log.d(TAG, "Command: HEY-I-SEE detected")
                                // TODO: Add any desired logic for this voice command
                            }

                            CMD_SLEEP -> {
                                Log.d(TAG, "Command: I-SEE-SLEEP detected")
                                // TODO: Add any desired logic for this voice command
                            }

                            CMD_VOLUME_UP -> {
                                Log.d(TAG, "Command: I-SEE-VOLUME-UP detected")
                                // TODO: Add any desired logic for this voice command
                            }

                            CMD_VOLUME_DOWN -> {
                                Log.d(TAG, "Command: I-SEE-VOLUME-DOWN detected")
                                // TODO: Add any desired logic for this voice command
                            }

                            else -> {
                                Log.w(TAG, "Unknown command received: ${event.cmdId}")
                            }
                        }
                    }

                    is VoiceActivation.Event.Error -> Log.e(TAG, "UART error: ${event.message}")
                    VoiceActivation.Event.Started -> Log.i(TAG, "VoiceActivation started")
                    VoiceActivation.Event.Stopped -> Log.i(TAG, "VoiceActivation stopped")
                }
            }

            voiceActivation?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VAD", e)
        }
    }

    private fun stop() {
        voiceActivation?.stop()
    }

    companion object {
        private const val TAG = "VoiceActivation"
        const val CMD_WAKE_UP = 1       // I-SEE-WAKE-UP
        const val CMD_HEY_I_SEE = 2     // HEY-I-SEE
        const val CMD_SLEEP = 3         // I-SEE-SLEEP
        const val CMD_VOLUME_UP = 4     // I-SEE-VOLUME-UP
        const val CMD_VOLUME_DOWN = 5   // I-SEE-VOLUME-DOWN
    }
}