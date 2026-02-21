package org.joinmastodon.contentaccess.repository;

import org.joinmastodon.contentaccess.entity.ServerAccessPolicy;
import org.joinmastodon.contentaccess.entity.ServerAccessPolicy.AccessPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ServerAccessPolicy entities.
 */
@Repository
public interface ServerAccessPolicyRepository extends JpaRepository<ServerAccessPolicy, Long> {

    /**
     * Find policy by target domain.
     */
    Optional<ServerAccessPolicy> findByTargetDomain(String targetDomain);

    /**
     * Find all policies with a specific access policy.
     */
    List<ServerAccessPolicy> findByPolicy(AccessPolicy policy);

    /**
     * Find all blocked domains.
     */
    @Query("SELECT sap FROM ServerAccessPolicy sap WHERE sap.policy = 'BLOCKED' AND (sap.expiresAt IS NULL OR sap.expiresAt > CURRENT_TIMESTAMP)")
    List<ServerAccessPolicy> findActiveBlockedDomains();

    /**
     * Find all restricted domains.
     */
    @Query("SELECT sap FROM ServerAccessPolicy sap WHERE sap.policy = 'RESTRICTED' AND (sap.expiresAt IS NULL OR sap.expiresAt > CURRENT_TIMESTAMP)")
    List<ServerAccessPolicy> findActiveRestrictedDomains();

    /**
     * Check if a domain has a policy.
     */
    boolean existsByTargetDomain(String targetDomain);

    /**
     * Check if a domain is blocked.
     */
    @Query("SELECT CASE WHEN COUNT(sap) > 0 THEN true ELSE false END FROM ServerAccessPolicy sap WHERE sap.targetDomain = :domain AND sap.policy = 'BLOCKED' AND (sap.expiresAt IS NULL OR sap.expiresAt > CURRENT_TIMESTAMP)")
    boolean isDomainBlocked(@Param("domain") String domain);

    /**
     * Check if a domain is restricted.
     */
    @Query("SELECT CASE WHEN COUNT(sap) > 0 THEN true ELSE false END FROM ServerAccessPolicy sap WHERE sap.targetDomain = :domain AND sap.policy = 'RESTRICTED' AND (sap.expiresAt IS NULL OR sap.expiresAt > CURRENT_TIMESTAMP)")
    boolean isDomainRestricted(@Param("domain") String domain);

    /**
     * Delete policy by domain.
     */
    void deleteByTargetDomain(String targetDomain);
}
