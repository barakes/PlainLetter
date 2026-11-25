package io.github.barakes.plainletter.server.explain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VerifierTest {

    private val letter = """
        שלום רב,
        לפי רישומינו, לא שולם חוב ארנונה בסך 1,234.50 ₪ עבור התקופה 01/07/2026 עד 31/10/2026.
        עליך להסדיר את התשלום עד 15/12/2026. אם החוב לא ישולם עד מועד זה, יתווספו לו ריבית והפרשי הצמדה.
        לבירורים ניתן לפנות למוקד בטלפון [טלפון].
    """.trimIndent()

    @Test
    fun exactQuoteIsFound() {
        assertTrue(Verifier.quoteFound(letter, "עליך להסדיר את התשלום עד 15/12/2026"))
    }

    @Test
    fun spacingAndLineBreaksDoNotMatter() {
        assertTrue(Verifier.quoteFound(letter, "שלום  רב, לפי רישומינו"))
    }

    @Test
    fun surroundingQuoteMarksAndPeriodDoNotMatter() {
        assertTrue(Verifier.quoteFound(letter, "\"יתווספו לו ריבית והפרשי הצמדה.\""))
    }

    @Test
    fun hebrewGershayimMatchPlainQuoteMarks() {
        assertTrue(Verifier.quoteFound("עליך לשלם 250 ש\"ח", "250 ש״ח"))
    }

    @Test
    fun hiddenDetailLabelsCanBeQuoted() {
        assertTrue(Verifier.quoteFound(letter, "בטלפון [טלפון]"))
    }

    @Test
    fun inventedQuoteIsNotFound() {
        assertFalse(Verifier.quoteFound(letter, "יש לשלם עד סוף החודש"))
    }

    @Test
    fun emptyQuoteIsNotFound() {
        assertFalse(Verifier.quoteFound(letter, " \"\" "))
    }

    @Test
    fun amountWithOrWithoutCommaMatches() {
        assertTrue(Verifier.numbersFound(letter, "החוב הוא 1234.50 שקלים."))
        assertTrue(Verifier.numbersFound(letter, "החוב הוא 1,234.50 שקלים."))
    }

    @Test
    fun dateWrittenInWordsMatchesItsNumbers() {
        assertTrue(Verifier.numbersFound(letter, "צריך לשלם עד 15 בדצמבר 2026."))
    }

    @Test
    fun numberThatIsNotInTheLetterFails() {
        assertFalse(Verifier.numbersFound(letter, "צריך לשלם 2,500 ש\"ח."))
        assertFalse(Verifier.numbersFound(letter, "צריך לשלם עד 20/12/2026."))
    }

    @Test
    fun verifyMarksEachPoint() {
        val explanation = Explanation(
            summary = "דרישת תשלום ארנונה.",
            points = listOf(
                Point("צריך לשלם 1,234.50 ₪.", "לא שולם חוב ארנונה בסך 1,234.50 ₪"),
                Point("צריך לשלם עד 20/12/2026.", "עליך להסדיר את התשלום עד 15/12/2026"),
                Point("אפשר לערער.", "ניתן לערער תוך 30 יום"),
            ),
            disclaimer = DISCLAIMER,
        )
        val result = Verifier.verify(letter, explanation)
        assertEquals(listOf(true, false, false), result.points.map { it.verified })
    }
}
