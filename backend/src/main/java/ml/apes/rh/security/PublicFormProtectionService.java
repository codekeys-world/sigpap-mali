package ml.apes.rh.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import ml.apes.rh.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Limites locales complémentaires au contrôle par compte et aux limites du proxy. */
@Service
public class PublicFormProtectionService {

    private record Window(Instant expiresAt, int count) {}

    private final Map<String, Window> windows = new HashMap<>();
    private final Clock clock;

    public PublicFormProtectionService() {
        this(Clock.systemUTC());
    }

    PublicFormProtectionService(Clock clock) {
        this.clock = clock;
    }

    public void login(String address) {
        check("login:" + address, 120, Duration.ofMinutes(5));
    }

    public void reset(String address) {
        check("reset:" + address, 60, Duration.ofMinutes(15));
    }

    public void forgot(String address, String email) {
        check("forgot:" + address, 30, Duration.ofMinutes(15));
        check("email:" + digest(email.trim().toLowerCase(Locale.ROOT)), 3, Duration.ofMinutes(15));
    }

    private synchronized void check(String key, int limit, Duration duration) {
        Instant now = clock.instant();
        if (windows.size() >= 10_000) {
            windows.entrySet().removeIf(e -> !now.isBefore(e.getValue().expiresAt()));
            if (windows.size() >= 10_000 && !windows.containsKey(key)) throw limited();
        }
        Window old = windows.get(key);
        if (old == null || !now.isBefore(old.expiresAt())) {
            windows.put(key, new Window(now.plus(duration), 1));
        } else {
            if (old.count() >= limit) throw limited();
            windows.put(key, new Window(old.expiresAt(), old.count() + 1));
        }
    }

    private BusinessException limited() {
        return new BusinessException(
            "Trop de demandes rapprochées. Patientez quelques minutes avant de réessayer.",
            HttpStatus.TOO_MANY_REQUESTS
        );
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
