package ml.apes.rh.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ml.apes.rh.exception.BusinessException;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    @Test
    void verrouilleApresCinqEchecs() {
        LoginAttemptService s = new LoginAttemptService();
        for (int i = 0; i < 4; i++) s.echec("RH001");
        assertDoesNotThrow(() -> s.verifier("RH001"));
        s.echec("RH001");
        assertThrows(BusinessException.class, () -> s.verifier("rh001"));
    }

    @Test
    void unSuccesRemetLeCompteurAZero() {
        LoginAttemptService s = new LoginAttemptService();
        for (int i = 0; i < 4; i++) s.echec("ADMIN001");
        s.succes("ADMIN001");
        s.echec("ADMIN001");
        assertDoesNotThrow(() -> s.verifier("ADMIN001"));
    }
}
