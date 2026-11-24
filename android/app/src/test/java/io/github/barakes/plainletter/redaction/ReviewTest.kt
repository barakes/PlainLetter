package io.github.barakes.plainletter.redaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewTest {

    private fun words(segments: List<ReviewSegment>) =
        segments.filterIsInstance<ReviewSegment.Word>().map { it.text }

    @Test
    fun splitsWordsAndKeepsPunctuationOutside() {
        val segments = Review.segments("שלום ישראל, מה נשמע?", emptyList())
        assertEquals(listOf("שלום", "ישראל", "מה", "נשמע"), words(segments))
        val rebuilt = segments.joinToString("") {
            when (it) {
                is ReviewSegment.Plain -> it.text
                is ReviewSegment.Word -> it.text
                is ReviewSegment.Hidden -> it.redaction.original
            }
        }
        assertEquals("שלום ישראל, מה נשמע?", rebuilt)
    }

    @Test
    fun keepsAmountsDatesAndEmailsAsOneWord() {
        val segments = Review.segments("250 ש\"ח עד 15/12/2026 ל-a.b@c.com", emptyList())
        assertEquals(listOf("250", "ש\"ח", "עד", "15/12/2026", "ל-a.b@c.com"), words(segments))
    }

    @Test
    fun keepsLineBreaksInPlainText() {
        val segments = Review.segments("שורה\nשנייה", emptyList())
        assertEquals(ReviewSegment.Plain("\n"), segments[1])
    }

    @Test
    fun showsAutomaticRedactionsAsHidden() {
        val text = "הטלפון 050-1234567 שלי"
        val result = Redactor.redact(text)
        val segments = Review.segments(text, result.redactions)
        val hidden = segments.filterIsInstance<ReviewSegment.Hidden>()
        assertEquals(1, hidden.size)
        assertEquals("050-1234567", hidden[0].redaction.original)
        assertEquals(listOf("הטלפון", "שלי"), words(segments))
    }

    @Test
    fun tappingAWordHidesIt() {
        val text = "שלום ישראל ישראלי"
        val word = Review.segments(text, emptyList())
            .filterIsInstance<ReviewSegment.Word>()
            .first { it.text == "ישראל" }
        val redactions = Review.hide(text, emptyList(), word.start, word.end)
        assertEquals(1, redactions.size)
        assertEquals(RedactionKind.MANUAL, redactions[0].kind)
        assertEquals("שלום [הוסתר] ישראלי", Redactor.apply(text, redactions).text)
    }

    @Test
    fun hidingOverSomethingHiddenDoesNothing() {
        val text = "הטלפון 050-1234567"
        val before = Redactor.redact(text).redactions
        val after = Review.hide(text, before, 7, 10)
        assertEquals(before, after)
    }

    @Test
    fun tappingAHiddenDetailRevealsIt() {
        val text = "תעודת זהות 123456782 וטלפון 050-1234567"
        val before = Redactor.redact(text).redactions
        val phone = before.first { it.kind == RedactionKind.PHONE }
        val after = Review.reveal(before, phone)
        assertEquals("תעודת זהות [ת\"ז] וטלפון 050-1234567", Redactor.apply(text, after).text)
    }

    @Test
    fun invalidRangeIsIgnored() {
        val text = "שלום"
        assertTrue(Review.hide(text, emptyList(), 3, 2).isEmpty())
        assertTrue(Review.hide(text, emptyList(), 0, 99).isEmpty())
    }
}
