package com.trustvault.user;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(
    name = "app_users",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_app_users_email",
        columnNames = "email"
    )
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    protected AppUser() {
        // Required by JPA
    }

    public AppUser(String email, String passwordHash, UserRole role) {
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }
}
