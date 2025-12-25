package com.esma.bunble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.esma.bunble.domain.repository.IUserRepository
import com.esma.bunble.navigation.Navigation
import com.esma.bunble.presentation.theme.ui.BunbleTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: IUserRepository

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    // Zamanlayıcı coroutine'ini yönetmek için bir değişken
    private var timeUpdateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //enableEdgeToEdge()
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            BunbleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }

    /**
     * Uygulama ön plana geldiğinde (kullanıcı görmeye başladığında) bu fonksiyon çalışır.
     */
    override fun onResume() {
        super.onResume()
        // Zamanlayıcıyı başlat.
        startTrackingTime()
    }

    /**
     * Uygulama arka plana gittiğinde (kullanıcı başka bir uygulamaya geçtiğinde veya ekranı kapattığında) bu fonksiyon çalışır.
     */
    override fun onPause() {
        super.onPause()
        // Bellek sızıntısı olmaması için zamanlayıcıyı durdur.
        stopTrackingTime()
    }

    /**
     * Her dakika Firestore'u güncelleyecek olan zamanlayıcıyı başlatan fonksiyon.
     */
    private fun startTrackingTime() {
        // Eğer zamanlayıcı zaten çalışıyorsa, tekrar başlatma.
        if (timeUpdateJob?.isActive == true) return

        // Giriş yapmış bir kullanıcı yoksa, zamanlayıcıyı başlatma.
        val userId = firebaseAuth.currentUser?.uid ?: return

        // Activity'nin yaşam döngüsüne bağlı bir coroutine başlat.
        timeUpdateJob = lifecycleScope.launch {
            // Bu döngü, coroutine iptal edilene kadar devam eder.
            while (isActive) {
                // 1 dakika (60,000 milisaniye) bekle.
                delay(60000L)

                // Firestore'daki 'totalTimeSpentMinutes' alanını 1 artır.
                userRepository.incrementTotalTimeSpent(userId, 1)
            }
        }
    }

    /**
     * Zamanlayıcıyı durduran fonksiyon.
     */
    private fun stopTrackingTime() {
        // Coroutine'i iptal et ve değişkeni temizle.
        timeUpdateJob?.cancel()
        timeUpdateJob = null
    }


}

