package ml.apes.rh.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import ml.apes.rh.domain.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseEntityRepository<T extends BaseEntity> extends
    JpaRepository<T, Long>,
    JpaSpecificationExecutor<T> {
    Optional<T> findBySlug(String slug);
    List<T> findAllBySlugIn(Collection<String> slugs);
}
