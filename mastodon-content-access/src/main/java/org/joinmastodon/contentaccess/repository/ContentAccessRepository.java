package org.joinmastodon.contentaccess.repository;

import org.joinmastodon.contentaccess.entity.ContentAccess;
import org.joinmastodon.contentaccess.entity.ContentAccess.AccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ContentAccess entities.
 */
@Repository
public interface ContentAccessRepository extends JpaRepository<ContentAccess, Long> {

    /**
     * Find content access by status ID.
     */
    Optional<ContentAccess> findByStatusId(Long statusId);

    /**
     * Find all content with a specific access level.
     */
    List<ContentAccess> findByAccessLevel(AccessLevel accessLevel);

    /**
     * Find all content access entries for a specific account's statuses.
     */
    @Query("SELECT ca FROM ContentAccess ca WHERE ca.status.account.id = :accountId")
    List<ContentAccess> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Find content access entries where the account has been granted permission.
     */
    @Query("SELECT ca FROM ContentAccess ca JOIN ca.permissions p WHERE p.grantee.id = :accountId AND p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP")
    List<ContentAccess> findContentGrantedToAccount(@Param("accountId") Long accountId);

    /**
     * Check if a status has content access configuration.
     */
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM ContentAccess ca WHERE ca.status.id = :statusId")
    boolean existsByStatusId(@Param("statusId") Long statusId);

    /**
     * Delete content access by status ID.
     */
    void deleteByStatusId(Long statusId);
}
