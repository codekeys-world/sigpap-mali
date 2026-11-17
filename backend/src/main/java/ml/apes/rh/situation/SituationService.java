package ml.apes.rh.situation;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.ChangementSituation;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.domain.SituationAgent;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.personnel.PersonnelRepository;
import ml.apes.rh.personnel.PersonnelService;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.security.Roles;
import ml.apes.rh.situation.ChangementSituationRepository;
import ml.apes.rh.situation.Dtos.DemandeSituation;
import ml.apes.rh.storage.FileStorageService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Décès, maladie, abandon de poste, retraite et reprise : chaque changement est historisé. */
@Service
@RequiredArgsConstructor
@Transactional
public class SituationService {

    private final PersonnelService personnelService;
    private final PersonnelRepository repo;
    private final ChangementSituationRepository csRepo;
    private final CurrentUserService current;
    private final FileStorageService storage;

    public Long personnelId(String slug) {
        return personnelService.idParSlug(slug);
    }

    @Transactional(readOnly = true)
    public List<ChangementSituation> historique(Long id) {
        personnelService.get(id); // contrôle du périmètre
        return csRepo.findByPersonnelIdOrderByDateDebutDescIdDesc(id);
    }

    public Personnel changer(Long id, DemandeSituation req) {
        Personnel p = personnelService.get(id);
        Personnel moi = current.get();
        String role = moi.getRole() != null ? moi.getRole().getNom() : "";
        boolean adminRh = Roles.ADMINISTRATEUR.equals(role) || Roles.GESTIONNAIRE_RH.equals(role);

        SituationAgent actuelle = p.getSituation();
        SituationAgent cible = req.situation();
        if (cible == actuelle) throw new BusinessException(
            "L'agent est déjà dans la situation « " + cible.libelle() + " »."
        );

        boolean definitive =
            actuelle == SituationAgent.DECEDE || actuelle == SituationAgent.RETRAITE;
        boolean sensible =
            definitive || cible == SituationAgent.DECEDE || cible == SituationAgent.RETRAITE;
        if (sensible && !adminRh) {
            throw new AccessDeniedException(
                "Seuls l'administrateur et le gestionnaire RH peuvent déclarer un décès ou une retraite."
            );
        }
        if (sensible && moi.getId().equals(id)) {
            throw new BusinessException("Vous ne pouvez pas modifier votre propre situation.");
        }
        if (definitive && cible != SituationAgent.ACTIF) {
            throw new BusinessException(
                "Annulez d'abord la situation actuelle (retour en activité) avant d'en déclarer une autre."
            );
        }

        LocalDate date = req.date();
        if (date.isAfter(LocalDate.now())) throw new BusinessException(
            "La date ne peut pas être dans le futur."
        );
        if (p.getDateNaissance() != null && date.isBefore(p.getDateNaissance())) {
            throw new BusinessException(
                "La date ne peut pas précéder la date de naissance de l'agent."
            );
        }
        String motif = StringUtils.hasText(req.motif()) ? req.motif().trim() : null;
        if (cible == SituationAgent.DECEDE && motif == null) throw new BusinessException(
            "Le motif du décès est obligatoire."
        );
        if (cible == SituationAgent.MALADE && motif == null) throw new BusinessException(
            "La cause de la maladie est obligatoire."
        );
        if (definitive && motif == null) throw new BusinessException(
            "Précisez la raison de l'annulation."
        );
        String reference = StringUtils.hasText(req.reference()) ? req.reference().trim() : null;

        // clôture de la situation ouverte, puis nouvelle entrée d'historique
        String fichier = StringUtils.hasText(req.fichier()) ? req.fichier().trim() : null;
        if (fichier != null && !storage.existe(fichier)) throw new BusinessException(
            "La pièce jointe est introuvable : renvoyez-la."
        );
        csRepo
            .findByPersonnelIdAndDateFinIsNull(id)
            .forEach(c ->
                c.setDateFin(
                    c.getDateDebut() != null && date.isBefore(c.getDateDebut())
                        ? c.getDateDebut()
                        : date
                )
            );
        ChangementSituation c = new ChangementSituation();
        c.setPersonnel(p);
        c.setSituation(cible);
        c.setDateDebut(date);
        c.setMotif(motif);
        c.setReference(reference);
        c.setFichier(fichier);
        csRepo.save(c);

        p.setSituation(cible);
        p.setSituationDepuis(date);
        p.setSituationMotif(motif);
        switch (cible) {
            case DECEDE -> {
                p.setDecede(true);
                p.setDateDeces(date);
                p.setActif(false); // le compte de connexion est désactivé
                p.setEnFormation(false);
            }
            case RETRAITE -> p.setActif(false);
            case ACTIF -> {
                p.setDecede(false);
                p.setDateDeces(null);
                p.setActif(true);
            }
            default -> {
            }
        }
        return repo.save(p);
    }
}
