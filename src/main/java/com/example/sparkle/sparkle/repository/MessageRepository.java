package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message,Long> {
}
