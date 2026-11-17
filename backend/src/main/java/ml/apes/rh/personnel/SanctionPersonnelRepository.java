package ml.apes.rh.personnel;

import java.util.List;
import ml.apes.rh.domain.SanctionPersonnel;
import ml.apes.rh.repository.BaseEntityRepository;

public interface SanctionPersonnelRepository extends BaseEntityRepository<SanctionPersonnel> {
    List<SanctionPersonnel> findByPersonnelIdOrderByDateSanctionDescIdDesc(Long personnelId);
    void deleteByPersonnelId(Long personnelId);
}
