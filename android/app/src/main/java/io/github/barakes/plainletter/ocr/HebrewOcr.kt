package io.github.barakes.plainletter.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

// Reads the text of a letter from a photo, on the phone itself.
// The photo is never sent anywhere: only the text, after the user approves it, goes to the server.
object HebrewOcr {

    private const val TAG = "HebrewOcr"
    private const val LANGUAGE = "heb"
    private const val ASSET = "tessdata/heb.traineddata"

    // Bigger photos don't read better, they only take longer and use more memory.
    private const val MAX_SIDE = 2500

    suspend fun read(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.Default) {
        runCatching {
            val dataPath = prepareLanguageData(context)
            val bitmap = loadBitmap(context, uri)
            val tess = TessBaseAPI()
            try {
                if (!tess.init(dataPath, LANGUAGE)) throw IOException("Tesseract could not start")
                tess.setImage(bitmap)
                clean(tess.getUTF8Text() ?: "")
            } finally {
                tess.recycle()
                bitmap.recycle()
            }
        }.onFailure { Log.w(TAG, "Could not read the photo", it) }
    }

    // Tesseract needs the language file as a real file, in a folder named "tessdata".
    // It ships inside the app, so it's copied out once, on first use.
    private fun prepareLanguageData(context: Context): String {
        val root = File(context.filesDir, "tesseract")
        val tessdata = File(root, "tessdata").apply { mkdirs() }
        val target = File(tessdata, "$LANGUAGE.traineddata")
        if (!target.exists()) {
            // Copy to a temporary name first, so a copy that was cut off is never used.
            val partial = File(tessdata, "$LANGUAGE.traineddata.part")
            context.assets.open(ASSET).use { input ->
                partial.outputStream().use { output -> input.copyTo(output) }
            }
            if (!partial.renameTo(target)) throw IOException("Could not prepare Hebrew data")
        }
        return root.absolutePath
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        val resolver = context.contentResolver

        // First only measure the photo, then load it at a size that's enough for reading.
        // (Measuring returns no picture, only the sizes in "bounds".)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val stream = resolver.openInputStream(uri) ?: throw IOException("Could not open the photo")
        stream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Not a picture")
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Could not read the photo")

        // Phones often save photos sideways and note the right direction separately. Turn it upright.
        val degrees = resolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        if (degrees == 0f) return bitmap
        val rotated = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true
        )
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    // Trims every line and leaves at most one empty line between paragraphs.
    internal fun clean(text: String): String =
        text.lines()
            .map { it.trim() }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
}
