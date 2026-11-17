package ml.apes.rh.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/** Instancié dans SecurityConfig (volontairement pas un @Component : évite le double enregistrement servlet). */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest req,
        HttpServletResponse res,
        FilterChain chain
    ) throws ServletException, IOException {
        String token = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("APES_AUTH".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null && !token.isBlank()) {
            try {
                String matricule = jwtService.lire(token).getSubject();
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails u = userDetailsService.loadUserByUsername(matricule);
                    if (u.isEnabled()) {
                        UsernamePasswordAuthenticationToken t =
                            new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
                        t.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(t);
                    }
                }
            } catch (Exception ignored) {
                // jeton invalide ou expiré : la requête reste anonyme et sera rejetée (401) par l'entry point
            }
        }
        chain.doFilter(req, res);
    }
}
