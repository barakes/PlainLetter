package io.github.barakes.plainletter.redaction

// An Israeli ID number has 9 digits, and the last one is a check digit.
// Checking it tells a real ID number apart from any other 9-digit number (a ticket or an account number).
object IsraeliId {

    fun isValid(digits: String): Boolean {
        if (digits.length !in 5..9 || digits.any { !it.isDigit() }) {
            return false
        }
        // Shorter numbers are written without their leading zeros.
        val id = digits.padStart(9, '0')
        var sum = 0
        for ((index, char) in id.withIndex()) {
            // Digits are multiplied by 1, 2, 1, 2, ...; a two-digit result counts as the sum of its digits.
            val product = char.digitToInt() * (if (index % 2 == 0) 1 else 2)
            sum += if (product > 9) product - 9 else product
        }
        return sum % 10 == 0
    }
}
