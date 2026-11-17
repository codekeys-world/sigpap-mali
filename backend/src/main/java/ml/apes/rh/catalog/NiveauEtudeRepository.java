package ml.apes.rh.catalog;

import java.util.Optional;
import ml.apes.rh.domain.NiveauEtude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NiveauEtudeRepository
    extends ml.apes.rh.repository.BaseEntityRepository<NiveauEtude>
{
    Optional<NiveauEtude> findByNomIgnoreCase(String nom);
}
