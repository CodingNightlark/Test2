// src/main/java/mystudy/Storage.java
package mystudy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Storage {
    private static final Path SUBJECTS_FILE = Path.of(
        System.getProperty("user.home"), ".study-tracker", "subjects.json");
    private static final Path SESSIONS_FILE = Path.of(
        System.getProperty("user.home"), ".study-tracker", "sessions.json");
    private static final Gson GSON = new Gson();

    // Modified load() to merge saved subjects with session subjects
    public static List<Subject> load() {
        List<Subject> savedSubjects = loadFromFile(SUBJECTS_FILE, new TypeToken<List<Subject>>(){});
        List<String> sessionSubjects = getUniqueSubjectsFromSessions();
        
        // Merge with default 0 target hours for new subjects
        return Stream.concat(
            savedSubjects.stream(),
            sessionSubjects.stream()
                .filter(name -> savedSubjects.stream().noneMatch(s -> s.getName().equalsIgnoreCase(name)))
                .map(name -> new Subject(name, 0, 0, new ArrayList<>()))
                

        ).collect(Collectors.toList());
    }

    public static void save(List<Subject> subjects) {
        saveToFile(SUBJECTS_FILE, subjects);
    }

    // Helper methods
    private static <T> T loadFromFile(Path path, TypeToken<T> type) {
        try {
            if (!Files.exists(path)) return (T) Collections.emptyList();
            return GSON.fromJson(Files.readString(path), type.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return (T) Collections.emptyList();
        }
    }

    private static List<String> getUniqueSubjectsFromSessions() {
        List<StudySession> sessions = loadFromFile(SESSIONS_FILE, new TypeToken<List<StudySession>>(){});
        return sessions.stream()
            .map(StudySession::getSubject)
            .distinct()
            .collect(Collectors.toList());
    }

    private static void saveToFile(Path path, Object data) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}