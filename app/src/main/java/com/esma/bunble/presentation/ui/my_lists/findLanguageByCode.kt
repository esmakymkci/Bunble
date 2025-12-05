package com.esma.bunble.presentation.ui.my_lists

import com.esma.bunble.util.Language
import com.esma.bunble.util.supportedLanguages
import kotlin.text.equals

// Bu yardımcı fonksiyon, bayrakları bulmak için gerekli.
fun findLanguageByCode(code: String): Language? {
    return supportedLanguages.find { it.code.equals(code, ignoreCase = true) }
}
