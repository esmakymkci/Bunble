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
import coil.ImageLoader
import com.esma.bunble.data.repository.StoryRepositoryImpl
import com.esma.bunble.domain.repository.IStoryRepository

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
    fun provideStoryRepository(firestore: FirebaseFirestore): IStoryRepository {
        return StoryRepositoryImpl(firestore)
    }


}