package io.github.barakes.plainletter.server.explain

// Checks the explanation against the letter, so we don't rely only on the AI following its instructions.
// A point is verified when:
// - its quote really appears in the letter (ignoring spacing and the different kinds of quote marks), and
// - every number in its text (amounts, dates, days) appears in the letter too.
// Points that fail aren't removed: the app shows them with a warning to check the letter itself.
object Verifier {

    private val NUMBER = Regex("""\d+""")
    private val THOUSANDS_COMMA = Regex("""(?<=\d),(?=\d{3}(?!\d))""")
    private val SPACES = Regex("""\s+""")
    private const val EDGE_MARKS = " \"'.,:;!?()"

    fun verify(letter: String, explanation: Explanation): Explanation =
        explanation.copy(points = explanation.points.map { it.copy(verified = isSupported(letter, it)) })

    fun isSupported(letter: String, point: Point): Boolean =
        quoteFound(letter, point.quote) && numbersFound(letter, point.text)

    fun quoteFound(letter: String, quote: String): Boolean {
        val wanted = normalize(quote).trim { it in EDGE_MARKS }
        if (wanted.length < 2) return false
        return normalize(letter).contains(wanted)
    }

    fun numbersFound(letter: String, text: String): Boolean {
        val inLetter = numbers(letter)
        return numbers(text).all { it in inLetter }
    }

    // "1,234.50" and "1234.50" are the same number; "01" and "1" too.
    private fun numbers(text: String): Set<String> =
        NUMBER.findAll(text.replace(THOUSANDS_COMMA, ""))
            .map { it.value.trimStart('0').ifEmpty { "0" } }
            .toSet()

    // Same text written slightly differently: Hebrew gershayim or curly quotes, long dashes, line breaks.
    private fun normalize(text: String): String =
        text.replace('״', '"').replace('“', '"').replace('”', '"').replace('„', '"')
            .replace('׳', '\'').replace('‘', '\'').replace('’', '\'')
            .replace('–', '-').replace('—', '-').replace('־', '-')
            .replace(SPACES, " ")
            .trim()
}
