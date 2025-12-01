package com.esma.bunble.domain.model

sealed class QuizItem(
    open val id: String = "",
    open val type: String = ""
) {

     // Resim Seçme Sorusu Modeli
    data class ImageChoice(
        override val id: String = "",
        val questionText: String = "",
        val options: List<String> = emptyList(),
        val correctAnswer: String = ""
    ) : QuizItem()


     // Çoktan Seçmeli Metin Sorusu Modeli

    data class MultipleChoice(
        override val id: String = "",
        val questionText: String = "",
        val options: List<String> = emptyList(),
        val correctAnswer: String = ""
    ) : QuizItem()


    // Doğru/Yanlış Sorusu Modeli
    data class TrueFalse(
        override val id: String = "",
        val questionText: String = "",
        val correctAnswer: Boolean = false
    ) : QuizItem()


    //Desteklenmeyen veya hatalı bir soru tipi.
    data class Unsupported(
        override val id: String
    ) : QuizItem(id = id, type = "UNSUPPORTED")
}
