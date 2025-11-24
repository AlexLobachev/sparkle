package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@Slf4j
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Email
    @Column(unique = true, nullable = false)
    private String email;
    @NotBlank
    @Column(name = "external_id")
    private String externalId;
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender")
    private Gender preferredGender;
    @Temporal(TemporalType.DATE)
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "about_me")
    @Size(max = 200, message = "Максимальная длина сообщения не может быть больше 200 символов")
    private String aboutMe;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    @ToString.Exclude
    private City city;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    private List<UserPhoto> photos = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<UserInterest> interests = new ArrayList<>();
    @Column(name = "email_pending")
    private boolean emailPending = true;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserRoles> roles = new HashSet<>();


    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoles().getRole().toUpperCase(Locale.ROOT)))
                .collect(Collectors.toSet());
    }


    @Override
    public String getPassword() {
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return isEmailPending() == user.isEmailPending() && Objects.equals(getId(), user.getId()) && Objects.equals(getUsername(), user.getUsername()) && Objects.equals(getEmail(), user.getEmail()) && Objects.equals(getExternalId(), user.getExternalId()) && getProvider() == user.getProvider() && getGender() == user.getGender() && getPreferredGender() == user.getPreferredGender() && Objects.equals(getBirthDate(), user.getBirthDate()) && Objects.equals(getAboutMe(), user.getAboutMe()) && Objects.equals(getRoles(), user.getRoles()) && getStatus() == user.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getEmail(), getExternalId(), getProvider(), getGender(), getPreferredGender(), getBirthDate(), getAboutMe(), isEmailPending(), getRoles(), getStatus());
    }
}
