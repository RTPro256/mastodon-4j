package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Favourite;
import org.joinmastodon.core.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    Optional<Favourite> findByAccountAndStatus(Account account, Status status);

    List<Favourite> findByAccount(Account account);

    @Query("SELECT f FROM Favourite f JOIN FETCH f.account WHERE f.status = :status")
    List<Favourite> findByStatusWithAccount(@Param("status") Status status);

    @Query("SELECT f FROM Favourite f JOIN FETCH f.account WHERE f.status.id = :statusId")
    List<Favourite> findByStatusIdWithAccount(@Param("statusId") Long statusId);

    long countByStatusId(Long statusId);
}
