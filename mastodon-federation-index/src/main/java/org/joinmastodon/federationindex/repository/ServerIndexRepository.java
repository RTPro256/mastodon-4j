package org.joinmastodon.federationindex.repository;

import org.joinmastodon.federationindex.entity.ServerIndex;
import org.joinmastodon.federationindex.entity.ServerIndex.AccessPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ServerIndex entities.
 */
@Repository
public interface ServerIndexRepository extends JpaRepository<ServerIndex, Long> {

    /**
     * Find server by domain.
     */
    Optional<ServerIndex> findByDomain(String domain);

    /**
     * Find all servers with a specific access policy.
     */
    List<ServerIndex> findByAccessPolicy(AccessPolicy accessPolicy);

    /**
     * Find all online servers.
     */
    List<ServerIndex> findByIsOnlineTrue();

    /**
     * Find servers with rating above threshold.
     */
    @Query("SELECT si FROM ServerIndex si WHERE si.internalRating >= :minRating")
    List<ServerIndex> findByMinInternalRating(@Param("minRating") BigDecimal minRating);

    /**
     * Find servers with rating below threshold.
     */
    @Query("SELECT si FROM ServerIndex si WHERE si.internalRating < :maxRating")
    List<ServerIndex> findByMaxInternalRating(@Param("maxRating") BigDecimal maxRating);

    /**
     * Check if a server exists by domain.
     */
    boolean existsByDomain(String domain);

    /**
     * Count online servers.
     */
    long countByIsOnlineTrue();

    /**
     * Count blocked servers.
     */
    long countByAccessPolicy(AccessPolicy accessPolicy);

    /**
     * Find servers with availability score below threshold.
     */
    @Query("SELECT si FROM ServerIndex si WHERE si.availabilityScore < :threshold")
    List<ServerIndex> findUnreliableServers(@Param("threshold") BigDecimal threshold);

    /**
     * Find servers not seen since a given time.
     */
    @Query("SELECT si FROM ServerIndex si WHERE si.lastSeenAt < :threshold")
    List<ServerIndex> findStaleServers(@Param("threshold") java.time.Instant threshold);

    /**
     * Get average rating across all servers.
     */
    @Query("SELECT AVG(si.internalRating) FROM ServerIndex si WHERE si.internalRating IS NOT NULL")
    Optional<BigDecimal> getAverageRating();
}
