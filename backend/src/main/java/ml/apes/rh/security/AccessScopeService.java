package ml.apes.rh.security;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.ServiceApRepository;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.domain.ServiceAp;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Périmètre de données selon le rôle :
 * - Directeur régional : agents de sa direction régionale + maisons d'arrêt qui en relèvent ;
 * - Chef d'établissement : agents de sa maison d'arrêt ;
 * - Utilisateur : son propre dossier uniquement ;
 * - Autres rôles : pas de restriction.
 */
@Service
@RequiredArgsConstructor
public class AccessScopeService {

    private final CurrentUserService current;
    private final ServiceApRepository serviceRepo;

    /** @return null si aucune restriction. */
    public Set<Long> idsAutorises() {
        Personnel moi = current.get();
        String role = moi.getRole() != null ? moi.getRole().getNom() : "";
        if (Roles.UTILISATEUR.equals(role)) return null;
        boolean dr = Roles.DIRECTEUR_REGIONAL.equals(role);
        boolean chef = Roles.CHEF_ETABLISSEMENT.equals(role);
        if (!dr && !chef) return null;
        Set<Long> ids = new HashSet<>();
        ServiceAp s = moi.getServiceCourant();
        if (s == null) return ids;
        ids.add(s.getId());
        if (dr) serviceRepo.findByParentId(s.getId()).forEach(c -> ids.add(c.getId()));
        return ids;
    }

    public Long personnelIdSeul() {
        Personnel moi = current.get();
        return Roles.UTILISATEUR.equals(moi.getRole() != null ? moi.getRole().getNom() : "")
            ? moi.getId()
            : null;
    }

    public boolean estDansPerimetre(Long serviceId) {
        if (Roles.ADMINISTRATEUR.equals(current.role())) return true;
        Set<Long> ids = idsAutorises();
        return ids == null || (serviceId != null && ids.contains(serviceId));
    }

    public void verifierAcces(Personnel p) {
        Personnel moi = current.get();
        String role = moi.getRole() != null ? moi.getRole().getNom() : "";
        if (Roles.ADMINISTRATEUR.equals(role)) return;
        if (Roles.UTILISATEUR.equals(role)) {
            if (!moi.getId().equals(p.getId())) {
                throw new AccessDeniedException(
                    "Vous ne pouvez consulter que votre propre dossier."
                );
            }
            return;
        }
        Long sid = p.getServiceCourant() != null ? p.getServiceCourant().getId() : null;
        if (!estDansPerimetre(sid)) {
            throw new AccessDeniedException("Cet agent est hors de votre périmètre");
        }
    }
}
