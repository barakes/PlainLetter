package io.github.barakes.plainletter.server.limits

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class ClientAddressTest {

    private fun request(header: String? = null) = MockHttpServletRequest().apply {
        remoteAddr = "104.16.0.1"
        if (header != null) addHeader("CF-Connecting-IP", header)
    }

    @Test
    fun usesTheProxyHeader() {
        assertEquals("203.0.113.7", ClientAddress("CF-Connecting-IP").of(request("203.0.113.7")))
    }

    @Test
    fun acceptsIpv6() {
        assertEquals("2001:db8::1", ClientAddress("CF-Connecting-IP").of(request("2001:db8::1")))
    }

    @Test
    fun fallsBackWhenTheHeaderIsMissing() {
        assertEquals("104.16.0.1", ClientAddress("CF-Connecting-IP").of(request()))
    }

    @Test
    fun ignoresAHeaderThatIsNotAnAddress() {
        assertEquals("104.16.0.1", ClientAddress("CF-Connecting-IP").of(request("not an address; drop table")))
    }

    @Test
    fun withoutAConfiguredHeaderUsesTheConnection() {
        assertEquals("104.16.0.1", ClientAddress("").of(request("203.0.113.7")))
    }
}
