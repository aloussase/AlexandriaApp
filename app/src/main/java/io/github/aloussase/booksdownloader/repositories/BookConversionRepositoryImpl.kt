package io.github.aloussase.booksdownloader.repositories

import android.net.Uri
import io.github.aloussase.booksdownloader.Constants
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult
import io.github.aloussase.booksdownloader.domain.repository.BookConversionRepository
import io.github.aloussase.booksdownloader.remote.AlexandriaApi
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BookConversionRepositoryImpl(
    val alexandriaApi: AlexandriaApi
) : BookConversionRepository {
    companion object {
        const val TAG = "BookConversionRepository"
    }

    override suspend fun convert(
        from: BookFormat,
        to: BookFormat,
        filename: String,
        bytes: ByteArray
    ): ConversionResult {
        val mediaType = when (from) {
            BookFormat.PDF -> "application/pdf"
            BookFormat.EPUB -> "application/epub+zip"
            BookFormat.AZW3 -> "application/x-mobipocket-ebook"
            BookFormat.MOBI -> "application/x-mobipocket-ebook"
        }

        val file = MultipartBody.Part.createFormData(
            "book",
            filename,
            MultipartBody.create(MediaType.get(mediaType), bytes)
        )

        try {
            val result = alexandriaApi.convertBook(
                RequestBody.create(MediaType.parse("text/plain"), from.name),
                RequestBody.create(MediaType.parse("text/plain"), to.name),
                file
            )

            if (result.isSuccessful) {
                result.body()?.let { body ->
                    val uri = Uri.parse("${Constants.ALEXANDRIA_API_BASE_URL}${body.data.path}")
                    return ConversionResult.Success(uri)
                }
            }

            if (result.code() == 429) {
                return ConversionResult.LimitReached
            }

            return ConversionResult.Error
        } catch (e: Exception) {
            return ConversionResult.Error
        }
    }
}