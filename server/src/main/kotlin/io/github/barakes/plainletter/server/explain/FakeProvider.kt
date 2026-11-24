package io.github.barakes.plainletter.server.explain

import org.springframework.stereotype.Component

// A fake explanation, so the app and the server can be built and tested before any AI is connected.
// It never sends the text anywhere.
@Component
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

    companion object {
        const val DISCLAIMER = "ההסבר נועד לעזור להבין את המכתב, ואינו ייעוץ משפטי. במקרה של ספק, פנה לגוף ששלח את המכתב."
    }
}
