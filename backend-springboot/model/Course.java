package com.example.coursereco.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String code;

    @Column(nullable=false)
    private String title;

    private String department;
    private int credits;
    private String level;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name="course_tags", joinColumns=@JoinColumn(name="course_id"))
    @Column(name="tag")
    private Set<String> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name="course_prereq_codes", joinColumns=@JoinColumn(name="course_id"))
    @Column(name="prereq_code")
    private Set<String> prereqCodes = new HashSet<>();
}
