package io.github.barakes.plainletter.redaction

// Finds personal details in the text of a letter and hides them, before anything leaves the phone.
// It hides: ID numbers, phone numbers, emails, and any other long number (accounts, tickets, car plates).
// It keeps: dates and amounts of money, because the explanation needs them.
// Names and addresses are hard to find automatically; the user hides those by touching them
// on the confirmation screen.
object Redactor {

    // A number doesn't continue into a date or an amount (15/12/2026, 1,234.50). A period or comma counts
    // as part of a number only when a digit follows it, so the period that ends a sentence is not.
    private const val BEFORE = """(?<![\d+]|\d[,./ -])"""
    private const val AFTER = """(?![\d]|[,./]\d)"""

    private val EMAIL = Regex("""[\w.+-]+@[\w-]+(\.[\w-]+)+""")

    // Israeli phones: 05X-XXXXXXX (mobile), 0X-XXXXXXX (landline), 1-800/1-700 numbers, or +972 instead of the 0.
    // Dashes and spaces between the digits are allowed.
    private val PHONE = Regex(
        """$BEFORE(?:\+972[ -]?|0)(?:5\d|[2-489]|7\d)[ -]?\d{3}[ -]?\d{4}$AFTER""" +
            """|${BEFORE}1[ -]?[78]00[ -]?\d{2,3}[ -]?\d{3}$AFTER""",
    )

    // A run of digits that may be split by single dashes or spaces: 123456789, 12-345-678, 20-123-456789.
    // Never across a line break, so a numbered list doesn't turn into one long number.
    private val NUMBER = Regex("""$BEFORE\d+(?:[ -]\d+)*$AFTER""")

    // Words right before a number that mean it is money, or right after it.
    private val MONEY_AFTER = Regex("""^\s*(₪|ש"ח|ש״ח|שקל|ש\b)""")
    private val MONEY_BEFORE = Regex("""(₪|סך|סכום|בסך)\s*$""")

    // A number that is this long, or longer, is never left visible: it can identify a person or a case.
    private const val LONG_DIGITS = 6

    fun redact(text: String): RedactionResult {
        val found = mutableListOf<Redaction>()

        fun add(start: Int, end: Int, kind: RedactionKind) {
            // An earlier, more specific rule may already cover these characters.
            if (found.none { start < it.end && it.start < end }) {
                found += Redaction(start, end, kind, text.substring(start, end))
            }
        }

        val numbers = NUMBER.findAll(text).filterNot { isMoney(text, it.range.first, it.range.last + 1) }.toList()

        // Most specific first: emails, ID numbers (many start with 0 and look like a phone), phones, other numbers.
        EMAIL.findAll(text).forEach { add(it.range.first, it.range.last + 1, RedactionKind.EMAIL) }
        for (match in numbers) {
            val digits = match.value.filter { it.isDigit() }
            if (digits.length == 9 && IsraeliId.isValid(digits)) {
                add(match.range.first, match.range.last + 1, RedactionKind.ID_NUMBER)
            }
        }
        PHONE.findAll(text).forEach { add(it.range.first, it.range.last + 1, RedactionKind.PHONE) }
        for (match in numbers) {
            if (match.value.count { it.isDigit() } >= LONG_DIGITS) {
                add(match.range.first, match.range.last + 1, RedactionKind.LONG_NUMBER)
            }
        }

        return apply(text, found)
    }

    // Builds the text that leaves the phone from the original and a list of hidden parts.
    // The confirmation screen calls it again when the user hides or reveals something.
    fun apply(original: String, redactions: List<Redaction>): RedactionResult {
        val sorted = redactions.sortedBy { it.start }
        val result = StringBuilder()
        var position = 0
        for (redaction in sorted) {
            result.append(original, position, redaction.start)
            result.append(redaction.kind.label)
            position = redaction.end
        }
        result.append(original, position, original.length)
        return RedactionResult(result.toString(), sorted)
    }

    private fun isMoney(text: String, start: Int, end: Int): Boolean =
        MONEY_AFTER.containsMatchIn(text.substring(end, minOf(text.length, end + 6))) ||
            MONEY_BEFORE.containsMatchIn(text.substring(maxOf(0, start - 6), start))
}
