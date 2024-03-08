package io.github.aloussase.booksdownloader.domain.repository

import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult

interface BookConversionRepository {
    /**
     * Convert from the specified format to the target format.
     */
    suspend fun convert(
        from: BookFormat,
        to: BookFormat,
        filename: String,
        bytes: ByteArray
    ): ConversionResult
}