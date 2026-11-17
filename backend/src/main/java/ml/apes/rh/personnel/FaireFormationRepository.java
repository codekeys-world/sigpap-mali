package ml.apes.rh.personnel;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface FaireFormationRepository
    extends ml.apes.rh.repository.BaseEntityRepository<FaireFormation>
{
    List<FaireFormation> findByPersonnelId(Long personnelId);
    void deleteByPersonnelId(Long personnelId);
    List<FaireFormation> findByPersonnelIdIn(Collection<Long> ids);
}
