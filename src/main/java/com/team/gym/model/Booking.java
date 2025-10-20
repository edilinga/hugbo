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

    //edil

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    private Instant cancelledAt;

    //

    public Long getId() { return id; }
    public User getUser() { return user; }
    public ClassSession getClassSession() { return classSession; }
    public Instant getCreatedAt() { return createdAt; }

    public BookingStatus getStatus() { return status; }
    public Instant getCancelledAt() { return cancelledAt; }

    public void setId(long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setClassSession(ClassSession classSession) { this.classSession = classSession; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void setStatus(BookingStatus status) { this.status = status; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}
