package com.team.gym.model;

import jakarta.persistence.*;

@Entity
public class ClassSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private Integer capacity;

    public ClassSession() {}
    public ClassSession(String title, Integer capacity) {
        this.title = title; this.capacity = capacity;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Integer getCapacity() { return capacity; }
    public void setTitle(String title) { this.title = title; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
