package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.Match;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    @Query(
            value = """
                    SELECT *
                    FROM matches
                    WHERE first_user_id = :firstUser
                        AND second_user_id = :secondUser
                       OR first_user_id = :secondUser
                        AND second_user_id = :firstUser
                    """
            , nativeQuery = true)
    Set<Match> findLike(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    @Query("SELECT m.secondUser.id FROM Match m WHERE m.firstUser.id = :userId")
    List<Long> findSecondUserIdsByFirstUserId(@Param("userId") Long userId);

    Match findByFirstUserIdAndSecondUserId(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    Match findBySecondUserIdAndFirstUserId(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    List<Match> findByFirstUserIdAndMatchStatus(Long userId, Match.MatchStatus status);

    @Query(value = """
    SELECT m.first_user_id
    FROM matches m
    JOIN users u ON u.id = m.second_user_id
    WHERE m.second_user_id = :userId AND m.match_status = :status
    """, nativeQuery = true)
    List<Long> findSecondUsersByFirstUserIdAndMatchStatus(
            @Param("userId") Long userId,
            @Param("status") String status  // Принимаем строку!
    );


    @Query(value = """
            SELECT second_user_id
            FROM matches
            WHERE match_status IN ('LIKE', 'MATCHED')
              AND first_user_id = :userId;            
            """, nativeQuery = true)
    List<Long> findUsersCandidate(
            Long userId
    );


    @Modifying
    @Query(
            value = """
                    DELETE
                    FROM matches
                    WHERE (first_user_id = :firstUser AND second_user_id = :secondUser)
                       OR (first_user_id = :secondUser AND second_user_id = :firstUser);
                    """
            , nativeQuery = true)
    void deleteByFirstUserIdAndSecondUserId(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    @Modifying
    @Query(
            value = """
                    UPDATE matches 
                    SET match_status = :status
                    WHERE first_user_id = :firstUser AND second_user_id = :secondUser OR
                          first_user_id = :secondUser AND second_user_id = :firstUser
                    """
            , nativeQuery = true)
    void update(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser, String status);

    @Modifying
    @Query("""
            UPDATE Match m
            SET m.matchStatus = :status
            WHERE m.firstUser = :firstUserId AND m.secondUser = :secondUserId
            """)
    int updateMatchStatus(Long firstUserId, Long secondUserId, Match.MatchStatus status);


    @Modifying
    @Transactional
    @Query(value = """
    DELETE FROM matches m
    WHERE m.first_user_id = :userId AND m.match_status = :status
    """, nativeQuery = true)
    void deleteByFirstUserIdAndMatchStatus(
            @Param("userId") Long userId,
            @Param("status") String status  // Принимаем строку!
    );

    @Modifying
    @Transactional
    @Query("UPDATE Match m " +
            "SET m.matchStatus = :status " +
            "WHERE m.firstUser.id = :firstUserId " +
            "AND m.secondUser.id = :secondUserId")
    void updateStatus(@Param("firstUserId") Long firstUserId, @Param("secondUserId") Long secondUserId, @Param("status") Match.MatchStatus status);

}