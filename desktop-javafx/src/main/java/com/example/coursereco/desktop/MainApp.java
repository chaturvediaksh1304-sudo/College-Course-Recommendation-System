

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Course Recommendation Planner (LLM Agent)");
        stage.setScene(new Scene(new PlannerView(), 1200, 680));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
