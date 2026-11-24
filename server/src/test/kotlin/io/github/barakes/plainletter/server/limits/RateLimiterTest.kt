package io.github.barakes.plainletter.server.limits

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class RateLimiterTest {

    // A clock the test can move forward.
    private class TestClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("Asia/Jerusalem")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
    }

    private val clock = TestClock(Instant.parse("2026-11-24T10:00:00Z"))

    private fun limiter(perDevice: Int = 2, perIp: Int = 3, total: Int = 5) =
        RateLimiter(clock, perDevice, perIp, total)

    @Test
    fun deviceLimit() {
        val limiter = limiter()
        assertTrue(limiter.tryAcquire("1.1.1.1", "phone"))
        assertTrue(limiter.tryAcquire("1.1.1.1", "phone"))
        assertFalse(limiter.tryAcquire("1.1.1.1", "phone"))
        assertTrue(limiter.tryAcquire("1.1.1.1", "other-phone"))
    }

    @Test
    fun addressLimitCountsAllDevicesTogether() {
        val limiter = limiter()
        assertTrue(limiter.tryAcquire("1.1.1.1", "a"))
        assertTrue(limiter.tryAcquire("1.1.1.1", "b"))
        assertTrue(limiter.tryAcquire("1.1.1.1", "c"))
        assertFalse(limiter.tryAcquire("1.1.1.1", "d"))
        assertTrue(limiter.tryAcquire("2.2.2.2", "d"))
    }

    @Test
    fun withoutDeviceIdOnlyTheAddressCounts() {
        val limiter = limiter()
        repeat(3) { assertTrue(limiter.tryAcquire("1.1.1.1", null)) }
        assertFalse(limiter.tryAcquire("1.1.1.1", null))
    }

    @Test
    fun totalLimitStopsEveryone() {
        val limiter = limiter(perDevice = 10, perIp = 10, total = 2)
        assertTrue(limiter.tryAcquire("1.1.1.1", "a"))
        assertTrue(limiter.tryAcquire("2.2.2.2", "b"))
        assertFalse(limiter.tryAcquire("3.3.3.3", "c"))
    }

    @Test
    fun refusedRequestIsNotCounted() {
        val limiter = limiter(perDevice = 1, perIp = 2, total = 10)
        assertTrue(limiter.tryAcquire("1.1.1.1", "a"))
        assertFalse(limiter.tryAcquire("1.1.1.1", "a"))
        // The refused one didn't use the address's second place.
        assertTrue(limiter.tryAcquire("1.1.1.1", "b"))
    }

    @Test
    fun newDayStartsOver() {
        val limiter = limiter()
        assertTrue(limiter.tryAcquire("1.1.1.1", "phone"))
        assertTrue(limiter.tryAcquire("1.1.1.1", "phone"))
        assertFalse(limiter.tryAcquire("1.1.1.1", "phone"))
        clock.now = clock.now.plus(Duration.ofDays(1))
        assertTrue(limiter.tryAcquire("1.1.1.1", "phone"))
    }

    @Test
    fun dayChangesAtMidnightInIsrael() {
        // 21:30 UTC is 23:30 in Israel (winter), and 22:30 UTC is already the next day there.
        clock.now = Instant.parse("2026-11-24T21:30:00Z")
        val limiter = limiter(perDevice = 1)
        assertTrue(limiter.tryAcquire("1.1.1.1", "phone"))
        assertFalse(limiter.tryAcquire("1.1.1.1", "phone"))
        clock.now = Instant.parse("2026-11-24T22:30:00Z")
        assertTrue(limiter.tryAcquire("1.1.1.1", "phone"))
    }
}
