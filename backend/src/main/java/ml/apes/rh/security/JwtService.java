package ml.apes.rh.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-minutes}") long minutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = minutes * 60_000L;
    }

    public String generer(String matricule, String role) {
        return generer(matricule, role, false);
    }

    public String generer(String matricule, String role, boolean seSouvenirDeMoi) {
        Date now = new Date();
        long duree = seSouvenirDeMoi
            ? Math.max(expirationMs, 30L * 24 * 60 * 60 * 1000)
            : expirationMs;
        return Jwts.builder()
            .subject(matricule)
            .claim("role", role)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + duree))
            .signWith(key)
            .compact();
    }

    public Claims lire(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
