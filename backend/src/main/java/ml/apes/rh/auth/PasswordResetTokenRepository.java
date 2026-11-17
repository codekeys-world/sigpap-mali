package ml.apes.rh.auth;

import java.time.Instant;
import java.util.Optional;
import ml.apes.rh.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHashAndUsedFalseAndExpiresAtAfter(
        String tokenHash,
        Instant now
    );
    void deleteByPersonnelId(Long personnelId);
}
