package ml.apes.rh.catalog;

import java.util.List;
import ml.apes.rh.domain.BaseEntity;
import ml.apes.rh.exception.ResourceNotFoundException;
import ml.apes.rh.repository.BaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

/** CRUD générique des référentiels (corps, grades, formations, études, services, postes, rôles...). */
public abstract class CatalogService<T extends BaseEntity> {

    protected abstract BaseEntityRepository<T> repo();

    protected abstract JpaSpecificationExecutor<T> specs();

    /** Champs interrogés par la recherche texte (chemins "champ" ou "relation.champ"). */
    protected String[] champsRecherche() {
        return new String[] { "nom" };
    }

    protected String triParDefaut() {
        return "nom";
    }

    protected abstract T nouveau();

    protected abstract String libelle();

    /** Copie les champs modifiables de source vers cible (en résolvant les références). */
    protected abstract void copier(T source, T cible);

    @Transactional(readOnly = true)
    public Page<T> rechercher(String q, Pageable pageable) {
        Pageable p = pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                  pageable.getPageNumber(),
                  pageable.getPageSize(),
                  Sort.by(triParDefaut())
              );
        return specs().findAll(RechercheSpecs.<T>contient(q, champsRecherche()), p);
    }

    @Transactional(readOnly = true)
    public List<T> lister() {
        return repo().findAll();
    }

    @Transactional(readOnly = true)
    public T get(Long id) {
        return repo()
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(libelle() + " introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public T getBySlug(String slug) {
        return repo()
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException(libelle() + " introuvable"));
    }

    @Transactional
    public T creer(T in) {
        T t = nouveau();
        copier(in, t);
        return repo().saveAndFlush(t);
    }

    @Transactional
    public T modifier(Long id, T in) {
        T t = get(id);
        copier(in, t);
        return repo().saveAndFlush(t);
    }

    @Transactional
    public T modifierBySlug(String slug, T in) {
        return modifier(getBySlug(slug).getId(), in);
    }

    @Transactional
    public void supprimer(Long id) {
        T t = get(id);
        verifierSuppression(t);
        repo().delete(t);
        repo().flush(); // déclenche tout de suite les violations de clé étrangère (gérées par GlobalExceptionHandler)
    }

    @Transactional
    public void supprimerBySlug(String slug) {
        supprimer(getBySlug(slug).getId());
    }

    protected void verifierSuppression(T t) {}
}
