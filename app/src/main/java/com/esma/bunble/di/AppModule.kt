package com.esma.bunble.di

import com.esma.bunble.data.repository.LearningRepositoryImpl
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.app.Application
import android.content.Context
import coil.ImageLoader
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.data.remote.openai.OpenAIApi
import com.esma.bunble.data.remote.openai.OpenAIRepository
import com.esma.bunble.data.repository.StoryRepositoryImpl
import com.esma.bunble.data.repository.WordListRepositoryImpl
import com.esma.bunble.domain.repository.IStoryRepository
import com.esma.bunble.domain.repository.IWordListRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideLeanrRepository(
        firestore: FirebaseFirestore): ILearningRepository {
        return LearningRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideImageLoader(app: Application): ImageLoader {
        return ImageLoader.Builder(app)
            .crossfade(true) // Resimler yüklenirken yumuşak bir geçiş efekti
            .respectCacheHeaders(false) // Önbellek kontrolünü basitleştirir
            .build()
    }
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideStoryRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        userPrefs: UserPreferencesRepository
    ): IStoryRepository {
        return StoryRepositoryImpl(firestore, auth, userPrefs)
    }

    @Provides
    @Singleton
    fun provideWordListRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): IWordListRepository {
        return WordListRepositoryImpl(firestore, auth)
    }


    @Provides
    @Singleton
    fun provideOpenAIApi(): OpenAIApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAIRepository(api: OpenAIApi): OpenAIRepository {
        return OpenAIRepository(api)
    }



}