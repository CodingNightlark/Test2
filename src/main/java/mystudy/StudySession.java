package mystudy;

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
}