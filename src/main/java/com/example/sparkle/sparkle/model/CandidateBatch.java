package com.example.sparkle.sparkle.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "candidate_batch")
public class CandidateBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_batch_candidates", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "candidate_id")
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<Long> candidateIds = new ArrayList<>();


    private int currentIndex = 0;
    private LocalDateTime expiresAt;

    // Метод для безопасного обновления списка
    public void setCandidateIds(List<Long> candidateIds) {
        this.candidateIds.clear();
        this.candidateIds.addAll(candidateIds);
    }
}

