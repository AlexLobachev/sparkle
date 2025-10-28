package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    Match findLike(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);


    Match findByFirstUserIdAndSecondUserId(@Param("firstUser") Long firstUser, @Param("secondUser") Long secondUser);

    List<Match> findByFirstUserIdAndMatchStatus(Long userId, Boolean match);
    List<Match> findBySecondUserIdAndMatchStatus(Long userId, Boolean match);
    Match findByFirstUserId(Long userId);
    void deleteByFirstUserIdAndSecondUserId(Long firstUser,Long secondUser);
    void deleteBySecondUserId(Long userId);
}
