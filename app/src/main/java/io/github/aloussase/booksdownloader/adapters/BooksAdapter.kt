package io.github.aloussase.booksdownloader.adapters

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import io.github.aloussase.booksdownloader.GlideApp
import io.github.aloussase.booksdownloader.GlideRequest
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.cover
import io.github.aloussase.booksdownloader.data.parse

class BooksAdapter : RecyclerView.Adapter<BooksAdapter.BooksViewHolder>() {

    inner class BooksViewHolder(itemView: View) : ViewHolder(itemView)


    private val diffCallback = object : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var books: List<Book>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private lateinit var onItemDownloadListener: OnDownloadItemListener

    fun setOnItemDownloadListener(listener: OnDownloadItemListener) {
        onItemDownloadListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        return BooksViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.books_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        val book = books[position]
        holder.itemView.apply {

            val btnDownload = findViewById<Button>(R.id.btnDownload)
            btnDownload.setOnClickListener { onItemDownloadListener(book) }
            btnDownload.text = book.extension.uppercase()
            btnDownload.background = book.getDrawable(resources, context)

            val tvTitle = findViewById<TextView>(R.id.tvTitle)
            tvTitle.text = book.title

            val tvAuthors = findViewById<TextView>(R.id.tvAuthors)
            tvAuthors.text = book.authors.joinToString(", ")

            val ivCover = findViewById<ImageView>(R.id.ivBookCover)
            GlideApp
                .with(this)
                .load(book.cover())
                .withPlaceholder(context)
                .into(ivCover)
        }
    }
}

typealias OnDownloadItemListener = (Book) -> Unit

private fun Book.getDrawable(resources: Resources, context: Context): Drawable {
    val id = when (BookFormat.parse(extension)) {
        BookFormat.PDF -> R.drawable.pdf_download_button
        BookFormat.EPUB -> R.drawable.epub_download_button
        BookFormat.AZW3 -> R.drawable.azw3_download_button
        BookFormat.MOBI -> R.drawable.mobi_download_button
    }
    return ResourcesCompat.getDrawable(
        resources,
        id,
        context.theme
    )!!
}

private fun <T> GlideRequest<T>.withPlaceholder(context: Context): GlideRequest<T> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val circularProgressDrawable = CircularProgressDrawable(context).apply {
            arrowEnabled = true
            centerRadius = 30f
        }

        circularProgressDrawable.start()

        placeholder(circularProgressDrawable)
    } else {
        this
    }
}