package io.github.aloussase.booksdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepository
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesBookDownloadsRepository(
        @ApplicationContext context: Context
    ): BookDownloadsRepository {
        return BookDownloadsRepositoryImpl(
            context
        )
    }
}