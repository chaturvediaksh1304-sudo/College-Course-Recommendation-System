package com.example.coursereco.config;

import com.example.coursereco.model.Course;
import com.example.coursereco.model.StudentProfile;
import com.example.coursereco.repo.CourseRepo;
import com.example.coursereco.repo.StudentRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CourseRepo courseRepo;
    private final StudentRepo studentRepo;

    public DataSeeder(CourseRepo courseRepo, StudentRepo studentRepo) {
        this.courseRepo = courseRepo;
        this.studentRepo = studentRepo;
    }

    @Override
    public void run(String... args) {
        if (courseRepo.count() > 0 || studentRepo.count() > 0) return;

        // ---- CS
        saveCourse("CPS 101", "Intro to Programming", "CPS", 3, "100",
                "Programming basics in Java/Python-style thinking.",
                Set.of("programming", "foundations"),
                Set.of());

        saveCourse("CPS 210", "Data Structures", "CPS", 3, "200",
                "Arrays, lists, trees, graphs, Big-O.",
                Set.of("algorithms", "programming"),
                Set.of("CPS 101"));

        saveCourse("CPS 315", "Machine Learning", "CPS", 3, "300",
                "Supervised/unsupervised learning, evaluation, bias.",
                Set.of("ai", "ml", "data"),
                Set.of("CPS 210"));

        saveCourse("CPS 340", "Deep Learning", "CPS", 3, "300",
                "Neural nets, CNNs, transformers (conceptual + applied).",
                Set.of("ai", "ml", "deep-learning"),
                Set.of("CPS 315"));

        saveCourse("CPS 360", "Applied Data Science", "CPS", 3, "300",
                "Pipelines, cleaning, features, deployment basics.",
                Set.of("data", "ml", "analytics"),
                Set.of("CPS 210"));

        saveCourse("CPS 370", "Cybersecurity Fundamentals", "CPS", 3, "300",
                "Threat models, risk, basics of security engineering.",
                Set.of("cybersecurity", "networks"),
                Set.of("CPS 210"));

        // ---- Neuroscience / Psych / Bio
        saveCourse("NSC 110", "Intro to Neuroscience", "NSC", 3, "100",
                "Neurons, brain systems, behavior overview.",
                Set.of("neuroscience", "brain"),
                Set.of());

        saveCourse("NSC 250", "Neurophysiology", "NSC", 4, "200",
                "Neural signaling, synapses, circuits, electrophysiology.",
                Set.of("neuroscience", "physiology"),
                Set.of("NSC 110"));

        saveCourse("PSY 200", "Research Methods", "PSY", 3, "200",
                "Study design, ethics, basic statistics.",
                Set.of("research", "statistics", "data"),
                Set.of());

        saveCourse("BIO 230", "Genetics", "BIO", 3, "200",
                "Genes, expression, inheritance, molecular basics.",
                Set.of("biology", "genetics"),
                Set.of());

        // ---- Entrepreneurship / Business
        saveCourse("ENT 101", "Intro to Entrepreneurship", "ENT", 3, "100",
                "Ideation, validation, pitching, MVP thinking.",
                Set.of("entrepreneurship", "startup", "pitching"),
                Set.of());

        saveCourse("BUS 210", "Marketing Principles", "BUS", 3, "200",
                "Positioning, segmentation, consumer behavior.",
                Set.of("marketing", "business"),
                Set.of());

        saveCourse("BUS 320", "Product Management", "BUS", 3, "300",
                "Roadmaps, user research, metrics, strategy.",
                Set.of("product", "entrepreneurship", "strategy"),
                Set.of("BUS 210"));

        saveCourse("BIS 250", "Business Analytics", "BIS", 3, "200",
                "KPIs, dashboards, descriptive analytics, decisions.",
                Set.of("analytics", "data", "business"),
                Set.of());

        // ---- Demo Students
        // 1) Neuroscience + Entrepreneurship (YOUR EXAMPLE)
        StudentProfile neuro = new StudentProfile();
        neuro.setName("Demo - Neuroscience + Entrepreneurship");
        neuro.setMajor("Neuroscience");
        neuro.setYear("Sophomore");
        neuro.setInterests(Set.of("Entrepreneurship"));
        neuro.setCompletedCourseCodes(Set.of("NSC 110", "ENT 101"));
        studentRepo.save(neuro);

        // 2) Computer Science + AI/ML (YOUR EXAMPLE)
        StudentProfile cs = new StudentProfile();
        cs.setName("Demo - CS + AI/ML");
        cs.setMajor("Computer Science");
        cs.setYear("Junior");
        cs.setInterests(Set.of("AI", "ML"));
        cs.setCompletedCourseCodes(Set.of("CPS 101", "CPS 210"));
        studentRepo.save(cs);

        // 3) Another example
        StudentProfile biz = new StudentProfile();
        biz.setName("Demo - Business + Analytics/Product");
        biz.setMajor("Business");
        biz.setYear("Sophomore");
        biz.setInterests(Set.of("Data Analytics", "Product"));
        biz.setCompletedCourseCodes(Set.of("BUS 210"));
        studentRepo.save(biz);

        System.out.println("✅ Seeded demo data.");
        System.out.println("Student IDs: Neuro=" + neuro.getId() + ", CS=" + cs.getId() + ", Biz=" + biz.getId());
    }

    private void saveCourse(String code, String title, String dept, int credits, String level,
                            String desc, Set<String> tags, Set<String> prereqs) {
        Course c = new Course();
        c.setCode(code);
        c.setTitle(title);
        c.setDepartment(dept);
        c.setCredits(credits);
        c.setLevel(level);
        c.setDescription(desc);
        c.setTags(tags);
        c.setPrereqCodes(prereqs);
        courseRepo.save(c);
    }
}
