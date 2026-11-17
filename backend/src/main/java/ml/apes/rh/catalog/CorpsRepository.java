package ml.apes.rh.catalog;

import java.util.*;
import java.util.Optional;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface CorpsRepository extends ml.apes.rh.repository.BaseEntityRepository<Corps> {
    Optional<Corps> findByNomIgnoreCase(String nom);
}
