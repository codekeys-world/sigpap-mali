package ml.apes.rh.catalog;

import java.util.Optional;
import ml.apes.rh.domain.ArreteIntegration;

public interface ArreteIntegrationRepository
    extends ml.apes.rh.repository.BaseEntityRepository<ArreteIntegration>
{
    Optional<ArreteIntegration> findByReferenceIgnoreCase(String reference);
}
