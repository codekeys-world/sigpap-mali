package ml.apes.rh.catalog;

import jakarta.validation.Valid;
import java.util.List;
import ml.apes.rh.catalog.CatalogService;
import ml.apes.rh.domain.BaseEntity;
import ml.apes.rh.security.Acces;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Contrôleur générique des référentiels : lecture pour tous les connectés, écriture admin + RH. */
public abstract class CatalogController<T extends BaseEntity> {

    protected abstract CatalogService<T> service();

    /** Liste paginée avec recherche : ?q=texte&page=0&size=10 */
    @GetMapping
    @PreAuthorize(Acces.LECTEURS_RH)
    public Page<T> lister(
        @RequestParam(required = false) String q,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return service().rechercher(q, pageable);
    }

    /** Liste complète (alimente les listes déroulantes). */
    @GetMapping("/all")
    @PreAuthorize(Acces.LECTEURS_RH)
    public List<T> tous() {
        return service().lister();
    }

    @GetMapping("/{slug}")
    @PreAuthorize(Acces.LECTEURS_RH)
    public T get(@PathVariable String slug) {
        return service().getBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.ADMIN_RH)
    public T creer(@Valid @RequestBody T body) {
        return service().creer(body);
    }

    @PutMapping("/{slug}")
    @PreAuthorize(Acces.ADMIN_RH)
    public T modifier(@PathVariable String slug, @Valid @RequestBody T body) {
        return service().modifierBySlug(slug, body);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.ADMIN_RH)
    public void supprimer(@PathVariable String slug) {
        service().supprimerBySlug(slug);
    }
}
