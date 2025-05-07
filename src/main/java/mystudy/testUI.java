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

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Study‑Tracker UI
 * ----------------
 * ‑ Dropdown of subjects (with "+" to add)
 * ‑ ⚙ Settings button lets the user switch colour theme at runtime
 * ‑ Sessions and subjects are persisted in JSON under ~/.study‑tracker/
 */
public class testUI extends Application {

    /* ---------- Persistence ---------- */
    private static final Path SESSIONS_FILE = Path.of(
            System.getProperty("user.home"), ".study-tracker", "sessions.json");
    private static final Gson GSON = new Gson();

    /* ---------- Theme handling ---------- */
    private Theme currentTheme = Theme.PURPLE;   // default
    private final Preferences prefs = Preferences.userNodeForPackage(testUI.class);

    /* ---------- UI controls ---------- */
    private ComboBox<Subject> subjectCombo;
    private Button addSubjectButton;
    private Button settingsButton;
    private Button startButton;
    private Button stopButton;
    private Label  timerLabel;
    private TextArea logArea;

    /* ---------- Core state ---------- */
    private List<Subject> subjects  = new ArrayList<>();
    private List<StudySession> sessions = new ArrayList<>();
    private Subject      subject;
    private GoalTracker  tracker;
    private StudySession currentSession;
    private Timeline     timeline;

    @Override
    public void start(Stage stage) {
        /* --- 1. Restore theme & data --- */
        currentTheme = Theme.valueOf(prefs.get("theme", Theme.PURPLE.name()));
        subjects = Storage.load();
        loadSessions();

        /* --- 2. Build subject selector row --- */
        subjectCombo = new ComboBox<>(FXCollections.observableArrayList(subjects));
        subjectCombo.setPromptText("Select subject");
        subjectCombo.setOnAction(e -> onSubjectSelected());

        addSubjectButton = new Button("+");
        addSubjectButton.getStyleClass().add("button");
        addSubjectButton.setOnAction(e -> addSubject());

        settingsButton = new Button("⚙");
        settingsButton.getStyleClass().add("button");
        settingsButton.setOnAction(e -> openSettingsDialog());

        HBox topRow = new HBox(10, subjectCombo, addSubjectButton, settingsButton);

        /* --- 3. Session controls row --- */
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

        /* --- 4. Log area --- */
        logArea = new TextArea();
        logArea.setPrefRowCount(10);
        logArea.setEditable(false);
        logArea.getStyleClass().add("text-area");

        VBox root = new VBox(15,
                topRow,
                controlRow,
                new Label("Session Log:"),
                logArea);
        root.setPadding(new Insets(20));

        /* --- 5. Scene & stage --- */
        Scene scene = new Scene(root, 660, 500);
        applyTheme(currentTheme, scene);

        stage.setTitle("Study Tracker");
        stage.setScene(scene);
        stage.show();

        /* --- 6. Persist on exit --- */
        stage.setOnCloseRequest((WindowEvent e) -> {
            Storage.save(subjects);
            saveSessions();
            prefs.put("theme", currentTheme.name());
        });
    }

    /* ---------- Theme helpers ---------- */
    private void applyTheme(Theme theme, Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(theme.stylesheet());
        currentTheme = theme;
    }

    private void openSettingsDialog() {
        Dialog<Theme> dlg = new Dialog<>();
        dlg.setTitle("Settings");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Theme> themeBox = new ComboBox<>(FXCollections.observableArrayList(Theme.values()));
        themeBox.setValue(currentTheme);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Colour theme:"), 0, 0);
        grid.add(themeBox, 1, 0);

        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> btn == ButtonType.OK ? themeBox.getValue() : null);

        dlg.showAndWait().ifPresent(th -> applyTheme(th, subjectCombo.getScene()));
    }

    /* ---------- Subject handling ---------- */
    private void onSubjectSelected() {
        subject = subjectCombo.getValue();
        if (subject == null) return;
        tracker = new GoalTracker(subject, subject.getHoursPerWeek());

        logArea.clear();
        log("Loaded: " + subject);
        sessions.stream()
                .filter(s -> s.getSubject().equals(subject.getName()))
                .forEach(s -> log(String.format("Past session: %.2f hrs", s.getSessionDuration() / 3_600_000.0)));

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void addSubject() {
        TextInputDialog askName   = new TextInputDialog();
        askName.setTitle("New Subject");
        askName.setHeaderText("Add a new study subject");
        askName.setContentText("Subject name:");
        askName.showAndWait().ifPresent(name -> {
            TextInputDialog askTarget = new TextInputDialog();
            askTarget.setTitle("Weekly Target");
            askTarget.setHeaderText("Weekly target hours for " + name);
            askTarget.setContentText("Hours:");
            askTarget.showAndWait().ifPresent(tstr -> {
                try {
                    int t = Integer.parseInt(tstr.trim());
                    Subject newSub = new Subject(name, 0.0, t);
                    subjects.add(newSub);
                    subjectCombo.getItems().add(newSub);
                    subjectCombo.getSelectionModel().select(newSub);
                    Storage.save(subjects);
                } catch (NumberFormatException ex) {
                    alert("Invalid number for hours");
                }
            });
        });
    }

    /* ---------- Session handling ---------- */
    private void startSession() {
        if (subject == null) return;
        currentSession = new StudySession(subject.getName());
        currentSession.startSession();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        startButton.setDisable(true);
        stopButton.setDisable(false);
        log("Session started for " + subject.getName());
    }

    private void stopSession() {
        currentSession.endSession();
        if (timeline != null) timeline.stop();
        timerLabel.setText("00:00:00");

        double hrs = currentSession.getSessionDuration() / 3_600_000.0;
        subject.updateTotalHours(hrs);
        tracker.updateGoal(hrs);
        sessions.add(currentSession);
        saveSessions();

        log(String.format("Session stopped: %.2f hrs", hrs));
        log("Updated: " + subject);

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void updateTimer() {
        long elapsed = System.currentTimeMillis() - currentSession.getTimer().getStartTime();
        long h = elapsed / 3_600_000;
        long m = (elapsed % 3_600_000) / 60_000;
        long s = (elapsed % 60_000) / 1000;
        timerLabel.setText(String.format("%02d:%02d:%02d", h, m, s));
    }

    /* ---------- Persistence helpers ---------- */
    private void loadSessions() {
        try {
            if (Files.exists(SESSIONS_FILE)) {
                String json = Files.readString(SESSIONS_FILE);
                sessions = GSON.fromJson(json, new TypeToken<List<StudySession>>(){}.getType());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveSessions() {
        try {
            Files.createDirectories(SESSIONS_FILE.getParent());
            Files.writeString(SESSIONS_FILE, GSON.toJson(sessions));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /* ---------- Utility ---------- */
    private void log(String msg) { logArea.appendText(msg + ""); }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
/* 
enum Theme {
    PURPLE("purple.css"),
    BLUE("blue.css"),
    GREEN("green.css");

    private final String stylesheet;

    Theme(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public String stylesheet() {
        return getClass().getResource(stylesheet).toExternalForm();
    }
}*/