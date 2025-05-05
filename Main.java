import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
       Timer timer = new Timer();
       Scanner scanner = new Scanner(System.in);
         System.out.println("Enter Y to start the timer");
         String input = scanner.nextLine();
            if (input.equalsIgnoreCase("Y")) {
                timer.start();
                System.out.println("Timer started. Press Enter to stop.");
                scanner.nextLine(); // Wait for user to press Enter
                timer.stop();
                System.out.println("Timer stopped.");
                System.out.println("Elapsed time in seconds: " + timer.timeInSeconds());
            } else {
                System.out.println("Timer not started.");
            }

    }
}   