package com.team.gym.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "ssn")
    }
)

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String ssn;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "profile_image_key")
    private String profileImageKey;

    public Long getId() { return id; }
    public String getSsn() { return ssn; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Instant getCreatedAt() { return createdAt; }
    public String getProfileImageKey() { return profileImageKey; }

    public void setId(Long id) { this.id = id; }
    public void setSsn(String ssn) { this.ssn = ssn; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setProfileImageKey(String profileImageKey) { this.profileImageKey = profileImageKey; }

}
