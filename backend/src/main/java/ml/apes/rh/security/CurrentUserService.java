package ml.apes.rh.security;

import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.personnel.PersonnelRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final PersonnelRepository repository;

    public Personnel get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (
            authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken
        ) {
            throw new AccessDeniedException("Authentification requise");
        }
        return repository
            .findByMatricule(authentication.getName())
            .orElseThrow(() -> new AccessDeniedException("Utilisateur introuvable"));
    }

    public String role() {
        Personnel personnel = get();
        return personnel.getRole() != null ? personnel.getRole().getNom() : "";
    }
}
