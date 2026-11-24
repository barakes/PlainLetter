package io.github.barakes.plainletter.server.explain

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ExplainController(private val provider: ExplanationProvider) {

    // POST /api/explain with {"text": "..."} returns the explanation.
    // Empty text or text over 10,000 characters is refused with 400.
    @PostMapping("/explain")
    fun explain(@Valid @RequestBody request: ExplainRequest): Explanation =
        provider.explain(request.text)
}
