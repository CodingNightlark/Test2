package myStudy;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the subject name: ");
        String subjectName = scanner.nextLine();
        
        System.out.print("Enter your target hours for " + subjectName + ": ");
        double targetHours = Double.parseDouble(scanner.nextLine());

    Subject subject = new Subject(subjectName, 0, 0);
    GoalTracker tracker = new GoalTracker(subject, targetHours);

    System.out.println("Press Y to start the timer...");
    String input = scanner.nextLine();

    if (input.equalsIgnoreCase("Y")) {
     StudySession session = new StudySession(subjectName, "");
     session.startSession();

    Timer timer = session.getTimer();
     LiveTimerDisplay display = new LiveTimerDisplay(timer);
    display.start();

    System.out.println("\nTimer started. Press Enter to stop.");
    scanner.nextLine();

    session.endSession();
    display.stopDisplay();

    long millis = session.getSessionDuration();
    double hours = millis / (1000.0 * 60 * 60);
    System.out.print("How did you study? ");
    String notes = scanner.nextLine();
    session.setNotes(notes);

    tracker.updateGoal(hours);

    System.out.println("\nTimer stopped.");
    System.out.printf("Elapsed time: %.2f hours%n", hours);

    System.out.println("\n--- Subject Summary ---");
    subject.displaySubjectInfo();

    System.out.println("\n--- Goal Progress ---");
    tracker.displayProgress();

    System.out.println("Notes stored: " + notes);
    
    System.out.print("Any recent test score to log? (Y/N): ");
if (scanner.nextLine().equalsIgnoreCase("Y")) {
    System.out.print("Enter score (%): ");
    double score = Double.parseDouble(scanner.nextLine());

    LocalDate today = LocalDate.now(); // or prompt for date
    subject.addTestScore(new testScores(today, subject.getTotalHours(), score));
}

        
    
}
    }
}


