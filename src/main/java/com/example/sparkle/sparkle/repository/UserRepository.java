package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Modifying
    @Query(value = "" +
            "UPDATE users AS u " +
            "SET username   = COALESCE (:username, u.username), " +
            "    gender     = COALESCE(:gender, u.gender), " +
            "    email      = COALESCE (:email, u.email)," +
            "    birth_date  = COALESCE (:birthDate, u.birth_date) " +
            "WHERE u.id = :id",
            nativeQuery = true)
    int userUpdate(
            @Param("username") String username,
            @Param("gender") String gender,
            @Param("email") String email,
            @Param("birthDate") LocalDate birthDate,
            @Param("id") Long userId);



}
