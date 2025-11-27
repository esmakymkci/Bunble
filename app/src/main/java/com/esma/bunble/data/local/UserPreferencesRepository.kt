package com.esma.bunble.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context){
    // Anahtarlarımızı tanımlayalım
    private object Keys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val SOURCE_LANGUAGE = stringPreferencesKey("source_language")
        val TARGET_LANGUAGE = stringPreferencesKey("target_language")
    }

    // İlk açılış durumunu kaydet
    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun saveLanguageSelection(source: String, target: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SOURCE_LANGUAGE] = source
            preferences[Keys.TARGET_LANGUAGE] = target
        }
    }

    // İlk açılış mı? (Default: true)
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.IS_FIRST_LAUNCH] ?: true
    }

    // Kaydedilen dil yolunu (tr-de gibi) Flow olarak al
    val languagePath: Flow<String?> = context.dataStore.data.map { preferences ->
        val source = preferences[Keys.SOURCE_LANGUAGE]
        val target = preferences[Keys.TARGET_LANGUAGE]
        if (source != null && target != null) {
            "$source-$target"
        } else {
            null
        }
    }

}

/*

Uygulamanın ilk açılış bilgisini ve seçilen dil çiftini (source/target) kalıcı olarak DataStore’a kaydedip,
bunları Flow olarak ekranlarda kullanmana izin veriyor.

Extension property = Var olan bir tipe yeni bir property ekliyormuş gibi yazabildiğin,
ama aslında o tipin kaynağını değiştirmeyen, derleme zamanlı bir yardımcı özellik.

preferencesDataStore, "user_preferences" ismiyle cihazda bir dosya oluşturur ve tüm ayarlar bu dosyanın içine kaydedilir.

•Flow<...>:  Veriyi sadece bir kez okumak yerine, ona bir "akış" (Flow) olarak abone oluruz.
Bu şu anlama gelir: DataStore'daki bu veri her değiştiğinde, bu Flow'u dinleyen her yer otomatik olarak yeni değeri alır ve arayüz kendini günceller.

 */