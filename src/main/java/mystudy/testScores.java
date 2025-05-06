package mystudy;

import java.time.LocalDate;

public class testScores {
    private LocalDate date;
    private double hoursStudiedBefore;
    private double score;

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