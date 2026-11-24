package io.github.barakes.plainletter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.barakes.plainletter.redaction.Redactor
import io.github.barakes.plainletter.ui.PasteScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                // האפליקציה בעברית בלבד, לכן מימין לשמאל גם כשהטלפון באנגלית
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PlainLetterApp(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun PlainLetterApp(modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    var showPreview by rememberSaveable { mutableStateOf(false) }

    if (showPreview) {
        BackHandler { showPreview = false }
        RedactedPreview(
            text = text,
            onBack = { showPreview = false },
            modifier = modifier
        )
    } else {
        PasteScreen(
            text = text,
            onTextChange = { text = it },
            onContinue = { showPreview = true },
            modifier = modifier
        )
    }
}

/** זמני: יוחלף בשלב הבא במסך האישור עם הסתרה בנגיעה. */
@Composable
private fun RedactedPreview(text: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val result = remember(text) { Redactor.redact(text) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "זה מה שיישלח:", style = MaterialTheme.typography.headlineSmall)
        Text(text = result.text, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "הוסתרו ${result.redactions.size} פרטים",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = onBack) { Text("חזרה") }
    }
}
