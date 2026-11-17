package ml.apes.rh.situation;

import java.util.List;
import ml.apes.rh.domain.ChangementSituation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangementSituationRepository
    extends ml.apes.rh.repository.BaseEntityRepository<ChangementSituation>
{
    List<ChangementSituation> findByPersonnelIdOrderByDateDebutDescIdDesc(Long personnelId);
    List<ChangementSituation> findByPersonnelIdAndDateFinIsNull(Long personnelId);
    void deleteByPersonnelId(Long personnelId);
}
