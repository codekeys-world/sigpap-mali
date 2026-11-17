package ml.apes.rh.catalog;

import java.util.List;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface EtudeRepository extends ml.apes.rh.repository.BaseEntityRepository<Etude> {
    List<Etude> findByNiveauEtudeIgnoreCase(String niveauEtude);
    boolean existsByNiveauEtudeIgnoreCase(String niveauEtude);
}
