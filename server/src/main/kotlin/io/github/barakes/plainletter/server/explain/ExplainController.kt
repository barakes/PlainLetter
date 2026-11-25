package io.github.barakes.plainletter.server.explain

import io.github.barakes.plainletter.server.limits.ClientAddress
import io.github.barakes.plainletter.server.limits.LimitReachedException
import io.github.barakes.plainletter.server.limits.RateLimiter
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ExplainController(
    private val provider: ExplanationProvider,
    private val limiter: RateLimiter,
    private val clientAddress: ClientAddress,
) {

    // POST /api/explain with {"text": "..."} returns the explanation.
    // Empty text or text over 10,000 characters is refused with 400 and isn't counted.
    // Over the daily limit is refused with 429.
    // X-Device-Id is a random id the app creates once. Optional, because the address limit always applies.
    // Every answer is checked against the letter before it's sent back, whoever wrote it.
    @PostMapping("/explain")
    fun explain(
        @Valid @RequestBody request: ExplainRequest,
        @RequestHeader("X-Device-Id", required = false) deviceId: String?,
        http: HttpServletRequest,
    ): Explanation {
        if (!limiter.tryAcquire(clientAddress.of(http), deviceId?.take(64))) throw LimitReachedException()
        return Verifier.verify(request.text, provider.explain(request.text))
    }
}
