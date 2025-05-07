package mystudy;

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
    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();

    private Subject subject;
    private GoalTracker tracker;
    private StudySession currentSession;
    private Timeline timeline;
    private List<StudySession> sessions = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        // Subject input
        subjectField = new TextField();
        subjectField.setPromptText("Subject Name");
        subjectField.getStyleClass().add("input-field");

        targetField = new TextField();
        targetField.setPromptText("Target Hours");
        targetField.getStyleClass().add("input-field");

        initButton = new Button("Initialize Tracker");
        initButton.getStyleClass().add("button");
        initButton.setOnAction(e -> initializeTracker());

        // Session controls
        startButton = new Button("Start Session");
        stopButton  = new Button("Stop Session");
        startButton.getStyleClass().add("button");
        stopButton.getStyleClass().add("button");
        startButton.setDisable(true);
        stopButton.setDisable(true);
        startButton.setOnAction(e -> startSession());
        stopButton.setOnAction(e -> stopSession());

        timerLabel = new Label("00:00:00");
        timerLabel.getStyleClass().add("timer-label");

        logArea = new TextArea();
        logArea.setPrefRowCount(10);
        logArea.setEditable(false);
        logArea.getStyleClass().add("text-area");

        // Layout
        HBox inputBox = new HBox(10, subjectField, targetField, initButton);
        HBox sessionBox = new HBox(10, startButton, stopButton, timerLabel);
        VBox root = new VBox(15, inputBox, sessionBox, new Label("Session Log:"), logArea);
        root.setPadding(new Insets(20));

        // Scene & stylesheet
        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Study Tracker");
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
        log("Initialized: " + name + " (Target: " + target + " hrs)");
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
        double hrs = millis / 3600000.0;
        tracker.updateGoal(hrs);
        sessions.add(currentSession);

        log(String.format("Session stopped: %.2f hrs", hrs));
        log(String.format("Total studied: %.2f hrs | Remaining: %.2f hrs (%.1f%%)",
            subject.getTotalHours(),
            tracker.getHoursRemaining(),
            tracker.getPercentComplete()));
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
        long now = System.currentTimeMillis();
        long start = currentSession.getTimer().getStartTime();
        long elapsed = now - start;
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long secs = (elapsed % 60000) / 1000;
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

 