package ml.apes.rh.catalog;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface GradeRepository extends ml.apes.rh.repository.BaseEntityRepository<Grade> {
    List<Grade> findByCorpsIdOrderByNom(Long corpsId);
}
