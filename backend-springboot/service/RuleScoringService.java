package com.example.coursereco.service;

import com.example.coursereco.dto.Recommendation;
import com.example.coursereco.model.Course;
import com.example.coursereco.model.StudentProfile;
import com.example.coursereco.repo.CourseRepo;
import com.example.coursereco.repo.StudentRepo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleScoringService {

    private final StudentRepo studentRepo;
    private final CourseRepo courseRepo;

    public RuleScoringService(StudentRepo studentRepo, CourseRepo courseRepo) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
    }

    public StudentProfile mustGetStudent(Long id) {
        return studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public List<Course> allCourses() {
        return courseRepo.findAll();
    }

    public boolean prereqsMet(StudentProfile s, Course c) {
        for (String prereq : c.getPrereqCodes()) {
            if (!s.getCompletedCourseCodes().contains(prereq)) return false;
        }
        return true;
    }

    public boolean isCompleted(StudentProfile s, Course c) {
        return s.getCompletedCourseCodes().contains(c.getCode());
    }

    public double score(StudentProfile s, Course c, Set<String> extraInterests) {
        Set<String> interests = new HashSet<>(s.getInterests());
        if (extraInterests != null) interests.addAll(extraInterests);

        Set<String> interestLower = interests.stream()
                .filter(Objects::nonNull)
                .map(x -> x.toLowerCase().trim())
                .collect(Collectors.toSet());

        double score = 0;

        // major match
        if (majorMatches(s.getMajor(), c.getDepartment())) score += 5;

        // tag matches
        int matches = 0;
        for (String tag : c.getTags()) {
            if (interestLower.contains(tag.toLowerCase())) matches++;
        }
        score += matches * 3;

        // small level boost
        score += levelBoost(s.getYear(), c.getLevel());

        return score;
    }

    public String ruleReason(StudentProfile s, Course c, Set<String> extraInterests) {
        List<String> bits = new ArrayList<>();
        if (majorMatches(s.getMajor(), c.getDepartment())) bits.add("Matches major/department.");
        Set<String> interests = new HashSet<>(s.getInterests());
        if (extraInterests != null) interests.addAll(extraInterests);

        Set<String> interestLower = interests.stream()
                .filter(Objects::nonNull).map(x -> x.toLowerCase().trim()).collect(Collectors.toSet());

        List<String> matchedTags = c.getTags().stream()
                .filter(t -> interestLower.contains(t.toLowerCase()))
                .toList();
        if (!matchedTags.isEmpty()) bits.add("Matches interests via tags: " + matchedTags);

        return String.join(" ", bits);
    }

    public List<Recommendation> topRuleRecommendations(Long studentId, int limit, Set<String> extraInterests) {
        StudentProfile s = mustGetStudent(studentId);
        int lim = limit <= 0 ? 10 : limit;

        List<Course> candidates = courseRepo.findAll().stream()
                .filter(c -> !isCompleted(s, c))
                .filter(c -> prereqsMet(s, c))
                .toList();

        List<Recommendation> scored = candidates.stream()
                .map(c -> new Recommendation(
                        c.getCode(),
                        c.getTitle(),
                        c.getCredits(),
                        score(s, c, extraInterests),
                        ruleReason(s, c, extraInterests)
                ))
                .sorted((a,b) -> Double.compare(b.score(), a.score()))
                .limit(lim)
                .toList();

        return scored;
    }

    private boolean majorMatches(String major, String dept) {
        if (major == null || dept == null) return false;
        String m = major.toLowerCase();
        String d = dept.toLowerCase();

        if (m.contains("computer") && (d.equals("cps") || d.equals("cs"))) return true;
        if (m.contains("neuro") && d.equals("nsc")) return true;
        if (m.contains("business") && (d.equals("bus") || d.equals("bis") || d.equals("ent"))) return true;
        if (m.contains("psych") && d.equals("psy")) return true;
        return false;
    }

    private double levelBoost(String year, String level) {
        if (year == null || level == null) return 0;
        int lvl;
        try { lvl = Integer.parseInt(level); } catch (Exception e) { return 0; }

        String y = year.toLowerCase();
        if (y.contains("fresh") && (lvl == 100 || lvl == 200)) return 1.0;
        if (y.contains("soph") && (lvl == 200 || lvl == 300)) return 1.0;
        if (y.contains("jun") && (lvl == 300 || lvl == 400)) return 1.0;
        if (y.contains("sen") && (lvl == 400)) return 1.0;
        return 0;
    }
}
