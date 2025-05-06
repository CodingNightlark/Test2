import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask user for subject
        System.out.print("Enter the subject name: ");
        String subjectName = scanner.nextLine();

        // Create Subject object
        Subject subject = new Subject(subjectName, 0, 0); // You can extend this to load real data

        // Start study session
        System.out.println("Press Y to start the timer...");
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("Y")) {
            StudySession session = new StudySession(subjectName);
            session.startSession();

            System.out.println("Timer started. Press Enter to stop.");
            scanner.nextLine(); // Wait for Enter key

            session.endSession();

            long millis = session.getSessionDuration();
            double hours = millis / (1000.0 * 60 * 60); // fractional hours

            // Update subject
            subject.updateTotalHours(hours);

            System.out.println("Timer stopped.");
            System.out.printf("Elapsed time: %.2f hours%n", hours);

            System.out.println("\nSubject Summary");
            subject.displaySubjectInfo();
        } else {
            System.out.println("Timer not started.");
        }

        scanner.close();
    }
}
