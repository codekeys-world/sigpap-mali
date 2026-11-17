package ml.apes.rh.retirement;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.Grade;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.domain.SituationAgent;
import ml.apes.rh.personnel.PersonnelFilter;
import ml.apes.rh.personnel.PersonnelRepository;
import ml.apes.rh.personnel.PersonnelSpecs;
import ml.apes.rh.retirement.Dtos.RetraiteLigne;
import ml.apes.rh.security.AccessScopeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Suivi des retraites : date prévue = date de naissance + âge de retraite du corps de l'agent
 * (ou âge par défaut app.retraite.age-defaut). Respecte le périmètre de l'utilisateur.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetraiteService {

    private final PersonnelRepository repo;
    private final AccessScopeService scope;

    @Value("${app.retraite.age-defaut:60}")
    private int ageDefaut;

    public int ageRetraite(Personnel p) {
        var corps =
            p.getCorpsActuel() != null
                ? p.getCorpsActuel()
                : p.getGradeActuel() != null
                  ? p.getGradeActuel().getCorps()
                  : null;
        Integer a = corps != null ? corps.getAgeRetraite() : null;
        return a != null ? a : ageDefaut;
    }

    public LocalDate datePrevue(Personnel p) {
        return p.getDateNaissance() == null ? null : p.getDateNaissance().plusYears(ageRetraite(p));
    }

    private RetraiteLigne ligne(Personnel p, LocalDate today) {
        LocalDate prevue = datePrevue(p);
        String etat;
        if (p.getSituation() == SituationAgent.RETRAITE) etat = "RETRAITE";
        else if (prevue == null) etat = "NON_RENSEIGNE";
        else if (prevue.isBefore(today)) etat = "DEPASSEE";
        else etat = "A_VENIR";
        Long mois = prevue == null ? null : ChronoUnit.MONTHS.between(today, prevue);
        boolean retraite = p.getSituation() == SituationAgent.RETRAITE;
        var corps =
            p.getCorpsActuel() != null
                ? p.getCorpsActuel()
                : p.getGradeActuel() != null
                  ? p.getGradeActuel().getCorps()
                  : null;
        return new RetraiteLigne(
            p.getSlug(),
            p.getMatricule(),
            p.getNom(),
            p.getPrenom(),
            p.getPhoto(),
            p.getGradeActuel() != null ? p.getGradeActuel().getNom() : null,
            p.isSurveillant(),
            corps != null ? corps.getNom() : null,
            p.getServiceCourant() != null ? p.getServiceCourant().getNom() : null,
            p.getDateNaissance(),
            p.getDateNaissance() == null
                ? null
                : Period.between(p.getDateNaissance(), today).getYears(),
            ageRetraite(p),
            prevue,
            mois,
            etat,
            p.getSituation().name(),
            retraite ? p.getSituationDepuis() : null,
            retraite ? p.getSituationMotif() : null
        );
    }

    /** @param etat TOUS, DEPASSEE, A_VENIR (dans l'horizon), RETRAITE ou NON_RENSEIGNE */
    public List<RetraiteLigne> tous(String etat, int horizonMois, String q) {
        LocalDate today = LocalDate.now();
        String recherche = StringUtils.hasText(q) ? q.trim().toLowerCase() : null;
        Comparator<RetraiteLigne> parDate = Comparator.comparing(
            RetraiteLigne::datePrevue,
            Comparator.nullsLast(Comparator.naturalOrder())
        );
        return repo
            .findAll(
                PersonnelSpecs.build(
                    new PersonnelFilter(),
                    scope.idsAutorises(),
                    scope.personnelIdSeul()
                )
            )
            .stream()
            .filter(p -> !p.isDecede())
            .map(p -> ligne(p, today))
            .filter(
                l ->
                    recherche == null ||
                    (l.nom() + " " + l.prenom() + " " + l.matricule())
                        .toLowerCase()
                        .contains(recherche)
            )
            .filter(
                l ->
                    switch (etat == null ? "TOUS" : etat) {
                        case "DEPASSEE" -> l.etat().equals("DEPASSEE");
                        case "RETRAITE" -> l.etat().equals("RETRAITE");
                        case "NON_RENSEIGNE" -> l.etat().equals("NON_RENSEIGNE");
                        case "A_VENIR" -> l.etat().equals("A_VENIR") &&
                            !l.datePrevue().isAfter(today.plusMonths(horizonMois));
                        default -> true;
                    }
            )
            .sorted(parDate)
            .toList();
    }

    public Page<RetraiteLigne> lister(String etat, int horizonMois, String q, Pageable pageable) {
        List<RetraiteLigne> all = tous(etat, horizonMois, q);
        int from = (int) Math.min(pageable.getOffset(), all.size());
        int to = Math.min(from + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(from, to), pageable, all.size());
    }
}
