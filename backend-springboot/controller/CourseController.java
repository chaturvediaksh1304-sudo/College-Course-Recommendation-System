package com.example.coursereco.controller;

import com.example.coursereco.model.Course;
import com.example.coursereco.repo.CourseRepo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepo repo;

    public CourseController(CourseRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Course> all() {
        return repo.findAll();
    }
}
