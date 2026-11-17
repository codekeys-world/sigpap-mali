package ml.apes.rh.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class AuditJsonConfiguration {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer auditFieldsFilter() {
        SimpleBeanPropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            public void serializeAsField(
                Object bean,
                JsonGenerator generator,
                SerializerProvider provider,
                PropertyWriter writer
            ) throws Exception {
                boolean canReadAudit =
                    SecurityContextHolder.getContext().getAuthentication() != null &&
                    SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                    !(SecurityContextHolder.getContext().getAuthentication() instanceof
                        AnonymousAuthenticationToken) &&
                    SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .stream()
                        .noneMatch(authority ->
                            "ROLE_UTILISATEUR".equals(authority.getAuthority())
                        );
                String name = writer.getName();
                boolean fieldAudit =
                    "createdBy".equals(name) ||
                    "createdDate".equals(name) ||
                    "creePar".equals(name) ||
                    "creeLe".equals(name) ||
                    "lastModifiedBy".equals(name) ||
                    "lastModifiedDate".equals(name);
                if (canReadAudit || !fieldAudit) super.serializeAsField(
                    bean,
                    generator,
                    provider,
                    writer
                );
            }
        };
        SimpleFilterProvider filters = new SimpleFilterProvider()
            .addFilter("auditFields", filter)
            .setFailOnUnknownId(false);
        return builder -> builder.filters(filters);
    }
}
