package ml.apes.rh.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ml.apes.rh.domain.ActivityLog;
import ml.apes.rh.journal.ActivityLogRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Journalise : les écritures (POST/PUT/DELETE), les connexions (réussies ou non) et l'accès aux documents
 * sensibles (exports, fiches, cartes) avec l'IP, le navigateur, le système, l'appareil, la durée et le résultat.
 */
public class ActivityLogFilter extends OncePerRequestFilter {

    public static final String ATTR_MATRICULE = "apes.matricule";
    public static final String ATTR_ROLE = "apes.role";
    private final ActivityLogRepository repository;

    public ActivityLogFilter(ActivityLogRepository repository) {
        this.repository = repository;
    }

    private static String tronquer(String s, int max) {
        return s == null ? null : s.length() > max ? s.substring(0, max) : s;
    }

    static String ip(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String real = req.getHeader("X-Real-IP");
        return real != null && !real.isBlank() ? real.trim() : req.getRemoteAddr();
    }

    private static String action(String method, String uri, boolean login, int statut) {
        if (login) return statut < 400 ? "CONNEXION" : "ECHEC_CONNEXION";
        if ("GET".equals(method)) return uri.endsWith("/export") ? "EXPORT" : "DOCUMENT";
        if (uri.endsWith("/mot-de-passe")) return "CHANGEMENT_MOT_DE_PASSE";
        if (uri.endsWith("/situation")) return "CHANGEMENT_SITUATION";
        if ("POST".equals(method) && uri.endsWith("/personnels/import")) return "IMPORT";
        if (uri.startsWith("/api/fichiers")) return "TELEVERSEMENT";
        return switch (method) {
            case "POST" -> uri.endsWith("/photo") ? "MODIFICATION" : "CREATION";
            case "PUT" -> "MODIFICATION";
            case "DELETE" -> "SUPPRESSION";
            default -> "AUTRE";
        };
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest req,
        HttpServletResponse res,
        FilterChain chain
    ) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String method = req.getMethod();
        boolean login = "POST".equals(method) && "/api/auth/login".equals(uri);
        boolean document =
            "GET".equals(method) &&
            (uri.endsWith("/export") || uri.endsWith("/fiche") || uri.endsWith("/carte"));
        boolean ecriture =
            uri.startsWith("/api/") &&
            !"GET".equals(method) &&
            !"OPTIONS".equals(method) &&
            !"HEAD".equals(method);
        boolean suivre = login || document || ecriture;
        long debut = System.nanoTime();
        try {
            chain.doFilter(req, res);
        } finally {
            if (suivre) {
                try {
                    enregistrer(
                        req,
                        res,
                        uri,
                        method,
                        login,
                        (System.nanoTime() - debut) / 1_000_000
                    );
                } catch (Exception ignored) {
                    // la journalisation ne doit jamais casser la requête
                }
            }
        }
    }

    private void enregistrer(
        HttpServletRequest req,
        HttpServletResponse res,
        String uri,
        String method,
        boolean login,
        long dureeMs
    ) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        boolean connecte =
            a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);
        String attr = (String) req.getAttribute(ATTR_MATRICULE);
        String utilisateur = attr != null ? attr : connecte ? a.getName() : "anonyme";
        String role = (String) req.getAttribute(ATTR_ROLE);
        if (role == null && connecte) {
            role = a
                .getAuthorities()
                .stream()
                .map(g -> g.getAuthority())
                .filter(g -> g.startsWith("ROLE_"))
                .map(g -> g.substring(5))
                .findFirst()
                .orElse(null);
        }
        // ressource = segments non numériques ; identifiant = dernier segment numérique
        List<String> noms = new ArrayList<>();
        String id = null;
        for (String s : uri.replaceFirst("^/api/?", "").split("/")) {
            if (s.isBlank()) continue;
            if (s.matches("\\d+")) id = s;
            else noms.add(s);
        }
        UserAgentParser.Info ua = UserAgentParser.parse(req.getHeader("User-Agent"));
        String origine =
            req.getHeader("Origin") != null ? req.getHeader("Origin") : req.getHeader("Referer");

        ActivityLog log = new ActivityLog();
        log.setUtilisateur(tronquer(utilisateur, 100));
        log.setRole(tronquer(role, 40));
        log.setStatut(res.getStatus());
        log.setAction(action(method, uri, login, res.getStatus()));
        log.setMethode(method);
        log.setUri(tronquer(uri, 400));
        log.setParametres(tronquer(req.getQueryString(), 400));
        log.setRessource(tronquer(String.join(" › ", noms), 200));
        log.setRessourceId(id);
        log.setDureeMs(dureeMs);
        log.setDateAction(LocalDateTime.now());
        log.setIp(tronquer(ip(req), 64));
        log.setClient(tronquer(req.getHeader("User-Agent"), 500));
        log.setNavigateur(ua.navigateur());
        log.setSysteme(ua.systeme());
        log.setAppareil(ua.appareil());
        log.setOrigine(tronquer(origine, 200));
        repository.save(log);
    }
}
