package io.github.aloussase.booksdownloader.domain.use_case

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.empty
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test

class FilterBooksUseCaseTest {
    private lateinit var books: List<Book>

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse("https://example.com") } returns mockk()

        books = listOf(
            Book.empty().copy(extension = "pdf"),
            Book.empty().copy(extension = "epub"),
            Book.empty().copy(extension = "mobi"),
            Book.empty().copy(extension = "azw3"),
        )
    }


    @Test
    fun `filtering books returns all books when all filters are set`() {
        val filters = setOf(
            BookFormat.PDF,
            BookFormat.AZW3,
            BookFormat.MOBI,
            BookFormat.EPUB
        )

        val result = FilterBooksUseCase().execute(books, filters)

        assertThat(result).isEqualTo(books)
    }

    @Test
    fun `filtering books returns no books when no filter is set`() {
        val filters: Set<BookFormat> = emptySet()

        val result = FilterBooksUseCase().execute(books, filters)

        assertThat(result).isEmpty()
    }

    @Test
    fun `filtering books return all pdf books when only that format is set`() {
        val filters = setOf(BookFormat.PDF)

        val result = FilterBooksUseCase().execute(books, filters)

        assertThat(result).containsExactly(books[0])
    }

    @Test
    fun `filtering books when more than one filter is set returns the correct books`() {
        val filters = setOf(BookFormat.PDF, BookFormat.EPUB)

        val result = FilterBooksUseCase().execute(books, filters)

        assertThat(result).containsExactly(books[0], books[1])
    }
}