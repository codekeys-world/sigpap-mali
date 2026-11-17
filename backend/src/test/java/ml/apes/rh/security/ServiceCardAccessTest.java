package ml.apes.rh.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import ml.apes.rh.auth.AuthController;
import ml.apes.rh.personnel.Dtos;
import ml.apes.rh.personnel.PersonnelController;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class ServiceCardAccessTest {

    @Test
    void pdfAndQrEndpointsEnforceTheCardPermission() throws Exception {
        for (String method : List.of("carte", "qrCarte")) {
            assertEquals(
                Acces.CARTES_SERVICE,
                PersonnelController.class
                    .getMethod(method, String.class)
                    .getAnnotation(PreAuthorize.class)
                    .value()
            );
        }
    }

    private boolean allowed(Authentication authentication) {
        return allowed(Acces.CARTES_SERVICE, authentication);
    }

    private boolean allowed(String expression, Authentication authentication) {
        var root = new SecurityExpressionRoot(authentication) {};
        root.setTrustResolver(new AuthenticationTrustResolverImpl());
        return Boolean.TRUE.equals(
            new SpelExpressionParser()
                .parseExpression(expression)
                .getValue(new StandardEvaluationContext(root), Boolean.class)
        );
    }

    @Test
    void userCanReadProfileAndChangePasswordButCannotEditIdentity() throws Exception {
        Authentication user = new UsernamePasswordAuthenticationToken(
            "agent",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_UTILISATEUR"))
        );
        assertEquals(
            Acces.MODIFIER_PROFIL,
            AuthController.class
                .getMethod("modifierMonProfil", Dtos.ProfilPersonnel.class)
                .getAnnotation(PreAuthorize.class)
                .value()
        );
        assertFalse(allowed(Acces.MODIFIER_PROFIL, user));
        assertFalse(allowed(Acces.LECTURE_AUDIT, user));
        assertEquals(
            Acces.LECTURE_AUDIT,
            AuthController.class
                .getMethod("auteurAudit", String.class)
                .getAnnotation(PreAuthorize.class)
                .value()
        );
        assertTrue(
            allowed(
                AuthController.class.getMethod("moi").getAnnotation(PreAuthorize.class).value(),
                user
            )
        );
        assertTrue(
            allowed(
                AuthController.class
                    .getMethod("changerMotDePasse", Dtos.ChangerMotDePasse.class)
                    .getAnnotation(PreAuthorize.class)
                    .value(),
                user
            )
        );
        assertTrue(
            allowed(
                Acces.MODIFIER_PROFIL,
                new UsernamePasswordAuthenticationToken(
                    "admin",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATEUR"))
                )
            )
        );
    }

    @Test
    void userCannotPrintAndOtherRolesRetainAccess() {
        for (String role : List.of(
            "ADMINISTRATEUR",
            "GESTIONNAIRE_RH",
            "DIRECTEUR_REGIONAL",
            "CHEF_ETABLISSEMENT",
            "DIRECTEUR_GENERAL",
            "DIRECTEUR_GENERAL_ADJOINT",
            "CONSULTATION"
        )) {
            assertTrue(
                allowed(
                    new UsernamePasswordAuthenticationToken(
                        "agent",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    )
                ),
                role
            );
        }
        assertFalse(
            allowed(
                new UsernamePasswordAuthenticationToken(
                    "agent",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_UTILISATEUR"))
                )
            )
        );
        assertFalse(
            allowed(
                new AnonymousAuthenticationToken(
                    "key",
                    "anonymous",
                    List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
            )
        );
    }
}
