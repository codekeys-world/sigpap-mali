package ml.apes.rh.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ml.apes.rh.auth.PasswordResetTokenRepository;
import ml.apes.rh.domain.PasswordResetToken;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.personnel.PersonnelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReinitialisationMotDePasseService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final PersonnelRepository personnelRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final CompteEmailService emails;

    /** Réponse identique pour une adresse existante ou inconnue afin de ne pas divulguer les comptes. */
    @Transactional
    public void demander(String email) {
        if (!StringUtils.hasText(email)) return;
        var personnel = personnelRepository.findFirstByEmailIgnoreCaseAndActifTrue(email.trim());
        if (personnel.isEmpty()) return;
        Personnel p = personnel.get();
        if (!StringUtils.hasText(p.getEmail())) return;
        String jeton = newJeton();
        tokenRepository.deleteByPersonnelId(p.getId());
        PasswordResetToken reset = new PasswordResetToken();
        reset.setPersonnel(p);
        reset.setTokenHash(hash(jeton));
        reset.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        tokenRepository.save(reset);
        try {
            emails.envoyerLienReinitialisation(p.getEmail(), emails.lienReinitialisation(jeton));
        } catch (RuntimeException ex) {
            log.warn("Échec d'envoi du lien de réinitialisation pour un compte personnel");
            tokenRepository.delete(reset);
        }
    }

    @Transactional
    public void reinitialiser(String jeton, String nouveauMotDePasse, String confirmation) {
        if (
            !StringUtils.hasText(nouveauMotDePasse) || nouveauMotDePasse.length() < 8
        ) throw new BusinessException(
            "Le nouveau mot de passe doit contenir au moins 8 caractères."
        );
        if (!nouveauMotDePasse.equals(confirmation)) throw new BusinessException(
            "La confirmation du mot de passe ne correspond pas."
        );
        PasswordResetToken reset = tokenRepository
            .findByTokenHashAndUsedFalseAndExpiresAtAfter(hash(jeton), Instant.now())
            .orElseThrow(() ->
                new BusinessException("Ce lien de réinitialisation est invalide ou expiré.")
            );
        Personnel p = reset.getPersonnel();
        p.setMotDePasse(encoder.encode(nouveauMotDePasse));
        p.setMotDePasseTemporaire(false);
        personnelRepository.save(p);
        reset.setUsed(true);
        tokenRepository.save(reset);
        tokenRepository.deleteByPersonnelId(p.getId());
    }

    private static String newJeton() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String jeton) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                jeton.getBytes(StandardCharsets.UTF_8)
            );
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
