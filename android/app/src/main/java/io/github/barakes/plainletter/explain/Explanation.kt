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
)

// What came back from asking the server.
sealed interface ExplainResult {
    data class Success(val explanation: Explanation) : ExplainResult
    data class Failure(val message: String) : ExplainResult
}
