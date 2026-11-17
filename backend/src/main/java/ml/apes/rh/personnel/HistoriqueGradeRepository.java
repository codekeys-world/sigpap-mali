package ml.apes.rh.personnel;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface HistoriqueGradeRepository
    extends ml.apes.rh.repository.BaseEntityRepository<HistoriqueGrade>
{
    List<HistoriqueGrade> findByPersonnelId(Long personnelId);
    void deleteByPersonnelId(Long personnelId);
}
