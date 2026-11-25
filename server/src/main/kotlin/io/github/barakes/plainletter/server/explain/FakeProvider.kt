package io.github.barakes.plainletter.server.explain

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

// A fake explanation, so the app and the server can be built and tested before any AI is connected.
// It never sends the text anywhere. Used unless explain.provider is set to something else.
@Component
@ConditionalOnProperty(name = ["explain.provider"], havingValue = "fake", matchIfMissing = true)
class FakeProvider : ExplanationProvider {

    override fun explain(text: String): Explanation {
        val firstSentence = text.trim()
            .split('\n', '.', '?', '!')
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?: ""
        return Explanation(
            summary = "זה הסבר לדוגמה. השרת עוד לא מחובר לבינה מלאכותית.",
            points = listOf(
                Point(text = "כך נראית נקודה בהסבר, עם המילים מהמכתב שעליהן היא מבוססת.", quote = firstSentence.take(80)),
            ),
            disclaimer = DISCLAIMER,
        )
    }
}
