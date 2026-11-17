package ml.apes.rh.catalog;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface FormationRepository extends ml.apes.rh.repository.BaseEntityRepository<Formation> {
    Optional<Formation> findByTitreIgnoreCase(String titre);
}
