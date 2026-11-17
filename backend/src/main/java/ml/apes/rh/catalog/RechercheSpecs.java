package ml.apes.rh.catalog;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** Recherche texte « contient » (insensible à la casse) sur un ou plusieurs champs, y compris "relation.champ". */
public final class RechercheSpecs {

    private RechercheSpecs() {}

    public static <T> Specification<T> contient(String q, String... chemins) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(q)) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            List<Predicate> ps = new ArrayList<>();
            for (String c : chemins) {
                String[] parts = c.split("\\.");
                Path<?> p =
                    parts.length == 1
                        ? root.get(parts[0])
                        : root.join(parts[0], JoinType.LEFT).get(parts[1]);
                ps.add(cb.like(cb.lower(p.as(String.class)), like));
            }
            return cb.or(ps.toArray(new Predicate[0]));
        };
    }
}
