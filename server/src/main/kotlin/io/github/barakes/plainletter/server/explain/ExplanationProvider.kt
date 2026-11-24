package io.github.barakes.plainletter.server.explain

// Whoever writes the explanation. Today a fake one, later Gemini or another AI,
// chosen by one setting without touching the rest of the server.
interface ExplanationProvider {
    fun explain(text: String): Explanation
}
