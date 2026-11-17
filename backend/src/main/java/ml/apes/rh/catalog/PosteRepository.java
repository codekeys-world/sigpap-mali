package ml.apes.rh.catalog;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PosteRepository extends ml.apes.rh.repository.BaseEntityRepository<Poste> {
    Optional<Poste> findByNomIgnoreCase(String nom);
}
