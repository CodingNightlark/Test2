package mystudy;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;  

public class Main {   
    /*mvn clean compile exec:java  

  */     
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); 
        List<StudySession> sessions = new ArrayList<>(); 
        Subject subject;
        GoalTracker tracker;  
      
        // Prompt once for subject and goal
        System.out.print("Enter the subject name: ");
        String subjectName = scanner.nextLine();  
 
        System.out.print("Enter your target hours for " + subjectName + ": ");
        double targetHours = Double.parseDouble(scanner.nextLine());
 
        subject = new Subject(subjectName, 0, 0); 
        tracker = new GoalTracker(subject, targetHours);

        String continueInput;
        do { 
            System.out.println("Press Y to start a new study session, or any other key to exit.");
            continueInput = scanner.nextLine();
            if (continueInput.equalsIgnoreCase("Y")) {  
                // Create and start session 
                StudySession session = new StudySession(subjectName);
                session.startSession(); 

                // Live timer display 
                Timer timer = session.getTimer();
                LiveTimerDisplay display = new LiveTimerDisplay(timer);
                display.start();

                System.out.println("\nTimer started. Press Enter to stop.");
                scanner.nextLine();

                session.endSession(); 
                display.stopDisplay(); 

                long millis = session.getSessionDuration();
                double hours = millis / (1000.0 * 60 * 60);

                // Update goal and log session
                tracker.updateGoal(hours);
                sessions.add(session); 

                System.out.println("\nTimer stopped.");
                System.out.printf("Elapsed time: %.2f hours%n", hours);

                System.out.println("\n--- Subject Summary ---");
                subject.displaySubjectInfo();

                System.out.println("\n--- Goal Progress ---");
                tracker.displayProgress();
            }
        } while (continueInput.equalsIgnoreCase("Y"));
        // had to ask teacher for help with this part, I was stuck on how to get the loop to work properly

        // After loop, optionally print session history
        System.out.println("\nYou logged " + sessions.size() + " sessions this run.");
        for (int i = 0; i < sessions.size(); i++) {
            StudySession s = sessions.get(i);
            long millis = s.getSessionDuration();
            double hrs = millis / (1000.0 * 60 * 60); 
            System.out.printf("Session %d: %.2f hours%n", i + 1, hrs);
        }

        System.out.println("Goodbye!");
        scanner.close();
    }

    
}
