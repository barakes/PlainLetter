package io.github.barakes.plainletter.server

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HealthControllerTest {

    @Test
    fun answersOk() {
        assertEquals(mapOf("status" to "ok"), HealthController().health())
    }
}
