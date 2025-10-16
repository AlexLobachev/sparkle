package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.dto.user.UserDtoAuthenticate;
import com.example.sparkle.sparkle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    @Query(value = "" +
            "UPDATE users AS u " +
            "SET username   = COALESCE (:username, u.username), " +
            "    password   = COALESCE (:password, u.password)," +
            "    gender     = COALESCE (:gender, u.gender), " +
            "    email      = COALESCE (:email, u.email)," +
            "    birthDate  = COALESCE (:birthDate, u.birthdate) " +
            "WHERE u.id = :id"
            , nativeQuery = true)
    Optional<User> userUpdate(
            @Param("username") String username,
            @Param("password") String password,
            @Param("gender") String gender,
            @Param("email") String email,
            @Param("birthdate") LocalDate birthDate,
            @Param("id") Long userId
    );

}
