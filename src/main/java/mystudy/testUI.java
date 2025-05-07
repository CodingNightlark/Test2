package mystudy;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import mystudy.Storage;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class testUI extends Application {
    private static final Path SESSIONS_FILE = Path.of(
            System.getProperty("user.home"), ".study-tracker", "sessions.json");
    private static final Gson GSON = new Gson();

    // UI controls
    private ComboBox<Subject> subjectCombo;
    private Button addSubjectButton;
    private Button startButton;
    private Button stopButton;
    private Label timerLabel;
    private TextArea logArea;

    // Core model
    private Subject subject;
    private GoalTracker tracker;
    private StudySession currentSession;
    private Timeline timeline;

    // Data
    private List<Subject> subjects;
    private List<StudySession> sessions = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        // Top: subject selector + add button
        subjects = Storage.load();   
        subjectCombo = new ComboBox<>(FXCollections.observableArrayList(subjects));
        subjectCombo.setPromptText("Select subject");
        subjectCombo.setOnAction(e -> onSubjectSelected());

        addSubjectButton = new Button("+");
        addSubjectButton.getStyleClass().add("button");
        addSubjectButton.setOnAction(e -> addSubject());

        HBox topRow = new HBox(10, subjectCombo, addSubjectButton);

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

        HBox controlRow = new HBox(10, startButton, stopButton, timerLabel);

        // Log area
        logArea = new TextArea();
        logArea.setPrefRowCount(10);
        logArea.setEditable(false);
        logArea.getStyleClass().add("text-area");

        VBox root = new VBox(15, 
                topRow,
                controlRow,
                new Label("Session Log:"),
                logArea
        );
        root.setPadding(new Insets(20));

        // Load past sessions
        loadSessions();

        // Save on exit
        stage.setOnCloseRequest((WindowEvent e) -> {
            Storage.save(subjects);
            saveSessions();
        });

        // Scene & CSS
        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        stage.setTitle("Study Tracker");
        stage.setScene(scene);
        stage.show();
    }

    private void onSubjectSelected() {
        subject = subjectCombo.getValue();
        tracker = new GoalTracker(subject, subject.getHoursPerWeek());

        logArea.clear();
        log("Loaded: " + subject);
        sessions.stream()
                .filter(s -> s.getSubject().equals(subject.getName()))
                .forEach(s -> {
                    double hrs = s.getSessionDuration() / 3_600_000.0;
                    log("Past session: " + String.format("%.2f hrs", hrs));
                });

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void addSubject() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("New Subject");
        nameDialog.setHeaderText("Add a new study subject");
        nameDialog.setContentText("Subject name:");
        Optional<String> nameRes = nameDialog.showAndWait();
        nameRes.ifPresent(name -> {
            TextInputDialog targetDialog = new TextInputDialog();
            targetDialog.setTitle("Target Hours");
            targetDialog.setHeaderText("Set weekly target for " + name);
            targetDialog.setContentText("Target hours:");
            Optional<String> targetRes = targetDialog.showAndWait();
            targetRes.ifPresent(tstr -> {
                try {
                    int t = Integer.parseInt(tstr.trim());
                    Subject newSub = new Subject(name, 0.0, t);
                    subjects.add(newSub);
                    subjectCombo.getItems().add(newSub);
                    subjectCombo.getSelectionModel().select(newSub);
                    Storage.save(subjects);
                } catch (NumberFormatException ex) {
                    alert("Invalid number for target hours.");
                }
            });
        });
    }

    private void startSession() {
        if (subject == null) return;
        currentSession = new StudySession(subject.getName());
        currentSession.startSession();
        startButton.setDisable(true);
        stopButton.setDisable(false);
        startTimer();
        log("Session started for " + subject.getName());
    }

    private void stopSession() {
        currentSession.endSession();
        stopTimer();
        double hrs = currentSession.getSessionDuration() / 3_600_000.0;
        subject.updateTotalHours(hrs);
        tracker.updateGoal(hrs);

        log("Session stopped: " + String.format("%.2f hrs", hrs));
        log("Updated: " + subject);

        sessions.add(currentSession);
        saveSessions();

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
        long h = elapsed / 3_600_000;
        long m = (elapsed % 3_600_000) / 60_000;
        long s = (elapsed % 60_000) / 1000;
        timerLabel.setText(String.format("%02d:%02d:%02d", h, m, s));
    }

    private void loadSessions() {
        try {
            if (Files.exists(SESSIONS_FILE)) {
                String json = Files.readString(SESSIONS_FILE);
                sessions = GSON.fromJson(json,
                        new TypeToken<List<StudySession>>() {}.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSessions() {
        try {
            Files.createDirectories(SESSIONS_FILE.getParent());
            String json = GSON.toJson(sessions);
            Files.writeString(SESSIONS_FILE, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
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