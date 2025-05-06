package myStudy;
public class StudySession {
    private String subject;
    private Timer timer;
    private String notes;

    public StudySession(String subject, String notes) {
        this.subject = subject;
        this.timer = new Timer();
        this.notes = notes;
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

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
// is this necessary as it's own class?
