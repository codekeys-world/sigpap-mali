package ml.apes.rh.personnel;

import java.util.List;
import ml.apes.rh.domain.PieceJustificative;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PieceJustificativeRepository
    extends ml.apes.rh.repository.BaseEntityRepository<PieceJustificative>
{
    List<PieceJustificative> findByPersonnelIdOrderByDateDocumentDescIdDesc(Long personnelId);
    List<PieceJustificative> findByPersonnelId(Long personnelId);
    void deleteByPersonnelId(Long personnelId);
}
