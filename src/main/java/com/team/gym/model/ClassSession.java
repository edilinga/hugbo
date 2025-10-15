package com.team.gym.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "class_sessions")
public class ClassSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String type;
    @Column(nullable=false) private String teacher;
    @Column(nullable=false) private Integer capacity;

    @Column(nullable=false) private Instant startAt;
    @Column(nullable=false) private Instant endAt;

    @Column(nullable=false) private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getTeacher() { return teacher; }
    public Integer getCapacity() { return capacity; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }

    public void setId(Long id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }
    
}
