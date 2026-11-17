package ml.apes.rh.personnel;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface FaireEtudeRepository
    extends ml.apes.rh.repository.BaseEntityRepository<FaireEtude>
{
    List<FaireEtude> findByPersonnelId(Long personnelId);
    void deleteByPersonnelId(Long personnelId);
    List<FaireEtude> findByPersonnelIdIn(Collection<Long> ids);
}
