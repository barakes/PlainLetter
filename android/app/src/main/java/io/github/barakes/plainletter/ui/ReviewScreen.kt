package io.github.barakes.plainletter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.barakes.plainletter.redaction.Redaction
import io.github.barakes.plainletter.redaction.Review
import io.github.barakes.plainletter.redaction.ReviewSegment

// The confirmation screen: the letter as it will leave the phone.
// Tap a word to hide it, tap a hidden detail to bring it back.
@Composable
fun ReviewScreen(
    original: String,
    redactions: List<Redaction>,
    onRedactionsChange: (List<Redaction>) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val segments = remember(original, redactions) { Review.segments(original, redactions) }

    val colors = MaterialTheme.colorScheme
    val pressed = SpanStyle(background = colors.primaryContainer)
    val wordStyles = TextLinkStyles(pressedStyle = pressed)
    val hiddenStyles = TextLinkStyles(
        style = SpanStyle(
            background = colors.secondaryContainer,
            color = colors.onSecondaryContainer,
            fontWeight = FontWeight.Bold
        ),
        pressedStyle = pressed
    )

    val letter = buildAnnotatedString {
        for (segment in segments) {
            when (segment) {
                is ReviewSegment.Plain -> append(segment.text)

                is ReviewSegment.Word -> withLink(
                    LinkAnnotation.Clickable(
                        tag = "word-${segment.start}",
                        styles = wordStyles,
                        linkInteractionListener = {
                            onRedactionsChange(
                                Review.hide(original, redactions, segment.start, segment.end)
                            )
                        }
                    )
                ) { append(segment.text) }

                is ReviewSegment.Hidden -> withLink(
                    LinkAnnotation.Clickable(
                        tag = "hidden-${segment.redaction.start}",
                        styles = hiddenStyles,
                        linkInteractionListener = {
                            onRedactionsChange(Review.reveal(redactions, segment.redaction))
                        }
                    )
                ) { append(" " + segment.redaction.kind.label + " ") }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "בדוק מה יישלח",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "גע במילה כדי להסתיר אותה. גע בפרט מוסתר כדי להחזיר אותו.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 34.sp)
            )
        }
        Text(
            text = hiddenCountLine(redactions.size),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(text = "אישור", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(text = "חזרה לעריכת הטקסט")
        }
    }
}

private fun hiddenCountLine(count: Int): String = when (count) {
    0 -> "לא מוסתר אף פרט"
    1 -> "מוסתר פרט אחד"
    else -> "מוסתרים $count פרטים"
}
