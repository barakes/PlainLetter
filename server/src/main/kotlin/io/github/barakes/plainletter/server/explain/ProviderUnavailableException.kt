package io.github.barakes.plainletter.server.explain

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

// The AI didn't answer, or answered something unusable. Answered with 503; the app asks to try again.
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class ProviderUnavailableException(message: String) : RuntimeException(message)
