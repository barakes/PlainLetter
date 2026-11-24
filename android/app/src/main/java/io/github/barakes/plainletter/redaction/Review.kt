package io.github.barakes.plainletter.redaction

// One piece of the letter as the confirmation screen shows it.
sealed interface ReviewSegment {
    // Spaces, punctuation and line breaks. Shown as is, not tappable.
    data class Plain(val text: String) : ReviewSegment

    // A word the user can tap to hide. start and end are positions in the original text.
    data class Word(val start: Int, val end: Int, val text: String) : ReviewSegment

    // A hidden detail. The user can tap it to bring it back.
    data class Hidden(val redaction: Redaction) : ReviewSegment
}

// The logic behind the confirmation screen, kept apart from the UI so it can be tested.
object Review {

    // Letters and digits, with inner marks that belong to the word:
    // ש"ח, 15/12/2026, a.b@c.com, ת.ז. stays "ת.ז" without the last dot.
    private val WORD = Regex("""[\p{L}\p{N}]+(?:['"״׳.\-/@_][\p{L}\p{N}]+)*""")

    fun segments(original: String, redactions: List<Redaction>): List<ReviewSegment> {
        val out = mutableListOf<ReviewSegment>()
        var position = 0
        for (redaction in redactions.sortedBy { it.start }) {
            if (redaction.start < position) continue
            addWords(original, position, redaction.start, out)
            out += ReviewSegment.Hidden(redaction)
            position = redaction.end
        }
        addWords(original, position, original.length, out)
        return out
    }

    // Hides the part start..end of the original. Does nothing if it touches something already hidden.
    fun hide(original: String, redactions: List<Redaction>, start: Int, end: Int): List<Redaction> {
        if (start < 0 || end > original.length || start >= end) return redactions
        if (redactions.any { it.start < end && start < it.end }) return redactions
        val added = Redaction(start, end, RedactionKind.MANUAL, original.substring(start, end))
        return (redactions + added).sortedBy { it.start }
    }

    // Brings a hidden detail back into the text.
    fun reveal(redactions: List<Redaction>, redaction: Redaction): List<Redaction> =
        redactions - redaction

    private fun addWords(text: String, from: Int, to: Int, out: MutableList<ReviewSegment>) {
        var position = from
        for (match in WORD.findAll(text.substring(from, to))) {
            val start = from + match.range.first
            val end = from + match.range.last + 1
            if (start > position) out += ReviewSegment.Plain(text.substring(position, start))
            out += ReviewSegment.Word(start, end, match.value)
            position = end
        }
        if (to > position) out += ReviewSegment.Plain(text.substring(position, to))
    }
}
