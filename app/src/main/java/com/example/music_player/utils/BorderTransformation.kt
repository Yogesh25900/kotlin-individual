package com.example.music_player.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableResource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import java.security.MessageDigest

class BorderTransformation(private val borderWidth: Float, private val borderColor: Int) : Transformation<Bitmap> {

     fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap? {
        val borderBitmap = addBorderToBitmap(toTransform, borderWidth, borderColor)
        return borderBitmap
    }

    private fun addBorderToBitmap(bitmap: Bitmap, borderWidth: Float, borderColor: Int): Bitmap? {
        val width = bitmap.width + (borderWidth * 2).toInt()
        val height = bitmap.height + (borderWidth * 2).toInt()

        // Create a new bitmap with border
        val borderedBitmap = bitmap.config?.let { Bitmap.createBitmap(width, height, it) }
        val canvas = borderedBitmap?.let { Canvas(it) }

        // Draw the original image
        if (canvas != null) {
            canvas.drawBitmap(bitmap, borderWidth, borderWidth, null)
        }

        // Create paint for the border
        val paint = Paint()
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth

        // Draw the border
        if (canvas != null) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

        return borderedBitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("BorderTransformation" + borderWidth + borderColor).toByteArray(Key.CHARSET))
    }

    override fun transform(
        context: Context,
        resource: Resource<Bitmap>,
        outWidth: Int,
        outHeight: Int
    ): Resource<Bitmap> {
        TODO("Not yet implemented")
    }
}
