package io.github.aloussase.booksdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.aloussase.booksdownloader.Constants
import io.github.aloussase.booksdownloader.remote.AlexandriaApi
import io.github.aloussase.booksdownloader.repositories.BookConversionRepository
import io.github.aloussase.booksdownloader.repositories.BookConversionRepositoryImpl
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepository
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepositoryImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
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
    fun provideBookConversionRepository(alexandriaApi: AlexandriaApi): BookConversionRepository {
        return BookConversionRepositoryImpl(alexandriaApi)
    }

    @Singleton
    @Provides
    fun provideAlexandriaApi(): AlexandriaApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.ALEXANDRIA_API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(AlexandriaApi::class.java)
    }

}