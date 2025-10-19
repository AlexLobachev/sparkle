package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    //@Enumerated(EnumType.STRING)
    private String gender;
    @Temporal(TemporalType.DATE)
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    // Связь с интересами пользователя
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private Set<Interest> interests = new HashSet<>();

    // Список фотографий пользователя
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPhoto> photos = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return getId().equals(user.getId()) && getUsername().equals(user.getUsername()) && getPassword().equals(user.getPassword()) && getEmail().equals(user.getEmail()) && getGender().equals(user.getGender()) && getBirthDate().equals(user.getBirthDate()) && getCity().equals(user.getCity()) && getInterests().equals(user.getInterests()) && getPhotos().equals(user.getPhotos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getPassword(), getEmail(), getGender(), getBirthDate(), getCity(), getInterests(), getPhotos());
    }
}
