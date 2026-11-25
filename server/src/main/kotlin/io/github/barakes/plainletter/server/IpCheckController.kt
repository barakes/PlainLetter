package io.github.barakes.plainletter.server

import io.github.barakes.plainletter.server.limits.ClientAddress
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

// TEMPORARY: shows the caller what the server sees about their own address,
// to check that the daily limits get the real client address behind Render's proxy.
// It only returns the caller's own information. Delete this file after the check.
@RestController
class IpCheckController(private val clientAddress: ClientAddress) {

    @GetMapping("/debug/ip")
    fun ip(request: HttpServletRequest): Map<String, String?> = linkedMapOf(
        "usedForLimits" to clientAddress.of(request),
        "remoteAddr" to request.remoteAddr,
        "X-Forwarded-For" to request.getHeader("X-Forwarded-For"),
        "True-Client-IP" to request.getHeader("True-Client-IP"),
        "CF-Connecting-IP" to request.getHeader("CF-Connecting-IP"),
    )
}
