package io.github.aloussase.booksdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.aloussase.booksdownloader.repositories.BookConversionRepository
import io.github.aloussase.booksdownloader.repositories.BookConversionRepositoryImpl
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepository
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideBookDownloadsRepository(
        @ApplicationContext context: Context
    ): BookDownloadsRepository {
        return BookDownloadsRepositoryImpl(
            context
        )
    }

    @Singleton
    @Provides
    fun provideBookConversionRepository(): BookConversionRepository {
        return BookConversionRepositoryImpl()
    }

}