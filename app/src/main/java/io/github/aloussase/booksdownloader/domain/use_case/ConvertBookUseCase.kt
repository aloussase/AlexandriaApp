package io.github.aloussase.booksdownloader.domain.use_case

import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult
import io.github.aloussase.booksdownloader.domain.repository.BookConversionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConvertBookUseCase(
    val conversions: BookConversionRepository
) {
    suspend fun execute(
        from: BookFormat,
        to: BookFormat,
        filename: String,
        bytes: ByteArray
    ): ConversionResult = withContext(Dispatchers.IO) {
        if (from == to) {
            return@withContext ConversionResult.AlreadyInRightFormat
        }

        if (filename.isEmpty()) {
            return@withContext ConversionResult.EmptyFilename
        }

        if (bytes.isEmpty()) {
            return@withContext ConversionResult.EmptyContents
        }

        conversions.convert(
            from,
            to,
            filename,
            bytes
        )
    }
}