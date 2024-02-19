package io.github.aloussase.booksdownloader.repositories

import android.net.Uri
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult

class BookConversionRepositoryImpl : BookConversionRepository {
    override suspend fun convert(
        from: BookFormat,
        to: BookFormat,
        bytes: ByteArray
    ): ConversionResult {
        // TODO: Hit api endpoint.
        return ConversionResult(
            Uri.parse(
                "https://file-examples.com/wp-content/storage/2017/10/file-sample_150kB.pdf"
            )
        )
    }
}