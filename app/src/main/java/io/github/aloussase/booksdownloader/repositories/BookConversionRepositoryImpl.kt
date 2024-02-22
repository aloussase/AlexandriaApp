package io.github.aloussase.booksdownloader.repositories

import android.net.Uri
import android.util.Log
import io.github.aloussase.booksdownloader.Constants
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult
import io.github.aloussase.booksdownloader.remote.AlexandriaApi
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BookConversionRepositoryImpl(
    val alexandriaApi: AlexandriaApi
) : BookConversionRepository {
    companion object {
        const val TAG = "BookConversionRepositor"
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

            Log.e(TAG, "Conversion request failed with status: ${result.code()}")
            Log.e(TAG, "Response body: ${result.errorBody()?.string()}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "There was an error calling API: ${e.message}")
        }

        return ConversionResult.Error
    }
}