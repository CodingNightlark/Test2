public class StudySession {
    private String subject;
    private Timer timer;

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

    public long getSessionDuration() {
        return timer.getElapsedTime();
    }
}
// is this necessary as it's own class?
