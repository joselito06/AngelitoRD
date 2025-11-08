package com.example.angelitord.utils

import com.example.angelitord.repository.AngelitoRepository
import com.example.angelitord.repository.UnitOfWork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

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
    fun provideUnitOfWork(
        angelitoRepository: AngelitoRepository
    ): UnitOfWork {
        return UnitOfWork(angelitoRepository = angelitoRepository)
    }

}