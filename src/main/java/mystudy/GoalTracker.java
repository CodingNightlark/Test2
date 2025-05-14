package mystudy;

public class GoalTracker {
    private Subject subject; 
    private double targetHours;

    public GoalTracker(Subject subject, double targetHours) {
        this.subject = subject;
        this.targetHours = targetHours;
    }

    public void updateGoal(double hoursStudied) {
        subject.updateTotalHours(hoursStudied);
    }

    public double getHoursRemaining() {
        return Math.max(0, targetHours - subject.getTotalHours());
    }

    public double getPercentComplete() {
        return Math.min(100, (subject.getTotalHours() / targetHours) * 100);
    }

 public void displayProgress() {
    System.out.println("Progress toward goal for " + subject.getName() + ":");
    System.out.println("  Total studied: " + subject.getTotalHours() + " hours");
    System.out.println("  Target: " + targetHours + " hours");
    System.out.println("  Remaining: " + getHoursRemaining() + " hours");
    System.out.println("  Completion: " + getPercentComplete() + "%");
}
} 