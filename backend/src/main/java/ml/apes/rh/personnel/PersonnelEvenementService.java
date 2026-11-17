package ml.apes.rh.personnel;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.CongeAbsencePersonnel;
import ml.apes.rh.domain.DistinctionPersonnel;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.exception.ResourceNotFoundException;
import ml.apes.rh.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonnelEvenementService {

    private final PersonnelService personnelService;
    private final CongeAbsencePersonnelRepository congeRepo;
    private final DistinctionPersonnelRepository distinctionRepo;
    private final FileStorageService storage;

    private Personnel agent(String slug) {
        return personnelService.get(personnelService.idParSlug(slug));
    }

    @Transactional(readOnly = true)
    public List<CongeAbsencePersonnel> conges(String personnelSlug) {
        return congeRepo.findByPersonnelIdOrderByDateDebutDescIdDesc(agent(personnelSlug).getId());
    }

    public CongeAbsencePersonnel ajouterConge(String personnelSlug, CongeAbsencePersonnel demande) {
        CongeAbsencePersonnel conge = new CongeAbsencePersonnel();
        conge.setPersonnel(agent(personnelSlug));
        remplirConge(demande, conge);
        return congeRepo.saveAndFlush(conge);
    }

    public CongeAbsencePersonnel modifierConge(
        String personnelSlug,
        String slug,
        CongeAbsencePersonnel demande
    ) {
        CongeAbsencePersonnel conge = congeRepo
            .findBySlugAndPersonnelId(slug, agent(personnelSlug).getId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Congé ou absence introuvable dans ce dossier.")
            );
        String ancienFichier = conge.getFichier();
        remplirConge(demande, conge);
        CongeAbsencePersonnel saved = congeRepo.saveAndFlush(conge);
        supprimerAncienFichier(ancienFichier, saved.getFichier());
        return saved;
    }

    public void supprimerConge(String personnelSlug, String slug) {
        CongeAbsencePersonnel conge = congeRepo
            .findBySlugAndPersonnelId(slug, agent(personnelSlug).getId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Congé ou absence introuvable dans ce dossier.")
            );
        congeRepo.delete(conge);
        congeRepo.flush();
        if (StringUtils.hasText(conge.getFichier())) storage.supprimer(conge.getFichier());
    }

    private void remplirConge(CongeAbsencePersonnel demande, CongeAbsencePersonnel conge) {
        demande.verifierDates();
        conge.setType(demande.getType());
        conge.setNature(demande.getNature().trim());
        conge.setDateDebut(demande.getDateDebut());
        conge.setDateFin(demande.getDateFin());
        conge.setReference(texte(demande.getReference()));
        conge.setObservations(texte(demande.getObservations()));
        conge.setFichier(fichier(demande.getFichier()));
    }

    @Transactional(readOnly = true)
    public List<DistinctionPersonnel> distinctions(String personnelSlug) {
        return distinctionRepo.findByPersonnelIdOrderByDateAttributionDescIdDesc(
            agent(personnelSlug).getId()
        );
    }

    public DistinctionPersonnel ajouterDistinction(
        String personnelSlug,
        DistinctionPersonnel demande
    ) {
        DistinctionPersonnel distinction = new DistinctionPersonnel();
        distinction.setPersonnel(agent(personnelSlug));
        remplirDistinction(demande, distinction);
        return distinctionRepo.saveAndFlush(distinction);
    }

    public DistinctionPersonnel modifierDistinction(
        String personnelSlug,
        String slug,
        DistinctionPersonnel demande
    ) {
        DistinctionPersonnel distinction = distinctionRepo
            .findBySlugAndPersonnelId(slug, agent(personnelSlug).getId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Distinction introuvable dans ce dossier.")
            );
        String ancienFichier = distinction.getFichier();
        remplirDistinction(demande, distinction);
        DistinctionPersonnel saved = distinctionRepo.saveAndFlush(distinction);
        supprimerAncienFichier(ancienFichier, saved.getFichier());
        return saved;
    }

    public void supprimerDistinction(String personnelSlug, String slug) {
        DistinctionPersonnel distinction = distinctionRepo
            .findBySlugAndPersonnelId(slug, agent(personnelSlug).getId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Distinction introuvable dans ce dossier.")
            );
        distinctionRepo.delete(distinction);
        distinctionRepo.flush();
        if (StringUtils.hasText(distinction.getFichier())) storage.supprimer(
            distinction.getFichier()
        );
    }

    private void remplirDistinction(
        DistinctionPersonnel demande,
        DistinctionPersonnel distinction
    ) {
        distinction.setIntitule(demande.getIntitule().trim());
        distinction.setDateAttribution(demande.getDateAttribution());
        distinction.setAutorite(texte(demande.getAutorite()));
        distinction.setReference(texte(demande.getReference()));
        distinction.setMotif(texte(demande.getMotif()));
        distinction.setFichier(fichier(demande.getFichier()));
    }

    private String texte(String valeur) {
        return StringUtils.hasText(valeur) ? valeur.trim() : null;
    }

    private String fichier(String chemin) {
        String valeur = texte(chemin);
        if (valeur != null && !storage.existe(valeur)) {
            throw new BusinessException(
                "Le justificatif est introuvable. Veuillez le joindre à nouveau."
            );
        }
        return valeur;
    }

    private void supprimerAncienFichier(String ancien, String nouveau) {
        if (StringUtils.hasText(ancien) && !Objects.equals(ancien, nouveau)) storage.supprimer(
            ancien
        );
    }
}
