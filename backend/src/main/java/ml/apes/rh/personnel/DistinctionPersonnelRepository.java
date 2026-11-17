package ml.apes.rh.personnel;

import java.util.List;
import java.util.Optional;
import ml.apes.rh.domain.DistinctionPersonnel;
import ml.apes.rh.repository.BaseEntityRepository;

public interface DistinctionPersonnelRepository extends BaseEntityRepository<DistinctionPersonnel> {
    List<DistinctionPersonnel> findByPersonnelIdOrderByDateAttributionDescIdDesc(Long personnelId);
    Optional<DistinctionPersonnel> findBySlugAndPersonnelId(String slug, Long personnelId);
    void deleteByPersonnelId(Long personnelId);
}
