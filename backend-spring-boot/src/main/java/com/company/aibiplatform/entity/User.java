package com.company.aibiplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents both an authenticated account AND the "employee" who can be
 * attached to a Sale as the processor. The README's ERD models Employee as
 * a separate entity from Authentication; we merge them here for simplicity
 * — every staff member who can log in is inherently an "employee" for
 * sales-tracking purposes. Split this into two entities later if you need
 * HR-specific fields (hire date, salary, etc.) independent of login.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (!this.enabled) {
            this.enabled = true;
        }
    }
}