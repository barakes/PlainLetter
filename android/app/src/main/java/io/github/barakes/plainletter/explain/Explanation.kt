package io.github.barakes.plainletter.explain

// The explanation as the server sends it. Same shape as on the server.
data class Explanation(
    val summary: String,
    val points: List<Point>,
    val disclaimer: String,
)

data class Point(
    val text: String,
    val quote: String,
    // Set by the server: the quote and the numbers were found in the letter.
    val verified: Boolean = true,
)

// What came back from asking the server.
sealed interface ExplainResult {
    data class Success(val explanation: Explanation) : ExplainResult
    // canRetry is false when trying again now won't help, like after the daily limit.
    data class Failure(val message: String, val canRetry: Boolean = true) : ExplainResult
}
