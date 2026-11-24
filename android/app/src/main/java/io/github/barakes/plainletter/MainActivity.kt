package io.github.barakes.plainletter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.github.barakes.plainletter.redaction.Redaction
import io.github.barakes.plainletter.redaction.RedactionKind
import io.github.barakes.plainletter.redaction.Redactor
import io.github.barakes.plainletter.ui.ExplanationScreen
import io.github.barakes.plainletter.ui.PasteScreen
import io.github.barakes.plainletter.ui.ReviewScreen

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

    when (screen) {
        Screen.PASTE -> PasteScreen(
            text = text,
            onTextChange = { text = it },
            onContinue = {
                redactions = Redactor.redact(text).redactions
                screen = Screen.REVIEW
            },
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
