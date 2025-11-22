package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.AuthProvider;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query(value = """
                SELECT u FROM User u 
                JOIN FETCH u.photos up 
                JOIN FETCH up.photo 
                WHERE u.id = :userId    
            """)
    Optional<User> findUserWithPhotosById(@Param("userId") Long userId);

    //@Modifying
    //@Query(value = "" +
    //        "UPDATE users AS u " +
    //        "SET " +
    //        "    gender     = COALESCE(:gender, u.gender), " +
    //        "    preferred_gender     = COALESCE(:preferredGender, u.preferred_gender), " +
    //        "    email      = COALESCE (:email, u.email)," +
    //        "    birth_date  = COALESCE (:birthDate, u.birth_date), " +
    //        "    about_me  = COALESCE (:aboutMe, u.about_me) " +
    //        "WHERE u.id = :id",
    //        nativeQuery = true)
    //int userUpdate(
    //        @Param("gender") String gender,
    //        @Param("preferredGender") String preferredGender,
    //        @Param("email") String email,
    //        @Param("birthDate") LocalDate birthDate,
    //        @Param("aboutMe") String aboutMe,
    //        @Param("id") Long userId);

    @Modifying
    @Query(value = "" +
            "UPDATE users AS u " +
            "SET " +
            "    gender     = COALESCE(:gender, u.gender), " +
            "    preferred_gender     = COALESCE(:preferredGender, u.preferred_gender), " +
            "    email      = COALESCE (:email, u.email)," +
            "    birth_date  = COALESCE (:birthDate, u.birth_date), " +
            "    about_me  = COALESCE (:aboutMe, u.about_me) " +
            "WHERE u.id = :id",
            nativeQuery = true)
    int userUpdate(
            @Param("gender") String gender,
            @Param("preferredGender") String preferredGender,
            @Param("email") String email,
            @Param("birthDate") LocalDate birthDate,
            @Param("aboutMe") String aboutMe,
            @Param("id") Long userId);


    Optional<User> findByUsername(String name);



    @Query(value = """
    SELECT u.id
    FROM users u
    JOIN cities c ON u.city_id = c.id
    JOIN user_interests i ON u.id = i.user_id
    WHERE
        ST_DWithin(c.location, ST_SetSRID(ST_MakePoint(:x, :y), 4326), :distance)
        AND u.gender = :gender
        AND i.interest IN :interests
        AND u.id != :currentUserId
        AND u.id NOT IN (:seenIds)
    ORDER BY RANDOM()
    LIMIT 10
    """, nativeQuery = true)
    List<Long> findCandidateIdsNearLocation(
            @Param("x") double x,
            @Param("y") double y,
            @Param("distance") double distance,
            @Param("gender") String gender,
            @Param("interests") List<String> interests,
            @Param("currentUserId") Long currentUserId,
            @Param("seenIds") List<Long> seenIds
    );




    @Query(value = """
                SELECT u.*
                FROM users u                
                JOIN user_interests i on u.id = i.user_id
                WHERE                    
                    (:gender IS NULL OR u.gender = :gender)
                    AND (:interests IS NULL OR i.interest IN :interests)
                    AND u.id != :currentUserId
                                
            """, nativeQuery = true)
    Page<User> findUsersOffLocation(
            @Param("gender") String gender,
            @Param("interests") List<String> interests,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable

    );

    Optional<User> findByExternalId(String externalId);

    Optional<User> findByExternalIdAndProvider(String externalId, AuthProvider provider);


    /*@Query(value = """
                SELECT u.*
                FROM users u
                JOIN cities c on u.city_id = c.id
                JOIN user_interests i on u.id = i.user_id
                WHERE
                     ST_DWithin(
                                c.location,
                                ST_SetSRID(ST_MakePoint(:x,:y), 4326),
                                :distance
                                )
                                AND u.gender = :gender
                                AND i.interest IN :interests
                                AND u.id != :currentUserId
            """, nativeQuery = true)
    User findUsersNearLocation(
            @Param("x") double x,
            @Param("y") double y,
            @Param("distance") double distance,
            @Param("gender") String gender,
            @Param("interests") List<String> interests,
            @Param("currentUserId") Long currentUserId

    );*/


}
