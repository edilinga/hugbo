package com.team.gym.controller;

import com.team.gym.model.ClassSession;
import com.team.gym.repository.ClassSessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassSessionController {
    private final ClassSessionRepository repo;
    public ClassSessionController(ClassSessionRepository repo) { this.repo = repo; }

    @GetMapping
    public List<ClassSession> list() { return repo.findAll(); }

    @PostMapping
    public ClassSession create(@RequestBody ClassSession cs) { return repo.save(cs); }
}
