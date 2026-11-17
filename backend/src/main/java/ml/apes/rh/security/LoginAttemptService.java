package ml.apes.rh.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ml.apes.rh.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Protection contre la force brute : 5 échecs consécutifs => matricule verrouillé 15 minutes. */
@Service
public class LoginAttemptService {

    static final int MAX_ECHECS = 5;
    static final Duration VERROUILLAGE = Duration.ofMinutes(15);

    private record Etat(int echecs, Instant verrouilleJusqua) {}

    private final Map<String, Etat> etats = new ConcurrentHashMap<>();

    private static String cle(String matricule) {
        return matricule.trim().toLowerCase(Locale.ROOT);
    }

    public void verifier(String matricule) {
        Etat e = etats.get(cle(matricule));
        if (
            e != null &&
            e.verrouilleJusqua() != null &&
            Instant.now().isBefore(e.verrouilleJusqua())
        ) {
            long min = Duration.between(Instant.now(), e.verrouilleJusqua()).toMinutes() + 1;
            throw new BusinessException(
                "Trop de tentatives échouées. Réessayez dans " + min + " minute(s).",
                HttpStatus.TOO_MANY_REQUESTS
            );
        }
    }

    public void echec(String matricule) {
        etats.merge(cle(matricule), new Etat(1, null), (ancien, nouveau) -> {
            if (
                ancien.verrouilleJusqua() != null &&
                Instant.now().isAfter(ancien.verrouilleJusqua())
            ) return new Etat(1, null);
            int n = ancien.echecs() + 1;
            return n >= MAX_ECHECS
                ? new Etat(0, Instant.now().plus(VERROUILLAGE))
                : new Etat(n, null);
        });
    }

    public void succes(String matricule) {
        etats.remove(cle(matricule));
    }
}
