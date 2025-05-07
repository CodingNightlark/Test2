package mystudy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class StudySession {
    private String subject;
    private Timer timer;
    private String notes;

    public StudySession(String subject) {
        this.subject = subject;
        this.timer = new Timer();
    }

    public void startSession() {
        timer.start();
    }
 
    public void endSession() {
        timer.stop();
    }

    public Timer getTimer() {
        return this.timer;
    }

    public long getSessionDuration() { 
        return timer.getElapsedTime();
    }
    public String getSubject() {
        return subject;
    }

    public String getStartTimeFormatted() {
        // Assume Timer stores the start timestamp in milliseconds:
        long startMillis = timer.getStartTime();
        LocalDateTime dt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startMillis),
            ZoneId.systemDefault()
        );
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dt.format(fmt);
    }
}