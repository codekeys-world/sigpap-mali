package ml.apes.rh.dashboard;

import com.fasterxml.jackson.annotation.JsonFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record Stat(String label, long value) {}

    public record CorpsStat(String slug, String label, long hommes, long femmes) {}

    @JsonFilter("auditFields")
    public record AgentRecent(
        String slug,
        String nomComplet,
        String matricule,
        String grade,
        String photo,
        LocalDateTime creeLe,
        String creePar
    ) {}

    @JsonFilter("auditFields")
    public record AffectationRecente(
        String slug,
        String personnelSlug,
        String agent,
        String service,
        String type,
        LocalDate date,
        String creePar
    ) {}

    public record Referentiels(
        long corps,
        long grades,
        long services,
        long formations,
        long etudes,
        long niveauxEtude,
        long postes,
        long mutations
    ) {}

    /** Indicateurs de qualité des données et de gestion des comptes. */
    public record Qualite(
        long sansPhoto,
        long sansGrade,
        long sansService,
        long sansContact,
        long comptesInactifs,
        long motsDePasseTemporaires
    ) {}

    /** Activité du jour (réservée aux administrateurs). */
    public record Activite(long actions, long connexions, long echecsConnexion) {}

    public record Dashboard(
        long total,
        long surveillants,
        long enFormation,
        long decedes,
        long actifs,
        long hommes,
        long femmes,
        List<CorpsStat> parCorps,
        List<Stat> parGrade,
        List<Stat> parTypeService,
        List<AgentRecent> derniersAgents,
        List<AffectationRecente> affectationsRecentes,
        Referentiels referentiels,
        Qualite qualite,
        Activite activite,
        long affectationsTotal,
        long affectationsEnCours,
        List<Stat> parTrancheAge,
        List<Stat> parAnciennete,
        List<Stat> recrutements,
        List<Stat> mutations,
        List<Stat> topFormations,
        List<Stat> etudesParNiveau,
        List<Stat> parService,
        long malades,
        long abandons,
        Retraites retraites
    ) {}

    public record Retraites(
        long depassees,
        long dans12Mois,
        long dans24Mois,
        long retraites,
        List<Stat> departsParAnnee
    ) {}
}
