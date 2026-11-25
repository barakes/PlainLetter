package io.github.barakes.plainletter.server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

// GET /health answers {"status":"ok"} when the server is up.
// Render and the wake-up workflow use it. It doesn't touch the AI and isn't counted in the limits.
@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "ok")
}
