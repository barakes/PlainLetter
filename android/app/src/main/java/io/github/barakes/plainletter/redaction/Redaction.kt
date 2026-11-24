package io.github.barakes.plainletter.redaction

// One personal detail that was found in the letter and hidden.
// start and end are positions in the ORIGINAL text; the original value stays on the phone only,
// so the confirmation screen can show it and let the user bring it back.
data class Redaction(
    val start: Int,
    val end: Int,
    val kind: RedactionKind,
    val original: String,
)

// What kind of detail was hidden. The label is what replaces it in the text that leaves the phone.
enum class RedactionKind(val label: String) {
    ID_NUMBER("[ת\"ז]"),
    PHONE("[טלפון]"),
    EMAIL("[מייל]"),
    LONG_NUMBER("[מספר]"),
    MANUAL("[הוסתר]"),
}

// The result: the text with every detail replaced by its label, and the list of what was replaced.
data class RedactionResult(
    val text: String,
    val redactions: List<Redaction>,
)
