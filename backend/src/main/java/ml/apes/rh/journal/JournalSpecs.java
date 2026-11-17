package ml.apes.rh.journal;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ml.apes.rh.domain.ActivityLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class JournalSpecs {

    private JournalSpecs() {}

    public static Specification<ActivityLog> build(
        String q,
        String action,
        String resultat,
        String utilisateur,
        LocalDate debut,
        LocalDate fin
    ) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (StringUtils.hasText(q)) {
                String like = "%" + q.trim().toLowerCase() + "%";
                List<Predicate> or = new ArrayList<>();
                for (String c : new String[] {
                    "utilisateur",
                    "uri",
                    "ip",
                    "navigateur",
                    "systeme",
                    "ressource",
                    "role",
                    "action",
                    "parametres",
                }) {
                    or.add(cb.like(cb.lower(root.get(c)), like));
                }
                ps.add(cb.or(or.toArray(new Predicate[0])));
            }
            if (StringUtils.hasText(action)) ps.add(cb.equal(root.get("action"), action));
            if ("succes".equals(resultat)) ps.add(cb.lessThan(root.get("statut"), 400));
            if ("echec".equals(resultat)) ps.add(cb.greaterThanOrEqualTo(root.get("statut"), 400));
            if (StringUtils.hasText(utilisateur)) ps.add(
                cb.equal(root.get("utilisateur"), utilisateur)
            );
            if (debut != null) ps.add(
                cb.greaterThanOrEqualTo(root.get("dateAction"), debut.atStartOfDay())
            );
            if (fin != null) ps.add(
                cb.lessThan(root.get("dateAction"), fin.plusDays(1).atStartOfDay())
            );
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
