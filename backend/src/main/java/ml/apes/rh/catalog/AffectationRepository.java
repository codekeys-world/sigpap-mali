package ml.apes.rh.catalog;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AffectationRepository
    extends ml.apes.rh.repository.BaseEntityRepository<Affectation>
{
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        "delete from Affectation a where not exists (select 1 from AffectationPersonnel ap where ap.affectation = a)"
    )
    void supprimerOrphelines();
}
