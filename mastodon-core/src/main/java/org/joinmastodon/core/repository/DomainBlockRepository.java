package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.DomainBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainBlockRepository extends JpaRepository<DomainBlock, Long> {
    
    Optional<DomainBlock> findByDomain(String domain);
    
    boolean existsByDomain(String domain);
    
    Page<DomainBlock> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
