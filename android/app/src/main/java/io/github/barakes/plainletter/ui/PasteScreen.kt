package io.github.barakes.plainletter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// The first screen: the user pastes the letter's text, or picks a photo of it.
// The screen keeps no state of its own. The text and the actions come from outside.
@Composable
fun PasteScreen(
    text: String,
    onTextChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onContinue: () -> Unit,
    reading: Boolean,
    message: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "מה רוצים ממני?",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "בחר תמונה של המכתב, או הדבק כאן את הטקסט שלו.",
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedButton(
            onClick = onPickPhoto,
            enabled = !reading,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(text = "בחר תמונה של מכתב", style = MaterialTheme.typography.titleMedium)
        }

        if (reading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(text = "קורא את המכתב...", style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (message != null) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            enabled = !reading,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("הטקסט של המכתב") },
            textStyle = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "התמונה נשארת בטלפון. לפני השליחה תראה מה הוסתר, כמו תעודת זהות, טלפון ומספרי חשבון.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onContinue,
            enabled = text.isNotBlank() && !reading,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(
                text = "המשך",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
