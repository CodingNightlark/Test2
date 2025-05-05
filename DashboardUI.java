public class DashboardUI {
    private StudySession studySession;
    private Subject subject;

    public DashboardUI(StudySession studySession, Subject subject) {
        this.studySession = studySession;
        this.subject = subject;
    }

    public void displayDashboard() {
        System.out.println("Subject: " + subject.getName());
        System.out.println("Topic: " + studySession.getTopic());
        System.out.println("Notes: " + studySession.getNotes());
        System.out.println("Session Duration: " + studySession.getSessionDuration() + " milliseconds");
    }

    public static void main(String[] args) {
        Subject math = new Subject("Mathematics", "Algebra and Calculus", 3, 45, 3);
        StudySession session = new StudySession(math.getName(), "Calculus Basics");
        DashboardUI dashboard = new DashboardUI(session, math);

        session.startSession();
        
        // Simulate some work with a sleep
        try {
            Thread.sleep(2000); // Sleep for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        session.endSession();
        
        dashboard.displayDashboard();
    }
    
}
