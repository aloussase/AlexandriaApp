package io.github.aloussase.booksdownloader.domain.use_case

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult
import io.github.aloussase.booksdownloader.repository.FakeConversionsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ConvertBookUseCaseTest {
    private lateinit var convertBookUseCase: ConvertBookUseCase

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse("https://example.com") } returns mockk()

        convertBookUseCase = ConvertBookUseCase(FakeConversionsRepository())
    }

    @Test
    fun `converting a book to the same format returns an error`() {
        val filename = "book"
        val contents = "contents".toByteArray()

        val result = runBlocking {
            convertBookUseCase.execute(
                from = BookFormat.EPUB,
                to = BookFormat.EPUB,
                filename = filename,
                bytes = contents
            )
        }

        assertThat(result).isInstanceOf(ConversionResult.AlreadyInRightFormat::class.java)
    }

    @Test
    fun `converting a book providing an empty byte array returns an error`() {
        val filename = "book"
        val contents = byteArrayOf()

        val result = runBlocking {
            convertBookUseCase.execute(
                from = BookFormat.EPUB,
                to = BookFormat.PDF,
                filename = filename,
                bytes = contents
            )
        }

        assertThat(result).isInstanceOf(ConversionResult.EmptyContents::class.java)
    }

    @Test
    fun `converting a book providing an empty filename return an error`() {
        val filename = ""
        val contents = "contents".toByteArray()

        val result = runBlocking {
            convertBookUseCase.execute(
                from = BookFormat.EPUB,
                to = BookFormat.PDF,
                filename = filename,
                bytes = contents
            )
        }

        assertThat(result).isInstanceOf(ConversionResult.EmptyFilename::class.java)
    }

    @Test
    fun `converting a book providing correct data returns success`() {
        val filename = "book"
        val contents = "contents".toByteArray()

        val result = runBlocking {
            convertBookUseCase.execute(
                from = BookFormat.EPUB,
                to = BookFormat.PDF,
                filename = filename,
                bytes = contents
            )
        }

        assertThat(result).isInstanceOf(ConversionResult.Success::class.java)
    }
}