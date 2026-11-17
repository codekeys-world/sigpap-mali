package ml.apes.rh.personnel;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import ml.apes.rh.domain.FaireEtude;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.domain.SituationAgent;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PersonnelSpecs {

    private PersonnelSpecs() {}

    /** @param scope null = aucune restriction ; vide = aucun résultat ; sinon ids de services autorisés. */
    public static Specification<Personnel> build(
        PersonnelFilter f,
        Set<Long> scope,
        Long personnelId
    ) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (personnelId != null) ps.add(cb.equal(root.get("id"), personnelId));
            if (scope != null) {
                ps.add(
                    scope.isEmpty()
                        ? cb.disjunction()
                        : root.get("serviceCourant").get("id").in(scope)
                );
            }
            if (StringUtils.hasText(f.getQ())) {
                var service = root.join("serviceCourant", JoinType.LEFT);
                var telephone = root.<String>get("telephone");
                jakarta.persistence.criteria.Expression<String> numero = telephone;
                for (String separateur : List.of(" ", "-", ".", "(", ")", "+")) {
                    numero = cb.function(
                        "replace",
                        String.class,
                        numero,
                        cb.literal(separateur),
                        cb.literal("")
                    );
                }
                // Chaque terme peut correspondre à un champ différent : prénom, nom, service…
                for (String terme : f.getQ().trim().toLowerCase(Locale.ROOT).split("\\s+")) {
                    String like = "%" + echapperRecherche(terme) + "%";
                    List<Predicate> champs = new ArrayList<>(
                        List.of(
                            cb.like(cb.lower(root.get("nom")), like, '\\'),
                            cb.like(cb.lower(root.get("prenom")), like, '\\'),
                            cb.like(cb.lower(root.get("matricule")), like, '\\'),
                            cb.like(cb.lower(telephone), like, '\\'),
                            cb.like(cb.lower(service.get("nom")), like, '\\')
                        )
                    );
                    String chiffres = terme.replaceAll("[\\s+().-]", "");
                    if (chiffres.matches("[0-9]+")) {
                        champs.add(cb.like(numero, "%" + chiffres + "%"));
                    }
                    ps.add(cb.or(champs.toArray(new Predicate[0])));
                }
            }
            if (StringUtils.hasText(f.getGradeSlug())) ps.add(
                cb.equal(root.get("gradeActuel").get("slug"), f.getGradeSlug())
            );
            if (StringUtils.hasText(f.getCorpsSlug())) {
                var corps = root.join("corpsActuel", JoinType.LEFT);
                var grade = root.join("gradeActuel", JoinType.LEFT);
                var corpsDuGrade = grade.join("corps", JoinType.LEFT);
                // Même priorité que dans le tableau de bord : corps courant, puis corps du grade.
                ps.add(
                    cb.equal(
                        cb.coalesce(corps.<String>get("slug"), corpsDuGrade.<String>get("slug")),
                        f.getCorpsSlug()
                    )
                );
            }
            if (StringUtils.hasText(f.getServiceSlug())) ps.add(
                cb.equal(root.get("serviceCourant").get("slug"), f.getServiceSlug())
            );
            if (f.getSurveillant() != null) ps.add(
                cb.equal(root.get("surveillant"), f.getSurveillant())
            );
            if (f.getEnFormation() != null) ps.add(
                cb.equal(root.get("enFormation"), f.getEnFormation())
            );
            if (f.getDecede() != null) ps.add(cb.equal(root.get("decede"), f.getDecede()));
            if (StringUtils.hasText(f.getStatut())) {
                switch (f.getStatut()) {
                    case "ACTIF" -> {
                        ps.add(cb.equal(root.get("decede"), false));
                        ps.add(
                            cb.or(
                                cb.isNull(root.get("situation")),
                                cb.equal(root.get("situation"), SituationAgent.ACTIF)
                            )
                        );
                    }
                    case "DECEDE" -> ps.add(cb.equal(root.get("decede"), true));
                    case "MALADE" -> ps.add(cb.equal(root.get("situation"), SituationAgent.MALADE));
                    case "ABANDON_POSTE" -> ps.add(
                        cb.equal(root.get("situation"), SituationAgent.ABANDON_POSTE)
                    );
                    case "RETRAITE" -> ps.add(
                        cb.equal(root.get("situation"), SituationAgent.RETRAITE)
                    );
                    default -> {
                    }
                }
            }
            if (Boolean.TRUE.equals(f.getEnEtude())) {
                Subquery<Long> sq = query.subquery(Long.class);
                var fe = sq.from(FaireEtude.class);
                sq.select(fe.<Long>get("id")).where(
                    cb.equal(fe.get("personnel"), root),
                    cb.isNull(fe.get("dateFin")),
                    cb.isNull(fe.get("anneeFin"))
                );
                ps.add(cb.exists(sq));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private static String echapperRecherche(String valeur) {
        return valeur.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
