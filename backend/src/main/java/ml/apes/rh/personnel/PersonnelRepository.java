package ml.apes.rh.personnel;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PersonnelRepository extends ml.apes.rh.repository.BaseEntityRepository<Personnel> {
    Optional<Personnel> findByMatricule(String matricule);
    Optional<AuteurAuditProjection> findAuteurByMatricule(String matricule);

    interface AuteurAuditProjection {
        String getMatricule();
        String getPrenom();
        String getNom();
    }

    Optional<Personnel> findFirstByEmailIgnoreCaseAndActifTrue(String email);
    boolean existsByMatricule(String matricule);
    boolean existsByMatriculeAndIdNot(String matricule, Long id);
    boolean existsByArreteIntegrationId(Long arreteIntegrationId);
}
