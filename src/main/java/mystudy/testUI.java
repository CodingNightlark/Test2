package mystudy;
// built by chatGPT to try to test if javafx works, replace with your own code
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class testUI extends Application {
    private TextField subjectField;
    private TextField targetField;
    private Button initButton, startButton, stopButton;
    private Label timerLabel;
    private TextArea logArea;
    private final ObservableList<Subject> subjects =
        FXCollections.observableArrayList();  

    private Subject subject;
    private GoalTracker tracker;
    private StudySession currentSession;
    private Timeline timeline;
    private List<StudySession> sessions = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        subjectField = new TextField();
        subjectField.setPromptText("Subject Name");
        targetField = new TextField();
        targetField.setPromptText("Target Hours");

        initButton = new Button("Initialize Tracker");
        initButton.setOnAction(e -> initializeTracker());

        startButton = new Button("Start Session");
        stopButton  = new Button("Stop Session");
        startButton.setDisable(true);
        stopButton.setDisable(true);

        startButton.setOnAction(e -> startSession());
        stopButton.setOnAction(e -> stopSession());

        timerLabel = new Label("00:00:00");

        logArea = new TextArea();
        logArea.setPrefRowCount(10);
        logArea.setEditable(false);

        VBox root = new VBox(10,
            new HBox(5, subjectField, targetField, initButton),
            new HBox(5, startButton, stopButton, timerLabel),
            new Label("Session Log:"),
            logArea
        );
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Study Tracker UI");
        stage.show();
    }

    private void initializeTracker() {
        String name = subjectField.getText().trim();
        double target;
        try {
            target = Double.parseDouble(targetField.getText().trim());
        } catch (NumberFormatException ex) {
            alert("Invalid target hours");
            return;
        }
        subject = new Subject(name, 0, 0);
        tracker = new GoalTracker(subject, target);
        log("Initialized subject: " + name + " (Target: " + target + " hrs)");
        subjectField.setDisable(true);
        targetField.setDisable(true);
        initButton.setDisable(true);
        startButton.setDisable(false);
    }

    private void startSession() { 
        currentSession = new StudySession(subject.getName());
        currentSession.startSession();
        startButton.setDisable(true);
        stopButton.setDisable(false);
        startTimer();
        log("Session started...");
    }

    private void stopSession() {
        currentSession.endSession();
        stopTimer();
        long millis = currentSession.getSessionDuration();
        double hrs = millis / (1000.0 * 60 * 60);
        tracker.updateGoal(hrs);
        sessions.add(currentSession);

        log(String.format("Session stopped: %.2f hrs", hrs));
        log(String.format("Total studied: %.2f hrs | Remaining: %.2f hrs (%.1f%%)",
            subject.getTotalHours(),
            tracker.getHoursRemaining(),
            tracker.getPercentComplete()
        ));
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void startTimer() {
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopTimer() {
        if (timeline != null) timeline.stop();
        timerLabel.setText("00:00:00");
    }

    private void updateTimer() {
        // calculate “live” elapsed time
        long now     = System.currentTimeMillis();
        long start   = currentSession.getTimer().getStartTime();
        long elapsed = now - start;
    
        long hours   = elapsed / 3_600_000;
        long minutes = (elapsed % 3_600_000) / 60_000;
        long secs    = (elapsed % 60_000) / 1000;
    
        timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
    }
    
    

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
    
 