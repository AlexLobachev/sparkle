package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.CandidateBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateBatchRepository extends JpaRepository<CandidateBatch, Long> {
    Optional<CandidateBatch> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);

    // ВАЖНО: возвращает List<Long>, а не List<CandidateBatch>!
    @Query("""
                SELECT cb.candidateIds
                FROM CandidateBatch cb
                WHERE cb.user.id = :userId
            """)
    List<Long> findAllCandidateIdsByUserId(@Param("userId") Long userId);
}
