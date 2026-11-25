package io.github.barakes.plainletter

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.github.barakes.plainletter.ocr.HebrewOcr
import io.github.barakes.plainletter.ocr.LetterPhoto
import io.github.barakes.plainletter.redaction.Redaction
import io.github.barakes.plainletter.redaction.RedactionKind
import io.github.barakes.plainletter.redaction.Redactor
import io.github.barakes.plainletter.ui.ExplanationScreen
import io.github.barakes.plainletter.ui.PasteScreen
import io.github.barakes.plainletter.ui.ReviewScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                // The app is Hebrew only, so right-to-left even when the phone is set to English.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PlainLetterApp(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

private enum class Screen { PASTE, REVIEW, RESULT }

// Keeps the hidden details when the phone is rotated.
private val RedactionsSaver = listSaver<List<Redaction>, Any>(
    save = { list -> list.flatMap { listOf(it.start, it.end, it.kind.name, it.original) } },
    restore = { flat ->
        flat.chunked(4).map {
            Redaction(it[0] as Int, it[1] as Int, RedactionKind.valueOf(it[2] as String), it[3] as String)
        }
    }
)

@Composable
fun PlainLetterApp(modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    var screen by rememberSaveable { mutableStateOf(Screen.PASTE) }
    var redactions by rememberSaveable(stateSaver = RedactionsSaver) {
        mutableStateOf(emptyList<Redaction>())
    }

    // Reading a photo. Neither the camera nor the photo picker needs a permission,
    // and the photo stays on the phone: only the text, after the user approves it, is sent.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reading by remember { mutableStateOf(false) }
    var photoMessage by remember { mutableStateOf<String?>(null) }

    fun readPhoto(uri: Uri, afterReading: () -> Unit = {}) {
        scope.launch {
            reading = true
            photoMessage = null
            val result = HebrewOcr.read(context, uri)
            afterReading()
            reading = false
            val found = result.getOrNull()
            photoMessage = when {
                found == null -> "לא הצלחנו לקרוא את התמונה. נסה תמונה אחרת, או הדבק את הטקסט."
                found.isBlank() -> "לא מצאנו טקסט בתמונה. נסה תמונה חדה יותר, באור טוב."
                else -> {
                    text = found
                    "זה הטקסט שזיהינו. כדאי לעבור עליו ולתקן מילים שזוהו לא נכון."
                }
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) readPhoto(uri)
    }

    // Kept across screen rotation, because the camera app may rotate the phone.
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = cameraUri
        if (saved && uri != null) {
            readPhoto(uri, afterReading = { LetterPhoto.delete(context) })
        } else {
            LetterPhoto.delete(context)
        }
    }

    when (screen) {
        Screen.PASTE -> PasteScreen(
            text = text,
            onTextChange = { text = it },
            onTakePhoto = {
                val uri = LetterPhoto.newUri(context)
                cameraUri = uri
                try {
                    camera.launch(uri)
                } catch (e: ActivityNotFoundException) {
                    photoMessage = "לא נמצאה מצלמה בטלפון. אפשר לבחור תמונה קיימת, או להדביק את הטקסט."
                }
            },
            onPickPhoto = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onContinue = {
                photoMessage = null
                redactions = Redactor.redact(text).redactions
                screen = Screen.REVIEW
            },
            reading = reading,
            message = photoMessage,
            modifier = modifier
        )

        Screen.REVIEW -> {
            BackHandler { screen = Screen.PASTE }
            ReviewScreen(
                original = text,
                redactions = redactions,
                onRedactionsChange = { redactions = it },
                onBack = { screen = Screen.PASTE },
                onConfirm = { screen = Screen.RESULT },
                modifier = modifier
            )
        }

        Screen.RESULT -> {
            BackHandler { screen = Screen.REVIEW }
            ExplanationScreen(
                sentText = Redactor.apply(text, redactions).text,
                onBack = { screen = Screen.REVIEW },
                modifier = modifier
            )
        }
    }
}
