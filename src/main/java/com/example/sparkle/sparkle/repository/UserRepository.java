package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
