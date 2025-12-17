package com.esma.bunble.presentation.viewmodel.chat

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translator
import android.speech.tts.TextToSpeech
import java.util.Locale

@HiltViewModel
class ConversationViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app), RecognitionListener, TextToSpeech.OnInitListener {

    private val _state = MutableStateFlow(ConversationState())
    val state = _state.asStateFlow()

    private var translator: Translator? = null

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(app)

    private val tts: TextToSpeech = TextToSpeech(app, this)

    init {
        recreateTranslator()
        speechRecognizer.setRecognitionListener(this)
    }

    // Dil değiştiğinde translator'ı yeniden oluşturan fonksiyon
    private fun recreateTranslator() {
        translator?.close() // Önceki translator'ı kapat
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(state.value.sourceLanguage.code)
            .setTargetLanguage(state.value.targetLanguage.code)
            .build()
        translator = Translation.getClient(options)
    }



    fun startListening() {
        _state.update { it.copy(isListening = true, recognizedText = "", translatedText = "") }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, state.value.sourceLanguage.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Anlık sonuçları almak için
        }
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        _state.update { it.copy(isListening = false) }
        speechRecognizer.stopListening()
    }

    fun onSourceLanguageSelected(language: Language) {
        _state.update { it.copy(sourceLanguage = language, isSourceMenuVisible = false) }
        recreateTranslator() // Dil değişti, translator'ı yeniden kur
    }

    fun onTargetLanguageSelected(language: Language) {
        _state.update { it.copy(targetLanguage = language, isTargetMenuVisible = false) }
        recreateTranslator()
        val locale = Locale.forLanguageTag(language.code)
        if (tts.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
            tts.language = locale
        }
    }

    fun onSwapLanguages() {
        val currentState = state.value
        _state.update {
            it.copy(
                sourceLanguage = currentState.targetLanguage,
                targetLanguage = currentState.sourceLanguage,
                recognizedText = currentState.translatedText, // Metinleri de çevir
                translatedText = currentState.recognizedText
            )
        }
        recreateTranslator()
    }


    fun onSourceMenuToggle(isVisible: Boolean) {
        _state.update { it.copy(isSourceMenuVisible = isVisible) }
    }

    fun onTargetMenuToggle(isVisible: Boolean) {
        _state.update { it.copy(isTargetMenuVisible = isVisible) }
    }


    // Çeviri işlemini yapan fonksiyon
    private fun translateText(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isTranslating = true) }

            // Dil modelinin indirilmesini bekle
            translator?.downloadModelIfNeeded()?.addOnSuccessListener {
                // Çeviri işlemini yap
                translator?.translate(text)
                    ?.addOnSuccessListener { translatedResult ->
                        _state.update {
                            it.copy(isTranslating = false, translatedText = translatedResult)
                        }
                    }
                    ?.addOnFailureListener { exception ->
                        _state.update {
                            it.copy(isTranslating = false, translatedText = "Çeviri hatası")
                        }
                    }
            }?.addOnFailureListener { exception ->
                _state.update {
                    it.copy(isTranslating = false, translatedText = "Model indirilemedi")
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Başarılı olursa, varsayılan dili hedef dile ayarla
            val locale = Locale.forLanguageTag(state.value.targetLanguage.code)
            tts.language = locale
        } else {
            // Hata olursa state'i güncelleyebiliriz (şimdilik loglamak yeterli)
            // Log.e("TTS", "Initialization failed")
        }
    }

    fun speakTranslatedText() {
        val text = state.value.translatedText
        if (text.isNotBlank()) {
            val locale = Locale.forLanguageTag(state.value.targetLanguage.code)
            if (tts.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
                tts.language = locale
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }else{
                _state.update { it.copy(translatedText = "Dil desteklenmiyor") }
            }
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        // Bu fonksiyon, ses tanıma servisinin dinlemeye hazır olduğunu bildirir.
        // Kullanıcıya "Şimdi konuşun..." gibi bir mesaj gösterebiliriz.
        // Şimdilik boş bırakabiliriz veya state'i güncelleyebiliriz.
        _state.update { it.copy(recognizedText = "Listening...") }
    }

    override fun onBeginningOfSpeech() {
        // İsteğe bağlı: Dinleme animasyonunu burada başlatılabilir
    }

    override fun onRmsChanged(rmsdB: Float) {
        // İsteğe bağlı: Ses yüksekliğine göre animasyon için bu değeri kullanılabilir
    }


    fun onPermissionDenied() {
        _state.update { currentState ->
            currentState.copy(
                recognizedText = "İzin gerekli",
                isListening = false
            )
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)?.let { partialText ->
            _state.update { it.copy(recognizedText = partialText) }
        }
    }

    override fun onResults(results: Bundle?) {
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)?.let { fullText ->
            _state.update { it.copy(isListening = false, recognizedText = fullText) }
            translateText(fullText)
        }
    }

    override fun onError(error: Int) {
        _state.update { it.copy(isListening = false, recognizedText = "Bir hata oluştu, tekrar deneyin.") }
    }

    override fun onEndOfSpeech() {
        _state.update { it.copy(isListening = false) }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onCleared() {
        super.onCleared()
        translator?.close()
        speechRecognizer.destroy() // ViewModel temizlendiğinde speechRecognizer'ı yok et
        tts.stop()
        tts.shutdown()
    }
}
