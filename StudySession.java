public class StudySession {
    private String subject;
    private String topic;   
    private String notes;
    private Timer timer;


    public StudySession(String subject, String topic) {
        this.subject = subject;
        this.topic = topic;
        this.notes = "";
        this.timer = new Timer();
    }
    public void startSession() {
        timer.start();
    }
    public void endSession() {
        timer.stop();
    }
   
    public void addNotes(String notes) {
        this.notes += notes + "\n";
    }
    public String getNotes() {
        return notes;
    }
    public String getSubject() {
        return subject;
    }
    public String getTopic() {
        return topic;
    }
    public long getSessionDuration() {
        return timer.getElapsedTime();
    }
    public long recommendedBreakDuration() {
        return timer.getElapsedTime()/360;
    }
}
