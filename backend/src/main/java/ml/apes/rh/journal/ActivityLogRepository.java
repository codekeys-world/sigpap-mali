package ml.apes.rh.journal;

import java.util.*;
import ml.apes.rh.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ActivityLogRepository
    extends JpaRepository<ActivityLog, Long>, JpaSpecificationExecutor<ActivityLog>
{
    long countByDateActionGreaterThanEqual(java.time.LocalDateTime date);
    long countByActionAndDateActionGreaterThanEqual(String action, java.time.LocalDateTime date);
}
