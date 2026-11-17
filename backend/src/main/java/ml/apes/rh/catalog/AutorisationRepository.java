package ml.apes.rh.catalog;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AutorisationRepository
    extends ml.apes.rh.repository.BaseEntityRepository<Autorisation>
{
    Optional<Autorisation> findByNom(String nom);
}
