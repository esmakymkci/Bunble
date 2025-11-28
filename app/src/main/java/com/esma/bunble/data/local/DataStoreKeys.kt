package com.esma.bunble.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

// Anahtarlarımızı tanımlayalım
object Keys {
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    val SOURCE_LANGUAGE = stringPreferencesKey("source_language")
    val TARGET_LANGUAGE = stringPreferencesKey("target_language")
}