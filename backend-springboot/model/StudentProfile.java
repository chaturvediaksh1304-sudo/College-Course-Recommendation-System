package com.example.coursereco.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class StudentProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private String major;

    private String year;

    @ElementCollection
    @CollectionTable(name="student_interests", joinColumns=@JoinColumn(name="student_id"))
    @Column(name="interest")
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name="student_completed_courses", joinColumns=@JoinColumn(name="student_id"))
    @Column(name="course_code")
    private Set<String> completedCourseCodes = new HashSet<>();
}
