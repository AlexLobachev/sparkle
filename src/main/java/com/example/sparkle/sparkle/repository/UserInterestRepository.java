package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    UserInterest findAllByUserIdAndInterest(Long userId, Interest interest);
    List<UserInterest> findAllByUserId(Long userId);


    @Query(
            value = """
                            SELECT u.*
                            FROM users u
                            JOIN user_interests ui on u.id = ui.user_id
                            WHERE interest IN (SELECT interest
                            FROM user_interests
                            GROUP BY interest
                            HAVING COUNT(*) > 1)
                    GROUP BY u.id
                    ;
                            """, nativeQuery = true
    )
    List<User> getUsersWithTheSameInterests();

    @Query(value = """
                    SELECT u.*
                    FROM users u
                    JOIN user_interests ui on u.id = ui.user_id
                    WHERE interest IN (SELECT interest
                    FROM user_interests
                    WHERE ui.interest IN :interests
                    GROUP BY interest
                    HAVING COUNT(*) > 1)
            GROUP BY u.id
            ;
                    """, nativeQuery = true)
    List<User> getUsersWithTheSameInterestsByUserId(@Param("interests") List<String> interests);

    void deleteByUserIdAndInterest(Long userId,Interest interest);
}