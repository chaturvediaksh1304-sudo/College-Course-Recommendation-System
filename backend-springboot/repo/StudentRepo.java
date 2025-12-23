package com.example.coursereco.repo;

import com.example.coursereco.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepo extends JpaRepository<StudentProfile, Long> {}
