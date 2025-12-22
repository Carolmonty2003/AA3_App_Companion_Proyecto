package com.example.valorant_companion.utils

import android.graphics.BitmapFactory
import android.widget.ImageView
import java.net.HttpURLConnection
import java.net.URL

object SimpleImageLoader {

    fun load(url: String?, imageView: ImageView, fallbackResId: Int, onDone: (() -> Unit)? = null) {
        if (url.isNullOrBlank()) {
            imageView.setImageResource(fallbackResId)
            onDone?.invoke()
            return
        }

        // Para evitar bugs de reciclaje en RecyclerView
        imageView.tag = url
        imageView.setImageResource(fallbackResId)

        Thread {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    doInput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                    connect()
                }

                val bitmap = BitmapFactory.decodeStream(connection.inputStream)

                imageView.post {
                    // solo setea si sigue siendo el mismo item (tag)
                    if (imageView.tag == url)
                        imageView.setImageBitmap(bitmap)
                    onDone?.invoke()
                }
            } catch (_: Exception) {
                imageView.post {
                    if (imageView.tag == url)
                        imageView.setImageResource(fallbackResId)
                    onDone?.invoke()
                }
            }
        }.start()
    }
}
