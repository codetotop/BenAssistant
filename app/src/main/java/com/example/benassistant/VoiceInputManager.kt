package com.example.benassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceInputManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val handler = Handler(Looper.getMainLooper())

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle) {
                val texts = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!texts.isNullOrEmpty()) {
                    onResult(texts[0])
                } else {
                    onError("Vui lòng nói rõ hơn một chút giúp tôi!")
                }
            }

            override fun onError(error: Int) {
                onError(errorMessage(error))
            }

            // Các method còn lại có thể để trống
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        // Guard: check availability to avoid runtime errors on some devices/emulators
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Thiết bị chưa được hỗ trợ giọng nói")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)

        handler.postDelayed({
            speechRecognizer.stopListening()
        }, 4000)
    }

    fun release() {
        speechRecognizer.destroy()
    }

    companion object {
        private fun errorMessage(code: Int): String = when (code) {
            SpeechRecognizer.ERROR_AUDIO -> "Lỗi ghi âm"
            SpeechRecognizer.ERROR_CLIENT -> "Lỗi phía ứng dụng"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Thiếu quyền truy cập"
            SpeechRecognizer.ERROR_NETWORK -> "Lỗi mạng"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Hết thời gian mạng"
            SpeechRecognizer.ERROR_NO_MATCH -> "Vui lòng nói rõ hơn một chút giúp tôi!"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Hệ thống đang bận"
            SpeechRecognizer.ERROR_SERVER -> "Lỗi máy chủ"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Không phát hiện thấy giọng nói"
            else -> "Lỗi giọng nói: $code"
        }
    }
}
