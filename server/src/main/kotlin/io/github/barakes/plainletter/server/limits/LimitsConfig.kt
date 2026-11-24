package io.github.barakes.plainletter.server.limits

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class LimitsConfig {

    // Days start at midnight Israel time. Tests pass their own clock to move time forward.
    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of("Asia/Jerusalem"))
}
