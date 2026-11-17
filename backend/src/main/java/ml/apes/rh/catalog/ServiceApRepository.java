package ml.apes.rh.catalog;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ServiceApRepository extends ml.apes.rh.repository.BaseEntityRepository<ServiceAp> {
    List<ServiceAp> findByParentId(Long parentId);
}
