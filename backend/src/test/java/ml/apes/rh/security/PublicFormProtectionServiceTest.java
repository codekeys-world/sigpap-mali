package ml.apes.rh.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import ml.apes.rh.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PublicFormProtectionServiceTest {

    @Test
    void emailLimitAppliesAcrossAddressesAndIgnoresCase() {
        var service = new PublicFormProtectionService();
        for (int i = 0; i < 3; i++) service.forgot("ip-" + i, " Agent@example.ml ");
        assertThrows(BusinessException.class, () -> service.forgot("new-ip", "agent@EXAMPLE.ml"));
        assertDoesNotThrow(() -> service.forgot("new-ip", "other@example.ml"));
    }

    @Test
    void expiredWindowAllowsRequestsAgain() {
        class MutableClock extends Clock {

            Instant now = Instant.parse("2026-11-16T12:00:00Z");

            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            public Clock withZone(ZoneId zone) {
                return this;
            }

            public Instant instant() {
                return now;
            }
        }
        var clock = new MutableClock();
        var service = new PublicFormProtectionService(clock);
        for (int i = 0; i < 3; i++) service.forgot("ip", "agent@example.ml");
        var failure = assertThrows(BusinessException.class, () ->
            service.forgot("ip", "agent@example.ml")
        );
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, failure.getStatus());
        clock.now = clock.now.plus(Duration.ofMinutes(15));
        assertDoesNotThrow(() -> service.forgot("ip", "agent@example.ml"));
    }

    @Test
    void parallelLoginRequestsCannotExceedTheLimit() {
        var service = new PublicFormProtectionService();
        var accepted = new AtomicInteger();
        IntStream.range(0, 150)
            .parallel()
            .forEach(i -> {
                try {
                    service.login("ip");
                    accepted.incrementAndGet();
                } catch (BusinessException ignored) {}
            });
        assertEquals(120, accepted.get());
        assertDoesNotThrow(() -> service.login("another-ip"));
    }
}
