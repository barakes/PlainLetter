package io.github.barakes.plainletter.redaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactorTest {

    private fun hidden(text: String) = Redactor.redact(text).text

    // ---------- What is hidden ----------

    @Test
    fun aValidIdNumberIsHidden() {
        assertEquals("ת\"ז [ת\"ז] של הנמען", hidden("ת\"ז 000000018 של הנמען"))
    }

    @Test
    fun phoneNumbersAreHiddenInEveryCommonForm() {
        assertEquals("[טלפון] או [טלפון]", hidden("050-1234567 או 04-8123456"))
        assertEquals("מוקד [טלפון].", hidden("מוקד 1-800-123-456."))
        assertEquals("חו\"ל [טלפון]", hidden("חו\"ל +972-52-7654321"))
    }

    @Test
    fun emailsAreHidden() {
        assertEquals("כתבו ל־[מייל]", hidden("כתבו ל־arnona@city.gov.il"))
    }

    @Test
    fun longNumbersAreHiddenEvenWithDashes() {
        assertEquals("מספר נכס [מספר]", hidden("מספר נכס 20-123-456789"))
    }

    @Test
    fun aNumberAtTheEndOfASentenceIsStillHidden() {
        assertEquals("דוח מספר [מספר].", hidden("דוח מספר 87654321."))
    }

    // ---------- What stays ----------

    @Test
    fun datesStayVisible() {
        assertEquals("עד לתאריך 15/12/2026", hidden("עד לתאריך 15/12/2026"))
        assertEquals("עד 15.12.26", hidden("עד 15.12.26"))
    }

    @Test
    fun amountsOfMoneyStayVisibleEvenWhenLong() {
        assertEquals("לתשלום 1,234.50 ₪", hidden("לתשלום 1,234.50 ₪"))
        assertEquals("קנס של 250000 ש\"ח", hidden("קנס של 250000 ש\"ח"))
        assertEquals("בסך 1500000", hidden("בסך 1500000"))
    }

    @Test
    fun shortNumbersStayVisible() {
        assertEquals("סעיף 12, תוך 30 יום", hidden("סעיף 12, תוך 30 יום"))
    }

    @Test
    fun aNumberedListIsNotJoinedIntoOneLongNumber() {
        assertEquals("1\n2\n3\n4\n5\n6", hidden("1\n2\n3\n4\n5\n6"))
    }

    // ---------- The list of what was hidden ----------

    @Test
    fun everyHiddenDetailKeepsItsOriginalForTheConfirmationScreen() {
        val result = Redactor.redact("ת\"ז 000000018, טלפון 050-1234567")

        assertEquals(2, result.redactions.size)
        assertEquals(RedactionKind.ID_NUMBER, result.redactions[0].kind)
        assertEquals("000000018", result.redactions[0].original)
        assertEquals(RedactionKind.PHONE, result.redactions[1].kind)
        assertEquals("050-1234567", result.redactions[1].original)
    }

    @Test
    fun theUserCanHideAWordAndRevealADetail() {
        val original = "לכבוד ישראל ישראלי, טלפון 050-1234567"
        val auto = Redactor.redact(original)
        val name = Redaction(6, 11, RedactionKind.MANUAL, "ישראל")

        // Hide the first name, and bring back the phone number.
        val changed = Redactor.apply(original, listOf(name))

        assertEquals("לכבוד [הוסתר] ישראלי, טלפון 050-1234567", changed.text)
        assertTrue(auto.text.contains("[טלפון]"))
        assertFalse(changed.text.contains("[טלפון]"))
    }
}
