package io.github.aloussase.booksdownloader.domain.use_case

import android.net.Uri
import io.github.aloussase.booksdownloader.repository.FakeConversionsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before

class ConvertBookUseCaseTest {
    private lateinit var convertBookUseCase: ConvertBookUseCase

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse("https://example.com") } returns mockk()

        convertBookUseCase = ConvertBookUseCase(FakeConversionsRepository())
    }

    fun `converting a book to the same format returns an error`() {

    }

    fun `converting a book providing an empty byte array returns an error`() {

    }

    fun `converting a book providing an empty filename return an error`() {

    }

    fun `converting a book providing correct data returns success`() {
    }
}