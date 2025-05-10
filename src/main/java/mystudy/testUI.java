
//  mvn clean compile exec:java
package mystudy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import java.time.format.DateTimeFormatter;

public class testUI extends Application {
    private static final Path SESSIONS_FILE = Path.of(
        System.getProperty("user.home"), ".study-tracker", "sessions.json");
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
        .create();
    
    private final Preferences prefs = Preferences.userNodeForPackage(testUI.class);
    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();
    private final ObservableList<StudySession> sessions = FXCollections.observableArrayList();
    private final ObservableList<String> logItems = FXCollections.observableArrayList();
    
    private Subject activeSubject;
    private StudySession currentSession;
    private GoalTracker goalTracker;
    private AnimationTimer timer;
    private SessionBarChart sessionChart;
    
    // UI Components
    private ComboBox<Subject> subjectCombo;
    private Label timerLabel;
    private Button fab;
    private ListView<StudySession> sessionsList;
    private ListView<String> logList;
    private Scene mainScene;
    private Theme currentTheme;

    public enum Theme {
        PURPLE("purple.css"), LIGHT("light.css"), DARK("dark.css");
        private final String file;
        Theme(String file) { this.file = file; }
        public String getStylesheet() {
            return getClass().getResource("/themes/" + file).toExternalForm();
        }
    }

    @Override
    public void start(Stage stage) {
        initializeData();
        setupUI(stage);
        setupWindowCloseHandler(stage);
    }

    private void initializeData() {
        currentTheme = Theme.valueOf(prefs.get("theme", Theme.PURPLE.name()));
        subjects.addAll(Storage.load());
        loadSessions();
    }

    private void setupUI(Stage stage) {
        VBox mainLayout = new VBox(createHeader(), createMainContent());
        mainScene = new Scene(mainLayout, 900, 700);
        setupSceneHacks();
        applyTheme(currentTheme);
        
        stage.setScene(mainScene);
        stage.setTitle("Study Tracker 📚");
        stage.show();
    }


    private Button createIconButton(String text, String styleClass, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("icon-btn", styleClass);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private StackPane createMainContent() {
        TabPane tabPane = new TabPane(
            new Tab("Tracker", createTrackerTab()),
            new Tab("Insights", createChartTab())
        );
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        fab = createFAB();
        StackPane root = new StackPane(tabPane, fab);
        StackPane.setAlignment(fab, Pos.BOTTOM_CENTER);
        StackPane.setMargin(fab, new Insets(0, 0, 20, 0));
        return root;
    }

    private VBox createTrackerTab() {
        timerLabel = new Label("00:00:00");
        timerLabel.getStyleClass().add("timer-label");

        logList = new ListView<>(logItems);
        logList.setCellFactory(lv -> new LogListCell());

        sessionsList = new ListView<>();
        sessionsList.setCellFactory(lv -> new SessionListCell());

        VBox trackerTab = new VBox(20, 
            timerLabel,
            createCard("Live Session Log", logList),
            createCard("Session History", sessionsList)
        );
        trackerTab.setPadding(new Insets(20));
        return trackerTab;
    }

    private VBox createChartTab() {
        sessionChart = new SessionBarChart(List.of());
        return createCard("Study Progress", sessionChart);
    }

    private Button createFAB() {
        Button btn = new Button("▶");
        btn.getStyleClass().addAll("fab", "primary");
        btn.setDisable(true);
        btn.setOnAction(e -> toggleSession());
        return btn;
    }

    private void setupSceneHacks() {
        mainScene.setOnMouseMoved(e -> { /* Forces continuous repaints */ });
    }

    private void setupWindowCloseHandler(Stage stage) {
        stage.setOnCloseRequest(e -> {
            Storage.save(subjects);
            saveSessions();
            prefs.put("theme", currentTheme.name());
        });
    }

    private void toggleSession() {
        if (currentSession == null) startSession();
        else stopSession();
    }

    private void startSession() {
        currentSession = new StudySession(activeSubject.getName());
        currentSession.startSession();
        
        timer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) { // ~60 FPS
                    updateTimer(currentSession.getSessionDuration());
                    lastUpdate = now;
                }
            }
        };
        timer.start();
        
        fab.setText("⏹");
        fab.getStyleClass().add("danger");
        log("Session started for " + activeSubject.getName());
    }

    private void updateTimer(long millis) {
        Platform.runLater(() -> {
            long hours = millis / 3_600_000;
            long mins = (millis % 3_600_000) / 60_000;
            long secs = (millis % 60_000) / 1_000;
            
            timerLabel.setText(String.format("%02d:%02d:%02d", hours, mins, secs));
            timerLabel.setStyle("-fx-update: " + System.currentTimeMillis() + ";");
        });
    }

    private void stopSession() {
        if (timer != null) timer.stop();
        if (currentSession == null) return;

        currentSession.endSession();
        double hours = currentSession.getSessionDuration() / 3_600_000.0;
        
        activeSubject.updateTotalHours(hours);
        sessions.add(currentSession);
        refreshSubjectCombo();
        
        log(String.format("Session saved: %.2f hours", hours));
        log("Progress: " + goalTracker.getPercentComplete() + "% complete");
        
        updateSessionList();
        saveSessions();
        resetUIState();
    }

    private void resetUIState() {
        fab.setText("▶");
        fab.getStyleClass().remove("danger");
        timerLabel.setText("00:00:00");
        currentSession = null;
    }

    private void updateSessionList() {
        sessionsList.getItems().setAll(
            sessions.filtered(s -> s.getSubject().equals(activeSubject.getName()))
        );
    }

    private void setActiveSubject(Subject subject) {
        activeSubject = subject;
        if (subject == null) return;

        goalTracker = new GoalTracker(subject, subject.getHoursPerWeek());
        fab.setDisable(false);
        logItems.clear();
        
        List<StudySession> filtered = sessions.stream()
            .filter(s -> s.getSubject().equals(subject.getName()))
            .toList();
        
        sessionsList.getItems().setAll(filtered);
        sessionChart.updateSessions(filtered);
        
        logSelectionDetails(subject);
    }

    private void logSelectionDetails(Subject subject) {
        log("Selected: " + subject.getName());
        log("Target: " + subject.getHoursPerWeek() + " hrs/week");
        log("Progress: " + String.format("%.1f%%", goalTracker.getPercentComplete()));
    }

    private void refreshSubjectCombo() {
    // Sort subjects: those with history first, then by last session date (newest first)
    subjects.sort(Comparator
        .comparing((Subject s) -> !hasSessionHistory(s)) // Subjects WITHOUT history go last
        .thenComparing(this::getLastSessionDate).reversed() // Most recent sessions first
    );
    
    // Show ALL subjects in the dropdown (but sorted as above)
    subjectCombo.getItems().setAll(subjects);
}

    private boolean hasSessionHistory(Subject s) {
        return sessions.stream().anyMatch(sess -> sess.getSubject().equals(s.getName()));
    }

    private LocalDateTime getLastSessionDate(Subject s) {
    return sessions.stream()
        .filter(sess -> sess.getSubject().equals(s.getName()))
        .map(StudySession::getStartTime)
        .max(Long::compare)
        .map(timestamp -> LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ))
        .orElse(LocalDateTime.MIN);
}

    // ... [Inner classes for list cells remain unchanged from previous version] ...
    // ... [Storage methods (loadSessions, saveSessions) remain unchanged] ...
    // ... [Dialog methods (showAddSubjectDialog, showSettingsDialog) remain unchanged] ...
    // ... [Helper methods (createCard, log) remain unchanged] ...

    public static void main(String[] args) {
        launch(args);
    }

    // ================== INNER CLASSES ==================
    
    private class SubjectListCell extends ListCell<Subject> {
        @Override
        protected void updateItem(Subject subject, boolean empty) {
            super.updateItem(subject, empty);
            if (empty || subject == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(5);
                Label nameLabel = new Label(subject.getName());
                Label hoursLabel = new Label(String.format("(%.1f/%d hrs)", 
                    subject.getTotalHours(), subject.getHoursPerWeek()));
                
                if (hasSessionHistory(subject)) {
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                    hoursLabel.setStyle("-fx-text-fill: #757575;");
                    container.getChildren().addAll(createHistoryBadge(), nameLabel, hoursLabel);
                } else {
                    nameLabel.setStyle("-fx-text-fill: #616161;");
                    hoursLabel.setStyle("-fx-text-fill: #9E9E9E;");
                    container.getChildren().addAll(nameLabel, hoursLabel);
                }
                setGraphic(container);
            }
        }

        private Label createHistoryBadge() {
            Label badge = new Label("✓");
            badge.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            return badge;
        }
    }

    private class SessionListCell extends ListCell<StudySession> {
        private final Button deleteBtn = new Button("✕");
        private final HBox box = new HBox(10);
        
        public SessionListCell() {
            box.setAlignment(Pos.CENTER_LEFT);
            deleteBtn.getStyleClass().add("danger");
            deleteBtn.setOnAction(e -> deleteSession());
        }
        
        @Override
        protected void updateItem(StudySession session, boolean empty) {
            super.updateItem(session, empty);
            if (empty || session == null) {
                setGraphic(null);
            } else {
                Label label = new Label(String.format("%s - %.1f hrs", 
                    session.getStartTimeFormatted(),
                    session.getSessionDuration() / 3_600_000.0
                ));
                box.getChildren().setAll(label, deleteBtn);
                setGraphic(box);
            }
        }

        private void deleteSession() {
            StudySession session = getItem();
            sessions.remove(session);
            saveSessions();
            updateSessionList();
            sessionChart.updateSessions(sessions.stream()
                .filter(s -> s.getSubject().equals(activeSubject.getName()))
                .toList());
        }
    }

    // Add these missing methods to your testUI class

private void loadSessions() {
    try {
        if (Files.exists(SESSIONS_FILE)) {
            String json = Files.readString(SESSIONS_FILE);
            List<StudySession> loaded = GSON.fromJson(json, 
                new TypeToken<List<StudySession>>(){}.getType());
            sessions.addAll(loaded);
        }
    } catch (IOException e) {
        log("Error loading sessions: " + e.getMessage());
    }
}

private void saveSessions() {
    try {
        Files.createDirectories(SESSIONS_FILE.getParent());
        Files.writeString(SESSIONS_FILE, GSON.toJson(sessions));
    } catch (IOException e) {
        log("Error saving sessions: " + e.getMessage());
    }
}

private void applyTheme(Theme theme) {
    mainScene.getStylesheets().clear();
    mainScene.getStylesheets().add(theme.getStylesheet());
}



// Update the header creation code
private HBox createHeader() {
    subjectCombo = new ComboBox<>(subjects);
    subjectCombo.setPromptText("Select subject");
    subjectCombo.setCellFactory(lv -> new SubjectListCell());
    subjectCombo.setButtonCell(new SubjectListCell());
    subjectCombo.setOnAction(e -> setActiveSubject(subjectCombo.getValue()));
    refreshSubjectCombo();

    // Fix button creation with proper handlers
    // In createHeader() method:
Button addSubjectBtn = createIconButton("＋", "primary", () -> showAddSubjectDialog());
Button settingsBtn = createIconButton("⚙", "", () -> showSettingsDialog());

    HBox header = new HBox(10, subjectCombo, addSubjectBtn, settingsBtn);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getStyleClass().add("header");
    return header;
}

// Add the createCard helper method
private VBox createCard(String title, Region content) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("card-title");
    
    VBox card = new VBox(10, titleLabel, content);
    card.getStyleClass().add("card");
    card.setPadding(new Insets(15));
    VBox.setVgrow(content, Priority.ALWAYS);
    return card;
}


// Add missing dialog methods
private void showAddSubjectDialog() {
       Dialog<Subject> dialog = new Dialog<>();
        dialog.setTitle("New Subject");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Subject name");
        TextField targetField = new TextField();
        targetField.setPromptText("Weekly target hours");
        
        grid.addRow(0, new Label("Subject:"), nameField);
        grid.addRow(1, new Label("Target:"), targetField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().addAll(mainScene.getStylesheets());
        
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    int target = Integer.parseInt(targetField.getText().trim());
                    return new Subject(name, 0.0, target, new ArrayList<>());
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid target hours").show();
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(subject -> {
            subjects.add(subject);
            subjectCombo.getSelectionModel().select(subject);
            Storage.save(subjects);
        });
}

private void showSettingsDialog() {
    Dialog<Theme> dialog = new Dialog<>();
        dialog.setTitle("Appearance Settings");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        ComboBox<Theme> themeCombo = new ComboBox<>(
            FXCollections.observableArrayList(Theme.values())
        );
        themeCombo.setValue(currentTheme);
        
        VBox content = new VBox(10, new Label("Select theme:"), themeCombo);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().addAll(mainScene.getStylesheets());
        
        dialog.setResultConverter(btn -> 
            btn == ButtonType.OK ? themeCombo.getValue() : null
        );
        
        dialog.showAndWait().ifPresent(theme -> {
            currentTheme = theme;
            applyTheme(theme);
        });
}

// Add missing log method
private void log(String message) {
    logItems.add(message);
    logList.scrollTo(logItems.size() - 1);
}

    private class LogListCell extends ListCell<String> {
        @Override
        protected void updateItem(String entry, boolean empty) {
            super.updateItem(entry, empty);
            if (empty || entry == null) {
                setText(null);
                setStyle("");
            } else {
                setText(entry);
                applyLogStyle(entry);
            }
        }

        private void applyLogStyle(String entry) {
            if (entry.startsWith("Session started")) {
                setStyle("-fx-text-fill: #009688;");
            } else if (entry.contains("saved")) {
                setStyle("-fx-text-fill: #4CAF50;");
            } else if (entry.contains("error") || entry.contains("Invalid")) {
                setStyle("-fx-text-fill: #f44336;");
            } else {
                setStyle("-fx-text-fill: #616161;");
            }
        }
    }
}