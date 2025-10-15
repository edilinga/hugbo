package com.team.gym.model;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "bookings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","class_session_id"}))
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false) @JoinColumn(name="class_session_id")
    private ClassSession classSession;

    @Column(nullable=false) private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public ClassSession getClassSession() { return classSession; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setClassSession(ClassSession classSession) { this.classSession = classSession; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
