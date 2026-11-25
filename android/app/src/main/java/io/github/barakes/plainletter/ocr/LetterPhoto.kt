package io.github.barakes.plainletter.ocr

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// The single place a camera photo of a letter is kept: a private, temporary file.
// It's deleted as soon as the text is read, so photos of letters don't pile up on the phone.
object LetterPhoto {

    private fun file(context: Context): File =
        File(File(context.cacheDir, "photos").apply { mkdirs() }, "letter.jpg")

    // Where the camera app should save the next photo.
    fun newUri(context: Context): Uri {
        val file = file(context)
        file.delete()
        return FileProvider.getUriForFile(context, "${context.packageName}.photos", file)
    }

    fun delete(context: Context) {
        file(context).delete()
    }
}
