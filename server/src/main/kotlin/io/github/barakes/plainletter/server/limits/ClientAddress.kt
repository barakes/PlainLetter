package io.github.barakes.plainletter.server.limits

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// The address the daily limits count by.
// Takes it from one header that the proxy in front of the server writes itself (limits.client-ip-header),
// and falls back to the connection's own address when the header is missing or doesn't look like an address.
@Component
class ClientAddress(
    @param:Value("\${limits.client-ip-header:}") private val header: String,
) {
    // Only digits, a-f, dots and colons (IPv4 and IPv6), so nothing else can sneak in as an "address".
    private val looksLikeAddress = Regex("""^[0-9a-fA-F:.]{2,45}$""")

    fun of(request: HttpServletRequest): String {
        if (header.isNotBlank()) {
            val value = request.getHeader(header)?.trim()
            if (value != null && looksLikeAddress.matches(value)) return value
        }
        return request.remoteAddr
    }
}
