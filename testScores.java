package myStudy;

import java.time.LocalDate;

public class testScores {
   private LocalDate date;
    private double hoursStudiedBefore;
    private double score; // percentage

    public testScores(LocalDate date, double hoursStudiedBefore, double score) {
        this.date = date;
        this.hoursStudiedBefore = hoursStudiedBefore;
        this.score = score;
    }

    public double getHoursStudiedBefore() {
        return hoursStudiedBefore;
    }

    public double getScore() {
        return score;
    }
}




// Make a timer, study tracker, set goals, grade tracker, and study planner in Java.
// The timer will track the time spent studying.