package com.example.coursereco.desktop;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class PlannerView extends BorderPane {

    private final ApiClient api = new ApiClient("http://localhost:8080");

    private final ComboBox<JsonMini.StudentItem> studentCombo = new ComboBox<>();
    private final TextField interestsField = new TextField("Entrepreneurship");
    private final TextField limitField = new TextField("8");

    private final ListView<JsonMini.AgentReco> recoList = new ListView<>();
    private final ListView<String> planList = new ListView<>();
    private final Label status = new Label("");

    public PlannerView() {
        setPadding(new Insets(12));
        setTop(buildTop());
        setLeft(buildLeft());
        setCenter(buildCenter());
        setRight(buildRight());

        loadStudents();
    }

    private Pane buildTop() {
        Label title = new Label("Course Recommendation System (JavaFX + Spring Boot + MySQL + LLM Agent)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        BorderPane p = new BorderPane();
        p.setLeft(title);
        p.setRight(status);
        return p;
    }

    private Pane buildLeft() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setPrefWidth(340);

        interestsField.setPromptText("Extra interests (comma separated)");
        limitField.setPromptText("Limit");

        Button refreshStudents = new Button("Refresh Students");
        refreshStudents.setOnAction(e -> loadStudents());

        Button recommend = new Button("Get AI Recommendations");
        recommend.setOnAction(e -> loadRecommendations());

        box.getChildren().addAll(
                new Label("Student"),
                studentCombo,
                refreshStudents,
                new Label("Extra interests"),
                interestsField,
                new Label("Limit"),
                limitField,
                recommend
        );

        return box;
    }

    private Pane buildCenter() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        recoList.setCellFactory(_ -> new ListCell<>() {
            @Override protected void updateItem(JsonMini.AgentReco item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }

                setText(item.courseCode() + " | " + item.title() + " | " + item.credits() +
                        "cr | score " + String.format("%.2f", item.score()) +
                        "\nAgent: " + item.agentReason() +
                        "\nMajor fit: " + item.whyFitMajor() +
                        "\nInterest fit: " + item.whyFitInterests());
            }
        });

        Button add = new Button("Add Selected → Plan");
        add.setOnAction(e -> {
            var sel = recoList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            planList.getItems().add(sel.courseCode() + " - " + sel.title());
        });

        box.getChildren().addAll(new Label("AI Recommended Courses"), recoList, add);
        VBox.setVgrow(recoList, Priority.ALWAYS);
        return box;
    }

    private Pane buildRight() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setPrefWidth(340);

        Button remove = new Button("Remove Selected");
        remove.setOnAction(e -> {
            int idx = planList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) planList.getItems().remove(idx);
        });

        box.getChildren().addAll(new Label("Term Plan (local MVP)"), planList, remove);
        VBox.setVgrow(planList, Priority.ALWAYS);
        return box;
    }

    private void loadStudents() {
        try {
            status.setText("Loading students...");
            String json = api.get("/api/students");
            List<JsonMini.StudentItem> students = JsonMini.parseStudents(json);
            studentCombo.setItems(FXCollections.observableArrayList(students));
            if (!students.isEmpty()) studentCombo.getSelectionModel().select(0);
            status.setText("Students loaded.");
        } catch (Exception e) {
            status.setText("Failed to load students.");
        }
    }

    private void loadRecommendations() {
        try {
            var student = studentCombo.getSelectionModel().getSelectedItem();
            if (student == null) return;

            int limit = Integer.parseInt(limitField.getText().trim());
            List<String> extras = splitCsv(interestsField.getText());
            String extrasJson = toJsonArray(extras);

            String payload =
                    "{"
                    + "\"studentId\":" + student.id() + ","
                    + "\"limit\":" + limit + ","
                    + "\"extraInterests\":" + extrasJson
                    + "}";

            status.setText("Calling AI agent...");
            String res = api.postJson("/api/agent/recommend", payload);
            recoList.setItems(FXCollections.observableArrayList(JsonMini.parseAgentRecos(res)));
            status.setText("Done.");
        } catch (Exception e) {
            recoList.setItems(FXCollections.observableArrayList());
            status.setText("AI recommend failed (check backend logs + OPENAI_API_KEY).");
        }
    }

    private List<String> splitCsv(String s) {
        List<String> out = new ArrayList<>();
        if (s == null || s.isBlank()) return out;
        for (String p : s.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private String toJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i=0;i<items.size();i++) {
            if (i>0) sb.append(",");
            sb.append("\"").append(items.get(i).replace("\"","\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}
