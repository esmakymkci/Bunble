package com.esma.bunble.presentation.base.extension

// Cümleyi bulan yardımcı bir extension fonksiyon
fun String.findSentenceForChar(charIndex: Int): String {
    if (charIndex < 0 || charIndex >= this.length) return ""

    // Basılan karakterden geriye doğru cümlenin başlangıcını bul
    var start = this.lastIndexOfAny(charArrayOf('.', '!', '?'), startIndex = charIndex)
    start = if (start == -1) 0 else start + 1 // Cümle başı veya noktalama işaretinden sonrası

    // Basılan karakterden ileriye doğru cümlenin sonunu bul
    var end = this.indexOfAny(charArrayOf('.', '!', '?'), startIndex = charIndex)
    end = if (end == -1) this.length else end + 1 // Cümle sonu veya metin sonu

    return this.substring(start, end).trim()
}


/*

Verilen bir karakter indeksinden (charIndex) başlayarak, geriye ve ileriye doğru en yakın noktalama işaretlerini
(., !, ?) arar. Bu iki sınır arasındaki metni "cümle" olarak kabul eder ve temizlenmiş (trim) halini döndürür.

 */