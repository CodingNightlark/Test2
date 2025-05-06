// src/main/java/storage/Storage.java
package storage;

import mystudy.Subject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.file.*;
import java.util.*;

public class Storage {
    private static final Path FILE = Path.of(
        System.getProperty("user.home"), ".study-tracker", "data.json");
    private static final Gson GSON = new Gson();

    public static List<Subject> load() {
        try {
            if (!Files.exists(FILE)) return new ArrayList<>();
            String json = Files.readString(FILE);
            return GSON.fromJson(json, new TypeToken<List<Subject>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    } 

    public static void save(List<Subject> subjects) {
        try {
            Files.createDirectories(FILE.getParent());
            String json = GSON.toJson(subjects);
            Files.writeString(FILE, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
