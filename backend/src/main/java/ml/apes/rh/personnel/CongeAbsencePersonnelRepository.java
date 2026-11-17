package ml.apes.rh.personnel;

import java.util.List;
import java.util.Optional;
import ml.apes.rh.domain.CongeAbsencePersonnel;
import ml.apes.rh.repository.BaseEntityRepository;

public interface CongeAbsencePersonnelRepository
    extends BaseEntityRepository<CongeAbsencePersonnel>
{
    List<CongeAbsencePersonnel> findByPersonnelIdOrderByDateDebutDescIdDesc(Long personnelId);
    Optional<CongeAbsencePersonnel> findBySlugAndPersonnelId(String slug, Long personnelId);
    void deleteByPersonnelId(Long personnelId);
}
