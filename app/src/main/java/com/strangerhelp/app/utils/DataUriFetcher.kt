package com.strangerhelp.app.utils

import android.net.Uri
import android.util.Base64
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream

class DataUriFetcher(
    private val data: Uri,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val base64 = data.toString().substringAfter("base64,")
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val mimeType = data.toString().substringAfter("data:").substringBefore(";")

        return SourceResult(
            source = ImageSource(
                ByteArrayInputStream(bytes).source().buffer(),
                options.context
            ),
            mimeType = mimeType,
            dataSource = DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(
            data: Uri,
            options: Options,
            loader: ImageLoader
        ): Fetcher? {
            return if (data.scheme == "data") {
                DataUriFetcher(data, options)
            } else null
        }
    }
}
