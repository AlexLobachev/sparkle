package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.Match;
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


    Match findByFirstUserIdAndSecondUserId(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    Match findBySecondUserIdAndFirstUserId(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    List<Match> findByFirstUserIdAndMatchStatus(Long userId, Boolean match);

    @Query(value = """
            SELECT m.first_user_id
                                         FROM matches m
                                         JOIN users u on u.id = m.second_user_id
                                         WHERE m.second_user_id = :userId AND m.match_status = :match;
            """, nativeQuery = true)
    List<Long> findSecondUsersByFirstUserIdAndMatchStatus(
            Long userId, Boolean match
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
                    SET match_status = true 
                    WHERE first_user_id = :firstUser AND second_user_id = :secondUser OR
                          first_user_id = :secondUser AND second_user_id = :firstUser
                    """
            , nativeQuery = true)
    void update(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    @Modifying
    @Query("""
            UPDATE Match m
            SET m.matchStatus = false 
            WHERE m.firstUser = :firstUserId AND m.secondUser = :secondUserId
            """)
    int updateMatchStatus(Long firstUserId, Long secondUserId);
}
