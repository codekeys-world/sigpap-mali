package ml.apes.rh.personnel;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AffectationPersonnelRepository
    extends ml.apes.rh.repository.BaseEntityRepository<AffectationPersonnel>
{
    List<AffectationPersonnel> findByPersonnelId(Long personnelId);
    void deleteByPersonnelId(Long personnelId);
    long countByAffectationId(Long affectationId);
    List<AffectationPersonnel> findByPersonnelIdIn(Collection<Long> ids);
}
