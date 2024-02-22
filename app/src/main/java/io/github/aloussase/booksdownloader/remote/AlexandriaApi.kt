package io.github.aloussase.booksdownloader.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AlexandriaApi {
    @Multipart
    @POST("/api/v1/conversions")
    suspend fun convertBook(
        @Part("from") from: RequestBody,
        @Part("to") to: RequestBody,
        @Part book: MultipartBody.Part
    ): Response<AlexandriaResult>
}