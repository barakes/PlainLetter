package io.github.barakes.plainletter.server.explain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

// Checks the real AI against a set of fake letters (in src/test/resources/letters).
// For each letter:
// - every point must pass the Verifier (its quote is in the letter, its numbers too), and
// - the key facts (deadline, amount) must appear somewhere in the explanation.
//
// It calls Gemini, so it runs only when GEMINI_API_KEY is set. Normal test runs skip it.
// Only fake letters are used, because of the free tier's terms.
@SpringBootTest(properties = ["explain.provider=gemini"])
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class AccuracyTest @Autowired constructor(private val provider: ExplanationProvider) {

    // Letter file -> facts that must appear in the explanation.
    private val expected = linkedMapOf(
        "arnona.txt" to listOf("1,234.50", "15/12/2026"),
        "parking-fine.txt" to listOf("250", "90"),
        "national-insurance.txt" to listOf("3,480", "31/12/2026", "30"),
        "court-hearing.txt" to listOf("12/01/2027"),
        "bank-overdraft.txt" to listOf("2,150", "05/12/2026"),
    )

    @Test
    fun explainsTheTestLettersAccurately() {
        val problems = mutableListOf<String>()
        var pending = expected.keys.toList()

        // The free tier is often busy for a moment, so letters it skipped get up to two more tries,
        // after a longer pause.
        for (round in 1..3) {
            if (pending.isEmpty()) break
            if (round > 1) {
                println("Round $round: trying again ${pending.size} letters after a pause")
                Thread.sleep(30_000)
            }
            val stillBusy = mutableListOf<String>()

            for (file in pending) {
                val letter = javaClass.getResource("/letters/$file")!!.readText()
                val explanation = try {
                    Verifier.verify(letter, provider.explain(letter))
                } catch (e: ProviderUnavailableException) {
                    stillBusy += file
                    println("$file: Gemini busy")
                    continue
                } finally {
                    // The free tier allows only a few requests a minute.
                    Thread.sleep(5_000)
                }

                val allText = (listOf(explanation.summary) + explanation.points.map { it.text }).joinToString("\n")
                val unverified = explanation.points.count { !it.verified }
                val missing = expected.getValue(file).filter { !Verifier.numbersFound(allText, it) }

                println("$file: ${explanation.points.size} points, $unverified unverified, missing facts: $missing")
                if (explanation.points.isEmpty()) problems += "$file: no points"
                if (unverified > 0) problems += "$file: $unverified unverified points"
                if (missing.isNotEmpty()) problems += "$file: missing $missing"
            }
            pending = stillBusy
        }

        val checked = expected.size - pending.size
        println("Checked: $checked of ${expected.size}. Never answered: $pending")
        assertTrue(checked > 0, "Gemini was busy for every letter, nothing was checked")
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }
}
