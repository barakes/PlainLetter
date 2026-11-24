package io.github.barakes.plainletter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.barakes.plainletter.explain.DeviceId
import io.github.barakes.plainletter.explain.ExplainClient
import io.github.barakes.plainletter.explain.ExplainResult
import io.github.barakes.plainletter.explain.Explanation

// Sends the redacted text to the server and shows the answer.
@Composable
fun ExplanationScreen(
    sentText: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var attempt by remember { mutableIntStateOf(0) }
    var result by remember(sentText) { mutableStateOf<ExplainResult?>(null) }

    val context = LocalContext.current
    LaunchedEffect(sentText, attempt) {
        result = null
        result = ExplainClient.explain(sentText, DeviceId.get(context))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val current = result) {
            null -> Loading()

            is ExplainResult.Failure -> {
                Text(text = current.message, style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { attempt++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    Text(text = "נסה שוב", style = MaterialTheme.typography.titleMedium)
                }
            }

            is ExplainResult.Success -> ExplanationContent(current.explanation)
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(text = "חזרה")
        }
    }
}

@Composable
private fun Loading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text(text = "מכין הסבר...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ExplanationContent(explanation: Explanation) {
    val colors = MaterialTheme.colorScheme

    Text(text = "מה כתוב במכתב", style = MaterialTheme.typography.headlineSmall)
    Text(
        text = explanation.summary,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 30.sp)
    )

    if (explanation.points.isNotEmpty()) {
        Text(text = "מה חשוב לדעת", style = MaterialTheme.typography.titleLarge)
        for (point in explanation.points) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = point.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp)
                    )
                    if (point.quote.isNotBlank()) {
                        Surface(
                            color = colors.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "מתוך המכתב: \"${point.quote}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    Text(
        text = explanation.disclaimer,
        style = MaterialTheme.typography.bodySmall,
        color = colors.onSurfaceVariant
    )
}
