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
        System.out.printf("Progress toward goal for %s:%n", subject.getName());
        System.out.printf("  Total studied: %.2f hours%n", subject.getTotalHours());
        System.out.printf("  Target: %.2f hours%n", targetHours);
        System.out.printf("  Remaining: %.2f hours%n", getHoursRemaining());
        System.out.printf("  Completion: %.1f%%%n", getPercentComplete());
    }
}