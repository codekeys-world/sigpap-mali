package ml.apes.rh.personnel;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PosteAffectationRepository
    extends ml.apes.rh.repository.BaseEntityRepository<PosteAffectation>
{
    List<PosteAffectation> findByAffectationPersonnelId(Long id);
}
