package io.github.barakes.plainletter.server.explain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class FakeProviderTest {

    private val provider = FakeProvider()

    @Test
    fun quotesTheFirstSentenceOfTheLetter() {
        val result = provider.explain("שלום רב. עליך לשלם 250 ש\"ח עד 15/12/2026.")
        assertEquals("שלום רב", result.points[0].quote)
    }

    @Test
    fun skipsEmptyLinesAtTheStart() {
        val result = provider.explain("\n\n  הודעה על חוב\nפרטים נוספים")
        assertEquals("הודעה על חוב", result.points[0].quote)
    }

    @Test
    fun alwaysIncludesTheDisclaimer() {
        val result = provider.explain("מכתב")
        assertFalse(result.disclaimer.isBlank())
    }

    @Test
    fun controllerPassesTheTextToTheProvider() {
        val controller = ExplainController(provider)
        val result = controller.explain(ExplainRequest("בדיקה ראשונה. שורה שנייה"))
        assertEquals("בדיקה ראשונה", result.points[0].quote)
    }
}
