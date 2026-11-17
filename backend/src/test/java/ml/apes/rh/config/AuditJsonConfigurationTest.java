package ml.apes.rh.config;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import ml.apes.rh.dashboard.Dtos.AgentRecent;
import ml.apes.rh.domain.Personnel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuditJsonConfigurationTest {

    private ObjectMapper mapper() {
        var builder = Jackson2ObjectMapperBuilder.json();
        new AuditJsonConfiguration().auditFieldsFilter().customize(builder);
        return builder.build();
    }

    private Personnel agent() {
        Personnel agent = new Personnel();
        agent.setPrenom("Aminata");
        agent.setCreatedBy("CREATOR");
        agent.setCreatedDate(LocalDateTime.now());
        agent.setLastModifiedBy("EDITOR");
        agent.setLastModifiedDate(LocalDateTime.now());
        return agent;
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void everyProfessionalRoleCanSeeTheRecordedAuthorAndDates() {
        for (String role : List.of(
            "ADMINISTRATEUR",
            "GESTIONNAIRE_RH",
            "DIRECTEUR_REGIONAL",
            "CHEF_ETABLISSEMENT",
            "DIRECTEUR_GENERAL",
            "DIRECTEUR_GENERAL_ADJOINT",
            "CONSULTATION"
        )) {
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                    "reader",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                )
            );
            var json = mapper().valueToTree(agent());
            assertEquals("CREATOR", json.get("createdBy").asText(), role);
            assertTrue(json.has("createdDate"), role);
            assertEquals("EDITOR", json.get("lastModifiedBy").asText(), role);
        }
    }

    @Test
    void simpleUsersCannotReceiveAuditFieldsEvenInDashboardRecords() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "reader",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_UTILISATEUR"))
            )
        );
        var json = mapper().valueToTree(agent());
        for (String field : List.of(
            "createdBy",
            "createdDate",
            "lastModifiedBy",
            "lastModifiedDate"
        ))
            assertFalse(json.has(field), field);
        assertEquals("Aminata", json.get("prenom").asText());
        var recent = mapper().valueToTree(
            new AgentRecent(
                "slug",
                "Aminata Traore",
                "MAT",
                "Grade",
                null,
                LocalDateTime.now(),
                "CREATOR"
            )
        );
        assertFalse(recent.has("creePar"));
        assertFalse(recent.has("creeLe"));
    }

    @Test
    void anonymousRequestsCannotReceiveAuditFields() {
        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            )
        );
        assertFalse(mapper().valueToTree(agent()).has("createdBy"));
    }
}
