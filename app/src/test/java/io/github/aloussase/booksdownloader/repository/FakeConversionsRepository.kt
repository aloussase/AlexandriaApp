package io.github.aloussase.booksdownloader.repository

import android.net.Uri
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult
import io.github.aloussase.booksdownloader.domain.repository.BookConversionRepository

class FakeConversionsRepository : BookConversionRepository {
    override suspend fun convert(
        from: BookFormat,
        to: BookFormat,
        filename: String,
        bytes: ByteArray
    ): ConversionResult {
        return ConversionResult.Success(
            Uri.parse("https://example.com")
        )
    }
}