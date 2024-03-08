package io.github.aloussase.booksdownloader.domain.use_case

import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.parse

class FilterBooksUseCase {
    fun execute(
        books: List<Book>,
        filters: Set<BookFormat>
    ): List<Book> {
        return books.filter { book ->
            BookFormat.parse(book.extension) in filters
        }
    }
}