package io.github.barakes.plainletter.server.explain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// What the app sends: the letter text, after personal details were hidden on the phone.
data class ExplainRequest(
    @field:NotBlank
    @field:Size(max = 10_000)
    val text: String,
)

// What the server answers.
data class Explanation(
    // What this letter is, in one or two simple sentences.
    val summary: String,
    // What the reader needs to know or do. Each point carries the words from the letter it is based on.
    val points: List<Point>,
    // Reminder that this is not legal advice.
    val disclaimer: String,
)

data class Point(
    val text: String,
    // The exact words from the letter that the point is based on.
    val quote: String,
    // Set by the server, not by the AI: true only if the quote really appears in the letter
    // and every number in the text appears there too. The app warns about points that aren't.
    val verified: Boolean = true,
)

// Added by the server to every explanation, whoever wrote it.
const val DISCLAIMER = "ההסבר נועד לעזור להבין את המכתב, ואינו ייעוץ משפטי. במקרה של ספק, פנה לגוף ששלח את המכתב."
