package io.github.barakes.plainletter.server.limits

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate

// Daily limits, so one person (or a script) can't use up the AI budget.
// No database: counts live in memory and start over every day.
// A restart also starts over, which is fine for a small free service.
@Component
class RateLimiter(
    private val clock: Clock,
    @param:Value("\${limits.per-device}") private val perDevice: Int,
    @param:Value("\${limits.per-ip}") private val perIp: Int,
    @param:Value("\${limits.total}") private val total: Int,
) {
    private var day: LocalDate = LocalDate.now(clock)
    private val byDevice = HashMap<String, Int>()
    private val byIp = HashMap<String, Int>()
    private var totalToday = 0

    // Returns true and counts the request if every limit still has room.
    // A refused request isn't counted, so it doesn't use up anything.
    @Synchronized
    fun tryAcquire(ip: String, deviceId: String?): Boolean {
        startNewDayIfNeeded()
        if (totalToday >= total) return false
        if ((byIp[ip] ?: 0) >= perIp) return false
        if (deviceId != null && (byDevice[deviceId] ?: 0) >= perDevice) return false

        totalToday++
        byIp.merge(ip, 1, Int::plus)
        if (deviceId != null) byDevice.merge(deviceId, 1, Int::plus)
        return true
    }

    private fun startNewDayIfNeeded() {
        val today = LocalDate.now(clock)
        if (today != day) {
            day = today
            byDevice.clear()
            byIp.clear()
            totalToday = 0
        }
    }
}
