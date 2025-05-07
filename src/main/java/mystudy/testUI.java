package mystudy;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Main UI for the Study Tracker, now includes a BarChart of sessions.
 */
public class testUI extends Application {

    private static final Path SESSIONS_FILE = Path.of(
        System.getProperty("user.home"), ".study-tracker", "sessions.json");
    private static final Gson GSON = new Gson();

    public enum Theme { PURPLE, LIGHT, DARK;
        public String stylesheet() {
            String filename = switch(this) {
                case PURPLE -> "purple.css";
                case LIGHT  -> "light.css";
                default    -> "dark.css";
            };
            URL url = getClass().getResource("/themes/" + filename);
            if (url == null) throw new RuntimeException("Missing CSS: " + filename);
            return url.toExternalForm();
        }
    }

    private Theme currentTheme;
    private final Preferences prefs = Preferences.userNodeForPackage(testUI.class);
    private Scene mainScene;

    private ComboBox<Subject> subjectCombo;
    private Button addSubjectButton, settingsButton, startButton, stopButton;
    private Label timerLabel;
    private ListView<String> logList;
    private ListView<StudySession> pastSessionsList;
    private SessionBarChart chartPane;

    private List<Subject> subjects = new ArrayList<>();
    private List<StudySession> sessions = new ArrayList<>();
    private ObservableList<String> logItems = FXCollections.observableArrayList();

    private Subject subject;
    private GoalTracker tracker;
    private StudySession currentSession;
    private Timeline timeline;

    @Override
    public void start(Stage stage) {
        // Load preferences and sessions
        currentTheme = Theme.valueOf(prefs.get("theme", Theme.PURPLE.name()));
        subjects = Storage.load();
        loadSessions();

        // Top controls
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
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Session controls
        startButton = new Button("Start Session");
        stopButton = new Button("Stop Session");
        startButton.getStyleClass().add("button");
        stopButton.getStyleClass().add("button");
        startButton.setDisable(true);
        stopButton.setDisable(true);
        startButton.setOnAction(e -> startSession());
        stopButton.setOnAction(e -> stopSession());
        timerLabel = new Label("00:00:00");
        timerLabel.getStyleClass().add("timer-label");
        HBox controlRow = new HBox(10, startButton, stopButton, timerLabel);
        controlRow.setAlignment(Pos.CENTER_LEFT);

        // Live session log
        logList = new ListView<>(logItems);
        logList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    setText(msg);
                    setFont(Font.font("Segoe UI", 14));
                    setPadding(new Insets(8, 12, 8, 12));
                    if (msg.startsWith("Session started")) setTextFill(Color.web("#0066CC"));
                    else if (msg.toLowerCase().contains("error")) setTextFill(Color.web("#CC0000"));
                    else setTextFill(Color.web("#333333"));
                }
            }
        });
        logItems.addListener((ListChangeListener<String>) c -> logList.scrollTo(logItems.size() - 1));
        VBox logContainer = new VBox(10, new Label("Session Log:"), logList);
        logContainer.setAlignment(Pos.TOP_CENTER);
        logContainer.setPadding(new Insets(20));
        logContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
        logContainer.setEffect(new DropShadow(5, Color.gray(0.3)));

        // Past sessions
        pastSessionsList = new ListView<>();
        pastSessionsList.setCellFactory(lv -> new ListCell<>() {
            private final Button delBtn = new Button("🗑️");
            private final HBox row = new HBox(8);
            private final Label lbl = new Label();
            {
                row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().addAll(lbl, delBtn);
                delBtn.setOnAction(e -> {
                    StudySession s = getItem();
                    sessions.remove(s);
                    saveSessions();
                    double hrs = s.getSessionDuration() / 3_600_000.0;
                    subject.updateTotalHours(-hrs);
                    tracker.updateGoal(-hrs);
                    pastSessionsList.getItems().remove(s);
                    updateChart();
                });
            }
            @Override
            protected void updateItem(StudySession s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setGraphic(null);
                } else {
                    String time = String.format("%.2f hrs", s.getSessionDuration() / 3_600_000.0);
                    lbl.setText(s.getStartTimeFormatted() + " → " + time);
                    lbl.setFont(Font.font("Segoe UI", 14));
                    setGraphic(row);
                }
            }
        });
        VBox pastContainer = new VBox(10, new Label("Past Sessions"), pastSessionsList);
        pastContainer.setAlignment(Pos.TOP_CENTER);
        pastContainer.setPadding(new Insets(20));
        pastContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
        pastContainer.setEffect(new DropShadow(5, Color.gray(0.3)));

        // Chart pane
        chartPane = new SessionBarChart(Collections.emptyList());

        // Tabs
        TabPane tabs = new TabPane(
            new Tab("Tracker", new VBox(15, topRow, controlRow, logContainer, pastContainer)),
            new Tab("Chart", chartPane)
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));

        // Scene
        mainScene = new Scene(tabs, 800, 600);
        applyTheme(currentTheme, mainScene);
        stage.setScene(mainScene);
        stage.setTitle("Study Tracker");
        stage.show();

        stage.setOnCloseRequest((WindowEvent e) -> {
            Storage.save(subjects);
            saveSessions();
            prefs.put("theme", currentTheme.name());
        });
    }

    private void updateChart() {
        List<StudySession> filtered = sessions.stream()
            .filter(s -> subject != null && s.getSubject().equals(subject.getName()))
            .toList();
        chartPane.updateSessions(filtered);
    }

    private void applyTheme(Theme theme, Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(theme.stylesheet());
        currentTheme = theme;
    }

    private void openSettingsDialog() {
        Dialog<Theme> dlg = new Dialog<>();
        dlg.setTitle("Settings");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ComboBox<Theme> box = new ComboBox<>(FXCollections.observableArrayList(Theme.values()));
        box.setValue(currentTheme);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Theme:"), 0, 0);
        grid.add(box, 1, 0);
        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(b -> b == ButtonType.OK ? box.getValue() : null);
        dlg.showAndWait().ifPresent(th -> applyTheme(th, mainScene));
    }

    private void onSubjectSelected() {
        subject = subjectCombo.getValue();
        if (subject == null) return;
        tracker = new GoalTracker(subject, subject.getHoursPerWeek());
        logItems.clear();
        log("Loaded: " + subject);
        List<StudySession> filtered = sessions.stream()
            .filter(s -> s.getSubject().equals(subject.getName()))
            .toList();
        pastSessionsList.getItems().setAll(filtered);
        updateChart();
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void addSubject() {
        TextInputDialog d1 = new TextInputDialog();
        d1.setTitle("New Subject");
        d1.setHeaderText("Add subject");
        d1.setContentText("Name:");
        d1.showAndWait().ifPresent(n -> {
            TextInputDialog d2 = new TextInputDialog();
            d2.setTitle("Weekly Target");
            d2.setContentText("Hours:");
            d2.showAndWait().ifPresent(h -> {
                try {
                    int hrs = Integer.parseInt(h.trim());
                    Subject s = new Subject(n, 0.0, hrs);
                    subjects.add(s);
                    subjectCombo.getItems().add(s);
                    subjectCombo.setValue(s);
                    Storage.save(subjects);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.WARNING, "Invalid number").showAndWait();
                }
            });
        });
    }

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
        pastSessionsList.getItems().add(currentSession);
        updateChart();
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

    private void loadSessions() {
        try {
            if (Files.exists(SESSIONS_FILE)) {
                String json = Files.readString(SESSIONS_FILE);
                sessions = GSON.fromJson(json, new TypeToken<List<StudySession>>(){}.getType());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveSessions() {
        try {
            Files.createDirectories(SESSIONS_FILE.getParent());
            Files.writeString(SESSIONS_FILE, GSON.toJson(sessions));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void log(String msg) {
        logItems.add(msg);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
