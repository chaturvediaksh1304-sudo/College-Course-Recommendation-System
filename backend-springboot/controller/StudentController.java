package com.example.coursereco.controller;

import com.example.coursereco.model.StudentProfile;
import com.example.coursereco.repo.StudentRepo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepo repo;

    public StudentController(StudentRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<StudentProfile> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public StudentProfile one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
    }
}
