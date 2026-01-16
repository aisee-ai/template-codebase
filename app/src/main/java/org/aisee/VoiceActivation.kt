package org.aisee

import android.util.Log
import org.aisee.template_codebase.utils.*
import com.example.voice_activation_uart.VoiceActivation

class VoiceActivation {

    private var voiceActivation: VoiceActivation? = null

    fun start() {
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

            voiceActivation?.start()
        }
    }

    private fun stop() {
        voiceActivation?.stop()
    }

    companion object {
        private const val TAG = "VoiceActivation"
    }
}