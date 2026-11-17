package ml.apes.rh.auth;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.CatalogServices.AutorisationService;
import ml.apes.rh.catalog.CatalogServices.RoleService;
import ml.apes.rh.domain.Autorisation;
import ml.apes.rh.domain.Role;
import ml.apes.rh.security.Acces;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Gestion des rôles et autorisations : écriture réservée à l'administrateur du système. */
public final class SecurityAdminController {

    private SecurityAdminController() {}

    @RestController
    @RequestMapping("/api/roles")
    @RequiredArgsConstructor
    public static class RoleController {

        private final RoleService service;

        @GetMapping
        @PreAuthorize(Acces.ADMIN)
        public Page<Role> lister(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable
        ) {
            return service.rechercher(q, pageable);
        }

        @GetMapping("/all")
        @PreAuthorize(Acces.ADMIN)
        public List<Role> tous() {
            return service.lister();
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @PreAuthorize(Acces.ADMIN)
        public Role creer(@Valid @RequestBody Role body) {
            return service.creer(body);
        }

        @PutMapping("/{slug}")
        @PreAuthorize(Acces.ADMIN)
        public Role modifier(@PathVariable String slug, @Valid @RequestBody Role body) {
            return service.modifierBySlug(slug, body);
        }

        @DeleteMapping("/{slug}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @PreAuthorize(Acces.ADMIN)
        public void supprimer(@PathVariable String slug) {
            service.supprimerBySlug(slug);
        }
    }

    @RestController
    @RequestMapping("/api/autorisations")
    @RequiredArgsConstructor
    public static class AutorisationController {

        private final AutorisationService service;

        @GetMapping
        @PreAuthorize(Acces.ADMIN)
        public Page<Autorisation> lister(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable
        ) {
            return service.rechercher(q, pageable);
        }

        @GetMapping("/all")
        @PreAuthorize(Acces.ADMIN)
        public List<Autorisation> tous() {
            return service.lister();
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @PreAuthorize(Acces.ADMIN)
        public Autorisation creer(@Valid @RequestBody Autorisation body) {
            return service.creer(body);
        }

        @PutMapping("/{slug}")
        @PreAuthorize(Acces.ADMIN)
        public Autorisation modifier(
            @PathVariable String slug,
            @Valid @RequestBody Autorisation body
        ) {
            return service.modifierBySlug(slug, body);
        }

        @DeleteMapping("/{slug}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @PreAuthorize(Acces.ADMIN)
        public void supprimer(@PathVariable String slug) {
            service.supprimerBySlug(slug);
        }
    }
}
