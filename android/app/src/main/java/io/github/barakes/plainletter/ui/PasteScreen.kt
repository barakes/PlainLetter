package io.github.barakes.plainletter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * המסך הראשון: המשתמש מדביק את הטקסט של המכתב.
 * המסך לא שומר מצב בעצמו. הטקסט והפעולות מגיעים מבחוץ.
 */
@Composable
fun PasteScreen(
    text: String,
    onTextChange: (String) -> Unit,
    onContinue: () -> Unit,
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
            text = "הדבק כאן את הטקסט של המכתב שקיבלת.",
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("הטקסט של המכתב") },
            textStyle = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "לפני השליחה תראה מה הוסתר, כמו תעודת זהות, טלפון ומספרי חשבון.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onContinue,
            enabled = text.isNotBlank(),
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
