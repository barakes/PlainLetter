package io.github.barakes.plainletter.server.limits

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

// Answered with 429 Too Many Requests. The app shows its own message in Hebrew.
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
class LimitReachedException : RuntimeException("Daily limit reached")
