package com.esma.bunble.presentation.viewmodel.chat

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.remote.openai.Message
import com.esma.bunble.data.remote.openai.OpenAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.esma.bunble.R


// ViewModel'in tutacağı tüm ekran durumu (state)
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val textInput: String = "",
    val error: String? = null
)

// UI'da gösterilecek mesajlar için daha basit bir model
data class ChatMessage(
    val text: String,
    val author: Author
)

enum class Author { USER, AI }

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openAIRepository: OpenAIRepository,
    private val application: Application
) : ViewModel() {

    private val _chatState = mutableStateOf(ChatState())
    val chatState: State<ChatState> = _chatState

    init {
        // Eğer mesaj listesi boşsa (yani uygulama ilk kez açılıyorsa), başlangıç mesajını ekle.
        if (_chatState.value.messages.isEmpty()) {
            _chatState.value = _chatState.value.copy(
                messages = listOf(
                    ChatMessage(
                        application.getString(R.string.chat_initial_greeting),
                        Author.AI
                    )
                )
            )
        }
    }

    // METİN GİRİŞİNİ GÜNCELLEMEK İÇİN
    fun onTextInputChange(newText: String) {
        _chatState.value = _chatState.value.copy(textInput = newText)
    }

    fun sendMessage() {
        val userText = _chatState.value.textInput
        if (userText.isBlank() || _chatState.value.isLoading) return


        // Kullanıcının mesajını hemen UI'a ekleme
        val newUserMessage = ChatMessage(userText, Author.USER)
        _chatState.value = _chatState.value.copy(
            messages = _chatState.value.messages + newUserMessage,
            isLoading = true,
            textInput = "",
            error = null
        )

        viewModelScope.launch {
            try {
                // OpenAI'ye göndermek için tüm konuşma geçmişini hazırla
                val historyForAPI = buildChatHistory()
                // Mevcut repository'ni kullanarak cevabı al
                val aiResponseText = openAIRepository.getChatResponse(historyForAPI)
                // Gelen cevabı UI'a ekle
                val aiResponseMessage = ChatMessage(aiResponseText, Author.AI)
                // Cevap geldiğinde 'messages' listesini güncelle
                val currentMessages = _chatState.value.messages
                _chatState.value = _chatState.value.copy(
                    messages = currentMessages + aiResponseMessage,
                    isLoading = false
                )

            } catch (e: Exception) {
                e.printStackTrace()
                //  Hata durumunda UI'ı güncelle
                val errorMessage = ChatMessage(application.getString(R.string.chat_error_message), Author.AI)
                val currentMessages = _chatState.value.messages
                _chatState.value = _chatState.value.copy(
                    messages = currentMessages + errorMessage,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    // Konuşma geçmişini OpenAI API'sinin istediği `List<Message>` formatına çevirir
    private fun buildChatHistory(): List<Message> {
        val history = mutableListOf<Message>()

        // Sistem Mesajı: AI'nın rolünü ve kimliğini belirle. Bu, cevapların kalitesini artırır.
        history.add(Message(role = "system", content = "You are a friendly and expert language learning assistant named Bunble AI. Provide clear, concise, and helpful answers for learning languages."))

        // Konuşma Geçmişi: Önceki mesajları (başlangıç mesajı hariç) ekle
        _chatState.value.messages.drop(1).forEach { msg ->
            val role = if (msg.author == Author.USER) "user" else "assistant"
            history.add(Message(role = role, content = msg.text))
        }
        return history
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel yok edilirken, repository'nin kullandığı kaynakları serbest bırak.
        // Bu, ağ bağlantılarını kapatır ve "ManagedChannel" hatasını önler.
        try {
            openAIRepository.close()
        } catch (e: Exception) {
            // Log.e("ChatViewModel", "openAIRepository kapatılamadı", e)
        }
    }
}
