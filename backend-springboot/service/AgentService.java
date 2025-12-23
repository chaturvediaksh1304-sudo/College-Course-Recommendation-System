package com.example.coursereco.service;

import com.example.coursereco.dto.AgentRecommendation;
import com.example.coursereco.dto.AgentRecommendRequest;
import com.example.coursereco.dto.Recommendation;
import com.example.coursereco.model.Course;
import com.example.coursereco.model.StudentProfile;
import com.example.coursereco.repo.CourseRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final RuleScoringService rules;
    private final CourseRepo courseRepo;
    private final OpenAIResponsesClient openai;
    private final ObjectMapper mapper = new ObjectMapper();

    public AgentService(RuleScoringService rules, CourseRepo courseRepo, OpenAIResponsesClient openai) {
        this.rules = rules;
        this.courseRepo = courseRepo;
        this.openai = openai;
    }

    public List<AgentRecommendation> agentRecommend(AgentRecommendRequest req) {
        StudentProfile s = rules.mustGetStudent(req.studentId());
        int limit = req.limit() <= 0 ? 8 : Math.min(req.limit(), 12);

        // Step 1: pick top candidates via rule scoring
        List<Recommendation> top = rules.topRuleRecommendations(req.studentId(), 25, req.extraInterests());

        // Build compact candidate list
        String candidates = top.stream().map(r ->
                r.courseCode() + " | " + r.title() + " | " + r.credits() + "cr | ruleScore=" + String.format("%.2f", r.score()) +
                        " | ruleReason=" + r.reason()
        ).collect(Collectors.joining("\n"));

        // Step 2: LLM chooses best N + explains as JSON
        String instructions =
                "You are an academic advising agent. You must ONLY choose from the provided candidate course codes.\n" +
                "Return STRICT JSON ONLY: an array of objects with keys:\n" +
                "courseCode (string), agentReason (string), whyFitMajor (string), whyFitInterests (string).\n" +
                "No markdown, no extra text.";

        String input =
                "Student:\n" +
                "name=" + s.getName() + "\n" +
                "major=" + s.getMajor() + "\n" +
                "year=" + s.getYear() + "\n" +
                "interests=" + s.getInterests() + "\n" +
                "extraInterests=" + req.extraInterests() + "\n" +
                "completed=" + s.getCompletedCourseCodes() + "\n\n" +
                "Candidates (choose the BEST " + limit + "):\n" + candidates;

        String llmText;
        try {
            llmText = openai.generateText(instructions, input);
        } catch (Exception e) {
            // If LLM fails, return rule-based top results
            return top.stream().limit(limit).map(r -> new AgentRecommendation(
                    r.courseCode(), r.title(), r.credits(), r.score(),
                    "LLM unavailable; using rule-based ranking.",
                    "Matches major/rules.",
                    "Matches interests/tags where applicable."
            )).toList();
        }

        // Step 3: parse + validate
        List<AgentRecommendation> out = parseAndValidate(llmText, s, top, limit);

        // If parsing failed, fallback
        if (out.isEmpty()) {
            return top.stream().limit(limit).map(r -> new AgentRecommendation(
                    r.courseCode(), r.title(), r.credits(), r.score(),
                    "Could not parse LLM response; using rule-based ranking.",
                    "Matches major/rules.",
                    "Matches interests/tags where applicable."
            )).toList();
        }
        return out;
    }

    private List<AgentRecommendation> parseAndValidate(String llmJson, StudentProfile s,
                                                       List<Recommendation> ruleTop, int limit) {
        try {
            JsonNode arr = mapper.readTree(llmJson);
            if (!arr.isArray()) return List.of();

            // Map ruleTop by code for score + title
            Map<String, Recommendation> ruleMap = ruleTop.stream()
                    .collect(Collectors.toMap(Recommendation::courseCode, x -> x, (a,b) -> a));

            List<AgentRecommendation> out = new ArrayList<>();
            for (JsonNode n : arr) {
                String code = n.path("courseCode").asText("").trim();
                if (code.isEmpty()) continue;

                Recommendation base = ruleMap.get(code);
                if (base == null) continue; // enforce "only from candidates"

                // enforce prereqs & not completed (double safety)
                Optional<Course> courseOpt = courseRepo.findByCode(code);
                if (courseOpt.isEmpty()) continue;
                Course c = courseOpt.get();

                if (rules.isCompleted(s, c)) continue;
                if (!rules.prereqsMet(s, c)) continue;

                out.add(new AgentRecommendation(
                        code,
                        base.title(),
                        base.credits(),
                        base.score(),
                        n.path("agentReason").asText(""),
                        n.path("whyFitMajor").asText(""),
                        n.path("whyFitInterests").asText("")
                ));
                if (out.size() >= limit) break;
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }
}
