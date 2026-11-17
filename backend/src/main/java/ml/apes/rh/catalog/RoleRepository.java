package ml.apes.rh.catalog;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface RoleRepository extends ml.apes.rh.repository.BaseEntityRepository<Role> {
    Optional<Role> findByNom(String nom);
}
