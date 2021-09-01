package com.example.filesgo.di

import android.content.ContentResolver
import android.content.Context
import com.example.filesgo.repository.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providesRepository(contentResolver: ContentResolver): FileRepository {
        return FileRepository(contentResolver)
    }

    @Provides
    @Singleton
    fun providesContentResolver(@ApplicationContext context: Context): ContentResolver{
        return context.contentResolver
    }
}